package com.ctrip.framework.cornerstone.configuration;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by jiang.j on 2016/3/30.
 */

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.ctrip.framework.cornerstone.enterprise.EnFactory;
import com.ctrip.framework.cornerstone.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationManager {

    static volatile Configuration instance = null;
    static volatile boolean customConfigurationInstalled = false;
    static PropertiesConfiguration customProperties;
    static final String TEMPFILENAME = "vi_config.properties";

    static final String PROP_NEXT_LOAD = "@next";
    private static Map<String,Set<String>> configProMap = new HashMap<>();
    private static boolean enableDefaultload = true;
    private static String CONFIGPATH = "config/";

    private static Set<String> loadedPropertiesURLs = new CopyOnWriteArraySet<>();
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    static {
        try {
            String className = System.getProperty("vi.default.configuration.class");
            if (className != null) {
                instance = (Configuration) Class.forName(className).newInstance();
                customConfigurationInstalled = true;
            } else {
                String factoryName = System.getProperty("vi.default.configuration.factory");
                if (factoryName != null) {
                    Method m = Class.forName(factoryName).getDeclaredMethod("getInstance", new Class[]{});
                    m.setAccessible(true);
                    instance = (Configuration) m.invoke(null, new Object[]{});
                    customConfigurationInstalled = true;
                }
            }

        } catch (Throwable e) {
            throw new RuntimeException("Error initializing configuration", e);
        }
    }

    static void setEnableDefaultload(boolean enabled){
        enableDefaultload =enabled;
    }

    static void setConfigPath(String path){
        CONFIGPATH = path;
    }
    static Set<String> getLoadedPropertiesURLs() {
        return loadedPropertiesURLs;
    }


    public static Map<String,Map<String,String>> getAllProperties() throws InitConfigurationException {

        if (instance == null) {
            instance = getConfigInstance();
        }
        Map<String,Map<String,String>> allProps = new HashMap<>();
        for(String configName:configProMap.keySet()){
            allProps.put(configName,getPropertiesByConfigName(configName));
        }
        return allProps;
    }

    public static Map<String,String> getPropertiesByConfigName(String configName) throws InitConfigurationException {


        Map<String,String> pros = new HashMap<>();
        Set<String> keys = getConfigKeys(configName);
        if(keys==null){
            return pros;
        }

        for(String proKey : keys){
            pros.put(proKey, String.valueOf(instance.getProperty(proKey)));
        }
        return pros;
    }

    public static Set<String> getConfigKeys(String configName) throws InitConfigurationException {
        if (instance == null) {
            instance = getConfigInstance();
        }

        return configProMap.get(configName);
    }

    protected static void setProperties(Map<String,String> configs,String user) throws InitConfigurationException {

        String nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        if (instance == null) {
            instance = getConfigInstance();
        }
        for(String key:configs.keySet()) {
            if(key.startsWith("#.")){
                continue;
            }
            if(!customProperties.containsKey("$"+key)){
                customProperties.setProperty("$" + key, instance.getString(key));
            }

            String value = configs.get(key);
            instance.setProperty(key, value);
            customProperties.setProperty(key, value);
            customProperties.setProperty("@"+key,user + " "+nowDate);
        }

        try {
            customProperties.save();
            logger.info("save custom property");
        } catch (Throwable e) {
            logger.error("save tmp fc file failed!",e);
        }
    }

    static Map<String, String> allCustomRemarks() {
        Map<String,String> rtn = new HashMap<>();
        Iterator<String> keys = customProperties.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.startsWith("@")) {
                String realKey = key.substring(1);
                String value =customProperties.getString("$"+realKey);
                if(value != null && !value.equals(instance.getString(realKey))) {
                    rtn.put(key.substring(1), customProperties.getString(key) + " from: "+value);
                }
            }
        }
        return rtn;
    }


    /**
     * Install the system wide configuration with the ConfigurationManager.
     * This call can be made only once, otherwise IllegalStateException will be thrown.
     */
    static synchronized void install(Configuration config) throws IllegalStateException {
        if (!customConfigurationInstalled) {
            setDirect(config);
        } else {
            throw new IllegalStateException("A non-default configuration is already installed");
        }
    }

    public static synchronized boolean isConfigurationInstalled() {
        return customConfigurationInstalled;
    }

    private static Configuration createDefaultConfigInstance() throws InitConfigurationException {
        PropertiesConfiguration config = new PropertiesConfiguration();

        if(enableDefaultload) {
            try {
                String path = CONFIGPATH;
                loadFromJars(path, config);

                Set<String> paths = getResourcesFolderFiles(path);
                for (String configPath : paths) {
                    String name = configPath.substring(path.length());
                    Properties props = loadCascadedProperties(configPath);
                    addNewProps(name,props,config);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new InitConfigurationException(e);
            }
        }

        return config;
    }

    /**
     * add properties to current config and add new keys to configProMap
     * @param name
     * @param props
     * @param config
     */
    private static void addNewProps(String name,Properties props,Configuration config){

        Set<String> newKeys = ConfigurationUtils.loadProperties(name, props, config);
        if(configProMap.containsKey(name)){
            configProMap.get(name).addAll(newKeys);
        }else {
            configProMap.put(name, newKeys);
        }
    }

    private static void loadFromJars(String path, Configuration config) throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> urls = loader.getResources(path);

        while (urls.hasMoreElements()){
           URL rawUrl = urls.nextElement();
            if("jar".equals(rawUrl.getProtocol())){

                String urlStr = rawUrl.toString();
                String location = urlStr.substring(urlStr.indexOf('f'),urlStr.lastIndexOf('!'));

                URL url = new URL(location);
                Map<String,Properties> allProps = new HashMap<>();
                Map<String,Properties> envProps = new HashMap<>();
                String environment = EnFactory.getEnBase().getEnvType();
                if(environment!=null){
                    environment=environment.toLowerCase();
                }

                try ( ZipInputStream zip = new ZipInputStream(url.openStream())) {
                    ZipEntry ze;
                    while ((ze = zip.getNextEntry()) != null) {

                        String entryName = ze.getName();
                        if (entryName.startsWith(path)&&!ze.isDirectory()){
                            String fname = entryName.substring(path.length());

                            Properties props = new Properties();
                            props.load(zip);
                            int suffixIndex = fname.lastIndexOf(".");
                            if(suffixIndex<0){
                                continue;
                            }
                            fname = fname.substring(0,suffixIndex);
                            int envIndex = fname.indexOf("-");

                            if(envIndex>0) {
                                String env = fname.substring(envIndex+1);
                                fname = fname.substring(0, envIndex);
                                if (environment != null && environment.length() > 0 && env.equals(environment) )  {
                                    envProps.put(fname,props);
                                }

                            }else {
                                allProps.put(fname, props);
                            }
                        }
                    }

                    for(String name:envProps.keySet()){

                        if(allProps.containsKey(name)){
                            Properties seleEnvProps = envProps.get(name);
                            Enumeration<Object> keys =seleEnvProps.keys();
                            while (keys.hasMoreElements()){
                                String nowKey = String.valueOf(keys.nextElement());
                                allProps.get(name).setProperty(nowKey, seleEnvProps.getProperty(nowKey));
                            }

                        }else{

                            addNewProps(name, envProps.get(name), config);
                        }

                    }

                    for(String name : allProps.keySet()) {
                        addNewProps(name, allProps.get(name), config);
                    }
                }
            }
        }
    }


    private final static Set<String> getResourcesFolderFiles(String dir) throws IOException {

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Set<String> paths = new HashSet<>();

        String[] dirFiles;
        InputStream dirStream = loader.getResourceAsStream(dir);
        if(dirStream!=null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(dirStream));
            List<String> names = new ArrayList<>();
            String fname;
            while ((fname = reader.readLine()) != null) {
                names.add(fname);
            }
            dirFiles = names.toArray(new String[0]);
        }else{

            URL dirUrl = loader.getResource(dir);
            if(dirUrl == null || "".equals(dirUrl.getFile()))
                return paths;

            File configDir = new File(dirUrl.getFile());
            dirFiles = configDir.list();
        }

        if(dirFiles == null){
            return paths;
        }

        for(String fname:dirFiles){
            if(!fname.endsWith(".properties")){
                continue;
            }
            fname = fname.substring(0,fname.lastIndexOf("."));
            int envIndex = fname.indexOf("-");

            if(envIndex>0) {
                fname = fname.substring(0, envIndex);
            }
            paths.add(dir+fname);
        }

        return paths;
    }

    public static Configuration getConfigInstance() throws InitConfigurationException {
        if (instance == null) {
            synchronized (ConfigurationManager.class) {
                if (instance == null) {
                    instance = createDefaultConfigInstance();
                    try {
                        customProperties = Tools.loadPropertiesFromFile(TEMPFILENAME);
                    } catch (Throwable e) {
                        logger.error("load temp custom properties failed!", e);
                    }

                    if(customProperties!=null) {
                        Iterator<String> keys = customProperties.getKeys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if (instance.containsKey(key)) {
                                instance.setProperty(key, customProperties.getString(key));
                            }
                        }
                    }
                }
            }
        }
        return instance;
    }


    static synchronized void setDirect(Configuration config) {
        if (instance != null) {
            // transfer listeners
            // transfer properties which are not in conflict with new configuration
            for (Iterator<String> i = instance.getKeys(); i.hasNext();) {
                String key = i.next();
                Object value = instance.getProperty(key);
                if (value != null && !config.containsKey(key)) {
                    config.setProperty(key, value);
                }
            }
        }
        ConfigurationManager.removeDefaultConfiguration();
        ConfigurationManager.instance = config;
        ConfigurationManager.customConfigurationInstalled = true;
    }

    /**
     * Load properties from resource file(s) into the system wide configuration
     * @param path relative path of the resources
     * @throws IOException
     */
    static void loadPropertiesFromResources(String path)
            throws IOException, InitConfigurationException {
        if (instance == null) {
            instance = getConfigInstance();
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = loader.getResources(path);
        if (!resources.hasMoreElements()) {
            //non-existent config path. Throw an exception. Issue #150
            throw new IOException("Cannot locate " + path + " as a classpath resource.");
        }
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            InputStream fin = url.openStream();
            Properties props = ConfigurationUtils.loadPropertiesFromInputStream(fin);
            ConfigurationUtils.loadProperties(props, instance);
        }
    }

    /**
     * Load resource configName.properties first. Then load configName-deploymentEnvironment.properties
     * into the system wide configuration. For example, if configName is "application", and deployment environment
     * is "test", this API will first load "application.properties", then load "application-test.properties" to
     * override any property that also exist in "application.properties".
     *
     * @param configName prefix of the properties file name.
     * @throws IOException
     */
    static void loadCascadedPropertiesFromResources(String configName) throws IOException, InitConfigurationException {
        Properties props = loadCascadedProperties(configName);

        if (instance == null) {
            instance = getConfigInstance();
        }
        ConfigurationUtils.loadProperties(props, instance);
    }

    private static Properties loadCascadedProperties(String configName) throws IOException {
        String defaultConfigFileName = configName + ".properties";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(defaultConfigFileName);
        if (url == null) {
            throw new IOException("Cannot locate " + defaultConfigFileName + " as a classpath resource.");
        }
        Properties props = getPropertiesFromFile(url);
        String environment = EnFactory.getEnBase().getEnvType();
        if(environment!=null) {
            environment = environment.toLowerCase();
        }
        if (environment != null && environment.length() > 0) {
            String envConfigFileName = configName + "-" + environment + ".properties";
            url = loader.getResource(envConfigFileName);
            if(url==null){
                url = loader.getResource(configName.replace("config/",environment+"-config/") +".properties");
            }
            if (url != null) {
                Properties envProps = getPropertiesFromFile(url);
                if (envProps != null) {
                    props.putAll(envProps);
                }
            }
        }
        return props;
    }



    /**
     * Load the specified properties into system wide configuration
     */
    static void loadProperties(Properties properties) throws InitConfigurationException {
        if (instance == null) {
            instance = getConfigInstance();
        }
        ConfigurationUtils.loadProperties(properties, instance);
    }


    private static String getConfigName(URL propertyFile)
    {
        String name = propertyFile.toExternalForm();
        name = name.replace('\\', '/'); // Windows
        final String scheme = propertyFile.getProtocol().toLowerCase();
        if ("jar".equals(scheme) || "zip".equals(scheme)) {
            // Use the unqualified name of the jar file.
            final int bang = name.lastIndexOf("!");
            if (bang >= 0) {
                name = name.substring(0, bang);
            }
            final int slash = name.lastIndexOf("/");
            if (slash >= 0) {
                name = name.substring(slash + 1);
            }
        } else {
            // Use the URL of the enclosing directory.
            final int slash = name.lastIndexOf("/");
            if (slash >= 0) {
                name = name.substring(0, slash);
            }
        }
        return name;
    }

    private static synchronized void removeDefaultConfiguration() {
        if (instance == null || customConfigurationInstalled) {
            return;
        }
        instance = null;
    }

    public static Configuration getConfigFromPropertiesFile(URL startingUrl)
    throws FileNotFoundException {
        return ConfigurationUtils.getConfigFromPropertiesFile(startingUrl,
                getLoadedPropertiesURLs(), PROP_NEXT_LOAD);
    }

    public static Properties getPropertiesFromFile(URL startingUrl)
            throws IOException {
        Properties pros = new Properties();
        pros.load(startingUrl.openStream());
        return pros;
    }

    public static void installReadonlyProperties(Properties pros) throws InitConfigurationException {

        if (instance == null) {
            instance = getConfigInstance();
        }

        Set<String> newKeys = new HashSet<>();
        for(String p:pros.stringPropertyNames()){

            String newKey = "#."+p;
            newKeys.add(newKey);
            instance.setProperty(newKey,pros.getProperty(p));
        }
        configProMap.put("#",newKeys);


    }
}

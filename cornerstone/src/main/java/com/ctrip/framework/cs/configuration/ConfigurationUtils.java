package com.ctrip.framework.cs.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
/**
 * Created by jiang.j on 2016/4/7.
 */
public class ConfigurationUtils {
     private static final Logger logger = LoggerFactory.getLogger(ConfigurationUtils.class);



    /**
     * Utility method to obtain <code>Properties</code> given an instance of <code>Configuration</code>.
     * Returns an empty <code>Properties</code> object if the config has no properties or is null.
     * @param config Configuration to get the properties
     * @return properties extracted from the configuration
     */
    public static Properties getProperties(Configuration config) {
 	   Properties p = new Properties();
 	   if (config != null){
	 	   Iterator<String> it = config.getKeys();
	 	   while (it.hasNext()){
	 		   String key = it.next();
	 		   if (key != null) {
	 		      Object value = config.getProperty(key);
                  if (value != null) {
                      p.put(key, value);
                  }	 		   }
	 	   }
 	   }
  	   return p;
    }

    public static void loadProperties(Properties props, Configuration config) {
        for (Entry<Object, Object> entry: props.entrySet()) {
            String key = (String) entry.getKey();
            config.setProperty(key, entry.getValue());
        }

    }
    public static Set<String> loadProperties(String prefix,Properties props, Configuration config) {
        Set<String> keys = new LinkedHashSet<>();
        for (Entry<Object, Object> entry: props.entrySet()) {
            String key = (String) entry.getKey();
            if(!key.startsWith(prefix+".")){
                key = prefix+"."+key;
            }
            config.setProperty(key, entry.getValue());

            keys.add(key);
        }

        return keys;
    }

    static void loadFromPropertiesFile(Configuration config, String baseUrl, Set<String> loaded, String... nextLoadKeys) {
        String nextLoad = getNextLoad(config, nextLoadKeys);
        if (nextLoad == null) {
            return;
        }
        String[] filesToLoad = nextLoad.split(",");
        for (String fileName: filesToLoad) {
            fileName = fileName.trim();
            try {
                URL url = new URL(baseUrl + "/" + fileName);
                // avoid circle
                if (loaded.contains(url.toExternalForm())) {
                    logger.warn(url + " is already loaded");
                    continue;
                }
                loaded.add(url.toExternalForm());
                PropertiesConfiguration nextConfig = new OverridingPropertiesConfiguration(url);
                copyProperties(nextConfig, config);
                logger.info("Loaded properties file " + url);
                loadFromPropertiesFile(config, baseUrl, loaded, nextLoadKeys);
            } catch (Throwable e) {
                logger.warn("Unable to load properties file", e);
            }
        }
    }


    public static Configuration getConfigFromPropertiesFile(URL startingUrl, Set<String> loaded, String... nextLoadKeys)
            throws FileNotFoundException {
        if (loaded.contains(startingUrl.toExternalForm())) {
            logger.warn(startingUrl + " is already loaded");
            return null;
        }
        PropertiesConfiguration propConfig = null;
        try {
            propConfig = new OverridingPropertiesConfiguration(startingUrl);
            logger.info("Loaded properties file " + startingUrl);
        } catch (ConfigurationException e) {
            Throwable cause = e.getCause();
            if (cause instanceof FileNotFoundException) {
                throw (FileNotFoundException) cause;
            } else {
                throw new RuntimeException(e);
            }
        }

        if (nextLoadKeys == null) {
            return propConfig;
        }
        String urlString = startingUrl.toExternalForm();
        String base = urlString.substring(0, urlString.lastIndexOf("/"));
        loaded.add(startingUrl.toString());
        loadFromPropertiesFile(propConfig, base, loaded, nextLoadKeys);
        return propConfig;
    }

    public static void copyProperties(Configuration from, Configuration to) {
        for (Iterator<String> i = from.getKeys(); i.hasNext(); ) {
            String key = i.next();
            if (key != null) {
                Object value = from.getProperty(key);
                if (value != null) {
                    to.setProperty(key, value);
                }
            }
        }
    }

    public static Properties getPropertiesFromFile(URL startingUrl, Set<String> loaded, String... nextLoadKeys)
            throws FileNotFoundException {
        Configuration config = getConfigFromPropertiesFile(startingUrl, loaded, nextLoadKeys);
        return getProperties(config);
    }


    private static String getNextLoad(Configuration propConfig, String... nextLoadPropertyKeys) {
        String nextLoadKeyToUse = null;
        for (String key: nextLoadPropertyKeys) {
            if (propConfig.getProperty(key) != null) {
                nextLoadKeyToUse = key;
                break;
            }
        }
        // there is no next load for this properties file
        if (nextLoadKeyToUse == null) {
            return null;
        }
        // make a copy of current existing properties
        PropertiesConfiguration config = new PropertiesConfiguration();

        // need to have all the properties to interpolate next load property value
        //copyProperties(ConfigurationManager.getConfigInstance(), config);
        copyProperties(propConfig, config);
        // In case this is a list of files to load, always treat the value as a list
        List<Object> list = config.getList(nextLoadKeyToUse);
        StringBuilder sb = new StringBuilder();
        for (Object value: list) {
            sb.append(value).append(",");
        }
        String nextLoad = sb.toString();
        propConfig.clearProperty(nextLoadKeyToUse);
        return nextLoad;
    }

    /**
     * Load properties from InputStream with utf-8 encoding, and it will take care of closing the input stream.
     * @param fin
     * @return
     * @throws IOException
     */
    public static Properties loadPropertiesFromInputStream(InputStream fin) throws IOException {
        Properties props = new Properties();
        if(fin == null){
            return props;
        }
        try(InputStreamReader reader = new InputStreamReader(fin, "UTF-8")) {
            props.load(reader);
            return props;
        } finally {
                fin.close();
        }
    }
}

class OverridingPropertiesConfiguration extends PropertiesConfiguration {

    public OverridingPropertiesConfiguration() {
        super();
    }


    public OverridingPropertiesConfiguration(URL url)
            throws ConfigurationException {
        super(url);
    }

}

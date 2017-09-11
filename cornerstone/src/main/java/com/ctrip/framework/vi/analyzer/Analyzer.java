package com.ctrip.framework.vi.analyzer;


import com.ctrip.framework.vi.IgniteManager;
import com.ctrip.framework.vi.MagicWord;
import com.ctrip.framework.vi.NotFoundException;
import com.ctrip.framework.vi.SimpleLoggerFactory;
import com.ctrip.framework.vi.enterprise.EnFactory;
import com.ctrip.framework.vi.ignite.AbstractIgnitePlugin;
import com.ctrip.framework.vi.ignite.IgnitePlugin;
import com.ctrip.framework.vi.util.LogHelper;
import com.ctrip.framework.vi.util.PomUtil;
import com.ctrip.framework.vi.util.Tools;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.naming.SizeLimitExceededException;
import javax.xml.XMLConstants;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by jiang.j on 2016/5/19.
 */
public class Analyzer {

    private static final Logger logger = LoggerFactory.getLogger(Analyzer.class);
    private static final String TMPFILENAME = "vi-analyzer-"+ EnFactory.getEnBase().getAppId()+new Date().getTime()+".obj";
    final static String SELFCHECKTAG = "Self-check";
    public static PomInfo[] getAllPomInfo() throws IOException, ClassNotFoundException {


        Gson gson = new Gson();
        File tmpFile = Paths.get(System.getProperty("java.io.tmpdir"),TMPFILENAME
        ).toFile();
        if(tmpFile.exists()){
            try(InputStream fis = new FileInputStream(tmpFile);
                Reader reader = new InputStreamReader(fis)){

                return gson.fromJson(reader,PomInfo[].class);
            }catch (Throwable e){
                logger.warn("DeSerialize pominfos failed!"+e.getMessage());
                return null;
            }
        }

        List<PomInfo> pomInfos = getAllJarPomInfo();
        pomInfos.add(0, getCurrentPomInfo());
        try(FileOutputStream fos = new FileOutputStream(tmpFile);
            Writer writer = new OutputStreamWriter(fos)){
            gson.toJson(pomInfos, writer);
        }catch (Throwable e){
            logger.warn("Serialize pominfos failed!"+e.getMessage());
        }

        return pomInfos.toArray(new PomInfo[pomInfos.size()]);
    }

    public static PomInfo getCurrentPomInfo(){
        PomInfo jarInfo = new PomInfo();
        try {

            URL root = Thread.currentThread().getContextClassLoader().getResource("/");

            if(root == null){
                return jarInfo;
            }
            File rootF = new File(root.getFile()).getParentFile().getParentFile();
            Path  path = Paths.get(rootF.getAbsolutePath(),"META-INF/maven");


            File file = path.toFile();

            File[] subFiles = file.listFiles();

            if(subFiles != null) {
                subFiles = subFiles[0].listFiles();
                if(subFiles != null) {
                    File pom = subFiles[0].listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            return f.getName().endsWith("pom.xml");
                        }
                    })[0];

                    try (FileInputStream is = new FileInputStream(pom)) {
                        jarInfo = readPom(is);
                    }
                }
            }
        }catch (Throwable e){
            logger.warn("get current jar info failed",e);
        }
        return jarInfo;
    }

    public static PomInfo readPom(InputStream is) throws SAXException, IOException {

        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
        PomDependencyHandler handler = new PomDependencyHandler();
        xmlReader.setContentHandler(handler);
        xmlReader.parse(new InputSource(is));
        return handler.getPomInfo();
    }

    public static List<String> listJarFolder(String jarName) throws IOException {
        List<String> rtn = new ArrayList<>();

        String name = Tools.getNoExtensionName(jarName);
        Path target = Paths.get(System.getProperty("java.io.tmpdir"),
                name + "-sources.jar");
        if(!Files.exists(target)) {
            try(InputStream stream = EnFactory.getEnMaven().getSourceJarByFileName(name)) {
                if(stream != null) {
                    Files.copy(stream, target);
                }
            }
        }

        if(Files.exists(target)) {
            File jarFile = target.toFile();
            try (ZipFile zipFile = new ZipFile(jarFile)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    rtn.add(entry.getName());
                }
            }
        }else{
            URL realUrl = new URL(getJarLocationByName(jarName));
            //InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(jarName);
            try (ZipInputStream zip = new ZipInputStream(realUrl.openStream())) {
                ZipEntry ze;
                while ((ze = zip.getNextEntry()) != null) {
                    rtn.add(ze.getName());
                }
            }
        }

        return rtn;
    }

    public static List<String> listJarClasses(String location) throws IOException, URISyntaxException {
        List<String> rtn = new ArrayList<>();
        if(!location.toLowerCase().startsWith("file:")){
           location = getJarLocationByName(location);
        }

        URL realUrl = new URL(location);
        logger.warn(location);
        if (location.endsWith(".jar")) {
            try (ZipInputStream zip = new ZipInputStream(realUrl.openStream())) {
                ZipEntry ze;
                while ((ze = zip.getNextEntry()) != null) {
                    String name = ze.getName();
                    if(name.endsWith(".class")) {
                        rtn.add(name.substring(0,name.length()-6));
                    }
                }
            }
        }else{
            Path p = Paths.get(realUrl.toURI());
            listFiles(p,p.toString().length()+1,rtn);
        }

        return rtn;

    }
    private static void listFiles(Path path,int prefixLen,List<String> files) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    listFiles(entry, prefixLen, files);
                }

                String p = entry.toString();
                if (p.endsWith(".class")) {
                    files.add(p.substring(prefixLen,p.length()-6).replace('\\', '/'));
                }
            }
        }
    }



    public static Set<String> getAllJarNames() throws IOException {

        Set<String> jarNames = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        String metaPath = "META-INF";
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(metaPath);

        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (url != null && "jar".equals(url.getProtocol())) {
                String urlStr = url.toString();
                String location = urlStr.substring(urlStr.indexOf('f'), urlStr.lastIndexOf('!'));
                jarNames.add(new File(location).getName());
            }
        }

        return jarNames;
    }

    public static String getJarLocationByName(String name) throws IOException {
        String rtn = "";
        String metaPath = "META-INF";
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(metaPath);

        name = "/"+name;
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (url != null){
                String urlStr = url.toString();
                if(urlStr.charAt(urlStr.length()-1)=='/'){
                    urlStr = urlStr.substring(0,urlStr.length()-2);
                }
                int last = urlStr.lastIndexOf('!');

                String location = urlStr.substring(urlStr.indexOf('f'),last>0?last:urlStr.lastIndexOf('/'));
                if(location.toLowerCase().contains(name.toLowerCase())){
                    return location;
                }
            }
        }


        return rtn;

    }

    public static Map<String,String> getAllModuleInfo() throws IOException {

        Map<String,String> rtn = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        String metaPath = "META-INF";
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(metaPath);

        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (url != null){
                String urlStr = url.toString();
                if(urlStr.charAt(urlStr.length()-1)=='/'){
                   urlStr = urlStr.substring(0,urlStr.length()-2);
                }
                int last = urlStr.lastIndexOf('!');

                String location = urlStr.substring(urlStr.indexOf('f'),last>0?last:urlStr.lastIndexOf('/'));
                rtn.put(new File(location).getName(),location);
            }
        }

        return rtn;
    }

    public static List<PomInfo> getAllJarPomInfo() throws IOException {

        List<PomInfo> pomInfos = new ArrayList<>();
        String metaPath = "META-INF";
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(metaPath);

        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (url != null && "jar".equals(url.getProtocol())) {
                String urlStr = url.toString();
                String location = urlStr.substring(urlStr.indexOf('f'),urlStr.lastIndexOf('!'));

                Properties properties = new Properties();
                URL realUrl = new URL(location);
                try ( ZipInputStream zip = new ZipInputStream(realUrl.openStream())) {
                    long jarSize = Files.size(Paths.get(realUrl.toURI()));
                    ZipEntry ze;
                    PomInfo pomInfo = null;
                    boolean hasPom =false;
                    while ((ze = zip.getNextEntry()) != null) {
                        String zePath = ze.getName();
                        if(zePath.startsWith(metaPath+"/maven")&&zePath.endsWith("pom.xml")){
                            pomInfo = readPom(zip);
                            hasPom =true;
                            break;
                        }
                        if(zePath.equals(metaPath+"/MANIFEST.MF")){
                            properties.load(zip);
                        }
                    }

                    if(!hasPom){
                        String fName = location.substring(location.lastIndexOf('/') + 1,location.length()-4);
                        String[] av = PomUtil.getArtifactIdAndVersion(fName);

                        if(av!=null) {
                            if(av[1].split("-").length>2 && properties.containsKey("Specification-Version")){
                                av[1] = properties.getProperty("Specification-Version").trim();
                            }

                            try (InputStream pomStream = PomUtil.getPomInfoByFileName(av,fName)) {
                                pomInfo = readPom(pomStream);
                            }
                        }

                        if(pomInfo == null) {
                            pomInfo = new PomInfo();
                            if(av!=null) {
                                pomInfo.artifactId = av[0];
                                pomInfo.version = av[1];
                            }else{
                                pomInfo.artifactId = fName;
                            }

                            pomInfo.groupId = "nopom";
                        }
                    }
                    pomInfo.location = location;
                    pomInfo.size = jarSize;
                    pomInfos.add(pomInfo);
                }catch (Throwable e){
                    logger.warn("get jar maven pom failed! location:"+location,e);
                }
            }
        }
        return pomInfos;
    }

    private static AtomicInteger currentSlfCheckThreadCount = new AtomicInteger(0);
    private static final int MAXSLFCHECKCOUNT = 6;

    public static String selfCheck(final String pluginId) throws NotFoundException, SizeLimitExceededException {

        final IgnitePlugin plugin = IgniteManager.getPluginMap().get(pluginId);
        if(plugin == null || !(plugin instanceof AbstractIgnitePlugin)){
            throw  new NotFoundException("ignite plugin: "+pluginId+" not found or the plugin is not extends from  AbstractIgnitePlugin!");
        }

        if(currentSlfCheckThreadCount.get()>MAXSLFCHECKCOUNT) {
            throw new SizeLimitExceededException("Self-check requests cannot exceed "+MAXSLFCHECKCOUNT+"!");
        }
        final String uid = pluginId + System.currentTimeMillis();
        final IgniteManager.SimpleLogger slogger = SimpleLoggerFactory.newSimpleLogger(uid);
        new Thread("vi-self-check"){
            @Override
            public void run() {
                currentSlfCheckThreadCount.incrementAndGet();
                boolean result = false;
                try {
                    slogger.info(LogHelper.beginBlock(SELFCHECKTAG,new String[]{"pluginId" , pluginId}));
                     result = ((AbstractIgnitePlugin) plugin).selfCheck(slogger);
                }catch (Throwable e){
                   slogger.error("run "+uid+" self-check failed!",e);
                } finally {
                    slogger.info(pluginId+" self check "+(result?"success":"failed")+"!");
                    slogger.info(LogHelper.endBlock(SELFCHECKTAG,new String[]{"isPass",String.valueOf(result)}));
                    slogger.info(MagicWord.CHECKEND+result);
                    try {
                        Thread.sleep(7000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    SimpleLoggerFactory.removeSimpleLogger(uid);
                    currentSlfCheckThreadCount.decrementAndGet();
                }
            }
        }.start();

        return uid;


    }
}

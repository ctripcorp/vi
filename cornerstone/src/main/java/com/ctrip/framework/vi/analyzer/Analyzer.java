package com.ctrip.framework.cornerstone.analyzer;


import com.ctrip.framework.cornerstone.enterprise.EnFactory;
import com.ctrip.framework.cornerstone.util.PomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by jiang.j on 2016/5/19.
 */
public class Analyzer {

    private static final Logger logger = LoggerFactory.getLogger(Analyzer.class);
    private static final String TMPFILENAME = "vi-analyzer-"+ EnFactory.getEnBase().getAppId()+new Date().getTime()+".obj";
    public static List<PomInfo> getAllPomInfo() throws IOException, ClassNotFoundException {

        List<PomInfo> pomInfos = null;
        File tmpFile = Paths.get(System.getProperty("java.io.tmpdir"),TMPFILENAME
        ).toFile();
        if(tmpFile.exists()){
            try(FileInputStream fis = new FileInputStream(tmpFile);
                ObjectInputStream ois = new ObjectInputStream(fis)){
                pomInfos = (List<PomInfo>) ois.readObject();
            }catch (Throwable e){
                logger.warn("DeSerialize pominfos failed!"+e.getMessage());
            }
        }

        if(pomInfos == null){
            pomInfos = getAllJarPomInfo();
            pomInfos.add(0, getCurrentPomInfo());
            try(FileOutputStream fos = new FileOutputStream(tmpFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos)){
                oos.writeObject(pomInfos);
            }catch (Throwable e){
                logger.warn("Serialize pominfos failed!"+e.getMessage());
            }
        }
        return pomInfos;
    }

    public static PomInfo getCurrentPomInfo(){
        PomInfo jarInfo = new PomInfo();
        try {

            URL root = Thread.currentThread().getContextClassLoader().getResource("/");

            File rootF = new File(root.getFile()).getParentFile().getParentFile();
            Path  path = Paths.get(rootF.getAbsolutePath(),"META-INF/maven");


            File file = path.toFile();

            File pom = file.listFiles()[0].listFiles()[0].listFiles(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    return f.getName().endsWith("pom.xml");
                }
            })[0];

            try(FileInputStream is = new FileInputStream(pom)) {
                jarInfo = readPom(is);
            }
        }catch (Throwable e){
            logger.warn("get current jar info failed",e);
            e.printStackTrace();
        }
        return jarInfo;
    }

    public static PomInfo readPom(InputStream is) throws SAXException, IOException {

        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        PomDependencyHandler handler = new PomDependencyHandler();
        xmlReader.setContentHandler(handler);
        xmlReader.parse(new InputSource(is));
        return handler.getPomInfo();
    }

    public static List<PomInfo> getAllJarPomInfo() throws IOException {

        List<PomInfo> pomInfos = new LinkedList<>();
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
}

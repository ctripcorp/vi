package com.ctrip.framework.cs.component.defaultComponents;

import com.ctrip.framework.cs.annotation.ComponentStatus;
import com.ctrip.framework.cs.enterprise.ConfigUrlContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by jiang.j on 2016/9/26.
 */
@ComponentStatus(id="vi.allconfigfiles",name="all config files",description = "查看所有配置文件",singleton = true,custom = true)
public class AllConfigFiles {


    public static class ConfigFile{
        public String suffix;
        public String name;
        public long lastModified;
        public long size;
        public String path;

    }

    private static List<ConfigFile> externalFiles = Collections.synchronizedList(new ArrayList<ConfigFile>());
    public static  boolean addConfigFile(File f){
        if(f!= null && f.exists()){

            boolean canAdd = false;
            for(String suf:suffixes){
                if(f.getName().toLowerCase().endsWith(suf)) {
                    canAdd = true;
                }
            }
            if(canAdd) {
                ConfigFile configFile = new ConfigFile();
                configFile.path = String.valueOf(ConfigUrlContainer.addUrl(f.getAbsolutePath()));
                configFile.name = f.getAbsolutePath();
                configFile.size = f.length();
                configFile.lastModified = f.lastModified();
                externalFiles.add(0,configFile);
                return true;
            }
        }

        return false;
    }

    private transient Logger logger = LoggerFactory.getLogger(getClass());
    private List<ConfigFile> allFiles;
    static final String[] suffixes = new String[]{"xml", "properties", "yaml", "json","config"};
    public AllConfigFiles(){

        allFiles = externalFiles;
        try {
            URL rootUrl = Thread.currentThread().getContextClassLoader().getResource("/");
            if(rootUrl != null) {
                Path rootFolderPath = Paths.get(rootUrl.toURI());
                allFiles.addAll(getAllConfigFiles(rootFolderPath, suffixes));
            }
            allFiles.addAll(getAllJarConfigs(suffixes));
        }catch (Throwable e){
            logger.warn("get app root path failed!",e);
        }
    }

    private List<ConfigFile> getAllJarConfigs(final String[] suffixes) throws IOException {

        List<ConfigFile> configs = new ArrayList<>();
        String metaPath = "META-INF";
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(metaPath);

        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (url != null && "jar".equals(url.getProtocol())) {
                String urlStr = url.toString();
                String location = urlStr.substring(urlStr.indexOf('f'), urlStr.lastIndexOf('!'));
                String jarName = location.substring(location.lastIndexOf('/')+1, location.length());

                URL realUrl = new URL(location);
                try (ZipInputStream zip = new ZipInputStream(realUrl.openStream())) {
                    ZipEntry ze;
                    while ((ze = zip.getNextEntry()) != null) {

                        boolean canAdd = false;
                        String zeNameLowerCase = ze.getName().toLowerCase();

                        /*
                        if(zeNameLowerCase.endsWith("pom.xml") || zeNameLowerCase.endsWith("pom.properties")){
                            continue;
                        }*/

                        for (String suf : suffixes) {
                           if(zeNameLowerCase.endsWith("." + suf)) {
                              canAdd = true;
                               break;
                           }
                        }

                        if(canAdd) {

                            ConfigFile configFile = new ConfigFile();
                            configFile.lastModified = ze.getTime();
                            configFile.size = ze.getSize();
                            configFile.name = jarName + "!/" + ze.getName();
                            configFile.path = location + "!/" + ze.getName();
                            configs.add(configFile);
                        }

                    }

                } catch (Throwable e) {
                    logger.warn("get jar maven pom failed! location:" + location, e);
                }
            }
        }

        return configs;
    }

    private List<ConfigFile> getAllConfigFiles(Path folderPath, final String[] suffixes){

        final List<ConfigFile> rtn = new ArrayList<>();
        final int prefixLen = folderPath.toString().length();
        try {
            Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path path, BasicFileAttributes attrs) throws IOException {
                    for (String suf : suffixes) {
                        String fullPath = path.toString();
                        if (fullPath.toLowerCase().endsWith("."+suf)) {
                            ConfigFile cfile = new ConfigFile();
                            cfile.lastModified = attrs.lastModifiedTime().toMillis();
                            cfile.size = attrs.size();
                            cfile.suffix = suf;
                            cfile.path = fullPath.substring(prefixLen);
                            cfile.name = cfile.path;
                            rtn.add(cfile);
                            return FileVisitResult.CONTINUE;
                        }

                    }
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (Throwable e) {
            logger.warn("get config files failed!", e);
        }

        return rtn;
    }
}

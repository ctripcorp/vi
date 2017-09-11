package com.ctrip.framework.vi.code;

import com.ctrip.framework.vi.asm.ClassReader;
import com.ctrip.framework.vi.asm.util.TraceClassVisitor;
import com.ctrip.framework.vi.configuration.ConfigurationManager;
import com.ctrip.framework.vi.configuration.InitConfigurationException;
import com.ctrip.framework.vi.enterprise.EnFactory;
import com.ctrip.framework.vi.util.IOUtils;
import com.ctrip.framework.vi.util.Tools;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by jiang.j on 2017/4/24.
 */
public final class SourceCodeHelper {

    public static class SourceCode{
        public int size;
        public String file_name;
        public String content;
    }
    public static SourceCode getJarSourceCode(String jarName,String path) throws IOException {

        SourceCode rtn= new SourceCode();
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
        File jarFile = target.toFile();
        if(jarFile.exists()) {
            try (ZipFile zipFile = new ZipFile(jarFile)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.getName().equalsIgnoreCase(path)) {
                        rtn.size = (int) entry.getSize();
                        rtn.file_name = Tools.getFileName(entry.getName());
                        rtn.content = IOUtils.readAll(zipFile.getInputStream(entry));
                    }
                }
            }
        }else{

            if(path.endsWith(".java")){
                path = path.substring(0,path.length()-5).replace('.','/')+".class";
            }
            URL realUrl = new URL(getJarLocationByPath(path));
            try (ZipInputStream zip = new ZipInputStream(realUrl.openStream())) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    if (entry.getName().equalsIgnoreCase(path)) {
                        rtn.size = (int) entry.getSize();
                        rtn.file_name = Tools.getFileName(entry.getName());
                        rtn.content = decompileClass(zip);
                    }
                }
            }
        }


        return rtn;
    }

    public static String decompileClass(InputStream is) throws IOException {

        ClassReader cr = new ClassReader(is);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TraceClassVisitor cv = new TraceClassVisitor(new PrintWriter(outputStream));
        cr.accept(cv,ClassReader.EXPAND_FRAMES);
        return outputStream.toString("UTF-8");
    }

    public static String getJarLocationByPath(URL url) throws IOException {

        String rtn = "";
        if(url == null){
            return rtn;
        }else {
            String urlStr = url.toString();
            if(urlStr.charAt(urlStr.length()-1)=='/'){
                urlStr = urlStr.substring(0,urlStr.length()-2);
            }
            int last = urlStr.lastIndexOf('!');

            return  urlStr.substring(urlStr.indexOf('f'),last>0?last:urlStr.lastIndexOf('/'));
        }
    }

    public static String getJarLocationByPath(String path) throws IOException {

        String rtn = "";
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);

        if(url == null){
            return rtn;
        }else {
            return getJarLocationByPath(url);
        }

    }

    public static String getCodePath(String namespace,String name) throws IOException, InitConfigurationException {

        String rtn = "";
        URL url = Thread.currentThread().getContextClassLoader().getResource(namespace.replace('.', '/')+"/"+name+".class");

        if(url == null){
            return rtn;
        }

        if ("jar".equals(url.getProtocol())) {
            String urlStr = url.toString();
            String location = urlStr.substring(urlStr.indexOf('f'), urlStr.lastIndexOf('!'));
            rtn = "jar|"+new File(location).getName()+"|"+namespace.replace('.','|');
        }else {

            rtn = "git|"+ ConfigurationManager.getConfigInstance().getString("app.source.folder");
        }

        return rtn;

    }
}

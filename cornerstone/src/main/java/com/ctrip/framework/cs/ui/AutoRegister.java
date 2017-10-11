package com.ctrip.framework.cs.ui;

import com.ctrip.framework.cs.IgniteManager;
import com.ctrip.framework.cs.asm.ClassReader;
import com.ctrip.framework.cs.asm.Opcodes;
import com.ctrip.framework.cs.component.ComponentManager;
import com.ctrip.framework.cs.configuration.Configuration;
import com.ctrip.framework.cs.configuration.ConfigurationManager;
import com.ctrip.framework.cs.configuration.InitConfigurationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by jiang.j on 2017/4/10.
 */
public class AutoRegister {

    private static final String SCANPATTERNKEY = "vi.scan.pattern";

    private static void scanClass(InputStream stream,Set<String> ignitePlugins,IgniteManager.SimpleLogger simpleLogger){

        ClassReader classReader = null;
        try {
            classReader = new ClassReader(stream);
            VIClassAnnotationVisitor visitor = new VIClassAnnotationVisitor(Opcodes.ASM5);
            classReader.accept(visitor, ClassReader.SKIP_CODE);
            String className = classReader.getClassName().replace("/",".");
            switch (visitor.annotationType()){
                case NONE:
                    break;
                case IGNITE:
                    simpleLogger.info("Register ignite plugin - " + className);
                    ignitePlugins.add("ignite." + className);
                    break;
                case COMPONENT:
                    try{
                        Class<?> componentClass = Class.forName(className);
                        simpleLogger.info("Register component - " + className.substring(className.lastIndexOf('.')) + ", IsSuccess: " + ComponentManager.add(componentClass));
                    }catch (Throwable e){
                        simpleLogger.error("Auto Register component failed! " + className);
                    }
                    simpleLogger.info(className);
                    break;
            }
        } catch (Throwable e) {
            simpleLogger.error("wrong class file format", e);
        }

    }

    private static void findInApp(File directory ,Set<String> ignitePlugins,IgniteManager.SimpleLogger simpleLogger){
        // get all the files from a directory
        File[] fList = directory.listFiles();
        if(fList == null) return;

        for (File file : fList) {
            if (file.isFile() && file.getName().endsWith(".class")) {
                try(InputStream fs = new FileInputStream(file)) {
                    scanClass(fs,ignitePlugins,simpleLogger);
                }catch (Throwable e) {
                    simpleLogger.error("visit class " + file.getName() + " failed!", e);
                }

            } else if (file.isDirectory()) {
                findInApp(file,ignitePlugins, simpleLogger);
            }
        }
    }
    private static void scanJar(Set<String> ignitePlugins,IgniteManager.SimpleLogger simpleLogger){

        String metaPath = "META-INF";
        try {
            Configuration configuration = ConfigurationManager.getConfigInstance();
            if(configuration.containsKey(SCANPATTERNKEY)){

                String patternStr = configuration.getString(SCANPATTERNKEY);
                simpleLogger.warn("Auto scan jar be enabled. Current scan pattern is "+ patternStr);
                Pattern pattern = Pattern.compile(patternStr);
                Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(metaPath);

                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    if (url != null && "jar".equals(url.getProtocol())) {
                        String urlStr = url.toString();
                        String location = urlStr.substring(urlStr.indexOf('f'), urlStr.lastIndexOf('!'));

                        URL realUrl = new URL(location);
                        try (ZipInputStream zip = new ZipInputStream(realUrl.openStream())) {
                            ZipEntry ze;
                            while ((ze = zip.getNextEntry()) != null ){
                                String name = ze.getName();
                                if(pattern.matcher(name).find() && name.endsWith(".class") ) {
                                    scanClass(zip,ignitePlugins,simpleLogger);
                                }
                            }
                        }
                    }
                }
            }else {
                simpleLogger.warn("Auto scan jar be disabled. Because no scan pattern be set");

            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void autoRegister(IgniteManager.SimpleLogger simpleLogger) throws InitConfigurationException {

        Set<String> ignitePlugins = ConfigurationManager.getConfigKeys("ignite");
        URL root = Thread.currentThread().getContextClassLoader().getResource("");
        simpleLogger.info("App root path:"+root);
        if(root != null) {
            File rootF = new File(root.getFile());
            findInApp(rootF, ignitePlugins, simpleLogger);
        }

        scanJar( ignitePlugins,simpleLogger);

        //remove default ignite plugin
        if(ignitePlugins != null) {
            ignitePlugins.remove("ignite.VICoreIgnite");
        }

    }
}

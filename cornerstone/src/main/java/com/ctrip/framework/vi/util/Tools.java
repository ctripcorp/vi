package com.ctrip.framework.cornerstone.util;

import com.ctrip.framework.cornerstone.configuration.ConfigurationException;
import com.ctrip.framework.cornerstone.configuration.PropertiesConfiguration;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang.j on 2016/3/25.
 */
public final class  Tools {

    private static final Logger logger = LoggerFactory.getLogger(Tools.class);

    public final static String byteToKB(long number){
        DecimalFormat decimalFormat = new DecimalFormat("#,###.# KB");
        return decimalFormat.format(number/1024);
    }
    public final static String byteToMB(long number){
        DecimalFormat decimalFormat = new DecimalFormat("#,###.# MB");
        return decimalFormat.format(number/1024/1024);
    }
    public final static String byteToGB(long number){
        DecimalFormat decimalFormat = new DecimalFormat("#,###.# GB");
        return decimalFormat.format(number/1024/1024/1024);
    }

    public final static String milToSec(long nao){

        DecimalFormat decimalFormat = new DecimalFormat("#,###.### sec");
        return decimalFormat.format(nao/Math.pow(10,3));
    }
    public final static String naoToSec(long nao){

        DecimalFormat decimalFormat = new DecimalFormat("#,###.### sec");
        return decimalFormat.format(nao/Math.pow(10,9));
    }


    public static PropertiesConfiguration loadPropertiesFromFile(String fileName) throws ConfigurationException {

        String tmpFileName = fileName;
        URL url = Thread.currentThread().getContextClassLoader().getResource("/");
        if(url != null){
            tmpFileName += new File(url.getFile()).lastModified();
        }
        File tmpFile = Paths.get(System.getProperty("java.io.tmpdir"),
                tmpFileName).toFile();
        return  new PropertiesConfiguration(tmpFile);
    }

    public static String getInnerResources(Class<?> belongClass,String folder,String id,String suffix) throws IOException {
        String rtn ="";
        try(InputStream is = belongClass.getClassLoader().getResourceAsStream(folder + "/" + id + "." + suffix)) {
            if (is != null) {
                ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
                byte[] bs = new byte[4096];
                int c;
                while ((c = is.read(bs)) > 0) {
                    os.write(bs, 0, c);
                }
                rtn = new String(os.toByteArray(), "utf-8");
            }
        }

        return rtn;
    }
    public static Object doClassStaticMethod(Class<?> belongClass,String method,Map<String,Object> paras) throws InvocationTargetException, IllegalAccessException {
        Method[] methods =belongClass.getMethods();
        String methodParaJson=null;
        if(paras!=null && paras.containsKey("req")){
            methodParaJson = String.valueOf(paras.get("req"));
        }
        for(Method m:methods){
            if(m.getName().equalsIgnoreCase(method) && (m.getModifiers()& Modifier.STATIC)== Modifier.STATIC){
                Type[] paraTypes = m.getParameterTypes();
                if(paraTypes.length==0 && methodParaJson==null){
                    return m.invoke(null);
                }else if(paraTypes.length==1 && methodParaJson !=null){
                    Gson gson = new Gson();
                    return m.invoke(null,gson.fromJson(methodParaJson, paraTypes[0]));
                }
            }
        }
        return new NoSuchMethodException("no public static method "+method+" be found in"+belongClass.getName());
    }
}

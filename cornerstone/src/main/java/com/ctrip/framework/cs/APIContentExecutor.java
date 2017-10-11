package com.ctrip.framework.cs;

import com.ctrip.framework.cs.annotation.Sensitive;
import com.ctrip.framework.cs.enterprise.ConfigUrlContainer;
import com.ctrip.framework.cs.localLog.LocalLogManager;
import com.ctrip.framework.cs.util.SecurityUtil;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by jiang.j on 2016/12/26.
 */
public class APIContentExecutor {

    static final String JSONFIELDS = "$fields";
    static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    static VIApiHandler handler = VIApiHandler.getInstance();

    public static RequestResult doGet(String callPath,Map<String,Object> params,String user,String token,String remoteIp){

        String validUser= SecurityUtil.getValidUserName(user,token,remoteIp);
        if(callPath.startsWith("/download/log/")) {
            return downloadLogFile(callPath.substring(14));
        }else if(callPath.startsWith("/download/config/") && validUser != null){
            return downloadConfigFile(callPath.substring(17));
        }

        RequestResult rtn = new RequestResult();

        rtn.user = validUser;

        rtn.headers.put("Access-Control-Allow-Origin", "*");
        rtn.headers.put("Access-Control-Allow-Headers", "Content-Type, Accept");
        rtn.headers.put("Access-Control-Expose-Headers",SecurityUtil.PERMISSIONKEY);
        rtn.contentType = "application/json";


        VIApiHandler.ExeResult exeResult = handler.executeService(callPath, validUser,params);
        rtn.headers.put(SecurityUtil.PERMISSIONKEY, String.valueOf(exeResult.getPermission()));
        rtn.responseCode = exeResult.getResponseCode();
        if(exeResult.getData() !=null) {
            try {
                rtn.content = toJsonBytes(exeResult.getData(), params == null ? null : params.get(JSONFIELDS));
            }catch (Throwable e){
                String reason =callPath + " serialize result failed";
                logger.warn(reason,e);
                rtn.content = reason.getBytes();
                rtn.responseCode = 500;
            }
        }

        return rtn;
    }

    public static void logWarn(String msg,Throwable e){
        logger.warn(msg,e);
    }
    public static void logError(String msg,Throwable e){
        logger.error(msg,e);
    }

    private static byte[] toJsonBytes(Object object, final Object rawFields){
        GsonBuilder builder = new GsonBuilder();

        if(rawFields != null){
            final String fields = (String) rawFields;
            builder.setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {

                    Pattern pattern = Pattern.compile("(^|,)" + f.getName() + "($|,)", Pattern.CASE_INSENSITIVE);
                    boolean shouldSkip = false;
                    if (!pattern.matcher(fields).find() || f.getAnnotation(Sensitive.class)!= null){
                        shouldSkip = true;
                    }
                    return shouldSkip;
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            });
        }else {
            builder.setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {

                    boolean shouldSkip = false;
                    if (f.getAnnotation(Sensitive.class)!= null){
                        shouldSkip = true;
                    }
                    return shouldSkip;
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            });

        }
        Gson gson = builder.create();

        return gson.toJson(object).getBytes(Charset.forName("UTF-8"));
    }

    public static RequestResult doPost(String callPath,Map<String,Object> params,String user,String token,String remoteIp)
    {
        RequestResult rtn = new RequestResult();
        //String json = IOUtils.readAll(req.getInputStream());
        rtn.headers.put("Access-Control-Allow-Origin", "*");
        rtn.headers.put("Access-Control-Allow-Headers", "Content-Type, Accept");
        rtn.contentType = "application/json";
        String validUser = SecurityUtil.getValidUserName(user,token,remoteIp);

        VIApiHandler.ExeResult exeResult = handler.executeService(callPath, validUser,params);
        rtn.responseCode = (exeResult.getResponseCode());
        rtn.headers.put(SecurityUtil.PERMISSIONKEY, String.valueOf(exeResult.getPermission()));
        try {
            rtn.content = toJsonBytes(exeResult.getData(), params == null ? null : params.get(JSONFIELDS));
        }catch (Throwable e){
            String reason =callPath + " serialize result failed";
            logger.warn(reason,e);
            rtn.content = reason.getBytes();
            rtn.responseCode = 500;
        }
        return rtn;
    }


    private static RequestResult downloadConfigFile(String configPath){

        RequestResult rtn = new RequestResult();

        rtn.headers.put("Access-Control-Allow-Origin", "*");
        rtn.headers.put("Access-Control-Allow-Headers", "Content-Type, Accept");
        //resp.setContentType(mediaType);


        rtn.responseCode = HttpServletResponse.SC_OK;
        if(configPath.endsWith(".xml")){
            rtn.contentType = "text/xml";
        }

        if(configPath.equals("root_web.xml")){

            try {
                URL rootUrl = Thread.currentThread().getContextClassLoader().getResource("/");
                if(rootUrl!=null) {
                    Path rootFolderPath = Paths.get(Paths.get(rootUrl.toURI()).getParent().toString(), "web.xml");
                    rtn.content = Files.readAllBytes(rootFolderPath);
                }

            }catch (Throwable e){
                logger.warn("get root path failed!",e);
            }
        }else {

            String realPath = configPath;
            if(configPath.length()>0 && Character.isDigit(configPath.charAt(0))){
                int key = Integer.parseInt(configPath);
                realPath = "file:" + ConfigUrlContainer.getUrl(key);

            }
            else if(configPath.startsWith("file:")) {
                realPath = "jar:" + configPath;
            }

            if(realPath.endsWith(".xml")){
                rtn.contentType = "text/xml";
            }

            try {
                rtn.streamContent = getConfigInputStream(realPath);
            } catch (Throwable e) {
                logger.warn("get config file failed!", e);
            }
        }

        return rtn;

    }

    private static InputStream getConfigInputStream(String configPath){
        try {
            if (configPath.startsWith("file:") || configPath.startsWith("jar:")) {
                return (InputStream) new URL(configPath).getContent();
            } else {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream(configPath);
            }
        }catch (Throwable e){
            logger.warn("get "+configPath + " failed!");
            return null;
        }
    }

    private static RequestResult downloadLogFile(String logName){

        RequestResult rtn = new RequestResult();
        try
        {
            String[] parts = logName.split("@@");

            if(parts.length>1){
               logName = parts[0]+"/"+parts[1];
            }

            File file = LocalLogManager.getLogFile(logName);
            rtn.streamContent = new FileInputStream(file);
            rtn.headers.put("Content-disposition", "attachment;filename=" + (parts.length>1?parts[1]:logName));
            //rtn.headers.put("Content-Length", String.valueOf(file.length()));
        } catch (Throwable e) {
            logger.warn("download log " + logName + " failed!", e);
        }
        return rtn;

    }
}

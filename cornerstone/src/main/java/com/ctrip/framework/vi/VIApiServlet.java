package com.ctrip.framework.cornerstone;

import com.ctrip.framework.cornerstone.enterprise.ConfigUrlContainer;
import com.ctrip.framework.cornerstone.localLog.LocalLogManager;
import com.ctrip.framework.cornerstone.util.HttpUtil;
import com.ctrip.framework.cornerstone.util.IOUtils;
import com.ctrip.framework.cornerstone.util.SecurityUtil;
import com.ctrip.framework.cornerstone.util.Tools;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by jiang.j on 2016/3/28.
 */
public class VIApiServlet extends HttpServlet {


    @Override
    public void init() throws ServletException {
        super.init();
        try {
            IgniteManager.ignite();
        }catch (Throwable e){
            logger.error("VI ignite failed",e);
        }
    }

    /*

        private void handlerRequest(HttpServletRequest req, HttpServletResponse resp,boolean isPost) throws ServletException, IOException {

            String remoteIp = HttpUtil.getIpAddr(req);

            RequestResult exeResult;
            if(!isPost) {
                exeResult = APIContentExecutor.doGet(req.getPathInfo(), HttpUtil.getReqParams(req), HttpUtil.getCookieByName(req, SecurityUtil.USERKEY, "/"),
                        HttpUtil.getCookieByName(req, SecurityUtil.TOKENKEY, "/"), remoteIp);
            }else {

                exeResult = APIContentExecutor.doPost(req.getPathInfo(), HttpUtil.loadPostParams(req), HttpUtil.getCookieByName(req, SecurityUtil.USERKEY, "/"),
                        HttpUtil.getCookieByName(req, SecurityUtil.TOKENKEY, "/"), remoteIp);
            }


            if(exeResult == null){
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }else{
                if(exeResult.user != null){
                    try {
                        HttpUtil.addCookie(resp, SecurityUtil.TOKENKEY, SecurityUtil.generateToken(exeResult.user, remoteIp), "/", -1);
                    } catch (Exception e) {
                        APIContentExecutor.logWarn("refresh token failed! ", e);
                    }
                }

                for(Map.Entry<String,String> entry:exeResult.headers.entrySet()){
                    resp.addHeader(entry.getKey(),entry.getValue());
                }
                if(exeResult.content!=null) {
                    resp.setContentType(exeResult.contentType);
                    resp.setCharacterEncoding("utf-8");
                    resp.setStatus(exeResult.responseCode);
                    resp.getOutputStream().write(exeResult.content);
                }else if(exeResult.streamContent != null){
                    OutputStream out = resp.getOutputStream();
                    try{
                        byte[] buf=new byte[8192];
                        int bytesread = 0, bytesBuffered = 0;
                        while( (bytesread = exeResult.streamContent.read( buf )) > -1 ) {
                            out.write( buf, 0, bytesread );
                            bytesBuffered += bytesread;
                            if (bytesBuffered > 1024 * 1024) { //flush after 1MB
                                bytesBuffered = 0;
                                out.flush();
                            }
                        }

                    }catch (Throwable e){
                        APIContentExecutor.logWarn("get " + req.getRequestURL() + " failed!", e);
                    }finally {
                        out.flush();
                        exeResult.streamContent.close();
                    }
                }
                resp.getOutputStream().close();
            }
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            handlerRequest(req,resp,false);
        }


        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException
        {
            handlerRequest(req,resp,true);

        }
         */
    final String JSONFIELDS = "$fields";
    Logger logger = LoggerFactory.getLogger(this.getClass());
    VIApiHandler handler = VIApiHandler.getInstance();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String path = req.getPathInfo();
        String validUser= SecurityUtil.getValidUserName(req);
        if(path.startsWith("/download/log/")) {
            downloadLogFile(resp, path.substring(14));
            return;
        }else if(path.startsWith("/download/config/") && validUser != null){
            downloadConfigFile(resp, path.substring(17));
            return;
        }

        if(validUser!=null){
            try {
                SecurityUtil.refreshToken(validUser,req,resp);
            } catch (Exception e) {
                logger.warn("refresh vi token failed!",e);
            }

        }

        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Headers","Content-Type, Accept");
        resp.addHeader("Access-Control-Expose-Headers",SecurityUtil.PERMISSIONKEY);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");


        Map<String,Object> params = HttpUtil.getReqParams(req);
        VIApiHandler.ExeResult exeResult = handler.executeService(path, validUser,params);
        resp.addHeader(SecurityUtil.PERMISSIONKEY, String.valueOf(exeResult.getPermission()));
        resp.setStatus(exeResult.getResponseCode());
        Object rtn = exeResult.getData();
        if(rtn !=null) {
            resp.getOutputStream().write(toJsonBytes(rtn, params==null?null:params.get(JSONFIELDS)));
        }
        resp.getOutputStream().close();

    }

    private byte[] toJsonBytes(Object object, final Object rawFields){
        GsonBuilder builder = new GsonBuilder();

        if(rawFields != null){
            final String fields = (String) rawFields;
            builder.setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {

                    Pattern pattern = Pattern.compile("(^|,)" + f.getName() + "($|,)", Pattern.CASE_INSENSITIVE);
                    boolean shouldSkip = false;
                    if (!pattern.matcher(fields).find()){
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        //String json = IOUtils.readAll(req.getInputStream());
        resp.setHeader("Access-Control-Allow-Origin","*");
        resp.addHeader("Access-Control-Allow-Headers","Content-Type, Accept");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");
        String validUser = SecurityUtil.getValidUserName(req);

        Map<String,Object> params = loadParams(req);
        VIApiHandler.ExeResult exeResult = handler.executeService(req.getPathInfo(), validUser,params);
        resp.setStatus(exeResult.getResponseCode());
        resp.addHeader(SecurityUtil.PERMISSIONKEY, String.valueOf(exeResult.getPermission()));
        resp.getOutputStream().write(toJsonBytes(exeResult.getData(),params==null?null:params.get(JSONFIELDS)));
        resp.getOutputStream().close();
    }

    private Map<String, Object> loadParams(HttpServletRequest req){
        Map<String,Object> params = null;

        try {
            Gson gson = new Gson();
            Type paraMap = new TypeToken<Map<String, JsonElement>>(){}.getType();
            String rawJson = IOUtils.readAll(req.getInputStream());
            if(rawJson == null || rawJson.length() == 0){

                for(Map.Entry<String, String[]> entry:req.getParameterMap().entrySet()){
                    rawJson += entry.getKey();

                    if(entry.getValue()[0].length()>0) {
                       rawJson +="=" + entry.getValue()[0];
                    }
                }
            }
            params = gson.fromJson(rawJson,paraMap);
            if(params == null){
                params = new HashMap<>();
            }
            params.put("req_ip",req.getRemoteAddr());
        } catch (Throwable e) {
            logger.warn("load params failed!"+e.getMessage()+"\n");
            e.printStackTrace();
        }
        return params;
    }

    private void downloadConfigFile(HttpServletResponse resp,String configPath) throws IOException {


        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Headers","Content-Type, Accept");
        //resp.setContentType(mediaType);

        byte[] buffer = new byte[10240];

        resp.setStatus(HttpServletResponse.SC_OK);
        if(configPath.endsWith(".xml")){
            resp.setContentType("text/xml");
        }

        if(configPath.equals("root_web.xml")){

            try {
                URL rootUrl = Thread.currentThread().getContextClassLoader().getResource("/");
                Path rootFolderPath = Paths.get(Paths.get(rootUrl.toURI()).getParent().toString(),"web.xml");
                resp.getOutputStream().write(Files.readAllBytes(rootFolderPath));

            }catch (Throwable e){
                logger.warn("get root path failed!",e);
            }
        }else {

            String realPath = configPath;
            if(configPath.length()>0 && Character.isDigit(configPath.charAt(0))){
                int key = Integer.parseInt(configPath);
                realPath = "file:" + ConfigUrlContainer.getUrl(key);
                if(realPath.endsWith(".xml")){
                    resp.setContentType("text/xml");
                }

            }
            else if(configPath.startsWith("file:")) {
                realPath = "jar:" + configPath;
            }

            try (
                    InputStream configStream = getConfigInputStream(realPath);
                    OutputStream output = resp.getOutputStream();
            ) {
                for (int length = 0; (length = configStream.read(buffer)) > 0; ) {
                    output.write(buffer, 0, length);
                }
            } catch (Throwable e) {
                logger.warn("get config file failed!", e);
            }
        }
        resp.getOutputStream().close();

        return ;

    }

    private InputStream getConfigInputStream(String configPath) throws IOException {
        if(configPath.startsWith("file:") || configPath.startsWith("jar:")){
            return (InputStream) new URL(configPath).getContent();
        }else{
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(configPath);
        }
    }

    private void downloadLogFile(HttpServletResponse resp,String logName){

        OutputStream out = null;
        try(FileInputStream fileInputStream  = new FileInputStream(LocalLogManager.getLogFile(logName)))
        {
            out = resp.getOutputStream();
            resp.setHeader("Content-disposition","attachment;filename="+logName);
            byte[] buf=new byte[8192];
            int bytesread = 0, bytesBuffered = 0;
            while( (bytesread = fileInputStream.read( buf )) > -1 ) {
                out.write( buf, 0, bytesread );
                bytesBuffered += bytesread;
                if (bytesBuffered > 1024 * 1024) { //flush after 1MB
                    bytesBuffered = 0;
                    out.flush();
                }
            }
        } catch (Throwable e) {
            logger.warn("download log " + logName + " failed!", e);
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    logger.warn("flush log "+logName+" to client failed!",e);
                }
            }
        }
    }

}

package com.ctrip.framework.vi;

import com.ctrip.framework.vi.enterprise.EnAuthentication;
import com.ctrip.framework.vi.enterprise.EnFactory;
import com.ctrip.framework.vi.enterprise.EnHost;
import com.ctrip.framework.vi.ui.Menu;
import com.ctrip.framework.vi.util.IOUtils;
import com.ctrip.framework.vi.util.IPUtil;
import com.ctrip.framework.vi.util.SecurityUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiang.j on 2016/12/22.
 */
public class StaticContentExecutor {

    /*

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String autoJumpUrl = HttpUtil.getCookieByName(req, SecurityUtil.JUMPKEY,"/");
        RequestResult exeResult = StaticContentExecutor.getContent(req.getRequestURL().toString(), req.getServletPath().substring(1),
                req.getPathInfo(), HttpUtil.getReqParams(req), HttpUtil.getCookieByName(req, SecurityUtil.USERKEY, "/"),
                HttpUtil.getCookieByName(req, SecurityUtil.TOKENKEY, "/"), HttpUtil.getIpAddr(req), autoJumpUrl);


        if(exeResult == null){
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if(exeResult.user != null && exeResult.token != null){
            HttpUtil.addCookie(resp, SecurityUtil.USERKEY, exeResult.user, "/", -1);
            HttpUtil.addCookie(resp, SecurityUtil.TOKENKEY, exeResult.token, "/", -1);
        }

        if(exeResult.jumpUrl != null){
            resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            resp.setHeader("Location", exeResult.jumpUrl);
            if(exeResult.jumpUrl.equalsIgnoreCase(autoJumpUrl)){
                HttpUtil.addCookie(resp, SecurityUtil.JUMPKEY, "", "/", 0);
            }
        }else{

            resp.setContentType(exeResult.contentType);
            resp.setCharacterEncoding("utf-8");
            resp.setStatus(exeResult.responseCode);
            for(Map.Entry<String,String> entry:exeResult.headers.entrySet()){
               resp.addHeader(entry.getKey(),entry.getValue());
            }
            if(exeResult.content!=null) {
                resp.getOutputStream().write(exeResult.content);
            }
            resp.getOutputStream().close();
        }
    }
     */
    final static Map<String, String> EXT_TO_MEDIATYPE = new HashMap<>();
    final static String WEBPATH = "vi-web";
    static {
        EXT_TO_MEDIATYPE.put("js", "text/javascript");
        EXT_TO_MEDIATYPE.put("png", "image/png");
        EXT_TO_MEDIATYPE.put("gif", "image/gif");
        EXT_TO_MEDIATYPE.put("css", "text/css");
        EXT_TO_MEDIATYPE.put("jpg", "image/jpeg");
        EXT_TO_MEDIATYPE.put("jpeg", "image/jpeg");
        EXT_TO_MEDIATYPE.put("html", "text/html");
    }

    final static ConcurrentHashMap<String, byte[]> CONTENT_CACHE = new ConcurrentHashMap<>();

    static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    public static RequestResult getContent(String reqUrl,String rootPath,String callPath,Map<String,Object> params,String user,String token,String remoteIp,String autoJumpUrl){
        {

            RequestResult rtn = new RequestResult();
            boolean isIndexHtml = false;
            if(!IPUtil.internalIp(remoteIp)){
                return null;
            }

            String path = callPath;
            String viRootPath = rootPath;

            if(path==null) {
                rtn.jumpUrl = viRootPath + "/index.html";
                return rtn;
            }
            else if(path.equals("/health") || path.equals("/health/detail")){
                String validUser= SecurityUtil.getValidUserName(user,token,remoteIp);
                VIApiHandler handler = VIApiHandler.getInstance();
                VIApiHandler.ExeResult exeResult = handler.executeService(path, validUser, params);

                rtn.headers.put("Access-Control-Allow-Origin", "*");
                rtn.headers.put("Access-Control-Allow-Headers","Content-Type, Accept");
                rtn.headers.put("Access-Control-Expose-Headers",SecurityUtil.PERMISSIONKEY);
                rtn.contentType = "application/json";
                rtn.responseCode = exeResult.getResponseCode();
                Object resultData = exeResult.getData();
                if(resultData!=null) {
                    rtn.content = (new Gson().toJson(resultData).getBytes(Charset.forName("UTF-8")));
                }
                return rtn;

            }


            if(path.equals("/logout")) {
                rtn.needCleanCookie = true;
                rtn.jumpUrl = EnFactory.getEnAuthentication().getLogoutUrl(reqUrl);
                return rtn;

            }else if(path.equals("/index.html")){

                String envType = EnFactory.getEnBase().getEnvType();
                isIndexHtml = true;
                if(envType == null || "dev".equals(envType.toLowerCase())){

                    try {
                        rtn.user ="developer";
                        rtn.token = SecurityUtil.generateToken(rtn.user, remoteIp);
                    } catch (Throwable e) {
                        logger.error("mock dev user failed",e);
                    }
                }
                else{
                    EnAuthentication enAuthentication = EnFactory.getEnAuthentication();
                    try {
                        String nowUser = enAuthentication.authentication(user,token,remoteIp,reqUrl,params);
                        if(nowUser == null){
                            rtn.jumpUrl =  enAuthentication.getJumpUrl(reqUrl);
                            return rtn;
                        }else{
                            rtn.user = nowUser;
                            rtn.token =SecurityUtil.generateToken(rtn.user,remoteIp);
                            if(autoJumpUrl != null){
                                rtn.jumpUrl = autoJumpUrl;
                                return rtn;
                            }

                        }
                    } catch (Throwable e) {
                        rtn.jumpUrl = enAuthentication.getLogoutUrl(reqUrl);
                        logger.warn("vi authentication failed! jump to"+rtn.jumpUrl,e);
                        return rtn;
                    }
                }

            }

            String ext = path.substring(path.lastIndexOf(".")+1);
            String mediaType = EXT_TO_MEDIATYPE.get(ext);
            byte[] contentBytes = null;

            contentBytes = CONTENT_CACHE.get(path);
            if (contentBytes == null) {
                InputStream is = StaticContentExecutor.class.getClassLoader().getResourceAsStream(WEBPATH + path);
                if (is != null) {
                    try {
                        if(!isIndexHtml) {
                            ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
                            byte[] bs = new byte[4096];
                            int c = 0;
                            while ((c = is.read(bs)) > 0) {
                                os.write(bs, 0, c);
                            }
                            contentBytes = os.toByteArray();
                            CONTENT_CACHE.putIfAbsent(path, contentBytes);
                        }else{
                            String indexHtml = IOUtils.readAll(is);
                            String pattern = "//$SMENU";
                            int menuIndex =  indexHtml.indexOf(pattern);
                            if(menuIndex > 0){
                                List<Menu> menus = EnFactory.getEnUI().getMenus();
                                Gson gson = new Gson();
                                EnHost enHost = EnFactory.getEnHost();
                                indexHtml = indexHtml.substring(0,menuIndex) + "$SMENU = " + gson.toJson(menus) +
                                        ";$CIP='"+enHost.getHostAddress()+"';$CHOSTNAME='"+enHost.getHostName()+"';"
                                        + indexHtml.substring(menuIndex+pattern.length());
                            }
                            contentBytes = indexHtml.getBytes();

                        }
                    } catch (IOException e) {
                        try {
                            is.close();
                        } catch (IOException e1) {
                            logger.warn("Could not close the resource " + path, e1);
                        }
                    }
                }

            }

            if (contentBytes == null) {
                return null;
            }else {

                rtn.headers.put("Access-Control-Allow-Origin", "*");
                rtn.headers.put("Access-Control-Allow-Headers","Content-Type, Accept");
                rtn.contentType = mediaType;
                rtn.responseCode = HttpServletResponse.SC_OK;
                rtn.content = contentBytes;
            }
            return rtn;
        }
    }
}

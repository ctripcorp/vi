package com.ctrip.framework.vi;


import com.ctrip.framework.vi.component.defaultComponents.HostInfo;
import com.ctrip.framework.vi.configuration.Configuration;
import com.ctrip.framework.vi.configuration.ConfigurationManager;
import com.ctrip.framework.vi.enterprise.EnAuthentication;
import com.ctrip.framework.vi.enterprise.EnBase;
import com.ctrip.framework.vi.enterprise.EnFactory;
import com.ctrip.framework.vi.enterprise.EnHost;
import com.ctrip.framework.vi.ui.Menu;
import com.ctrip.framework.vi.util.*;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author:jiang.j
 * @date: 3/22/2016
 */
public class StaticContentServlet extends HttpServlet {

    final static Map<String, String> EXT_TO_MEDIATYPE = new HashMap<String, String>();
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

    Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        boolean isIndexHtml = false;
        if(!IPUtil.internalIp(req.getRemoteAddr())){
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String path = req.getPathInfo();

        String servletPath = req.getServletPath();
        String viRootPath = "";
        if(servletPath.length()>1) {
            viRootPath = servletPath.substring(1);
        }

        if(path == null && viRootPath.startsWith("vi/")){
            path = viRootPath.substring(2);
        }
        if(path==null) {
            resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            resp.setHeader("Location", viRootPath + "/index.html");
            return;
        }else if(path.equals("/")){
            resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            resp.setHeader("Location", "index.html");
            return;
        }
        else if(path.equals("/health") || path.equals("/health/detail")){
            String validUser= SecurityUtil.getValidUserName(req);
            VIApiHandler handler = VIApiHandler.getInstance();
            VIApiHandler.ExeResult exeResult = handler.executeService(path, validUser, HttpUtil.getReqParams(req));
            resp.addHeader("Access-Control-Allow-Origin", "*");
            resp.addHeader("Access-Control-Allow-Headers","Content-Type, Accept");
            resp.addHeader("Access-Control-Expose-Headers",SecurityUtil.PERMISSIONKEY);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("utf-8");
            resp.setStatus(exeResult.getResponseCode());
            Object resultData = exeResult.getData();
            if(resultData!=null) {
                resp.getOutputStream().write(new Gson().toJson(resultData).getBytes(Charset.forName("UTF-8")));
            }
            resp.getOutputStream().close();
            return;

        }


        if(path.equals("/logout")) {
            String reqUrl = req.getRequestURL().toString();
            HttpUtil.addCookie(resp, SecurityUtil.USERKEY, "", "/", 0);
            HttpUtil.addCookie(resp, SecurityUtil.TOKENKEY, "", "/", 0);
            resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            resp.setHeader("Location", EnFactory.getEnAuthentication().getLogoutUrl(reqUrl));
            resp.getOutputStream().close();
            return;

        }else if(path.equals("/index.html")){

            String envType = EnFactory.getEnBase().getEnvType();
            isIndexHtml = true;
            if(envType == null || "dev".equalsIgnoreCase(envType)){

                try {
                    HttpUtil.addCookie(resp, SecurityUtil.USERKEY, SecurityUtil.DEVUSER, "/", -1);
                    HttpUtil.addCookie(resp, SecurityUtil.TOKENKEY, SecurityUtil.generateToken(SecurityUtil.DEVUSER, req), "/", -1);
                } catch (Throwable e) {
                    logger.error("mock dev user failed",e);
                }
            }
            else{
                EnAuthentication enAuthentication = EnFactory.getEnAuthentication();
                try {
                    String user = enAuthentication.authentication(HttpUtil.getCookieByName(req,SecurityUtil.USERKEY,"/"),
                            HttpUtil.getCookieByName(req,SecurityUtil.TOKENKEY,"/"), HttpUtil.getIpAddr(req),
                            String.valueOf(req.getRequestURL()),HttpUtil.getReqParams(req));
                    if(user == null){
                        resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                        resp.setHeader("Location", enAuthentication.getJumpUrl(String.valueOf(req.getRequestURL())));
                        return;
                    }else{
                        HttpUtil.addCookie(resp, SecurityUtil.USERKEY, user, "/", -1);
                        HttpUtil.addCookie(resp, SecurityUtil.TOKENKEY, SecurityUtil.generateToken(user, req), "/", -1);
                        String viJump = HttpUtil.getCookieByName(req,SecurityUtil.JUMPKEY);
                        if(viJump!=null){
                            HttpUtil.addCookie(resp, SecurityUtil.JUMPKEY, "", "/", 0);
                            resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                            resp.setHeader("Location", viJump);
                            return;
                        }
                    }
                } catch (Throwable e) {
                    //resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    //resp.getOutputStream().write(("authentication failed! because "+e.getMessage()).getBytes());
                    String jumpUrl = String.valueOf(req.getRequestURL());
                    resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                    resp.setHeader("Location", enAuthentication.getLogoutUrl(jumpUrl));
                    logger.warn("vi authentication failed!",e);
                    return;
                }
            }

        }

        String ext = path.substring(path.lastIndexOf(".") + 1);
        String mediaType = EXT_TO_MEDIATYPE.get(ext);
        byte[] contentBytes = null;

        contentBytes = CONTENT_CACHE.get(path);
        if (contentBytes == null) {
            InputStream is = StaticContentServlet.class.getClassLoader().getResourceAsStream(WEBPATH + path);
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
                        String portalUrl = null;
                        try{
                            Configuration config = ConfigurationManager.getConfigInstance();
                            if(config.containsKey("vi.portal.url")) {
                                EnBase enBase = EnFactory.getEnBase();
                                String env = "others";
                                switch (enBase.getEnvType().toLowerCase()){
                                    case "pro":
                                        env = "pro";
                                        break;
                                    case "uat":
                                        env="uat";
                                        break;
                                }
                                portalUrl = config.getString("vi.portal.url")+"#/app/"+enBase.getAppId() +"?env="+env;
                            }
                        }catch (Throwable e){
                           e.printStackTrace();

                        }

                        String pattern = "//$SMENU";
                        int menuIndex =  indexHtml.indexOf(pattern);
                        if(menuIndex > 0){
                            List<Menu> menus = EnFactory.getEnUI().getMenus();
                            Gson gson = new Gson();
                            EnHost enHost = EnFactory.getEnHost();
                            indexHtml = indexHtml.substring(0,menuIndex) + "$SMENU = " + gson.toJson(menus) +
                                    ";$CIP='"+enHost.getHostAddress()+"';$CHOSTNAME='"+enHost.getHostName()+"';"+
                                    "$VERSION='"+ Version.VERSION +"';"+
                                    (portalUrl!=null?"$PORTALURL='"+portalUrl+"';":"")
                                    + "$ISLINUX="+ HostInfo.isLinux()+";$ISTOMCAT="+HostInfo.isTomcat()+";"
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
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }else {

            resp.addHeader("Access-Control-Allow-Origin", "*");
            resp.addHeader("Access-Control-Allow-Headers","Content-Type, Accept");
            resp.setContentType(mediaType);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getOutputStream().write(contentBytes);
            resp.getOutputStream().close();
        }
    }

}
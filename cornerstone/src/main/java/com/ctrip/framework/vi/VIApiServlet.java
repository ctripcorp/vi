package com.ctrip.framework.vi;

import com.ctrip.framework.vi.util.HttpUtil;
import com.ctrip.framework.vi.util.SecurityUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

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
            APIContentExecutor.logError("VI ignite failed",e);
        }
    }


        private void handlerRequest(HttpServletRequest req, HttpServletResponse resp,boolean isPost) throws ServletException, IOException {

            String remoteIp = HttpUtil.getIpAddr(req);
            String path = req.getPathInfo();
            String servletPath = req.getServletPath();
            String viRootPath = "";
            if(servletPath.length()>1) {
               viRootPath = servletPath.substring(1);
            }

            if(path == null && viRootPath.startsWith("vi/api/")){
                path = viRootPath.substring(6);
            }

            RequestResult exeResult = null;
            if(path != null) {

                if (!isPost) {
                    exeResult = APIContentExecutor.doGet(path, HttpUtil.getReqParams(req), HttpUtil.getCookieByName(req, SecurityUtil.USERKEY, "/"),
                            HttpUtil.getCookieByName(req, SecurityUtil.TOKENKEY, "/"), remoteIp);
                } else {

                    exeResult = APIContentExecutor.doPost(req.getPathInfo(), HttpUtil.loadPostParams(req), HttpUtil.getCookieByName(req, SecurityUtil.USERKEY, "/"),
                            HttpUtil.getCookieByName(req, SecurityUtil.TOKENKEY, "/"), remoteIp);
                }
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
    }


package com.ctrip.framework.cs.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ctrip.framework.cs.enterprise.EnFactory;
import com.ctrip.framework.cs.enterprise.MyX509TrustManager;

/**
 * Created by jiang.j on 2016/5/3.
 */
public final class SecurityUtil {

    public final static String USERKEY="vi-user";
    public final static String TOKENKEY="vi-token";
    public final static String PERMISSIONKEY="VI-Permission";
    public final static String JUMPKEY="vi_jump";
    public final static String DEVUSER="developer";
    private static String defaultKey = "#@$vi#com!abc";
    private static int tokenInvalidMinute = 40;
    public static void setSecurityKey(String key){
        defaultKey = key;
    }

    public static void setTokenInvalidMinute(int minute){
        tokenInvalidMinute = minute;
    }

    public static int getTokenInvalidMinute(){
        return tokenInvalidMinute;
    }

    public static String generateToken(String user,HttpServletRequest req) throws Exception {

        String data = user+"#"+HttpUtil.getIpAddr(req)+"#"+System.currentTimeMillis();
        return DesUtil.encrypt(data,defaultKey);
    }

    public static String generateToken(String user,String ip) throws Exception {

        String data = user+"#"+ ip +"#"+System.currentTimeMillis();
        return DesUtil.encrypt(data,defaultKey);
    }

    public static void refreshToken(String user,HttpServletRequest req,HttpServletResponse resp) throws Exception {
        HttpUtil.addCookie(resp, SecurityUtil.TOKENKEY, SecurityUtil.generateToken(user, req), "/", -1);
    }

    public static String getValidUserName(String user,String token,String ip){

        String envType = EnFactory.getEnBase().getEnvType();
         if(envType != null && !"dev".equalsIgnoreCase(envType) && DEVUSER.equals(user)){
             return null;
         }

        String rtn = null;
        try {
            String[] data = DesUtil.decrypt(token, defaultKey).split("#");

            if (data.length == 3) {

                long tokenDate = Long.parseLong(data[2]);

                if (data[0].equals(user) && data[1].equals(ip) && (System.currentTimeMillis() - tokenDate) < tokenInvalidMinute * 60 * 1000) {
                    rtn = user;

                }
            }
        }catch (Throwable e){
            rtn = null;
        }
        return rtn;
    }

    public static String getValidUserName(HttpServletRequest req){
        Cookie[] cookies = req.getCookies();
        if(cookies==null){
            return null;
        }
        String nowUser = "", nowToken = "";
        try {
            for (Cookie cookie : cookies) {
                String path = cookie.getPath();
                boolean isRootPath = path == null || path.equals("/");
                if (cookie.getName().equals(USERKEY) && isRootPath){
                    nowUser = cookie.getValue();
                } else if (cookie.getName().equals(TOKENKEY) && isRootPath){
                    nowToken = cookie.getValue();
                }
            }
            return getValidUserName(nowUser,nowToken,HttpUtil.getIpAddr(req));
        }catch (Throwable e){
            return null;
        }

    }


    private static SSLSocketFactory VISSLFACTORY = null;
    public static SSLSocketFactory getSSLSocketFactory () throws KeyManagementException, NoSuchProviderException, NoSuchAlgorithmException {

        if(VISSLFACTORY == null) {
            TrustManager[] tm = {new MyX509TrustManager()};
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new SecureRandom());
            VISSLFACTORY = sslContext.getSocketFactory();
        }
        return VISSLFACTORY;
    }

    public static String md5(String data) throws NoSuchAlgorithmException {

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(StandardCharsets.UTF_8.encode(data));
        return String.format("%032x", new BigInteger(1, md5.digest()));
    }
}

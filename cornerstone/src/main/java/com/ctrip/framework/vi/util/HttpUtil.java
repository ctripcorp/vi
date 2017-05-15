package com.ctrip.framework.cornerstone.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by jiang.j on 2016/11/2.
 */
public final class HttpUtil {

    public static <T> T doGet(URL url, Class<T> infoClass) throws IOException {

        T rtn = null;
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept","application/json");
        conn.setRequestProperty("Content-Type","application/json;charset=UTF-8");
        conn.connect();

        try (Reader rd = new InputStreamReader(conn.getInputStream(), "UTF-8")) {
            Gson gson = new Gson();
            rtn = gson.fromJson(rd,infoClass);
        }

        return rtn;
    }
    public static <T> T[] doGetList(URL url, Class<T[]> infoClass) throws IOException {

        T[] rtn = null;
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept","application/json");
        conn.setRequestProperty("Content-Type","application/json;charset=UTF-8");
        conn.connect();

        try (Reader rd = new InputStreamReader(conn.getInputStream(), "UTF-8")) {

           Gson gson = new Gson();
           rtn = gson.fromJson(rd,infoClass);
        }

        return rtn;
    }

    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknow".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static String getCookieByName(HttpServletRequest req,String name) {
        return getCookieByName(req,name,null);
    }
    public static String getCookieByName(HttpServletRequest req,String name,String path) {
        Cookie[] cookies = req.getCookies();
        if(cookies == null){
            return null;
        }
        for(Cookie cookie:cookies){
            String cookiePath = cookie.getPath();
            if(cookiePath==null || "".equals(cookiePath)){
                cookiePath = "/";
            }

            if(name.equals(cookie.getName()) &&
                    (path ==null || path.equals(cookiePath))){
                return cookie.getValue();
            }
        }
        return null;
    }

    public static Map<String,Object> getReqParams(HttpServletRequest req){

        Map<String,Object> params = new HashMap<>();

        Enumeration<String> names = req.getParameterNames();
        while (names.hasMoreElements()){
            String key = names.nextElement();
            params.put(key.toLowerCase(),req.getParameter(key));
        }
        params.put("req_ip",req.getRemoteAddr());
        return params;
    }

    public static Map<String, Object> loadPostParams(HttpServletRequest req){
        Map<String,Object> params =null;

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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return params;
    }

     public static void addCookie(HttpServletResponse response,
                                  String cookieName,
                                  String cookieValue,
                                  String cookiePath,
                                  int maxAgeInSeconds) {


         Cookie cookie = new Cookie(cookieName,cookieValue);
         cookie.setPath(cookiePath);
         if(maxAgeInSeconds>=0) {
             cookie.setMaxAge(maxAgeInSeconds);
         }
        response.addCookie(cookie);
    }
}

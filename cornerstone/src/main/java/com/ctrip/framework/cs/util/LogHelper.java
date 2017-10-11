package com.ctrip.framework.cs.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by jiang.j on 2017/7/6.
 */
public class LogHelper {

    public static final String SPECIALINFOPREFIX="#@#";
    public static final String HASTIME="@#@#@";
    public static String beginBlock(String title){
        return beginBlock(title,new String[]{});
    }

    public static String beginBlock(String title,Map<String,String> params) {
        StringBuilder rtn = new StringBuilder(SPECIALINFOPREFIX + "Begin " + title+"@?");

        if(params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                rtn.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            return rtn.substring(0,rtn.length()-1);
        }
        return rtn.toString();
    }
    public static String beginBlock(String title,String[] params) {
        StringBuilder rtn = new StringBuilder(SPECIALINFOPREFIX + "Begin " + title+"@?");

            for (int i=0;i<params.length;i++){
                rtn.append( params[i]).append("=").append( params[++i]).append("&");
            }
        if(params.length>=2) {
            return rtn.substring(0, rtn.length() - 1);
        }
        return rtn.toString();
    }

    public static String endBlock(String title){
        return endBlock(title,new String[]{});
    }
    public static String endBlock(String title,String[] params) {
        StringBuilder rtn = new StringBuilder(SPECIALINFOPREFIX + "End " + title+"@?");

        for (int i=0;i<params.length;i++){
            rtn.append( params[i]).append("=").append( params[++i]).append("&");
        }
        if(params.length>=2) {
            return rtn.substring(0, rtn.length() - 1);
        }
        return rtn.toString();
    }

    public static String createMessage(String msg,Date date){
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return msg+"<time>"+dateFormat.format(date)+"</time>";
    }
}

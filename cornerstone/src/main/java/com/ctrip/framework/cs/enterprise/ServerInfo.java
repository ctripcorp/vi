package com.ctrip.framework.cs.enterprise;

/**
 * Created by jiang.j on 2016/10/21.
 */
public class ServerInfo {
    private String ip;
    private String name;
    private String idc;
    private String url;

    public void setIp(String ip){
         this.ip = ip;
    }

    public void setIdc(String idc){
        this.idc = idc;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setUrl(String url){
        this.url = url;
    }
    public String getIp(){
        return ip;
    }

    public String getIdc(){
        return idc;
    }

    public String getName(){
        return name;
    }

    public String getUrl(){
        return url;
    }
}

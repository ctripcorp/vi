package com.ctrip.framework.cs.enterprise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiang.j on 2016/10/26.
 */
public class ConfigUrlContainer {

    private static Map<Integer,String> container = new ConcurrentHashMap<>();

    public static int addUrl(String url){
        int key = url.hashCode();
        if(key <0){
            key = key * -1;
        }
        container.put(key,url);
        return key;
    }

    public static String getUrl(int key){
        return container.get(key);
    }
}

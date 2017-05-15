package com.ctrip.framework.cornerstone.enterprise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiang.j on 2016/10/26.
 */
public class ConfigUrlContainer {

    private static Map<Integer,String> container = new ConcurrentHashMap<>();

    public static int addUrl(String url){
        int key = Math.abs(url.hashCode());
        container.put(key,url);
        return key;
    }

    public static String getUrl(int key){
        return container.get(key);
    }
}

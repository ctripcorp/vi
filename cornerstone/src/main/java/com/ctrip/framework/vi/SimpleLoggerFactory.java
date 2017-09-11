package com.ctrip.framework.vi;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiang.j on 2017/7/4.
 */
public class SimpleLoggerFactory {

    private static ConcurrentHashMap<String,IgniteManager.SimpleLogger> container = new ConcurrentHashMap<>();
    public static IgniteManager.SimpleLogger newSimpleLogger(String id){

        IgniteManager.SimpleLogger rtn = new IgniteManager.SimpleLogger();
        container.put(id, rtn);
        return rtn;

    }

    public static IgniteManager.SimpleLogger getSimpleLogger(String id){
        return container.get(id);
    }

    public static void removeSimpleLogger(String id){
        container.remove(id);
    }
}

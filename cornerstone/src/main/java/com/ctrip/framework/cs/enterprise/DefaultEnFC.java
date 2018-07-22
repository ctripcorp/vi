package com.ctrip.framework.cs.enterprise;

import com.ctrip.framework.cs.Permission;
import com.ctrip.framework.cs.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by jiang.j on 2016/4/29.
 */
public class DefaultEnFC implements EnFC {

    ConcurrentMap<String,Boolean> allFeatures = new ConcurrentHashMap<>();
    ConcurrentMap<String,String> allRemarks = new ConcurrentHashMap<>();
    Logger logger = LoggerFactory.getLogger(getClass());

    public DefaultEnFC(){
        Properties properties = new Properties();

        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("vi_en_fc.properties");
            if (inputStream != null) {
                properties.load(inputStream);
            }
            for(String key:properties.stringPropertyNames()){
               allFeatures.put(key,Boolean.parseBoolean(properties.getProperty(key, "false")));
            }
        }catch (Throwable e){

            logger.warn("read default fc failed!",e);
        }
    }
    @Override
    public boolean isFeatureEnable(String key) {
        if(allFeatures.containsKey(key)) {
            return allFeatures.get(key);
        }else{
            return false;
        }
    }

    @Override
    public void setFeatures(Map<String, Boolean> features, String user) {
        String nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        for(Map.Entry<String,Boolean> entry : features.entrySet()){
            allFeatures.put(entry.getKey(),entry.getValue());

            allRemarks.put(entry.getKey(), user + " " + nowDate);
        }
        logger.info("vi fc update," + user + " update " + TextUtils.join(",", features.keySet()));
    }

    @Override
    public Map<String, Boolean> allFeatures() {
        return allFeatures;
    }

    @Override
    public Map<String, String> allCustomRemarks() {
        return allRemarks;
    }

    @Override
    public int getPermission(String user) {
        return Permission.ALL.getValue();
    }
}

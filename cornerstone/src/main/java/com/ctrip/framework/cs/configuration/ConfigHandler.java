package com.ctrip.framework.cs.configuration;

import com.ctrip.framework.cs.Permission;
import com.ctrip.framework.cs.ViFunctionHandler;
import com.google.gson.JsonElement;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang.j on 2016/6/13.
 */
public class ConfigHandler implements ViFunctionHandler {
    private String startPath = "/config/";
    @Override
    public Object execute(String path, String user, int permission, Logger logger, Map<String, Object> params) throws Exception{
        Object rtn=null;
        if(path.equals(startPath+"all")){
            rtn = ConfigurationManager.getAllProperties();
        }
        else if(path.equals(startPath+"get")){
           String key = String.valueOf(params.get("key"));
            String[] keys = key.split(",");
            if(keys.length > 1){

                String[] vals = new String[keys.length];

                for (int i = 0; i < keys.length; i++) {

                    vals[i] = ConfigurationManager.getConfigInstance().getString(keys[i]);
                }
                rtn = vals;
            }else {
                rtn = ConfigurationManager.getConfigInstance().getString(key);
            }
        }
        else if(path.equals(startPath+"update")){

            Map<String,String> configs =new HashMap<>();

            StringBuilder sb = new StringBuilder();
            for(String key:params.keySet()){
                Object newVal = params.get(key);
                String valStr;
                if("req_ip".equals(key)){
                    continue;
                }

                if(newVal instanceof JsonElement) {
                    valStr = ((JsonElement)newVal).getAsString();
                }else{
                    valStr = String.valueOf(newVal);
                }

                if(ConfigurationManager.getConfigInstance().containsKey(key)) {
                    sb.append(" \n"+key + ":" + ConfigurationManager.getConfigInstance().getProperty(key)
                            + "->" + valStr );
                }
                configs.put(key, valStr);
            }
            ConfigurationManager.setProperties(configs,user);
            logger.info("vi configuration update," + user + ":"+params.get("req_ip") + ". detail: " + sb.toString());

        }
        else if(path.equals(startPath+"remarks")){

            rtn = ConfigurationManager.allCustomRemarks();
        }
        else {
            rtn = path + " not found ";
        }

        return rtn;
    }

    @Override
    public String getStartPath() {
        return startPath;
    }

    @Override
    public Permission getPermission(String user) {
        return Permission.ALL;
    }
}

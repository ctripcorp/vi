package com.ctrip.framework.cornerstone.fc;

import com.ctrip.framework.cornerstone.NoPermissionException;
import com.ctrip.framework.cornerstone.Permission;
import com.ctrip.framework.cornerstone.ViFunctionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang.j on 2016/6/13.
 */
public class FCHandler implements ViFunctionHandler {
    private String startPath = "/fc/";
    @Override
    public Object execute(String path, String user, int permission, Logger logger, Map<String, Object> params) throws NoPermissionException {
        Object rtn=null;
        if(path.equals(startPath+"all")){
            rtn = FCManager.getAllFeature();
        }
        else if(path.equals(startPath+"update")){
            if((permission & Permission.ALL.getValue())!=Permission.ALL.getValue() &&
                    (permission & Permission.EDIT.getValue()) != Permission.EDIT.getValue()){
                throw new NoPermissionException();
            }
            Map<String,Boolean> features =new HashMap<>();
            for(String key:params.keySet()){
                features.put(key, Boolean.parseBoolean(String.valueOf(params.get(key))));
            }
            FCManager.setFeatures(features,user);
        }
        return rtn;
    }

    @Override
    public String getStartPath() {
        return startPath;
    }

    @Override
    public Permission getPermission(String user) {
        return Permission.valueOf(FCManager.getPermission(user));
    }
}

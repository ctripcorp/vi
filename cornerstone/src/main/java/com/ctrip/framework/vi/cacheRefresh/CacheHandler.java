package com.ctrip.framework.cornerstone.cacheRefresh;

import com.ctrip.framework.cornerstone.Permission;
import com.ctrip.framework.cornerstone.ViFunctionHandler;
import com.ctrip.framework.cornerstone.util.TextUtils;
import com.google.gson.JsonElement;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Created by jiang.j on 2016/5/17.
 */
public class CacheHandler implements ViFunctionHandler {

    private String startPath="/cache/";
    @Override
    public Object execute(String path, String user, int permission,Logger logger, Map<String, Object> params) throws Exception{
        Object rtn=null;
        String statusPath = startPath+"status/";
        if(path.startsWith(statusPath)){
            String typeName = path.substring(statusPath.length());
            rtn = CacheManager.status(typeName);
        }
        else if(path.equals(startPath+"types")){
            rtn = CacheManager.types();
        }
        else if(path.equals(startPath+"refresh")){
            Object id = (params.get("id"));
            Object typeName = (params.get("typeName"));
            if(id instanceof JsonElement){
                id = ((JsonElement)id).getAsString();
            }
            if(typeName instanceof  JsonElement){
                typeName = ((JsonElement)typeName).getAsString();
            }
            logger.info(user + " refresh cache,cach id:"+id+", cache type:"+typeName);
            rtn = CacheManager.refreshById(String.valueOf(id),String.valueOf(typeName));
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

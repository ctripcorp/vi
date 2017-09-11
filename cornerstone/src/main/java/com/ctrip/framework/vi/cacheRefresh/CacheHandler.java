package com.ctrip.framework.vi.cacheRefresh;

import com.ctrip.framework.vi.Permission;
import com.ctrip.framework.vi.ViFunctionHandler;
import com.ctrip.framework.vi.util.TextUtils;
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
            String[] idAndType = getIdAndTypeFromParams(params);
            logger.info(user + " refresh cache,cach id:"+idAndType[0]+", cache type:"+idAndType[1]);
            rtn = CacheManager.refreshById(idAndType[0],idAndType[1]);
        }
        else if(path.equals(startPath+"getbykey")){
            Object key = (params.get("key"));
            if(key instanceof  JsonElement){
                key = ((JsonElement)key).getAsString();
            }
            String[] idAndType = getIdAndTypeFromParams(params);
            logger.info(user + " get cache content by key. cache id:"+idAndType[0]+", cache type:" +idAndType[1] +" key:"+key);
            rtn = CacheManager.findCellById(idAndType[0],idAndType[1]).getByKey((String)key);

        }
        else if(path.equals(startPath+"getbyindex")){
            Object index = (params.get("index"));
            if(index instanceof  JsonElement){
                index = ((JsonElement)index).getAsString();
            }
            String[] idAndType = getIdAndTypeFromParams(params);
            logger.info(user + " get cache content by index. cache id:"+idAndType[0]+", cache type:" +idAndType[1] +" index:"+index);
            rtn = CacheManager.findByIndex(idAndType[0],idAndType[1],Integer.parseInt((String)index));

        }
        return rtn;
    }

    private String[] getIdAndTypeFromParams(Map<String,Object> params){
        Object id = (params.get("id"));
        Object typeName = (params.get("typeName"));
        if(id instanceof JsonElement){
            id = ((JsonElement)id).getAsString();
        }
        if(typeName instanceof  JsonElement){
            typeName = ((JsonElement)typeName).getAsString();
        }
        return new String[]{(String)id,(String)typeName};
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

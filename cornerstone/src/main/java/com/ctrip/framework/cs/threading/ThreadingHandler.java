package com.ctrip.framework.cs.threading;

import com.ctrip.framework.cs.Permission;
import com.ctrip.framework.cs.ViFunctionHandler;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Created by jiang.j on 2016/6/13.
 */
public class ThreadingHandler implements ViFunctionHandler {
    private  String startPath ="/threading/";
    @Override
    public Object execute(String path, String user, int permission, Logger logger, Map<String, Object> params) throws Exception{
        Object rtn=null;
        int maxDepth = 3;
        String key="maxdepth";

        if(params!=null && params.containsKey(key)){
            try {
                maxDepth = Integer.parseInt( String.valueOf(params.get(key)));
            }catch (Throwable e){
                logger.warn("cast maxDepth failed, maxDepth "+params.get(key)+" is not number.",e);
            }
        }
        if(path.equals(startPath+"all")){
            rtn = ThreadingManager.getAllThreadInfo();
        }
        else if(path.equals(startPath+"stats")){
            rtn = ThreadingManager.getThreadStats();
        }
        else if(path.equals(startPath+"dump")){
            String key1 = "onlydeadlock";
            Boolean onlyDeadLock =false;
            if(params!=null && params.containsKey(key1)){
                try {
                    onlyDeadLock = Boolean.parseBoolean( String.valueOf(params.get(key1)));
                }catch (Throwable e){
                    logger.warn("cast maxDepth failed, onlydeadlock "+params.get(key1)+" is not bool.",e);
                }
            }
            rtn = ThreadingManager.dump(maxDepth,onlyDeadLock);
        }
        else if(path.startsWith(startPath+"detail/")){
            rtn = ThreadingManager.getThreadInfo(Long.parseLong(path.substring(18)),maxDepth);
        }
        else{
            rtn = path + " not found";
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

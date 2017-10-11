package com.ctrip.framework.cs.localLog;

import com.ctrip.framework.cs.ViFunctionHandler;
import com.ctrip.framework.cs.Permission;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Created by jiang.j on 2016/6/13.
 */
public class LogHandler implements ViFunctionHandler {
    private String startPath="/log/";
    @Override
    public Object execute(String path, String user, int permission, Logger logger, Map<String, Object> params) {

        Object rtn=null;
        if(path.equals(startPath+"all")){
            rtn = LocalLogManager.getLogList();
        }else{
            String fileName = path.substring(5);
            try {
               String encoding = "";
                int partitionIndex = -1;
                if(params.containsKey("encoding")){
                    encoding = params.get("encoding").toString();
                }

                if(params.containsKey("partitionindex")){

                    try {
                        partitionIndex = Integer.parseInt(String.valueOf(params.get("partitionindex")));
                    }catch(Exception e) {

                    }
                }
                rtn = LocalLogManager.getLogConent(fileName,partitionIndex,encoding);
            } catch (Throwable e) {
                e.printStackTrace();
                logger.warn("read local log failed!",e);
            }

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

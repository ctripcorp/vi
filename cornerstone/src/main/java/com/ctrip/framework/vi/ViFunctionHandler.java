package com.ctrip.framework.vi;

import org.slf4j.Logger;

import java.util.Map;

/**
 * Created by jiang.j on 2016/5/17.
 */
public interface ViFunctionHandler {
    Object execute(String path,String user,int permission,Logger logger, Map<String, Object> params) throws Exception;
    String getStartPath();
    Permission getPermission(String user);
}

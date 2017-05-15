package com.ctrip.framework.cornerstone;

import com.ctrip.framework.cornerstone.component.ComponentManager;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Created by jiang.j on 2016/8/29.
 */
public class AppHandler implements ViFunctionHandler {
    private  String startPath ="/app/";
    @Override
    public Object execute(String path, String user, int permission, Logger logger, Map<String, Object> params) throws Exception {
        Object rtn = null;
        AppInfo appInfo = ComponentManager.getStatus(AppInfo.class);
        if(!(user!=null && user.length()>2 && user.equals(appInfo.getAppOwner()))) {
            throw new NoPermissionException();
        }
        if(path.equals(startPath+"markdown")){
            OwnerJudge.getInstance().toAbnormal();
        }else if(path.equals(startPath+"markup")){
            OwnerJudge.getInstance().toNormal();
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

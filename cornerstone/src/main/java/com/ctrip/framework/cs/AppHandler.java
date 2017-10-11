package com.ctrip.framework.cs;

import com.ctrip.framework.cs.component.ComponentManager;
import com.ctrip.framework.cs.util.HttpUtil;
import com.ctrip.framework.cs.watcher.EventLogger;
import com.ctrip.framework.cs.watcher.EventLoggerFactory;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Created by jiang.j on 2016/8/29.
 */
public class AppHandler implements ViFunctionHandler {
    private  String startPath ="/app/";
    private transient EventLogger eventLogger = EventLoggerFactory.getLogger(getClass(),"vi.uv");

    @Override
    public Object execute(String path, String user, int permission, Logger logger, Map<String, Object> params) throws Exception {
        Object rtn = "";
        AppInfo appInfo = ComponentManager.getStatus(AppInfo.class);

        String opPath = path.substring(startPath.length()).toLowerCase();
        if(!(user!=null && user.length()>2 && user.equals(appInfo.getAppOwner())) && !"uvtrace".equals(opPath)) {
            throw new NoPermissionException();
        }
        switch (opPath){
            case "markdown":
                if(!appInfo.isStatusSourceEnabled()){
                    throw new Exception("can't markdwon. because statusSources be disabled!");
                }
                OwnerJudge.getInstance().toAbnormal();
                logger.warn(user + " markdown the app.");
                break;
            case "markup":
                if(!appInfo.isStatusSourceEnabled()){
                    throw new Exception("can't markup. because statusSources be disabled!");
                }
                OwnerJudge.getInstance().toNormal();
                logger.warn(user + " markup the app.");
                break;
            case "enablestatussource":
                appInfo.enableStatusSource();
                logger.warn(user + " enable statusSources.");
                break;
            case "disablestatussource":
                appInfo.disableStatusSource();
                logger.warn(user + " disable statusSources.");
                break;
            case "uvtrace":
                eventLogger.fireEvent(HttpUtil.getJsonParamVal(params.get("name")));
                break;
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

package com.ctrip.framework.cs.helloworld.plugins;

import com.ctrip.framework.cs.AppInfo;

import com.ctrip.framework.cs.AppStatus;
import com.ctrip.framework.cs.IgniteManager;
import com.ctrip.framework.cs.annotation.Ignite;
import com.ctrip.framework.cs.component.ComponentManager;
import com.ctrip.framework.cs.ignite.AbstractIgnitePlugin;
import com.ctrip.framework.cs.ignite.IgnitePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang.j on 2016/8/22.
 */
@Ignite(id = "redis.ignite",type = Ignite.PluginType.Component)
public class RedisIgnite extends AbstractIgnitePlugin implements IgnitePlugin,AppInfo.StatusChangeListener{
    Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String helpUrl() {
        return "http://wiki/redis";
    }

    @Override
    public boolean warmUP(IgniteManager.SimpleLogger logger) {
        logger.warn("no max memory setting");
        AppInfo appInfo = ComponentManager.getStatus(AppInfo.class);
        appInfo.addStatusChangeListener(this);
        return true;
    }

    @Override
    public Map<String, String> coreConfigs() {
        Map<String,String> rtn = new HashMap<>();
        rtn.put("maxKey","100");
        rtn.put("timeout","3000");
        return rtn;
    }

    @Override
    public boolean selfCheck(IgniteManager.SimpleLogger logger) {
       logger.info("Redis server is OK");

        return true;
    }


    @Override
    public void statusChanged(AppStatus oldStatus, AppStatus newStatus) {
        logger.info("Status changed! from "+oldStatus+" to "+newStatus);
    }
}

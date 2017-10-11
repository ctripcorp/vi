package com.ctrip.framework.cs.helloworld.plugins;

import com.ctrip.framework.cs.AppInfo;

import com.ctrip.framework.cs.AppStatus;
import com.ctrip.framework.cs.IgniteManager;
import com.ctrip.framework.cs.annotation.Ignite;
import com.ctrip.framework.cs.component.ComponentManager;
import com.ctrip.framework.cs.ignite.IgnitePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jiang.j on 2016/8/22.
 */
@Ignite(id = "redis.ignite",type = Ignite.PluginType.Component)
public class RedisIgnite implements IgnitePlugin,AppInfo.StatusChangeListener{
    Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public boolean run(IgniteManager.SimpleLogger logger) {
        logger.warn("no max memory setting");
        AppInfo appInfo = ComponentManager.getStatus(AppInfo.class);
        appInfo.addStatusChangeListener(this);
        return true;
    }

    @Override
    public void statusChanged(AppStatus oldStatus, AppStatus newStatus) {
        logger.info("Status changed! from "+oldStatus+" to "+newStatus);
    }
}

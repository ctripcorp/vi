package com.ctrip.framework.cs.helloworld.plugins;

import com.ctrip.framework.cs.IgniteManager;
import com.ctrip.framework.cs.annotation.Ignite;
import com.ctrip.framework.cs.ignite.IgnitePlugin;

/**
 * Created by jiang.j on 2016/8/22.
 */
@Ignite(id = "hermes.ignite",before ="redis.ignite")
public class HermesIgnite implements IgnitePlugin{
    @Override
    public boolean run(IgniteManager.SimpleLogger logger) {
        logger.warn("这是一个警告！！");
        return true;
    }
}

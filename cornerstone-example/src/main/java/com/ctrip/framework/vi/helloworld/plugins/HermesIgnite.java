package com.ctrip.framework.vi.helloworld.plugins;

import com.ctrip.framework.vi.IgniteManager;
import com.ctrip.framework.vi.annotation.Ignite;
import com.ctrip.framework.vi.ignite.IgnitePlugin;

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

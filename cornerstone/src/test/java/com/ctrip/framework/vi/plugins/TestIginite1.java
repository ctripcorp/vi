package com.ctrip.framework.vi.plugins;

import com.ctrip.framework.vi.IgniteManager;
import com.ctrip.framework.vi.annotation.Ignite;
import com.ctrip.framework.vi.ignite.AbstractIgnitePlugin;
import com.ctrip.framework.vi.ignite.IgnitePlugin;

import java.util.Map;

/**
 * Created by jiang.j on 2016/8/22.
 */
@Ignite(id = "vi.ignite1")
public class TestIginite1 extends AbstractIgnitePlugin{
    @Override
    public String helpUrl() {

        return "http://inner.com";
    }

    @Override
    public boolean warmUP(IgniteManager.SimpleLogger logger) {
        logger.info("warm up");
        return true;
    }

    @Override
    public Map<String, String> coreConfigs() {
        return null;
    }

    @Override
    public boolean selfCheck(IgniteManager.SimpleLogger logger) {
        logger.info("do some check!");
        logger.info("do more check!");
        logger.info("check network");
        logger.info("check function");
        logger.info("check config");
        return true;
    }

}

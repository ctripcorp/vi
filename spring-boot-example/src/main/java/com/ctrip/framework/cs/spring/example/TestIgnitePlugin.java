package com.ctrip.framework.cs.spring.example;

import com.ctrip.framework.cs.IgniteManager;
import com.ctrip.framework.cs.annotation.Ignite;
import com.ctrip.framework.cs.ignite.IgnitePlugin;

/**
 * Created by jiang.j on 2017/9/28.
 */
@Ignite(id="cornerstone.spring.test",auto = true)
public class TestIgnitePlugin implements IgnitePlugin{
    @Override
    public boolean run(IgniteManager.SimpleLogger logger) {

	logger.info("only for test");
        return true;
    }
}

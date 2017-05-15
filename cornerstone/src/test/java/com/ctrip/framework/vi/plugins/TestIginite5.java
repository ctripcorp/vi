package com.ctrip.framework.cornerstone.plugins;

import com.ctrip.framework.cornerstone.IgniteManager;
import com.ctrip.framework.cornerstone.annotation.Ignite;
import com.ctrip.framework.cornerstone.ignite.IgnitePlugin;

/**
 * Created by jiang.j on 2016/8/22.
 */
@Ignite(id = "vi.ignite5")
public class TestIginite5 implements IgnitePlugin{
    @Override
    public boolean run(IgniteManager.SimpleLogger logger) {
        return false;
    }
}

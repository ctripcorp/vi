package com.ctrip.framework.cs.plugins;

import com.ctrip.framework.cs.IgniteManager;
import com.ctrip.framework.cs.annotation.Ignite;
import com.ctrip.framework.cs.ignite.IgnitePlugin;

/**
 * Created by jiang.j on 2016/8/22.
 */
@Ignite(id = "vi.ignite3",before ="vi.ignite2")
public class TestIginite3 implements IgnitePlugin{
    @Override
    public boolean run(IgniteManager.SimpleLogger logger) {
        return true;
    }
}

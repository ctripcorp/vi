package com.ctrip.framework.cs.ignite;

import com.ctrip.framework.cs.IgniteManager;

/**
 * Created by jiang.j on 2016/8/11.
 */
public interface IgnitePlugin {
    boolean run(IgniteManager.SimpleLogger logger);
}

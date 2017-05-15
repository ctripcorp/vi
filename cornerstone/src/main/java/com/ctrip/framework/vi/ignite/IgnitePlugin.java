package com.ctrip.framework.cornerstone.ignite;

import com.ctrip.framework.cornerstone.IgniteManager;

/**
 * Created by jiang.j on 2016/8/11.
 */
public interface IgnitePlugin {
    boolean run(IgniteManager.SimpleLogger logger);
}

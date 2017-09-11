package com.ctrip.framework.vi.ignite;

import com.ctrip.framework.vi.IgniteManager;

/**
 * Created by jiang.j on 2016/8/11.
 */
public interface IgnitePlugin {
    boolean run(IgniteManager.SimpleLogger logger);
}

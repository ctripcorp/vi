package com.ctrip.framework.vi.ui;

/**
 * Created by jiang.j on 2016/11/1.
 */
public class BuildInMetrics implements BuildInPage {
    @Override
    public String getId() {
        return "Dashboard";
    }

    @Override
    public String getName() {
        return "Metrics";
    }

    @Override
    public String getIcon() {
        return "fa-tachometer";
    }
}

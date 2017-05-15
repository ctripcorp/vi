package com.ctrip.framework.cornerstone.ui;

/**
 * Created by jiang.j on 2016/11/1.
 */
public class BuildInCache implements BuildInPage {
    @Override
    public String getId() {
        return "Cache";
    }

    @Override
    public String getName() {
        return "Cache Refresh";
    }

    @Override
    public String getIcon() {
        return "fa-database";
    }
}

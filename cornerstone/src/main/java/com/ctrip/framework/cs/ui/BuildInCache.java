package com.ctrip.framework.cs.ui;

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
        return "Cache Manager";
    }

    @Override
    public String getIcon() {
        return "fa-database";
    }
}

package com.ctrip.framework.cornerstone.ui;

/**
 * Created by jiang.j on 2016/11/1.
 */
public class BuildInFC implements BuildInPage {
    @Override
    public String getId() {
        return "FC";
    }

    @Override
    public String getName() {
        return "FC";
    }

    @Override
    public String getIcon() {
        return "fa-toggle-on";
    }
}

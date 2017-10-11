package com.ctrip.framework.cs.ui;

/**
 * Created by jiang.j on 2017/3/17.
 */
public class BuildInDebug implements BuildInPage {
    @Override
    public String getId() {
        return "Code";
    }

    @Override
    public String getName() {
        return "Debug";
    }

    @Override
    public String getIcon() {
        return "fa-bug";
    }
}

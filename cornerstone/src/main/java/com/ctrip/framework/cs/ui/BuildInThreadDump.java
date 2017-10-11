package com.ctrip.framework.cs.ui;

/**
 * Created by jiang.j on 2016/11/1.
 */
public class BuildInThreadDump implements BuildInPage {
    @Override
    public String getId() {
        return "ThreadDump";
    }

    @Override
    public String getName() {
        return "Thread dump";
    }

    @Override
    public String getIcon() {
        return "fa-camera";
    }
}

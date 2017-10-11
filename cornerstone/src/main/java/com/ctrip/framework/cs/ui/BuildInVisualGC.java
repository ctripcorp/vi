package com.ctrip.framework.cs.ui;

/**
 * Created by jiang.j on 2017/3/6.
 */
public class BuildInVisualGC implements  BuildInPage {
    @Override
    public String getId() {

        return "Analyzer-VisualGC";
    }

    @Override
    public String getName() {
        return "VisualGC";
    }

    @Override
    public String getIcon() {
        return null;
    }
}

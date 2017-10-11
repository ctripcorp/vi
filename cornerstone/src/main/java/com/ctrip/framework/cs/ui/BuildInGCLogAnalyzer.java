package com.ctrip.framework.cs.ui;

/**
 * Created by jiang.j on 2017/3/6.
 */
public class BuildInGCLogAnalyzer implements BuildInPage {
    @Override
    public String getId() {
        return "Analyzer-GCLogAnalyzer";
    }

    @Override
    public String getName() {
        return "GCLogAnalyzer";
    }

    @Override
    public String getIcon() {
        return null;
    }
}

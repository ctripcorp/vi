package com.ctrip.framework.cornerstone.ui;

/**
 * Created by jiang.j on 2016/11/1.
 */
public class BuildInJarDep implements BuildInPage {
    @Override
    public String getId() {
        return "Analyzer-Jardependency";
    }

    @Override
    public String getName() {
        return "Jar dependency";
    }

    @Override
    public String getIcon() {
        return null;
    }
}

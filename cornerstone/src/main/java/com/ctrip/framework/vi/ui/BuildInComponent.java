package com.ctrip.framework.cornerstone.ui;

/**
 * Created by jiang.j on 2016/11/1.
 */
public class BuildInComponent implements BuildInPage{
    @Override
    public String getId() {
        return "Component";
    }

    @Override
    public String getName() {
        return "Component";
    }

    @Override
    public String getIcon() {
        return "fa-codepen";
    }
}

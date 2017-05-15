package com.ctrip.framework.cornerstone.ui;

/**
 * Created by jiang.j on 2016/11/1.
 */
public class BuildInFileLog implements BuildInPage
{
    @Override
    public String getId() {
        return "Log";
    }

    @Override
    public String getName() {
        return "File log";
    }

    @Override
    public String getIcon() {
        return "fa-file";
    }
}

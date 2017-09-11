package com.ctrip.framework.vi.ui;

/**
 * Created by jiang.j on 2016/11/1.
 */
public class BuildInConfiguration implements BuildInPage
{
    @Override
    public String getId() {
        return "Configuration";
    }

    @Override
    public String getName() {
        return "Configuration";
    }

    @Override
    public String getIcon() {
        return "fa-cogs";
    }
}

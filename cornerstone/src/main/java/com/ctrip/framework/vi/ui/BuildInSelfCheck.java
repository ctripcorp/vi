package com.ctrip.framework.vi.ui;

/**
 * Created by jiang.j on 2017/7/7.
 */
public class BuildInSelfCheck implements BuildInPage {

    @Override
    public String getId() {

        return "Analyzer-SelfCheck";
    }

    @Override
    public String getName() {
        return "Self Check";
    }

    @Override
    public String getIcon() {
        return null;
    }
}

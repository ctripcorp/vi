package com.ctrip.framework.cs.ui;

/**
 * Created by jiang.j on 2016/11/1.
 */
public class BuildInJvmSampler implements BuildInPage {
    @Override
    public String getId() {
        return "Analyzer-JVMSampler";
    }

    @Override
    public String getName() {
        return "JVM Sampler";
    }

    @Override
    public String getIcon() {
        return null;
    }
}

package com.ctrip.framework.cs.ignite;

/**
 * Created by jiang.j on 2017/1/3.
 */
public class IgnitePluginNoIgniteAnnotationException extends Exception {

    public IgnitePluginNoIgniteAnnotationException(String className){
        super("can't found ignite plugin class:"+className + "'s ignite annotation.");
    }
}

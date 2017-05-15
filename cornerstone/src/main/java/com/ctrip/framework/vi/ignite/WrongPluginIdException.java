package com.ctrip.framework.cornerstone.ignite;

/**
 * Created by jiang.j on 2016/8/23.
 */
public class WrongPluginIdException extends Exception {
    public WrongPluginIdException(String className,String id){
        super(String.format("Ignite Plugin %s ref Id:%s don't exist!",className,id));
    }
}

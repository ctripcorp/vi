package com.ctrip.framework.cornerstone.enterprise;

/**
 * Created by jiang.j on 2016/12/16.
 */
public class NoAppIdException extends Exception {

    public NoAppIdException(){
        super("can't get appId, make sure appId is configured correctly in your app");
    }
}

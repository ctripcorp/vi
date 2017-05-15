package com.ctrip.framework.cornerstone;

/**
 * Created by jiang.j on 2016/7/28.
 */
public class NoPermissionException extends Exception {

    public NoPermissionException(){
        super("You do not have permission for this operation!");
    }
}

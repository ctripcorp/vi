package com.ctrip.framework.cs;

/**
 * Created by jiang.j on 2017/7/4.
 */
public class NotFoundException extends Exception {
    public NotFoundException(){
    }
    public NotFoundException(String msg){
        super(msg);
    }
}

package com.ctrip.framework.vi.enterprise;

/**
 * Created by jiang.j on 2016/10/20.
 */
public class AuthenticationFailedException extends Exception {

    public AuthenticationFailedException(Throwable e){
        super(e);
    }
}

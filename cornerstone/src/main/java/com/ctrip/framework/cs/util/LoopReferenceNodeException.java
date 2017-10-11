package com.ctrip.framework.cs.util;


import java.util.Arrays;

/**
 * Created by jiang.j on 2016/8/24.
 */
public class LoopReferenceNodeException extends Exception {

    String[] _ids;
    public LoopReferenceNodeException(String[] ids){
        super("loop reference be found in [" + TextUtils.join(",", Arrays.asList(ids)) + "]");
        _ids = ids;
    }

    public String[] ids(){
        return _ids;
    }
}

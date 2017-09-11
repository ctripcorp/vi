package com.ctrip.framework.vi.ignite;

/**
 * Created by jiang.j on 2016/8/23.
 */
public class DuplicatePluginIdException extends  Exception {
    public DuplicatePluginIdException(String message){
        super(message);
    }
}

package com.ctrip.framework.vi.watcher;

/**
 * Created by jiang.j on 2016/10/17.
 */
public interface EventLogger {

    public static final String TRANSSTART = "trans-start";
    public static final String TRANSEND = "trans-end";
    public static final String TRANSFINALLY = "trans-finally";
    public static final String ERROR = "error";
    public void fireEvent(String message,Object... args);
}

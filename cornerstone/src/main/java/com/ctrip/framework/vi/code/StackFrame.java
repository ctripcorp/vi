package com.ctrip.framework.vi.code;

import java.util.Map;

/**
 * Created by jiang.j on 2017/3/16.
 */
public interface StackFrame {

    Map<String, Object> getLocals() ;
    Map<String, Object> getFields();
    Map<String, Object> getStaticFields();
    StackTraceElement[] getStacktrace() ;

}

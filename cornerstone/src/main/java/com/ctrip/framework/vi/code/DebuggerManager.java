package com.ctrip.framework.vi.code;

import com.ctrip.framework.vi.code.debug.DefaultDebugger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

/**
 * Created by jiang.j on 2017/3/16.
 */
public final class DebuggerManager {

    static Debugger debugger;
    private static boolean isDefault = true;
    private static Logger logger = LoggerFactory.getLogger(DebuggerManager.class);
    static {
        try {
            debugger = new DefaultDebugger();
        }catch (Throwable e){
            logger.error("init error",e);
        }

    }

    public static boolean isDefaultDebugger(){

        return isDefault;
    }
    public static void setDebugger(Debugger newDebugger){
        if(newDebugger!= null && !(newDebugger instanceof  DefaultDebugger)) {
            debugger = newDebugger;
            isDefault = false;
        }
    }
    public static Debugger getCurrent(){
        return debugger;
    }
}

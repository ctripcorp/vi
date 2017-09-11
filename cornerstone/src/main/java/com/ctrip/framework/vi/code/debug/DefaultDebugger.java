package com.ctrip.framework.vi.code.debug;

import com.ctrip.framework.vi.NotFoundException;
import com.ctrip.framework.vi.asm.Opcodes;
import com.ctrip.framework.vi.code.Debugger;
import com.ctrip.framework.vi.code.StackFrame;
import com.ctrip.framework.vi.instrument.AgentTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Created by jiang.j on 2017/7/12.
 */
public class DefaultDebugger implements Debugger {
    Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public boolean startup() {
        return true;
    }

    @Override
    public boolean registerBreakpoint(String source, int line, String breakpointId) {
        return registerBreakpoint(source,line,breakpointId,null,null);
    }

    @Override
    public boolean registerBreakpoint(String source, int line, String breakpointId, String condition) {
        return false;
    }
    public boolean registerBreakpoint(String source, int line, String breakpointId,Condition[] conditions,String owner) {
        try {
            String className = source;
            if(source.endsWith(".java")){
                className = source.substring(0,source.length()-5);
            }
            AgentTool.addDebugClass(className, new DebugInfo(line,breakpointId,Condition.checkAndCorrect(conditions),owner));

            return true;
        }catch (Throwable e){

            logger.error("register breakpoint failed! source:"+source+", line:"+line,e);
            return false;
        }
    }

    @Override
    public boolean triggerBreakpoint(String breakpointId) {
        DebugTool.setThreadTraceId(breakpointId);
        return true;
    }

    @Override
    public StackFrame getCapturedFrame(String breakpointId) throws NotFoundException {
        final Map<String,Object> traceInfos = AgentTool.removeDebugClassByTraceId(breakpointId,false);
        if(traceInfos == null){
            return null;
        }
        StackFrame stackFrame = new StackFrame() {
            @Override
            public Map<String, Object> getLocals() {
                Map<String,Object> rtn = new HashMap<>();
                for(Map.Entry<String,Object> entry:traceInfos.entrySet()){
                    String key = entry.getKey();
                    if(key.startsWith("var.")){
                        rtn.put(key.substring(4),entry.getValue());
                    }
                }
                return rtn;
            }

            @Override
            public Map<String, Object> getFields() {
                Map<String,Object> rtn = new HashMap<>();
                for(Map.Entry<String,Object> entry:traceInfos.entrySet()){
                    String key = entry.getKey();
                    if(key.startsWith("field.")){
                        rtn.put(key.substring(4),entry.getValue());
                    }
                }
                return rtn;
            }

            @Override
            public Map<String, Object> getStaticFields() {
                Map<String,Object> rtn = new HashMap<>();
                for(Map.Entry<String,Object> entry:traceInfos.entrySet()){
                    String key = entry.getKey();
                    if(key.startsWith("static_field.")){
                        rtn.put(key.substring(4),entry.getValue());
                    }
                }
                return rtn;
            }

            @Override
            public StackTraceElement[] getStacktrace() {
                return (StackTraceElement[]) traceInfos.get(DebugTool.STACKKEY);
            }
        };

        return stackFrame;
    }

    @Override
    public void stopTrace(String breakpointId) throws NotFoundException {
        AgentTool.removeDebugClassByTraceId(breakpointId,true);
    }
}

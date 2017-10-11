package com.ctrip.framework.cs.instrument;

import com.ctrip.framework.cs.annotation.ComponentStatus;
import com.ctrip.framework.cs.code.debug.DebugTool;
import com.ctrip.framework.cs.NotFoundException;
import com.ctrip.framework.cs.code.debug.DebugInfo;

import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang.j on 2017/8/3.
 */
@ComponentStatus(id="vi.agentstatus",name="agent status",description = "vi java agent 状态",custom = true)
public class AgentStatus {
    private boolean isLoaded;
    private Map<String,String> modifiedClassInfos = new HashMap<>();
    public AgentStatus(){
        this.isLoaded = AgentTool.agentIsLoaded();

        String[] debugClasses = AgentTool.getNeedDebugClasses();
        for(String name:debugClasses){
            modifiedClassInfos.put(name,"debug");
        }

        String[] metricsClasses = AgentTool.getNeedMetricsClasses();
        for(String name:metricsClasses){
            modifiedClassInfos.put(name, "metrics");
        }

    }

    public class ViewASMCodeReq{
       public String className;
    }
    public static String viewModifiedASMCode(ViewASMCodeReq req) throws Exception {

        return AgentTool.getModifiedClassASMCode(req.className);

    }

    public static String viewOriginASMCode(ViewASMCodeReq req) throws Exception {

        return AgentTool.getOriginClassASMCode(req.className);

    }

    public static Map<String,Object> viewDetail(ViewASMCodeReq req){
        Map<String,Object> rtn = new HashMap<>();
        DebugInfo debugInfo = AgentTool.getDebugInfoByClassName(req.className);
        if(debugInfo != null){
            rtn.put("debugInfo",debugInfo);
            rtn.put("traceInfo", DebugTool.viewCurrentTrace(debugInfo.getTraceId()));
        }
        return rtn;
    }

    public static void restoreClass(ViewASMCodeReq req) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, UnmodifiableClassException, IllegalAccessException, NotFoundException {
        DebugInfo debugInfo = AgentTool.getDebugInfoByClassName(req.className);
        if(debugInfo != null){
            AgentTool.removeDebugClassByTraceId(debugInfo.getTraceId(),true);

        }else {
            AgentTool.removeMetricsClass(req.className);
        }
    }


}

package com.ctrip.framework.cs.code;

import com.ctrip.framework.cs.NoPermissionException;
import com.ctrip.framework.cs.ViFunctionHandler;
import com.ctrip.framework.cs.code.debug.DebugTool;
import com.ctrip.framework.cs.configuration.ConfigurationManager;
import com.ctrip.framework.cs.NotFoundException;
import com.ctrip.framework.cs.Permission;
import com.ctrip.framework.cs.code.debug.Condition;
import com.ctrip.framework.cs.code.debug.DefaultDebugger;
import com.ctrip.framework.cs.enterprise.EnApp;
import com.ctrip.framework.cs.enterprise.EnFactory;
import com.ctrip.framework.cs.instrument.AgentTool;
import com.ctrip.framework.cs.util.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang.j on 2017/3/15.
 */
public class CodeHandler implements ViFunctionHandler {
    private String startPath = "/code/";

    private Object getCaptureFrames(Map<String, Object> params) throws NotFoundException {

        Object rtn =null;
        String traceId = (String) params.get("traceid");
        if(traceId == null) {
            rtn ="wrong trace id";
            return rtn;
        }
        StackFrame stackFrame = DebuggerManager.getCurrent().getCapturedFrame(traceId);
        if(stackFrame == null){
            return null;
        }
        Map<String,Object> data = new HashMap<>();
        Map<String,Object> staticFields = new HashMap<>();
        Map<String,Object> fields = new HashMap<>();
        Map<String,Object> locals = new HashMap<>();

        if(stackFrame.getStaticFields()!=null) {
            staticFields = stackFrame.getStaticFields();
        }
        data.put("staticFields",staticFields);
        if(stackFrame.getFields()!=null) {
            fields = stackFrame.getFields();
        }
        data.put("fields",fields);

        if(stackFrame.getLocals()!=null) {
            locals = stackFrame.getLocals();
        }
        data.put("locals",locals);
        data.put("stackTrace",stackFrame.getStacktrace());
        rtn = data;
        return rtn;
    }

    private Object registerBreakpoint(Map<String, Object> params,String user) throws Exception {

        Object rtn = null;
        Debugger debugger = DebuggerManager.getCurrent();

        boolean hasConditionsKey = params.containsKey("conditions");
        boolean isConditionBP = debugger instanceof DefaultDebugger && hasConditionsKey;
        if(DebuggerManager.getCurrent().startup()) {
            if(isConditionBP){
                Gson gson = new Gson();
                JsonObject rawObj =  gson.fromJson(HttpUtil.getJsonParamVal(params.get("conditions")), JsonObject.class);
                Condition[] conditionList =  gson.fromJson(rawObj.getAsJsonArray("d"),Condition[].class);
                DefaultDebugger defaultDebugger = (DefaultDebugger) debugger;
                rtn = defaultDebugger.registerBreakpoint(HttpUtil.getJsonParamVal(params.get("source")), Integer.parseInt(HttpUtil.getJsonParamVal(params.get("line"))),HttpUtil.getJsonParamVal(params.get("breakpointid")),conditionList,user);

            }else {
                if(!hasConditionsKey) {
                    rtn = debugger.registerBreakpoint(HttpUtil.getJsonParamVal(params.get("source")), Integer.parseInt(HttpUtil.getJsonParamVal(params.get("line"))),
                            HttpUtil.getJsonParamVal(params.get("breakpointid")));
                }else{
                    rtn = debugger.registerBreakpoint(HttpUtil.getJsonParamVal(params.get("source")), Integer.parseInt(HttpUtil.getJsonParamVal(params.get("line"))),
                            HttpUtil.getJsonParamVal(params.get("breakpointid")),HttpUtil.getJsonParamVal(params.get("conditions")));
                }
            }
        }else{
            throw new Exception("Debugger start failed! Cannot debug!");
        }
        return rtn;
    }

    @Override
    public Object execute(String path, String user, int permission, Logger logger, Map<String, Object> params) throws Exception {
        Object rtn=null;
        String opPath = path.substring(startPath.length()).toLowerCase();

        switch (opPath){
            case "gitinfo":
                Map<String,String> gitInfo = new HashMap<>();
                gitInfo.put("url", ConfigurationManager.getConfigInstance().getString("vi.git.api.url"));
                EnApp currentApp = EnFactory.getEnApp();
                gitInfo.put("commitId",currentApp.getGitCommitId());
                gitInfo.put("prjPath",currentApp.getGitPrjPath());

                rtn = gitInfo;
                break;

            case "registerbreakpoint":
                if(user == null){
                    logger.warn("NoPermissionException "+ path+" "+user);
                    throw  new NoPermissionException();
                }
                rtn = registerBreakpoint(params,user);
                break;
            case "enabledebug":
                rtn = DebuggerManager.getCurrent().startup();
                break;

            case "getcapturedframe":
                rtn = getCaptureFrames(params);
                break;

            case "getsourcecode":
                rtn = SourceCodeHelper.getJarSourceCode((String) params.get("jarname"), (String) params.get("path"));
                break;
            case "getcodepath":
                String ns = (String) params.get("ns");
                String fileName = (String)params.get("name");
                if(ns == null) {
                    rtn ="wrong namespace";
                    return rtn;
                }
                rtn = SourceCodeHelper.getCodePath(ns,fileName);
                break;
            case "gettracekey":
                rtn = DebugTool.getTraceIdKey();
                break;
            case "settracekey":
                DebugTool.setTraceIdKey(HttpUtil.getJsonParamVal(params.get("key")));
                break;
            case "getlinevars":
                String className = HttpUtil.getJsonParamVal(params.get("classname"));
                int lineNum = Integer.parseInt(HttpUtil.getJsonParamVal(params.get("linenum")));

                rtn = AgentTool.getLineAccessMetadata(className, lineNum);
                break;
            case "getclassfields":
                rtn = AgentTool.getClassFields(HttpUtil.getJsonParamVal(params.get("classname")));
                break;
            case "stopdebug":
                DebuggerManager.getCurrent().stopTrace(HttpUtil.getJsonParamVal(params.get("id")));
                break;
            case "isdefaultdebugger":
                rtn = DebuggerManager.isDefaultDebugger();
                break;

        }
        return rtn;
    }

    @Override
    public String getStartPath() {
        return startPath;
    }

    @Override
    public Permission getPermission(String user) {
        return Permission.ALL;
    }
}

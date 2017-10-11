package com.ctrip.framework.cs.analyzer;

import com.ctrip.framework.cs.SimpleLoggerFactory;
import com.ctrip.framework.cs.ViFunctionHandler;
import com.ctrip.framework.cs.localLog.LocalLogManager;
import com.ctrip.framework.cs.IgniteManager;
import com.ctrip.framework.cs.Permission;
import com.ctrip.framework.cs.instrument.AgentTool;
import com.ctrip.framework.cs.util.TextUtils;
import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Map;

/**
 * Created by jiang.j on 2016/6/13.
 */
public class AnalyzerHandler implements ViFunctionHandler {
    private String startPath ="/analyzer/";
    @Override
    public Object execute(String path, String user, int permission, Logger logger, Map<String, Object> params) throws Exception{
        Object rtn=null;
        if(path.equals(startPath+"deps")) {
            rtn = Analyzer.getAllPomInfo();
        }else if(path.equals(startPath+"heaphisto")){
            rtn = JVMSampler.getCurrentHeapHisto();
        }else if(path.startsWith(startPath+"vmsnapshot")){
            VMMonitor.VMSnapShot vmSnapShot = VMMonitor.getCurrent();
            int maxWait = 10;
            while (!vmSnapShot.isInitiated() && maxWait>0){
                Thread.sleep(10);
                maxWait--;
            }
            rtn = VMMonitor.getCurrent();
        }else if(path.startsWith(startPath+"getgcloglist")){
            rtn = LocalLogManager.getGCLogList();

        }else if(path.startsWith(startPath+"getjvmoptions")){
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            rtn = TextUtils.join(" ", runtimeBean.getInputArguments());
        }else if(path.startsWith(startPath+"parsegclog")){
            if(params.containsKey("name")) {
                String fileName = (String) params.get("name");
                if (fileName.startsWith("gc-")) {
                    rtn = GCLogAnalyzer.parseToJson(LocalLogManager.getFullPathByName(fileName));
                } else {
                    rtn = "";
                }
            }
        }else if(path.equals(startPath+"jars")) {
            rtn = Analyzer.getAllJarNames();
        }else if(path.equals(startPath+"addclassformetrics")) {
            AgentTool.addMethodMetricsClass((String) params.get("name"));
        }else if(path.equals(startPath+"listsource")) {
            rtn = Analyzer.listJarFolder((String)params.get("jarname"));
        }else if(path.equals(startPath+"getallmoduleinfo")) {
            rtn = Analyzer.getAllModuleInfo();
        }else if(path.equals(startPath+"getneedmetricsclasses")) {
            rtn = AgentTool.getNeedMetricsClasses();
        }else if(path.equals(startPath+"removemetricsclass")) {
            String className = (String) params.get("name");
             AgentTool.removeMetricsClass(className);
        }else if(path.equals(startPath+"listclasses")) {
            rtn = Analyzer.listJarClasses((String)params.get("location"));
        }else if(path.equalsIgnoreCase(startPath+"allIgnitePlugins")){
            rtn = IgniteManager.getPluginMap().keySet();
        }else if(path.equalsIgnoreCase(startPath+"selfcheck")){
            String pluginId = (String) params.get("id");
            rtn = Analyzer.selfCheck(pluginId);

        }else if(path.equalsIgnoreCase(startPath+"getSelfCheckMsgs")){
           String uid = (String) params.get("uid");
            int startIndex = Integer.parseInt((String) params.get("index"));
            rtn = SimpleLoggerFactory.getSimpleLogger(uid).getMsgs(startIndex);
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

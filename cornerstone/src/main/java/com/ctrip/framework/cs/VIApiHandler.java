package com.ctrip.framework.cs;

import com.ctrip.framework.cs.analyzer.AnalyzerHandler;
import com.ctrip.framework.cs.code.CodeHandler;
import com.ctrip.framework.cs.component.ComponentHandler;
import com.ctrip.framework.cs.component.ComponentManager;
import com.ctrip.framework.cs.configuration.ConfigHandler;
import com.ctrip.framework.cs.fc.FCHandler;
import com.ctrip.framework.cs.localLog.LogHandler;
import com.ctrip.framework.cs.metrics.MetricsCollector;
import com.ctrip.framework.cs.metrics.MetricsHandler;
import com.ctrip.framework.cs.threading.ThreadingHandler;
import com.ctrip.framework.cs.watcher.EventLogger;
import com.ctrip.framework.cs.annotation.EventSource;
import com.ctrip.framework.cs.cacheRefresh.CacheHandler;
import com.ctrip.framework.cs.enterprise.EnterpriseHandler;
import com.ctrip.framework.cs.metrics.Metrics;
import com.ctrip.framework.cs.util.TextUtils;
import com.ctrip.framework.cs.watcher.EventLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by jiang.j on 2016/4/18.
 */
@EventSource(name="vi.api")
public class VIApiHandler{
    transient Logger logger = LoggerFactory.getLogger(this.getClass());
    private  final int SC_OK = 200;
    private  final int SC_NOTFOUND = 404;
    private  final int SC_METHODNOTALLOWED = 405;
    private static VIApiHandler handler;
    private Map<String,ViFunctionHandler> handlersMap = new HashMap<>();
    public static synchronized VIApiHandler getInstance(){
        if(handler==null){
            handler= new VIApiHandler();
        }

        return handler;
    }

    public boolean register(ViFunctionHandler apiHandler){
        if(handlersMap.containsKey(apiHandler.getStartPath())){
            return false;
        }
        handlersMap.put(apiHandler.getStartPath(),apiHandler);
        return true;
    }

    public class ExeResult{
        private final Object data;
        private final int permission;
        private final int responseCode;
        public ExeResult(Object data,int permission,int responseCode){
            this.data =data;
            this.permission = permission<=0 ?Permission.DENY.getValue():permission;
            this.responseCode = responseCode;
        }


        public int getResponseCode(){
            return this.responseCode;
        }

        public Object getData(){
            return this.data;
        }

        public int getPermission(){
            return this.permission;
        }
    }

    public VIApiHandler(){

        this.register(new CacheHandler());
        this.register(new ComponentHandler());
        this.register(new ConfigHandler());
        this.register(new AnalyzerHandler());
        this.register(new FCHandler());
        this.register(new LogHandler());
        this.register(new ThreadingHandler());
        this.register(new MetricsHandler());
        this.register(new AppHandler());
        this.register(new EnterpriseHandler());
        this.register(new CodeHandler());
    }

    public ExeResult  executeService(String path,String user,Map<String,Object> params){

        long startTime = System.nanoTime();
        Object rtn=null;
        int responseCode = SC_OK;
        int permission = Permission.ALL.getValue();
        EventLogger eventLogger = EventLoggerFactory.getTransLogger(getClass());

        if(path == null){
            return new ExeResult("NO PATH ERROR",0,SC_NOTFOUND);
        }
        path = path.toLowerCase();

        boolean isHandled =false;

        try {
            eventLogger.fireEvent(EventLogger.TRANSSTART,path);
            switch (path) {
                case "/ip":
                    rtn = params.get("req_ip");
                    isHandled = true;
                    break;
                case "/status":
                case "/health":
                    AppInfo appInfo = ComponentManager.getStatus(AppInfo.class);
                    switch (appInfo.getStatus()) {
                        case Uninitiated:
                            responseCode = AppInfo.UNINITIATED;
                            break;
                        case Initiating:
                            responseCode = AppInfo.INITIATING;
                            break;
                        case Initiated:
                            responseCode = AppInfo.INITIATED;
                            break;
                        case InitiatedFailed:
                            responseCode = AppInfo.INITIATEDFAILED;
                            break;
                        case MarkDown:
                            responseCode = AppInfo.MARKDOWN;
                            break;
                    }
                    rtn = appInfo.getStatus();
                    if(appInfo.getStatus() == AppStatus.MarkDown){
                        rtn = "markdown by "+appInfo.getMarkDownReason();
                    }
                    isHandled = true;
                    break;
                case "/status/detail":
                case "/health/detail":
                    appInfo = ComponentManager.getStatus(AppInfo.class);
                    Map<String,Object> statusDetail = new LinkedHashMap<>();
                    statusDetail.put("appStatus",appInfo.getStatus());
                    if(appInfo.getStatus()==AppStatus.MarkDown){
                        statusDetail.put("markDownReason",appInfo.getMarkDownReason());
                    }
                    statusDetail.put("igniteDetail",ComponentManager.getStatus(IgniteStatus.class));
                    rtn = statusDetail;
                    isHandled = true;
                    break;
                default:
                    for (String startPath : handlersMap.keySet()) {
                        if (path.startsWith(startPath)) {
                            isHandled = true;

                            ViFunctionHandler functionHandler = handlersMap.get(startPath);
                            permission = functionHandler.getPermission(user).getValue();
                            rtn = functionHandler.execute(path, user, permission, logger, params);
                            break;
                        }
                    }
                    break;
            }
            eventLogger.fireEvent(EventLogger.TRANSEND);

        }catch (Throwable e){
            logger.warn("execute service error",e);
            e.printStackTrace();
            eventLogger.fireEvent(EventLogger.TRANSEND,e);
            Throwable rootCause;
            if(e.getCause()!=null){
                rootCause = e.getCause();
            }else{
                rootCause = e;
            }
            rtn = TextUtils.makeJSErrorMsg(rootCause.getMessage(),e.getClass().getName());
            if(e instanceof  NoPermissionException){
                responseCode = SC_METHODNOTALLOWED;
            }
        }finally {
            eventLogger.fireEvent(EventLogger.TRANSFINALLY);
        }

        if(!isHandled){
            rtn = path + " not found";
        }

        long cost = (System.nanoTime()-startTime)/1000L;

        try {
            MetricsCollector.getCollector().record(Metrics.VIAPI, cost);
        }catch (Throwable e){
            logger.error("metrics collect failed!",e);
        }
        return new ExeResult(rtn,permission,responseCode);

    }
}

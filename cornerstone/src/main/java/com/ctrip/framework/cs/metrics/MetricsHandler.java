package com.ctrip.framework.cs.metrics;

import com.ctrip.framework.cs.Permission;
import com.ctrip.framework.cs.ViFunctionHandler;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by jiang.j on 2016/7/5.
 */
public class MetricsHandler implements ViFunctionHandler {
    private  String startPath ="/metrics/";
    MetricsCollector metricsCollector = MetricsCollector.getCollector();
    @Override
    public Object execute(String path, String user, int permission, Logger logger, Map<String, Object> params) throws Exception {
        Object rtn = null;
        if(path.equals(startPath+"register")){
            Gson gson = new Gson();
            if(params == null){
                return null;
            }
            Object idRaw = params.get("id");
            String observerId = null;
            MetricsObserver observer =null;
            if(idRaw!=null) {
                String id = gson.fromJson((JsonElement)(idRaw),String.class);
                observer = metricsCollector.getObserver(id);
                observerId = id;
            }

            if(observer==null){
                observer = new MetricsObserverImpl();
                observerId = null;
            }

            String namesRaw = String.valueOf(params.get("names"));
            logger.info(namesRaw);
            HashMap<String,String> tags = new HashMap<>();
            HashSet<String> names = new HashSet<>();
            double[] percentiles=null;
            names = gson.fromJson(namesRaw,names.getClass());

            if(names == null){
                return null;
            }
            if(params.containsKey("tags")){
                tags = gson.fromJson(String.valueOf(params.get("tags")),tags.getClass());
            }

            if(params.containsKey("percentiles")){
               percentiles =gson.fromJson(String.valueOf(params.get("percentiles")),double[].class);
            }

            observer.setFilter(names,tags,percentiles);
            if(observerId==null) {
                rtn = metricsCollector.addObserver(user, observer);
            }else{
                return  observerId;
            }
        }else if(path.equals(startPath+"current")){
            Gson gson = new Gson();
            String id = gson.fromJson((JsonElement)(params.get("id")),String.class);

            rtn = metricsCollector.getOberserStats(id);
            if(rtn==null){
                throw new NoObserverException(id);
            }
        }else if(path.equals(startPath+"names")){
            rtn = metricsCollector.getMetricNames();
        }else if(path.equals(startPath+"clearqueue")){
            metricsCollector.clearWaitingQueue();
            rtn = true;
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

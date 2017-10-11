package com.ctrip.framework.cs;

import com.ctrip.framework.cs.metrics.Metrics;
import com.ctrip.framework.cs.metrics.MetricsCollector;
import com.ctrip.framework.cs.metrics.MetricsSnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2016/7/6.
 */
public class MetricsHandlerTest {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Test
    public void testMetricsRegister() throws Exception {
        VIApiHandler apiHandler = new VIApiHandler();
        String metricsPath = "/metrics/";
        String rawJson = "{'names':['"+Metrics.VIAPI+"']}";


        String observerId = (String) apiHandler.executeService(metricsPath+"register","me",loadParasFromJsonString(rawJson,false)).getData();

        Set<String> names = (Set<String>) apiHandler.executeService(metricsPath+"names","me",loadParasFromJsonString(rawJson,false)).getData();

        assertTrue(observerId.length() > 5);
        rawJson = "{'id':'"+observerId+"'}";

        System.out.println(rawJson);
        Thread.sleep(200);
       Map<String,MetricsSnapshot> currentStat;
        Object raw = apiHandler.executeService(metricsPath+"current","me",loadParasFromJsonString(rawJson,true)).getData();

        if(raw instanceof  String){
            System.out.println(raw);
        }
        currentStat = (Map<String, MetricsSnapshot>) raw;
        assertTrue(currentStat.containsKey(Metrics.VIAPI));
        assertEquals(2, currentStat.get(Metrics.VIAPI).count);

        MetricsCollector.getCollector().stopAndClear();
        Thread.sleep(500);

    }
    @Test
    public void testHavePercentilesMetricsRegister() throws Exception {
        VIApiHandler apiHandler = new VIApiHandler();
        String metricsPath = "/metrics/";
        String rawJson = "{'names':['"+ Metrics.VIAPI+"'],'percentiles':[99.0,95.0]}";


        String observerId = (String) apiHandler.executeService(metricsPath+"register","me",loadParasFromJsonString(rawJson,false)).getData();

        Set<String> names = (Set<String>) apiHandler.executeService(metricsPath+"names","me",loadParasFromJsonString(rawJson,false)).getData();

        assertTrue(observerId.length() > 5);
        rawJson = "{'id':'"+observerId+"'}";

        System.out.println(rawJson);
        Thread.sleep(200);
        Map<String,MetricsSnapshot> currentStat;
        Object raw = apiHandler.executeService(metricsPath+"current","me",loadParasFromJsonString(rawJson,true)).getData();

        Thread.sleep(200);
        if(raw instanceof  String){
            System.out.println(raw);
        }
        currentStat = (Map<String, MetricsSnapshot>) raw;
        assertTrue(currentStat.containsKey(Metrics.VIAPI));
        assertEquals(2, currentStat.get(Metrics.VIAPI).count);
        Thread.sleep(100);

        raw = apiHandler.executeService(metricsPath+"current","me",loadParasFromJsonString(rawJson,true)).getData();

        currentStat = (Map<String, MetricsSnapshot>) raw;
        assertTrue(currentStat.containsKey(Metrics.VIAPI));
        assertEquals(1, currentStat.get(Metrics.VIAPI).count);
        MetricsCollector.getCollector().stopAndClear();
        Thread.sleep(500);

    }

    public Map<String,Object>loadParasFromJsonString(String str,boolean isPrimitive){
        Map<String,Object> params =null;

        Gson gson = new Gson();
        Type paraMap;
        if(isPrimitive){
            paraMap= new TypeToken<Map<String, JsonPrimitive>>(){}.getType();
        }else{

            paraMap= new TypeToken<Map<String, JsonArray>>(){}.getType();
        }
        params = gson.fromJson(str,paraMap);
        return params;
    }
}

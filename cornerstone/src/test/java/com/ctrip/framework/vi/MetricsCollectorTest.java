package com.ctrip.framework.cornerstone;

import com.ctrip.framework.cornerstone.metrics.*;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;


/**
 * Created by jiang.j on 2016/7/4.
 */
public class MetricsCollectorTest {

    @Test
    public void testCollector() throws InterruptedException, InstantiationException, IllegalAccessException {

        MetricsCollector metricsCollector = MetricsCollector.getCollector();
        MetricsObserver observer = new MetricsObserverImpl();
        Set<String> keySet = new HashSet<>();
        observer.setFilter(keySet,null,null);
        keySet.add("key0");
        keySet.add("key1");
        keySet.add("key2");
        String id = metricsCollector.addObserver("127.0.0.1",observer);
        int testCount =1000;
        while (testCount-->0){
            metricsCollector.record("key"+testCount,10,null);
            metricsCollector.record("key"+testCount,10,null);
            metricsCollector.record("key"+testCount,10,null);
        }

        Thread.sleep(500);
        Map<String, MetricsSnapshot> currentStats = metricsCollector.getOberserStats(id);

        assertEquals(3, currentStats.size());
        assertEquals(3,currentStats.get("key0").count);
        testCount =1000;
        while (testCount-->0){
            metricsCollector.record("key"+testCount,10,null);
        }
        Thread.sleep(500);
         currentStats = metricsCollector.getObserver(id).drainDry();
        assertEquals(1,currentStats.get("key0").count);
        assertEquals(3, currentStats.size());
        metricsCollector.stopAndClear();
    }

    /*
    @Test
    public void testTags() throws InstantiationException, IllegalAccessException, InterruptedException {

        MetricsCollector metricsCollector = MetricsCollector.getCollector();
        MetricsObserver observer = new MetricsObserverImpl();
        Set<String> keySet = new HashSet<>();
        Map<String,String> tags = new HashMap<>();
        tags.put("version","1");
        tags.put("appid","10090");
        observer.setFilter(keySet,tags);
        keySet.add("key0");
        keySet.add("key1");
        keySet.add("key2");
        String id = metricsCollector.addObserver("127.0.0.1",observer);
        int testCount =1000;
        while (testCount-->0){

            Map<String,String> tags1 = new HashMap<>();
            tags1.put("version","1");
            tags1.put("appid","10090");
            metricsCollector.record("key"+testCount,10,tags1);

            tags1 = new HashMap<>();
            tags1.put("appid","10090");
            tags1.put("version","1");
            tags1.put("operation","add");
            metricsCollector.record("key"+testCount,10,tags1);

            tags1 = new HashMap<>();
            tags1.put("appid","10090");
            tags1.put("operation", "add");
            metricsCollector.record("key"+testCount,10,tags1);
        }

        Thread.sleep(500);
        Map<String, MetricsObserver.MetricsSnapshot> currentStats = metricsCollector.getObserver(id).drainDry();

        assertEquals(3, currentStats.size());
        assertEquals(2,currentStats.get("key0").count);
        testCount =1000;
        while (testCount-->0){
            metricsCollector.record("key"+testCount,10,null);
        }
        Thread.sleep(500);
         currentStats = metricsCollector.getObserver(id).drainDry();
        assertEquals(null,currentStats.get("key0"));
        assertEquals(0, currentStats.size());
        metricsCollector.stopAndClear();
    }*/
}

package com.ctrip.framework.cornerstone.metrics;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by jiang.j on 2016/7/4.
 */
public class MetricsCollector {

    private ConcurrentHashMap<String,MetricsObserver> obs;
    private boolean isRunning =false,senderIsRuning=false;
    private Set<String> metricNames;
    private static MetricsCollector metricsCollector;
    private final int SENDERMAXWAITTIME=10000;
    BlockingQueue<StatsInfo> queue ;

    static final Object locker = new Object();

    private volatile long senderLastUpdateTime;
    public static MetricsCollector getCollector(){
        if(metricsCollector ==null){
            synchronized (locker){
                if(metricsCollector ==null){
                    metricsCollector = new MetricsCollector();
                }
            }
        }
        return metricsCollector;
    }


    private MetricsCollector() {
        obs = new ConcurrentHashMap<>();
        queue = new LinkedBlockingQueue<>();
        metricNames = new ConcurrentSkipListSet<>();
    }

    public boolean isRunning(){
        return isRunning;
    }

    public Map<String,MetricsObserver.ObserverStatus> getObserverStatus(){
        Enumeration<String> keys = obs.keys();
        Map<String,MetricsObserver.ObserverStatus> rtn = new HashMap<>();
        while (keys.hasMoreElements()){
            String key = keys.nextElement();
            rtn.put(key,obs.get(key).getObserStatus());
        }
        return rtn;
    }
    public int getMetricsNameCount(){
        return metricNames.size();
    }

    public void stopAndClear(){
        isRunning =false;
        queue.clear();
        for(MetricsObserver ob:obs.values()){
            ob.clearStore();
        }
        obs.clear();
    }

    public Set<String> getMetricNames(){
        return this.metricNames;
    }

    public synchronized String addObserver(String ip,MetricsObserver observer) throws IllegalAccessException, InstantiationException {
        if (ip == null){
            ip = "dev";
        }
        String observerId = ip + System.currentTimeMillis();
        obs.put(observerId,observer);
        if(!isRunning && !senderIsRuning){
            new Thread("vi-metrics-sender") {
                @Override
                public void run() {

                    while (isRunning) {
                        try {
                            notifyObservers(queue.take());
                            if(obs.size()==0){
                                stopAndClear();
                            }
                            senderLastUpdateTime = System.currentTimeMillis();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    senderIsRuning =false;
                }
            }.start();
            senderIsRuning =true;
        }
        senderLastUpdateTime =System.currentTimeMillis();
        isRunning=true;
        return observerId;
    }

    public MetricsObserver getObserver(String id){
        if(obs == null || id == null)
            return null;
        return obs.get(id);
    }

    public Map<String,MetricsSnapshot> getOberserStats(String id){
        MetricsObserver observer = getObserver(id);
        if(observer==null){
            return null;
        }
        return observer.drainDry();
    }

    /**
     * If this object has changed, as indicated by the
     * <code>hasChanged</code> method, then notify all of its observers
     * and then call the <code>clearChanged</code> method to indicate
     * that this object has no longer changed.
     * <p>
     * Each observer has its <code>update</code> method called with two
     * arguments: this observable object and the <code>arg</code> argument.
     *
     * @param   arg   any object.
     * @see     java.util.Observable#clearChanged()
     * @see     java.util.Observable#hasChanged()
     * @see     java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    private void notifyObservers(StatsInfo arg) {
        Enumeration<String> keys = obs.keys();
        while (keys.hasMoreElements()){
           String currentKey = keys.nextElement();
            MetricsObserver observer =obs.get(currentKey);
            if(observer != null) {
                if (!observer.isExpire()) {
                    observer.record(arg.key, arg.cost, arg.tags);
                } else {
                    obs.remove(currentKey);
                }
            }
        }

    }


    /**
     * Returns the number of observers of this <tt>Observable</tt> object.
     *
     * @return  the number of observers of this object.
     */
    public synchronized int getObserversCount() {
        return obs.size();
    }
    public class StatsInfo{
        public String key;
        public long cost;
        public Map<String,String> tags;
    }


    public int waitedMsgCount(){
        return queue.size();
    }

    public void clearWaitingQueue(){
        this.queue.clear();
    }

    private boolean isFilterKey(String key){
        Iterator<MetricsObserver> obsIterator = obs.values().iterator();
        while (obsIterator.hasNext()){
            if(obsIterator.next().hasMetricsKey(key)){
                return true;
            }
        }
        return false;
    }

    public void record(final String key,final long value,Map<String,String> tags){
        try {
            metricNames.add(key);
            if (isRunning) {
                if (obs.size() == 0) {
                    stopAndClear();
                    return;
                }
            }

            if (isRunning && isFilterKey(key)) {
                StatsInfo info = new StatsInfo();
                info.key = key;
                info.cost = value;
                //info.tags = tags;
                queue.offer(info);
                if ((System.currentTimeMillis() - senderLastUpdateTime) > SENDERMAXWAITTIME) {
                    stopAndClear();
                    return;
                }
            }
        }catch (Throwable e){

        }
    }

    public void record(String key,long value){
        record(key,value,null);
    }

    public void record(String key){
        record(key+"##",-1,null);
    }
    public void record(String key,Map<String,String> tags){
        record(key+"##",-1,tags);
    }
}

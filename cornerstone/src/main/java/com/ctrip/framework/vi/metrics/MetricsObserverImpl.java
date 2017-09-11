package com.ctrip.framework.vi.metrics;

import java.util.*;

/**
 * Created by jiang.j on 2016/7/5.
 */
public class MetricsObserverImpl implements MetricsObserver{

    Map<String,MetricsStatsBuffer> statsStore = new HashMap<>();
    Set<String> filterKeys;
    Map<String,String> filterTags;
    double[] percentiles;
    long lastUpdateTime;

    public MetricsObserverImpl(){
        lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public void record(String key, long cost, Map<String, String> tags) {

        boolean skip = true;
        if(filterKeys!=null && filterKeys.contains(key)) {
            if(filterTags!=null && filterTags.size()>0){
                if(tags ==null){
                    return;
                }

                for (Map.Entry<String, String> t : filterTags.entrySet()) {

                    String finded = tags.get(t.getKey());
                    if (null == finded || !finded.equalsIgnoreCase(t.getValue())) {
                        return;
                    }
                }
            }
            skip =false;
        }

        if(skip)
            return;

        MetricsStatsBuffer stats;
        if (!statsStore.containsKey(key)) {
            stats = new MetricsStatsBuffer(this.percentiles);
            statsStore.put(key, stats);
        } else {
            stats= statsStore.get(key);
        }
        stats.record(cost);
    }

    @Override
    public void setFilter(Set<String> keys,Map<String,String> tags,double[] percentiles) {
        this.filterKeys = keys;
        this.filterTags = tags;
        this.percentiles = percentiles;
    }

    public synchronized Map<String,MetricsSnapshot> drainDry(){

        lastUpdateTime = System.currentTimeMillis();
        Map<String,MetricsSnapshot> rtn = new HashMap<>();
        for(String key:this.filterKeys){
            MetricsStatsBuffer buffer = statsStore.get(key);
            if(buffer!=null){
                rtn.put(key,buffer.drain());
            }
        }
        return rtn;
    }

    public synchronized  void clearStore(){
        statsStore.clear();
    }

    @Override
    public boolean isExpire() {
        return System.currentTimeMillis()-lastUpdateTime>MetricsObserver.MAXWAITTIME;
    }

    @Override
    public boolean hasMetricsKey(String key) {
        if(this.filterKeys == null){
            return false;
        }else {
            return this.filterKeys.contains(key);
        }
    }

    @Override
    public ObserverStatus getObserStatus() {
        List<MetricsStatus> statuses = new ArrayList<>();
        for(Map.Entry<String,MetricsStatsBuffer> entry :statsStore.entrySet()){
            MetricsStatsBuffer statsBuffer = entry.getValue();
            MetricsStatus status = new MetricsStatus(entry.getKey(),statsBuffer.getBufferSize(),statsBuffer.getCount());
            statuses.add(status);
        }
        return new ObserverStatus(lastUpdateTime,statuses,percentiles);
    }


}

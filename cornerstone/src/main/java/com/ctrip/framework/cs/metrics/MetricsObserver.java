package com.ctrip.framework.cs.metrics;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jiang.j on 2016/7/4.
 */
public interface MetricsObserver {

    public final int MAXWAITTIME = 3000;
    void record(String key,long value,Map<String,String> tags);

    void setFilter(Set<String> keys,Map<String,String> tags,double[] percentiles);
    Map<String,MetricsSnapshot> drainDry();
    void clearStore();
    boolean isExpire();
    boolean hasMetricsKey(String key);
    ObserverStatus getObserStatus();


    class  ObserverStatus{
        private List<MetricsStatus> metricsStatus;
        private long lastUpdateTime;
        private double[] percentiles;

        ObserverStatus(long lastUpdateTime,List<MetricsStatus> metricsStatus,double[] percentiles){
            this.metricsStatus = metricsStatus;
            this.lastUpdateTime = lastUpdateTime;
            this.percentiles = percentiles;
        }

        public List<MetricsStatus> getMetricsStatus(){
            return this.metricsStatus;
        }

        public long getLastUpdateTime(){
            return this.lastUpdateTime;
        }

        public double[] getPercentiles(){
            return this.percentiles;
        }

    }
    class MetricsStatus {
        private String key;
        private int bufferSize;
        private int currentCount;
        MetricsStatus(String key,int bufferSize,int currentCount){
            this.key = key;
            this.bufferSize = bufferSize;
            this.currentCount = currentCount;
        }

        public String getKey(){
            return key;
        }

        public int getBufferSize(){
            return bufferSize;
        }

        public int getCurrentCount(){
            return getCurrentCount();
        }

    }
}

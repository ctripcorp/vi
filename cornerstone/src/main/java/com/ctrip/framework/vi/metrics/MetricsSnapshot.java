package com.ctrip.framework.vi.metrics;

/**
 * Created by jiang.j on 2016/8/19.
 */
public class MetricsSnapshot {
    protected MetricsSnapshot(){

    }
    public int count;
    public long total;
    public long min;
    public long max;
    public double stddev;
    public double[] percentileValues;
}

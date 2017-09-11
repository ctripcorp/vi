package com.ctrip.framework.vi.metrics;

/**
 * Created by jiang.j on 2017/5/5.
 */
public enum MetricsValueType {
    Sec(0), MillSec(1), MicroSec(2), NaoSec(3);

    int value;
     MetricsValueType(int i) {
        this.value = i;
    }

    public int getValue(){
        return this.value;
    }
}

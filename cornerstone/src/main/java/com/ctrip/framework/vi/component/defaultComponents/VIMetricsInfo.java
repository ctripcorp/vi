package com.ctrip.framework.vi.component.defaultComponents;

import com.ctrip.framework.vi.annotation.ComponentStatus;
import com.ctrip.framework.vi.annotation.FieldInfo;
import com.ctrip.framework.vi.metrics.MetricsCollector;
import com.ctrip.framework.vi.metrics.MetricsObserver;

import java.util.Map;

/**
 * Created by jiang.j on 2016/7/29.
 */
@ComponentStatus(id="vi.metricsinfo",name = "vi metrics status",description = "VI metrics 状态信息",custom = true,jmx = true)
public class VIMetricsInfo {
    private  final boolean isRunning;

    private  final int waitMsgCount;
    private  final int observersCount;
    private  final Map<String, MetricsObserver.ObserverStatus> observersStatus;
    private  final int metricsCount;
    public VIMetricsInfo(){
        MetricsCollector collector = MetricsCollector.getCollector();
        this.isRunning = collector.isRunning();
        this.observersCount = collector.getObserversCount();
        this.observersStatus = collector.getObserverStatus();
        this.waitMsgCount = collector.waitedMsgCount();
        this.metricsCount = collector.getMetricsNameCount();
    }
}

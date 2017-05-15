package com.ctrip.framework.cornerstone.component.defaultComponents;

import com.ctrip.framework.cornerstone.annotation.ComponentStatus;
import com.ctrip.framework.cornerstone.util.LinuxInfoUtil;
import com.sun.management.OperatingSystemMXBean;

import java.io.File;
import java.io.IOException;
import java.lang.management.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by jiang.j on 2016/4/15.
 */
@ComponentStatus(id="vi.performancemonitor",name="performance monitor",description = "性能监视",custom = true)
public class PerformanceStatus {
    private final long committedVirtualMemorySize;

    private final long freePhysicalMemorySize;

    private final long totalPhysicalMemorySize;
    private final long totalSwapSpaceSize;
    private final long freeSwapSpaceSize;
    private final long processCpuTime;
    private final int availableProcessors;
    private final double processCpuLoad;
    private final double systemCpuLoad;
    private final double systemLoadAverage;
    private final String appStartUpTime;
    private final int currentThreadCount;
    private final int daemonThreadCount;
    private final long beanCreatedThreadCount;
    private final int peakThreadCount;
    private final int loadedClassCount;
    private final long totalLoadedClassCount;
    private final long unloadedClassCount;
    private final long runtime;
    private long availableMemory=-1;
    private final String os;
    private final MemoryUsage heapMemoryUsage;
    private final MemoryUsage nonHeapMemoryUsage;
    private final List<RootFile> rootFiles = new LinkedList<>();
    private int minorGcCount;
    //private long minorGcTime;
    private int fullGcCount;
    //private long fullGcTime;
    private int otherGcCount;
    //private long otherGcTime;

    class RootFile{
        private String path;
        private long totalSize;
        private long availableSize;
        public RootFile(String path,long totalSize,long availableSize){
            this.path = path;
            this.totalSize = totalSize;
            this.availableSize = availableSize;
        }
    }
    public PerformanceStatus() {
        OperatingSystemMXBean bean= (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        heapMemoryUsage= memBean.getHeapMemoryUsage();
        nonHeapMemoryUsage = memBean.getNonHeapMemoryUsage();

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        currentThreadCount=threadBean.getThreadCount();
        daemonThreadCount= threadBean.getDaemonThreadCount();
        beanCreatedThreadCount= threadBean.getTotalStartedThreadCount();
        peakThreadCount = threadBean.getPeakThreadCount();

        ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
        loadedClassCount=classLoadingBean.getLoadedClassCount();
        totalLoadedClassCount=classLoadingBean.getTotalLoadedClassCount();
        unloadedClassCount=classLoadingBean.getUnloadedClassCount();
        committedVirtualMemorySize = (bean.getCommittedVirtualMemorySize());
        freePhysicalMemorySize =(bean.getFreePhysicalMemorySize());
        totalPhysicalMemorySize =(bean.getTotalPhysicalMemorySize());

        freeSwapSpaceSize =(bean.getFreeSwapSpaceSize());
        totalSwapSpaceSize =(bean.getTotalSwapSpaceSize());
        processCpuTime =(bean.getProcessCpuTime());
        availableProcessors =bean.getAvailableProcessors();
        processCpuLoad =bean.getProcessCpuLoad();

        systemCpuLoad =bean.getSystemCpuLoad();
        systemLoadAverage =bean.getSystemLoadAverage();
        appStartUpTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(runtimeBean.getStartTime()));
        runtime = (new Date().getTime() - runtimeBean.getStartTime())/1000;
        os = bean.getName()+" "+bean.getVersion();

        if(HostInfo.isLinux()){
            try {
                availableMemory = (LinuxInfoUtil.getAvailableMemKB()*1024l);
            } catch (Throwable e) {
            }
        }

        File[] roots = File.listRoots();
        for(File file:roots){
            rootFiles.add(new RootFile(file.getAbsolutePath(),file.getTotalSpace(),file.getFreeSpace()));
        }
        getGCStatus();
    }

    private void getGCStatus() {

         String[] youngGenCollectorNames = { "Copy", "ParNew", "PS Scavenge", "Garbage collection optimized for short pausetimes Young Collector", "Garbage collection optimized for throughput Young Collector", "Garbage collection optimized for deterministic pausetimes Young Collector" };

         String[] oldGenCollectorNames = { "MarkSweepCompact", "PS MarkSweep", "ConcurrentMarkSweep", "Garbage collection optimized for short pausetimes Old Collector", "Garbage collection optimized for throughput Old Collector", "Garbage collection optimized for deterministic pausetimes Old Collector" };
         List<String> young = Arrays.asList(youngGenCollectorNames);
         List<String> old = Arrays.asList(oldGenCollectorNames);
         List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
         for (GarbageCollectorMXBean b : beans) {
             String name = b.getName();
             if (young.contains(name)) {
                 minorGcCount = (int)(minorGcCount + b.getCollectionCount());
                 //minorGcTime += b.getCollectionTime();
             } else if (old.contains(name)) {
                 fullGcCount = (int)(fullGcCount + b.getCollectionCount());
                 //fullGcTime += b.getCollectionTime();
             } else {
                 otherGcCount = (int)(otherGcCount + b.getCollectionCount());
                 //otherGcTime += b.getCollectionTime();
             }
         }
     }
}

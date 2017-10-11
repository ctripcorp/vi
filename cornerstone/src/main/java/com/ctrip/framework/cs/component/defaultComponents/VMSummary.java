 package com.ctrip.framework.cs.component.defaultComponents;

 import com.ctrip.framework.cs.annotation.ComponentStatus;
 import com.ctrip.framework.cs.annotation.FieldInfo;
 import com.ctrip.framework.cs.util.TextUtils;
 import com.sun.management.OperatingSystemMXBean;

 import java.lang.management.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;

 @ComponentStatus(id="vi.vmsummary",name = "vm summary",description = "VM概要",custom = true)
 public class VMSummary
 {
     class GCBean{
         String name;
         long gcCount;
         long gcTime;
     }
     @FieldInfo(name = "Heap Commited Memory",description = "堆提交内存")
     public final long heapCommitedMemory;
     @FieldInfo(name = "Heap Used Memory",description = "当前堆内存")
     public final long heapUsedMemory;
     @FieldInfo(name = "Heap Max Memory",description = "最大堆大小")
     public final long heapMaxMemory;
     @FieldInfo(name = "Nonheap Commited Memory",description = "非堆提交内存")
     public final long nonHeapCommitedMemory;
     @FieldInfo(name = "Nonheap Used Memory",description = "当前非堆内存")
     public final long nonHeapUsedMemory;
     @FieldInfo(name = "Nonheap Max Memory",description = "最大非堆大小")
     public final long nonHeapMaxMemory;

     @FieldInfo(name = "Loaded Class Count",description = "已加载当前类")
     public final long loadedClassCount;
     @FieldInfo(name = "Total Loaded Class Count",description = "已加载类总数")
     public final long totalLoadedClassCount;
     @FieldInfo(name = "Unloaded Class Count",description = "已卸载类总数")
     public final long unloadedClassCount;
     @FieldInfo(name = "Current Thread Count",description = "当前线程数")
     public final int currentThreadCount;
     @FieldInfo(name = "Daemon Thread Count",description = "守护线程数")
     public final int daemonThreadCount;
     @FieldInfo(name = "Peak Thread Count",description = "线程峰值")
     public final int peakThreadCount;
     @FieldInfo(name = "Total Started Thread Count",description = "启动线程总数")
     public final long totalStartedThreadCount;
     public final String libraryPath;
     public final String vmOptions;
     public final String bootClassPath;
     public final long upTime;
     public final String vmName;
     public final String vmVendor;
     public final String jdkVersion;
     public final long processCpuTime;
     public final String jitCompiler;
     public final String os;
     public final String osArch;
     public final int availableProcessors;
     public final long commitedVirtualMemory;
     public final long freePhysicalMemorySize;
     public final long totalPhysicalMemorySize;
     public final long freeSwapSpaceSize;
     public final long totalSwapSpaceSize;
     int minorGcCount = 0;
     int fullGcCount = 0;
     int otherGcCount = 0;
     long minorGcTime = 0L;
     long fullGcTime = 0L;
     long otherGcTime = 0L;

     @FieldInfo(name = "Java Class Path",description = "类路径")
     final String classPath;
     List<GCBean> gcInfos;

     public VMSummary(){
         MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
         MemoryUsage u = bean.getHeapMemoryUsage();
         heapCommitedMemory= (u.getCommitted());
         heapUsedMemory=(u.getUsed());
         heapMaxMemory=(u.getMax());

         u = bean.getNonHeapMemoryUsage();
         nonHeapCommitedMemory=(u.getCommitted());
         nonHeapUsedMemory=(u.getUsed());
         nonHeapMaxMemory=(u.getMax());

         ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
         currentThreadCount=threadBean.getThreadCount();
         daemonThreadCount= threadBean.getDaemonThreadCount();
         totalStartedThreadCount= threadBean.getTotalStartedThreadCount();
         peakThreadCount = threadBean.getPeakThreadCount();

         ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
         loadedClassCount=classLoadingBean.getLoadedClassCount();
         totalLoadedClassCount=classLoadingBean.getTotalLoadedClassCount();
         unloadedClassCount=classLoadingBean.getUnloadedClassCount();
         getGCStatus();

         RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
         classPath = runtimeBean.getClassPath();
         libraryPath = runtimeBean.getLibraryPath();
         vmOptions = TextUtils.join(" ",runtimeBean.getInputArguments());
         bootClassPath = runtimeBean.getBootClassPath();
         upTime = runtimeBean.getUptime();

         vmName=runtimeBean.getVmName();
         vmVendor= runtimeBean.getVmVendor();


         OperatingSystemMXBean osBean= (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

         processCpuTime = osBean.getProcessCpuTime();

         jdkVersion=System.getProperty("java.version");
         jitCompiler=System.getProperty("java.vm.name");

         os = osBean.getName() + " "+osBean.getVersion();
         osArch = osBean.getArch();
         availableProcessors = osBean.getAvailableProcessors();

          commitedVirtualMemory = osBean.getCommittedVirtualMemorySize();
         freePhysicalMemorySize =(osBean.getFreePhysicalMemorySize());
         totalPhysicalMemorySize =(osBean.getTotalPhysicalMemorySize());

         freeSwapSpaceSize =(osBean.getFreeSwapSpaceSize());
         totalSwapSpaceSize =(osBean.getTotalSwapSpaceSize());

         List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
         gcInfos = new ArrayList<>(beans.size());
         for (GarbageCollectorMXBean b : beans) {
             GCBean gcBean = new GCBean();
             gcBean.name =b.getName();
             gcBean.gcCount = b.getCollectionCount();
             gcBean.gcTime = b.getCollectionTime();
             gcInfos.add(gcBean);
         }

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
                 minorGcTime += b.getCollectionTime();
             } else if (old.contains(name)) {
                 fullGcCount = (int)(fullGcCount + b.getCollectionCount());
                 fullGcTime += b.getCollectionTime();
             } else {
                 otherGcCount = (int)(otherGcCount + b.getCollectionCount());
                 otherGcTime += b.getCollectionTime();
             }
         }
     }

 }


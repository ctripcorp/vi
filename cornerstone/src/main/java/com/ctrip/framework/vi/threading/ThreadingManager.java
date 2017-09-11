package com.ctrip.framework.vi.threading;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * Created by jiang.j on 2016/5/4.
 */
public class ThreadingManager {
    public ThreadingManager(){

    }
    public static class TInfo{
        public String name;
        public long id;
        public long cpuTime;
        public Thread.State state;
    }
    public static List<TInfo> getAllThreadInfo(){

        List<TInfo> threads = new ArrayList<>();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long[] ids = threadBean.getAllThreadIds();
        ThreadInfo[] infos = threadBean.getThreadInfo(ids);
        for (ThreadInfo info : infos){
            long id = info.getThreadId();
            TInfo tInfo = new TInfo();
            tInfo.name = info.getThreadName();
            tInfo.id = id;
            tInfo.state = info.getThreadState();
            tInfo.cpuTime = threadBean.getThreadCpuTime(id);
            threads.add(tInfo);
        }
        Collections.sort(threads,new Comparator<TInfo>() {
            @Override
            public int compare(TInfo o1, TInfo o2) {
                return Long.compare(o2.cpuTime,o1.cpuTime);
            }
        });
        return threads;
    }

    public static ThreadInfo getThreadInfo(long threadId,int maxDepth){
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        return threadBean.getThreadInfo(threadId,maxDepth);
    }

    public static Map<String,Number> getThreadStats(){

        Map<String,Number> rtn = new HashMap<>();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        rtn.put("currentThreadCount",threadBean.getThreadCount());
        rtn.put("daemonThreadCount", threadBean.getDaemonThreadCount());
        rtn.put("totalStartedThreadCount", threadBean.getTotalStartedThreadCount());
        rtn.put("peakThreadCount", threadBean.getPeakThreadCount());
        return rtn;
    }

    public static ThreadInfo[] dump(int maxDepth,boolean onlyDeadLock){
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long[] ids ;
        if(onlyDeadLock) {
            ids = threadBean.findDeadlockedThreads();
        }else {
           ids = threadBean.getAllThreadIds();
        }
        if(ids !=null) {
            return threadBean.getThreadInfo(ids, maxDepth);
        }
        else {
            return null;
        }


    }


}

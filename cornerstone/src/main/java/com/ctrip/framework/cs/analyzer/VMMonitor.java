package com.ctrip.framework.cs.analyzer;

import com.ctrip.framework.cs.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.Date;

/**
 * Created by jiang.j on 2017/2/27.
 */
public class VMMonitor implements Closeable{

    static class VMSnapShot{
        private transient boolean _isInitiated = false;
        public boolean isInitiated(){
            return _isInitiated;
        }

        //private Long newGenMinSize;
        private Long ageTableSize;
        private Long[] ageTableSizes;
        //private Long newGenMaxSize;
        //private Long newGenCurSize;
        private Long edenSize;
        private Long edenCapacity;
        private Long edenUsed;
        private Long edenGCTime;
        private Long edenGCEvents;
        private Long survivor0Size;
        private Long survivor0Capacity;
        private Long survivor0Used;
        private Long survivor1Size;
        private Long survivor1Capacity;
        private Long survivor1Used;
        private Long tenuredSize;
        private Long tenuredCapacity;
        private Long tenuredUsed;
        private Long tenuredGCTime;
        private Long tenuredGCEvents;
        private Long permSize;
        private Long permCapacity;
        private Long permUsed;
        private Long metaSize;
        private Long metaCapacity;
        private Long metaUsed;
        private Long tenuringThreshold;
        private Long maxTenuringThreshold;
        private Long desiredSurvivorSize;
        private Long classLoadTime;
        private Long classesLoaded;
        private Long classesUnloaded;
        private Long classBytesLoaded;
        private Long classBytesUnloaded;
        private Long totalCompileTime;
        private Long totalCompile;
        private Long osElapsedTime;
        private Long osFrequency;
        //private Long lastModificationTime;
        private String lastGCCause;
        //private String currentGCCause;
        //private String collector0name;
        //private String collector1name;
    }

    private static final String VM_IDENTIFIER_CLASS_NAME = "sun.jvmstat.monitor.VmIdentifier";
    private static final String MONITORED_HOST_CLASS_NAME = "sun.jvmstat.monitor.MonitoredHost";
    private static final String MONITORED_VM_CLASS_NAME = "sun.jvmstat.monitor.MonitoredVm";
    private static final String MONITOR_CLASS_NAME = "sun.jvmstat.monitor.Monitor";
    private Object VM;
    private static final Logger logger = LoggerFactory.getLogger(VMMonitor.class);
    private MethodHandle detachMH = null;
    private MethodHandle findByNameMH = null;
    private MethodHandle getValueMH = null;

    public synchronized boolean init() {
        try {

            Class<?> vmIdentifierClass = Tools.loadJDKToolClass(VM_IDENTIFIER_CLASS_NAME);
            Class<?> monitoredHostClass = Tools.loadJDKToolClass(MONITORED_HOST_CLASS_NAME);
            Class<?> monitordVmClass = Tools.loadJDKToolClass(MONITORED_VM_CLASS_NAME);
            Class<?> monitorClass = Tools.loadJDKToolClass(MONITOR_CLASS_NAME);


            Constructor constructor = vmIdentifierClass.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            Object vmIdentifier = constructor.newInstance(Tools.currentPID());
            MethodHandle getMonitoredHostMH = MethodHandles.lookup().findStatic(monitoredHostClass, "getMonitoredHost",
                    MethodType.methodType(monitoredHostClass, vmIdentifierClass) );
            Object monitoredHost = getMonitoredHostMH.invoke(vmIdentifier);

            MethodHandle getMonitoredVMMH = MethodHandles.lookup().findVirtual(monitoredHostClass,"getMonitoredVm",
                    MethodType.methodType(monitordVmClass, vmIdentifierClass));

            detachMH = MethodHandles.lookup().findVirtual(monitordVmClass,"detach",MethodType.methodType(void.class));
            findByNameMH = MethodHandles.lookup().findVirtual(monitordVmClass,"findByName",
                    MethodType.methodType(monitorClass,String.class));
            getValueMH = MethodHandles.lookup().findVirtual(monitorClass,"getValue",MethodType.methodType(Object.class));
            VM = getMonitoredVMMH.invoke(monitoredHost,vmIdentifier);


        }catch (Throwable e){
            logger.warn("attach failed!",e);
            return false;
        }
        return true;
    }

    private Long findByName(String name) throws Throwable {
        Object tmp = findByNameMH.invoke(VM, name);
        if(tmp == null)
            return null;

        Object rtn =getValueMH.invoke(tmp);
        if(rtn == null){
            return null;
        }

        return (Long) rtn;
    }
    private String findByNameGetString(String name) throws Throwable {
        return (String) getValueMH.invoke(findByNameMH.invoke(VM, name));
    }
    private void refreshSnapshot() throws Throwable {

        currentSnapshot.osElapsedTime = findByName("sun.os.hrt.ticks");
        currentSnapshot.osFrequency = findByName("sun.os.hrt.frequency");

        currentSnapshot.survivor0Size = findByName("sun.gc.generation.0.space.1.maxCapacity");
        currentSnapshot.survivor0Capacity = findByName("sun.gc.generation.0.space.1.capacity");
        currentSnapshot.survivor0Used = findByName("sun.gc.generation.0.space.1.used");

        currentSnapshot.survivor1Size = findByName("sun.gc.generation.0.space.2.maxCapacity");
        currentSnapshot.survivor1Capacity = findByName("sun.gc.generation.0.space.2.capacity");
        currentSnapshot.survivor1Used = findByName("sun.gc.generation.0.space.2.used");

        currentSnapshot.edenSize = findByName("sun.gc.generation.0.space.0.maxCapacity");
        currentSnapshot.edenCapacity = findByName("sun.gc.generation.0.space.0.capacity");
        currentSnapshot.edenUsed = findByName("sun.gc.generation.0.space.0.used");

        currentSnapshot.tenuredSize = findByName("sun.gc.generation.1.space.0.maxCapacity");
        currentSnapshot.tenuredCapacity = findByName("sun.gc.generation.1.space.0.capacity");
        currentSnapshot.tenuredUsed = findByName("sun.gc.generation.1.space.0.used");

        currentSnapshot.permSize = findByName("sun.gc.generation.2.space.0.maxCapacity");
        currentSnapshot.permCapacity = findByName("sun.gc.generation.2.space.0.capacity");
        currentSnapshot.permUsed = findByName("sun.gc.generation.2.space.0.used");

        currentSnapshot.metaSize = findByName("sun.gc.metaspace.maxCapacity");
        currentSnapshot.metaCapacity = findByName("sun.gc.metaspace.capacity");
        currentSnapshot.metaUsed = findByName("sun.gc.metaspace.used");

        currentSnapshot.edenGCEvents = findByName("sun.gc.collector.0.invocations");
        currentSnapshot.edenGCTime = findByName("sun.gc.collector.0.time");

        currentSnapshot.tenuredGCEvents = findByName("sun.gc.collector.1.invocations");
        currentSnapshot.tenuredGCTime = findByName("sun.gc.collector.1.time");

        currentSnapshot.ageTableSize = findByName("sun.gc.generation.0.agetable.size");
        if (currentSnapshot.ageTableSize != null)
        {
            currentSnapshot.maxTenuringThreshold = findByName("sun.gc.policy.maxTenuringThreshold");
            currentSnapshot.tenuringThreshold = findByName("sun.gc.policy.tenuringThreshold");
            currentSnapshot.desiredSurvivorSize = findByName("sun.gc.policy.desiredSurvivorSize");
            int i = currentSnapshot.ageTableSize.intValue();
            currentSnapshot.ageTableSizes = new Long[i];

            String str = "sun.gc.generation.0.agetable.bytes.";
            for (int j = 0; j < i; j++) {
                if (j < 10) {
                    currentSnapshot.ageTableSizes[j] = findByName(str + "0" + j);
                } else {
                    currentSnapshot.ageTableSizes[j] = findByName(str + j);
                }
            }
        }

        currentSnapshot.classLoadTime = findByName("sun.cls.time");
        currentSnapshot.classesLoaded = findByName("java.cls.loadedClasses");
        currentSnapshot.classesUnloaded = findByName("java.cls.unloadedClasses");
        currentSnapshot.classBytesLoaded = findByName("sun.cls.loadedBytes");
        currentSnapshot.classBytesUnloaded = findByName("sun.cls.unloadedBytes");

        currentSnapshot.totalCompileTime = findByName("java.ci.totalTime");
        currentSnapshot.totalCompile = findByName("sun.ci.totalCompiles");

        /*
        currentSnapshot.newGenMaxSize = findByName("sun.gc.generation.0.maxCapacity");
        currentSnapshot.newGenMinSize = findByName("sun.gc.generation.0.minCapacity");
        currentSnapshot.newGenCurSize = findByName("sun.gc.generation.0.capacity");
        */
        currentSnapshot.lastGCCause = findByNameGetString("sun.gc.lastCause");
        //currentSnapshot.currentGCCause = findByNameGetString("sun.gc.cause");
        //currentSnapshot.collector0name = findByNameGetString("sun.gc.collector.0.name");
        currentSnapshot._isInitiated = true;

    }

    @Override
    public void close() throws IOException {

        if(VM!=null && detachMH!=null) {
            try {
                detachMH.invoke(VM);
                VM=null;
            } catch (Throwable e) {
                logger.warn("detach failed!",e);
            }
        }
    }


    private static VMSnapShot currentSnapshot = new VMSnapShot();
    private static boolean isRunning=false;
    private static Date lastGetSamplerTime=new Date();
    private static Object locker = new Object();
    private static int maxWaitingTime=40000;


    public static void setMaxWaitingTime(int milliseconds){
        maxWaitingTime=milliseconds;
    }

    static void sampler(){
        Thread worker = new Thread("gc-monitor"){
            @Override
            public void run() {
                VMMonitor gcMonitor = new VMMonitor();
                long lastGetDataTime = 0;
                if(gcMonitor.init()){
                    try {
                        while ((new Date().getTime() - lastGetSamplerTime.getTime())< maxWaitingTime) {
                            if(lastGetSamplerTime.getTime() - lastGetDataTime>10000) {
                                lastGetDataTime = new Date().getTime();
                            }

                            gcMonitor.refreshSnapshot();

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }catch (Throwable e){
                        logger.warn("get vm snapshot failed!",e);
                    }finally {
                        try {
                            gcMonitor.close();
                        } catch (IOException e) {
                            logger.warn("close vm monitor failed!",e);
                        }
                        isRunning=false;
                        currentSnapshot = new VMSnapShot();
                    }
                    logger.info("jvm sampler thread stop.");
                }
            }
        };
        worker.start();
        logger.info("jvm sampler thread start.");
        isRunning=true;


    }
    public static VMSnapShot getCurrent(){
        lastGetSamplerTime = new Date();
        synchronized (locker) {
            if (!isRunning) {
                sampler();
            }
        }
        return currentSnapshot;
    }

}

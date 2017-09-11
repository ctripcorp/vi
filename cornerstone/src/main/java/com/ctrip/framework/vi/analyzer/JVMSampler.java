package com.ctrip.framework.vi.analyzer;

import com.ctrip.framework.vi.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jiang.j on 2016/6/15.
 */
public class JVMSampler implements Closeable{

    private final String virtualMachineClassName = "com.sun.tools.attach.VirtualMachine";
    private final String hotspotVMName = "sun.tools.attach.HotSpotVirtualMachine";
    private Object VM;
    private Method heapHistoMethod;
    private Method detachMethod;
    private static Class<?> vmProviderClass=null;
    private static final Logger logger = LoggerFactory.getLogger(JVMSampler.class);

    private boolean init() {
        try {

            Class<?> vmClass = Tools.loadJDKToolClass(virtualMachineClassName);
            Class<?> hotspotVMClass = Tools.loadJDKToolClass(hotspotVMName);
            String pid = Tools.currentPID();
            if(vmProviderClass!=null){

                Object vmProvider = vmProviderClass.newInstance();
                VM=vmProviderClass.getMethod("attachVirtualMachine",String.class).invoke(vmProvider, pid);
                heapHistoMethod = VM.getClass().getMethod("heapHisto", Object[].class);
                detachMethod = VM.getClass().getMethod("detach");
            }else {
                Method attacheMethod = vmClass.getMethod("attach", String.class);
                VM = attacheMethod.invoke(null, pid);
                vmProviderClass = vmClass.getMethod("provider").invoke(VM).getClass();
                heapHistoMethod = hotspotVMClass.getMethod("heapHisto", Object[].class);
                detachMethod = vmClass.getMethod("detach");
            }
        }catch (Throwable e){
            logger.warn("attach failed!",e);
            return false;
        }
        return true;
    }

    private List<Object[]> getHeapHisto() {
        List<Object[]> rtn = new ArrayList<>();
        if(VM!=null && heapHistoMethod!=null) {
            try (InputStream in = (InputStream) heapHistoMethod.invoke(VM, new Object[]{new Object[]{"-all"}})){
                if(in!=null) {
                    try(final InputStreamReader reader = new InputStreamReader(in, Charset.forName("UTF-8"))) {

                        int readChar;
                        boolean isItem = false;

                        StringBuilder sb = new StringBuilder();
                        while ((readChar = reader.read()) >= 0) {

                            if ((char) readChar == '\n') {
                                if (isItem) {
                                    String[] raw = sb.toString().trim().split("\\s+");
                                    Object[] classInfo = new Object[3];
                                    classInfo[0] = raw[3].replace('[', '#');
                                    classInfo[1] = Long.parseLong(raw[1]);//count
                                    classInfo[2] = Long.parseLong(raw[2]);//bytes
                                    rtn.add(classInfo);
                                }
                                sb.delete(0, sb.length());
                                isItem = false;

                            } else {
                                if ((char) readChar == ':') {
                                    isItem = true;
                                }
                                sb.append((char) readChar);
                            }

                        }
                    }
                }
            } catch (Throwable e) {
                logger.warn("get heaphisto failed!", e);
            }
        }
        return  rtn;
    }

    @Override
    public void close() throws IOException {

        if(VM!=null && detachMethod!=null) {
            try {
                detachMethod.invoke(VM);
                VM=null;
            } catch (Throwable e) {
                logger.warn("detach failed!",e);
            }
        }
    }


    private static List<Object[]> currentInstanceInfo;
    private static boolean isRunning=false;
    private static Date lastGetSamplerTime=new Date();
    private static Object locker = new Object();
    private static int maxWaitingTime=40000;


    public static void setMaxWaitingTime(int milliseconds){
        maxWaitingTime=milliseconds;
    }

    static void sampler(){
        Thread worker = new Thread("jvm-sampler"){
            @Override
            public void run() {
                JVMSampler sampler = new JVMSampler();
                long lastGetDataTime = 0;
                if(sampler.init()){
                    try {
                        while ((new Date().getTime() - lastGetSamplerTime.getTime())< maxWaitingTime) {
                            if(lastGetSamplerTime.getTime() - lastGetDataTime>10000) {
                                currentInstanceInfo = sampler.getHeapHisto();
                                lastGetDataTime = new Date().getTime();
                            }
                            if (currentInstanceInfo.isEmpty()) {
                                break;
                            }

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }finally {
                        try {
                            sampler.close();
                        } catch (IOException e) {
                            logger.warn("close jvm sampler failed!",e);
                        }
                        isRunning=false;
                        currentInstanceInfo=null;
                    }
                    logger.info("jvm sampler thread stop.");
                }
            }
        };
        worker.start();
        logger.info("jvm sampler thread start.");
        isRunning=true;


    }
    public static List<Object[]> getCurrentHeapHisto(){
        lastGetSamplerTime = new Date();
        synchronized (locker) {
            if (!isRunning) {
                sampler();
            }
        }
        return currentInstanceInfo;
    }

}

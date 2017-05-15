package com.ctrip.framework.cornerstone.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jiang.j on 2017/1/3.
 */
public class VIThreadFactory implements ThreadFactory {

    private final String namePrefix;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    public VIThreadFactory(String prefix){

        namePrefix = prefix+"-";
    }
    @Override
    public Thread newThread(Runnable r) {
        Thread rtn = new Thread(r,namePrefix+threadNumber.getAndIncrement());
        rtn.setDaemon(true);
        return rtn;
    }
}

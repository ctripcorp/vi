package com.ctrip.framework.vi.code;


/**
 * Created by jiang.j on 2017/4/27.
 */
public class ProfilerManager {

    public void start(){
        long l = System.nanoTime();
        if(l==1000)
        System.out.print(l);
    }

    public void testA(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("hello world!");
            }
        };
        runnable.run();
    }

}

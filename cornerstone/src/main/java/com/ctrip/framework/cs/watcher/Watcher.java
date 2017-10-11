package com.ctrip.framework.cs.watcher;

/**
 * Created by jiang.j on 2016/12/16.
 */
public class Watcher {

    public interface Executor{

       Object execute(Object... params);
    }
    public static Object doInWatch(Executor executor,Object... params){
        return executor.execute(params);
    }

}

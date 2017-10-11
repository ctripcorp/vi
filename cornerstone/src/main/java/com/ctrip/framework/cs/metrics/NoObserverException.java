package com.ctrip.framework.cs.metrics;

/**
 * Created by jiang.j on 2016/7/28.
 */
public class NoObserverException extends Exception {

    public NoObserverException(String observerId){
        super("metrics observerId [" + observerId+ "] not found! please remonitor the metrics!");
    }
}

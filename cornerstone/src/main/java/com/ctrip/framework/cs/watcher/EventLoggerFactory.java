package com.ctrip.framework.cs.watcher;

import com.ctrip.framework.cs.annotation.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by jiang.j on 2016/10/18.
 */
public class EventLoggerFactory {

    private static ConcurrentMap<Class<? extends EventLogger>,Boolean> _eventLoggers = new ConcurrentHashMap<>();
    private static Map<Integer,EventLogger> _classEventLoggers = new ConcurrentHashMap<>();

    private static Logger _logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public synchronized static boolean addLogger(Class<? extends EventLogger> clazz,boolean canLogTrans){
       boolean rtn = _eventLoggers.put(clazz,canLogTrans)==null;
        updateCurrentLogger(clazz,canLogTrans);

        return rtn;
    }
    public synchronized static boolean addLogger(Class<? extends EventLogger> clazz){
        return addLogger(clazz,false);
    }

    private static void updateCurrentLogger(Class<? extends EventLogger> clazz,boolean canLogTrans){

        for(EventLogger logger:_classEventLoggers.values()){
            if(logger instanceof  EventProxy){
                EventProxy proxy = (EventProxy) logger;
                if(!canLogTrans && proxy.isTrans()){
                    continue;
                }

                boolean hasThis = false;
                List<EventLogger> loggers = proxy.getLoggers();
                for(EventLogger eventLogger:loggers){

                    if(clazz.isAssignableFrom(eventLogger.getClass())){
                        hasThis = true;
                        break;
                    }
                }

                if(!hasThis){
                    try {
                        loggers.add(clazz.newInstance());
                    } catch (Throwable e) {
                        _logger.warn("initiate event logger failed",e);
                    }
                }

            }
        }

    }
    private static class EventProxy implements EventLogger{

        List<EventLogger> loggers = new ArrayList<>();
        private String typeName;
        private boolean trans;


        public EventProxy(boolean isTrans,Class<?> belongClass){
            this(isTrans,belongClass,null);
        }

        public List<EventLogger> getLoggers(){
            return this.loggers;
        }

        public boolean isTrans(){
           return this.trans;
        }

        public EventProxy(boolean isTrans,Class<?> belongClass,String type){

            this.trans = isTrans;
            if(type != null){
                typeName = type;
            }else {
                EventSource eventSource = belongClass.getAnnotation(EventSource.class);
                if (eventSource != null) {
                    typeName = eventSource.name();
                } else {
                    typeName = belongClass.getName();
                }
            }
            for(Map.Entry<Class<? extends EventLogger>,Boolean> entry : _eventLoggers.entrySet()){

                if((isTrans && entry.getValue()) || !isTrans ) {
                    try {
                        loggers.add(entry.getKey().newInstance());
                    } catch (Throwable e) {
                        _logger.error("create new eventlogger failed!", e);
                    }
                }
            }

        }
        @Override
        public void fireEvent(final String message, final Object... args) {

            try {
                for (EventLogger eventLogger : loggers) {
                    try {
                        eventLogger.fireEvent(message, typeName, args);
                    } catch (Throwable e) {
                        _logger.error("logger fire event failed!", e);
                    }
                }
            }catch (Throwable e){
                _logger.error("fire event failed!",e);
            }
        }
    }
    public static EventLogger getLogger(Class clazz){
        return getLogger(clazz,null);
    }
    public static EventLogger getLogger(Class clazz,String typeName){

        int key = clazz.hashCode();
        if( _classEventLoggers.containsKey(key)){
            return _classEventLoggers.get(key);
        }else{
            _classEventLoggers.put(key,new EventProxy(false,clazz,typeName));
            return _classEventLoggers.get(key);
        }
    }

    public static EventLogger getTransLogger(Class clazz){
        return new EventProxy(true,clazz);
    }

}

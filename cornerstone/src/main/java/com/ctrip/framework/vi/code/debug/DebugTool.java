package com.ctrip.framework.vi.code.debug;

import com.ctrip.framework.vi.util.MiserJsonTreeWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiang.j on 2017/7/12.
 */
final public class DebugTool {

    static Logger logger = LoggerFactory.getLogger(DebugTool.class);
    public final static String STACKKEY = "#stack";
    static String traceIdKey = "traceId";
    final static Map<String,Map<String,Object>> container;
    final static Map<String,String> traceIdContainer = new ConcurrentHashMap<>();
    static {
        logger.info("init debugTool");
        container  =new ConcurrentHashMap<>();
    }

    public static void setThreadTraceId(String traceId){
        if(traceId != null) {
            traceIdContainer.put(String.valueOf(Thread.currentThread().getId()), traceId);
        }
    }

    public static boolean needMonitor(String traceId){
        if(traceId == null){
            return false;
        }

        String currentTraceId = traceIdContainer.remove(String.valueOf(Thread.currentThread().getId()));
        return traceId.equals(currentTraceId);
    }


    public static String getTraceIdKey(){
        return traceIdKey;
    }

    public static void setTraceIdKey(String newKey){
        traceIdKey = newKey;
    }

    public static void log(String lineId,Map<String,Object> vars){

        try {
            Gson gson = new Gson();
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

            for(Map.Entry<String,Object> e:vars.entrySet()){
                try {

                    Object val = e.getValue();
                    if(val != null) {
                        MiserJsonTreeWriter jsonTreeWriter = new MiserJsonTreeWriter();
                        gson.toJson(val, val.getClass(), jsonTreeWriter);
                        e.setValue(jsonTreeWriter.get());
                    }
                }catch (Throwable throwable){
                    e.setValue(throwable.getMessage());
                    logger.error("serialize object "+ e.getKey() +" to json failed!",throwable);

                }
            }

            if (stackTraceElements.length > 2) {
                stackTraceElements = Arrays.copyOfRange(stackTraceElements, 2, stackTraceElements.length - 1);
                vars.put(STACKKEY, stackTraceElements);
            }
            container.put(lineId, vars);
        }catch (Throwable e){
            logger.warn("log debug info failed!",e);
        }
    }

    public static Map<String, Object> removeTraceInfo(String traceId){
        return container.remove(traceId);
    }

    public static Map<String,Object> viewCurrentTrace(String traceId){
        return container.get(traceId);
    }

    public static int getPrivateFieldInt(Object value,String className,String fname) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Object rtn = getPrivateFieldValue(value,className,fname);
        if(rtn instanceof Boolean){
            if((boolean) rtn){
                return 1;
            }else{
                return 0;
            }

        }else {
            return (int) rtn;
        }
    }

    public static double getPrivateFieldDouble(Object value,String className,String fname) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return (double)getPrivateFieldValue(value,className,fname);
    }

    public static float getPrivateFieldFloat(Object value,String className,String fname) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return (float)getPrivateFieldValue(value,className,fname);
    }

    public static long getPrivateFieldLong(Object value,String className,String fname) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return (long)getPrivateFieldValue(value,className,fname);
    }

    public static String getPrivateFieldString(Object value,String className,String fname) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return (String)getPrivateFieldValue(value,className,fname);
    }


    public static Object getPrivateFieldValue(Object value,String className,String fname){
        try {
            Field f = value.getClass().getDeclaredField(fname);
            f.setAccessible(true);
            return f.get(value);
        }catch (Throwable e){
            logger.error(e.getMessage());
            return null;
        }

    }
}

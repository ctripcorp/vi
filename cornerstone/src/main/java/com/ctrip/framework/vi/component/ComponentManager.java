package com.ctrip.framework.cornerstone.component;

import com.ctrip.framework.cornerstone.annotation.ComponentStatus;
import com.ctrip.framework.cornerstone.annotation.FieldInfo;
import com.ctrip.framework.cornerstone.component.defaultComponents.HostInfo;
import com.ctrip.framework.cornerstone.jmx.VIDynamicMBean;
import com.ctrip.framework.cornerstone.util.ArrayUtils;
import com.ctrip.framework.cornerstone.util.ServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiang.j on 2016/3/30.
 */
public final class ComponentManager {

    private static ConcurrentHashMap<String,Class<?>> container = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String,Object> instances = new ConcurrentHashMap<>();
    private static Logger logger = LoggerFactory.getLogger(ComponentManager.class);
    private static Map<String,String> overrideFieldDescription = new ConcurrentHashMap<>();

    private static Class<? extends HostInfo> hostInfoExtend =null;

    public static void setFieldDescription(String fieldId,String description){
        overrideFieldDescription.put(fieldId.toLowerCase(),description);
    }

    public static void setHostInfoExtend(Class<? extends HostInfo> extend){
        hostInfoExtend = extend;
        try {
                instances.put(HostInfo.class.getName(),hostInfoExtend.newInstance());
        } catch (Throwable e) {
            logger.warn("new hostInfoExtend failed, class:" + hostInfoExtend.getName(), e);
        }
    }
    public static synchronized boolean add(Class<?> cb){
        ComponentStatus comp =  cb.getAnnotation(ComponentStatus.class);
        if(comp!=null) {
            String compId =comp.id().toLowerCase();
                if(container.putIfAbsent(compId, cb)==null) {
                    if (comp.singleton()) {
                        try {
                            instances.putIfAbsent(cb.getName(), cb.newInstance());
                        } catch (Throwable e) {
                            logger.error("init component status failed", e);
                            return false;
                        }
                    }
                }
        }else{
            return false;
        }

        if(comp.jmx()){
            logger.info(comp.id() + " register self to jmx");
            final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            try {
                server.registerMBean(new VIDynamicMBean(cb), new ObjectName("VI:type=" + ServerConnector.getPort()
                         + (HostInfo.isTomcat()?System.getProperty("vi.context.path"):"") + ",name=" +cb.getSimpleName()));
            } catch (InstanceAlreadyExistsException e) {
                logger.info("VI JMX MBeans "+cb.getSimpleName() + " already exist!");
            }catch (Throwable e) {
                logger.warn("VI JMX MBeans error",e);
            }

            logger.info(comp.id() + " finish register!");
        }
        return true;
    }

    @Deprecated
    public static synchronized void register(Class<?> cb){
        add(cb);
    }

    public static <T> T getStatus(Class<T> statusClass){
        if(!container.values().contains(statusClass)) {
          add(statusClass);
        }


        T rtn = (T) instances.get(statusClass.getName());
        if(rtn == null){
            try {
                rtn = statusClass.newInstance();
            } catch (Throwable e) {
                logger.error("init component status failed", e);
            }
        }else if(rtn instanceof  Refreshable) {
            ((Refreshable)rtn).refresh();
        }

        return rtn;
    }


    static Map<String,Class<?>> getAllComponents(){
        return container;
    }

    static List<Map<String,String>> getComponentMeta(){

        List<Map<String,String>> metas = new ArrayList<>();

        for (Class<?> component : container.values() ){

            ComponentStatus comp =  component.getAnnotation(ComponentStatus.class);
            if(comp!=null){

                Map<String,String> meta = new HashMap<>();
                meta.put("id",comp.id().toLowerCase());
                meta.put("name",comp.name());
                meta.put("list", String.valueOf(comp.list()));
                meta.put("custom", String.valueOf(comp.custom()));
                meta.put("description",comp.description());
                metas.add(meta);
            }
        }

        return metas;

    }


    static List<Map<String,String>> getFieldMeta(){

        List<Map<String,String>> metas = new ArrayList<>();

        for (Class<?> component : container.values() ){

            ComponentStatus comp =  component.getAnnotation(ComponentStatus.class);
            if(comp==null) {
                continue;
            }


            Field[] fields;

            if(component.getGenericSuperclass() instanceof  ParameterizedType){
                ParameterizedType parameterizedType = (ParameterizedType) component.getGenericSuperclass();
                fields = parameterizedType.getActualTypeArguments()[0].getClass().getDeclaredFields();
            }else{
                fields = component.getDeclaredFields();
            }
            try {
                if (hostInfoExtend != null && component.equals(HostInfo.class)) {
                    fields = ArrayUtils.concatenate(fields, hostInfoExtend.getDeclaredFields());
                }
            }catch (Throwable e){
                logger.warn("concatenate hostinfo field failed!",e);
            }
            for(Field field : fields) {
                if(Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())){
                    continue;
                }

                Map<String, String> meta = new HashMap<>();
                String fieldId =comp.id().toLowerCase()+"."+field.getName();
                meta.put("id", fieldId);
                FieldInfo finfo = field.getAnnotation(FieldInfo.class);
                String name = field.getName();

                String type = String.valueOf(FieldInfo.FieldType.Txt);
                if(finfo!=null) {
                    name = finfo.name();
                    meta.put("description", finfo.description());
                    if(finfo.type()== FieldInfo.FieldType.Txt && isNumberType(field.getType())){
                        type=String.valueOf(FieldInfo.FieldType.Number);
                    }else {
                        type = String.valueOf(finfo.type());
                    }
                }

                if(overrideFieldDescription.containsKey(fieldId.toLowerCase())){
                    meta.put("description",overrideFieldDescription.get(fieldId.toLowerCase()));
                }

                meta.put("name", name);
                meta.put("type", type);
                metas.add(meta);
            }

        }

        return metas;

    }

    private static boolean isNumberType(Class<?> fieldType){
         if(Number.class.isAssignableFrom(fieldType)){
             return true;
         }else if(fieldType.isPrimitive() &&
                 (fieldType == int.class || fieldType == long.class || fieldType == float.class || fieldType == double.class || fieldType == short.class)){
             return true;
         }
        return false;
    }

}

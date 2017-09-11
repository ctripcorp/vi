package com.ctrip.framework.vi.jmx;

import com.ctrip.framework.vi.annotation.ComponentStatus;
import com.ctrip.framework.vi.annotation.FieldInfo;
import com.ctrip.framework.vi.component.ComponentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.openmbean.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * Created by jiang.j on 2016/10/12.
 */
public class VIDynamicMBean implements DynamicMBean {
    private Class<?> _beanClass;
    private Object _instance;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Map<String,OpenType<?>> listFieldMap = new HashMap<>();
    public VIDynamicMBean(Class<?> beanClass){

        this._beanClass = beanClass;
    }
    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        _instance = ComponentManager.getStatus(_beanClass);

        Object rtn=null;
        Class<?> beanClass = this._beanClass;

        try {
            if(Map.class.isAssignableFrom(beanClass) && attribute.equals("value")){
                    Map<String,Object> data = (Map<String, Object>) _instance;
                    String[] keys = data.keySet().toArray(new String[data.keySet().size()]);
                    OpenType<?>[] openTypes = new OpenType<?>[data.keySet().size()];
                    Arrays.fill(openTypes,JMXTypeFactory.getMapValueOpenType(beanClass));

                    CompositeType compositeType = new CompositeType("map","map",keys,keys, openTypes);
                    CompositeData compositeData = new CompositeDataSupport(compositeType,data);

                    return compositeData;
            }else {
                Field field = beanClass.getDeclaredField(attribute);
                field.setAccessible(true);
                rtn = field.get(_instance);
                if(listFieldMap.containsKey(attribute)){
                    TabularType tabularType = (TabularType) listFieldMap.get(attribute);
                    TabularData result = new TabularDataSupport((TabularType) listFieldMap.get(attribute));

                    for(Object val : (List<Object>)rtn){
                        Map<String,Object> data = new HashMap<>();
                        for(String itemName:tabularType.getIndexNames()){
                            Field itemField = val.getClass().getDeclaredField(itemName);
                            itemField.setAccessible(true);
                            data.put(itemName,itemField.get(val));
                        }
                        result.put(new CompositeDataSupport(tabularType.getRowType(),data));
                    }

                    return result;
                }
                if (field.getType().isEnum()) {
                    rtn = String.valueOf(rtn);
                }
            }
        } catch (Throwable e) {
            logger.warn("get component status attribute failed!", e);
        }
        return  rtn;
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {

    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        return null;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        return null;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        ComponentStatus comp =  _beanClass.getAnnotation(ComponentStatus.class);
        if(comp==null) {
            return null;
        }
        Class<?> beanClass = this._beanClass;
        ArrayList<MBeanAttributeInfo> attributeInfos = new ArrayList<>();

        if(Map.class.isAssignableFrom(beanClass)){

            attributeInfos.add(new MBeanAttributeInfo("value", beanClass.getName(), beanClass.getName(), true, false, false));
        }else {
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                FieldInfo finfo = field.getAnnotation(FieldInfo.class);
                String name = field.getName();
                String type = field.getType().getName();
                Class<?> fieldClass = field.getType();
                String description = "";
                if (finfo != null) {
                    description = finfo.description();
                }

                Class<?> realClass = null;
                if(List.class.isAssignableFrom(fieldClass) && (field.getGenericType() instanceof ParameterizedType)){

                    ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                    realClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                    if(realClass.isPrimitive() || realClass.getName().startsWith("java.lang.")){

                        realClass = null;
                    }
                }

                if(realClass!=null){
                    try {
                        description = realClass.getName();
                        TabularType tabularType =JMXTypeFactory.getTabularType(name,description,realClass);
                        listFieldMap.put(name,tabularType);
                        attributeInfos.add(new OpenMBeanAttributeInfoSupport(name,description,tabularType, true, false, false));
                    } catch (OpenDataException e) {
                        logger.warn("get Mbean info failed!",e);
                    }
                }
                else {
                    attributeInfos.add(new MBeanAttributeInfo(name, type, description, true, false, false));
                }

            }
        }

        return new MBeanInfo(_beanClass.getName(),comp.description(),attributeInfos.toArray(new MBeanAttributeInfo[attributeInfos.size()]),null,null,null);
    }
}

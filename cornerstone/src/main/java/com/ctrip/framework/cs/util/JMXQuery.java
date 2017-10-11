package com.ctrip.framework.cs.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Attribute;
import javax.management.openmbean.CompositeDataSupport;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by jiang.j on 2016/6/6.
 */
public class JMXQuery {

    public static final String CATALINA="Catalina";
    public static final String JAVALANG="java.lang";
    MBeanServer mbs;
    Logger _logger = LoggerFactory.getLogger(this.getClass());
    public JMXQuery(){

        try{
             mbs = ManagementFactory.getPlatformMBeanServer();
        }catch (Throwable e){
            _logger.warn("get tomcat run info failed",e);

        }
    }

    public List<Map<String,Object>> query(String category,String typeName,String[] attributes){
        return query(category,null,typeName,attributes);
    }

    public List<Map<String,Object>> query(String category,String objNamePattern,String typeName,String[] attributes){

        List<Map<String,Object>> rtn = new ArrayList<>();
        final ObjectName pool;
        try {
            pool = new ObjectName(category + ":type=" + typeName + ",*");
        }catch (Throwable e){
            _logger.warn("wrong jmx type!",e);
            return null;
        }
        final Set<ObjectName> names = mbs.queryNames(pool, null);
        if (names == null) {
            return null;
        }

        for (ObjectName name : names) {
            if(objNamePattern!=null && !Pattern.matches(objNamePattern,name.getCanonicalName())){
                continue;
            }
            Map<String,Object> attrs = new HashMap<>();
            try {
                AttributeList list = mbs.getAttributes(name, attributes);
                for (Attribute a : list.asList()) {
                    Object attrVal = a.getValue();
                    if(attrVal instanceof CompositeDataSupport)
                    {
                        CompositeDataSupport dataSupport = (CompositeDataSupport) attrVal;
                        attrs.put(a.getName(),dataSupport.values());

                    }else {
                        attrs.put(a.getName(), attrVal);
                    }
                }
                rtn.add(attrs);
            }catch (Throwable e){
                _logger.warn("get jmx object attribute failed!",e);
                continue;
            }
        }

        return rtn;

    }
}

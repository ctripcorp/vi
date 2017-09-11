package com.ctrip.framework.vi.component.defaultComponents;

import com.ctrip.framework.vi.annotation.ComponentStatus;

import java.util.HashMap;

/**
 * Created by jiang.j on 2016/5/5.
 */
@ComponentStatus(id="vi.systemproperties",name = "current system properties",description = "当前系统参数")
public class SystemProperties  extends HashMap<String, String> {
    public SystemProperties(){
        for(Object key :System.getProperties().keySet()){
           this.put((String) key,System.getProperty(String.valueOf(key)));
        }
    }
}

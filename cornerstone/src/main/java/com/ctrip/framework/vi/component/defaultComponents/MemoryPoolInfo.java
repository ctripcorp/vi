package com.ctrip.framework.vi.component.defaultComponents;

import com.ctrip.framework.vi.annotation.ComponentStatus;
import com.ctrip.framework.vi.util.JMXQuery;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by jiang.j on 2016/6/3.
 */
@ComponentStatus(id = "vi.memorypoolinfo",name = "jvm memory pool info",description = "jvm内存池监控",custom = true)
public class MemoryPoolInfo extends ArrayList<Map<String,Object>> {

    public MemoryPoolInfo(){
        JMXQuery jmxQuery = new JMXQuery();
        this.addAll(jmxQuery.query(JMXQuery.JAVALANG,"MemoryPool",new String[]{"Name","Usage"}));
    }
}

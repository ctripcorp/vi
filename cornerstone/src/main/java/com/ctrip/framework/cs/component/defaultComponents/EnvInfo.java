package com.ctrip.framework.cs.component.defaultComponents;

import com.ctrip.framework.cs.annotation.ComponentStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang.j on 2016/4/21.
 */
@ComponentStatus(id="vi.envinfo",name = "current system environment",description = "当前系统环境参数")
public class EnvInfo extends HashMap<String, String> {
    public EnvInfo(){
        Map<String,String> envs =System.getenv();
        for(String key:envs.keySet()) {
            this.put(key, envs.get(key));
        }

    }
}

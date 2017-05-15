package com.ctrip.framework.cornerstone.helloworld;

import com.ctrip.framework.cornerstone.cacheRefresh.CacheCell;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang.j on 2016/5/17.
 */
public class Memcache implements CacheCell {
    private String id;
    Map<String,Object> rtn = new HashMap<>();
    public Memcache(String id){
        this.id = id;
        rtn.put("refreshTime",new Date());
        rtn.put("hitCount",1233);
        rtn.put("lastOperator","ctrip");
    }
    @Override
    public String id() {
        return this.id;
    }

    @Override
    public boolean refresh() {
        rtn.put("refreshTime",new Date());
        return true;
    }

    @Override
    public Map<String, Object> getStatus() {
        return  rtn;
    }
}

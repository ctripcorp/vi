package com.ctrip.framework.cornerstone;

import com.ctrip.framework.cornerstone.cacheRefresh.CacheCell;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang.j on 2016/5/17.
 */
public class RedisCache implements CacheCell {
    private String id;
    public RedisCache(String id){
        this.id =id;

    }
    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public Map<String, Object> getStatus() {
        HashMap<String,Object> rtn = new HashMap<>();
        rtn.put("hello",id);
        rtn.put("ni","dddd");
        return rtn;
    }
}

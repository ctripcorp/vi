package com.ctrip.framework.vi.helloworld;

import com.ctrip.framework.vi.cacheRefresh.CacheCell;

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

    @Override
    public Object getByKey(String key) {
        return null;
    }

    @Override
    public Iterable<String> keys() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }
}

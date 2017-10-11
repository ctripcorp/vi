package com.ctrip.framework.cs.cacheRefresh;

import java.util.Map;

/**
 * Created by jiang.j on 2016/5/12.
 */
public interface CacheCell {
    String id();
    boolean refresh();
    Map<String,Object> getStatus();
    Object getByKey(String key);
    Iterable<String> keys();
    int size();

}

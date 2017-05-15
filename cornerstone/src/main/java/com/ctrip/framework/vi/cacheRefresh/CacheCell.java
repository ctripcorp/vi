package com.ctrip.framework.cornerstone.cacheRefresh;

import java.util.Map;

/**
 * Created by jiang.j on 2016/5/12.
 */
public interface CacheCell {
    String id();
    boolean refresh();
    Map<String,Object> getStatus();
}

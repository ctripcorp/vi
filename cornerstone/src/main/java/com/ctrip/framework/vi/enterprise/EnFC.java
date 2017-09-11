package com.ctrip.framework.vi.enterprise;

import com.ctrip.framework.vi.HasPermission;
import java.util.Map;

/**
 * Created by jiang.j on 2016/4/29.
 */
public interface EnFC extends HasPermission{
    boolean isFeatureEnable(String key);
    void setFeatures(Map<String,Boolean> features,String user);
    Map<String,Boolean> allFeatures();
    Map<String,String> allCustomRemarks();
}

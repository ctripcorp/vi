package com.ctrip.framework.vi;

import com.ctrip.framework.vi.fc.FCManager;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2016/4/26.
 */
public class FCManagerTest {
    @Test
    public void testLoadFeatures(){

    }

    @Test
    public void testNoneKey(){

        assertTrue(!FCManager.isFeatureEnable("none.exist"));
    }
}

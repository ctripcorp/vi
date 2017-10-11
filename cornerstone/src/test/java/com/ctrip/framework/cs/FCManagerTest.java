package com.ctrip.framework.cs;

import com.ctrip.framework.cs.fc.FCManager;
import org.junit.Test;

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

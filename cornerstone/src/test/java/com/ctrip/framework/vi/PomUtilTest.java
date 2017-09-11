package com.ctrip.framework.vi;

import com.ctrip.framework.vi.util.PomUtil;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2016/5/30.
 */
public class PomUtilTest {
    @Test
    public  void getPomInfoTest(){
            String[] av = PomUtil.getArtifactIdAndVersion("tomcat-jdbc");
        assertTrue(av == null);

    }
}

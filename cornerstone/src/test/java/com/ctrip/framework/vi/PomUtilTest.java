package com.ctrip.framework.cornerstone;

import com.ctrip.framework.cornerstone.util.PomUtil;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by jiang.j on 2016/5/30.
 */
public class PomUtilTest {
    @Test
    public  void getPomInfoTest(){
            String[] av = PomUtil.getArtifactIdAndVersion("tomcat-jdbc");
        Assert.assertTrue(av==null);

    }
}

package com.ctrip.framework.cornerstone;

import com.ctrip.framework.cornerstone.util.DesUtil;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by jiang.j on 2016/5/3.
 */
public class DesUtilTest {
    @Test
    public void testEncrypt(){
        String key="somekey09!@%%$";
        String data = "hello world";
        try {
            Assert.assertEquals(data,DesUtil.decrypt(DesUtil.encrypt(data,key),key));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

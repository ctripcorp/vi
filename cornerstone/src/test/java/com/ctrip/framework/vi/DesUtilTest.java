package com.ctrip.framework.vi;

import com.ctrip.framework.vi.util.DesUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jiang.j on 2016/5/3.
 */
public class DesUtilTest {
    @Test
    public void testEncrypt(){
        String key="somekey09!@%%$";
        String data = "hello world";
        try {
            assertEquals(data, DesUtil.decrypt(DesUtil.encrypt(data, key), key));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

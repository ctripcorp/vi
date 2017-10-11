package com.ctrip.framework.cs;

import org.junit.Test;
import java.lang.reflect.Field;

/**
 * Created by jiang.j on 2016/4/27.
 */
public class ConfigTest {
    @Test
    public void testInjectValue(){
        Class<?> myclass = TestConfigBean.class;
        Field[] fileds =myclass.getDeclaredFields();

        for (int i = 0; i < fileds.length; i++) {

            System.out.println(fileds[i].getName());
        }

        System.out.println(TestConfigBean.getTestKey());
        Field field = fileds[0];
        field.setAccessible(true);
        try {
            field.set(null,"myname");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        System.out.println(TestConfigBean.getTestKey());
    }
}

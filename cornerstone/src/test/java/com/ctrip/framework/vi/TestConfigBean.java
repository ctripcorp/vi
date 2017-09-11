package com.ctrip.framework.vi;

import com.ctrip.framework.vi.annotation.Config;
import com.ctrip.framework.vi.annotation.FieldValidation;
import com.ctrip.framework.vi.util.Predicate;


/**
 * Created by jiang.j on 2016/4/12.
 */
@Config(name = "test")
public class TestConfigBean {
@FieldValidation(validator = TestKeyValidator.class,errorMsg = "")
private static String TestKey="chinese";
    public static String getTestKey(){
        return TestKey;
    }

    public class TestKeyValidator implements Predicate<String> {
        @Override
        public boolean test(String s) {
            return false;
        }
    }

}

package com.ctrip.framework.vi.annotation;

import com.ctrip.framework.vi.util.Predicate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jiang.j on 2016/4/12.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface FieldValidation {
    Class<? extends Predicate> validator();
    String errorMsg();
}

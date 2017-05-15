package com.ctrip.framework.cornerstone.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jiang.j on 2016/3/31.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ComponentStatus {
    /**
     * the value indicating a version number since this member
     * or type has been present.
     */
    String id();
    String name();
    String description();
    boolean custom() default false;
    boolean list() default false;
    boolean singleton() default false;
    boolean jmx() default false;
}


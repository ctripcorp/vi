package com.ctrip.framework.cs.util;

/**
 * Created by jiang.j on 2016/4/25.
 */
public interface Predicate<T> {
    boolean test(T t);
}

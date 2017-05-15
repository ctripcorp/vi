package com.ctrip.framework.cornerstone.util;

import java.lang.reflect.Array;

/**
 * Created by jiang.j on 2016/8/23.
 */
public class ArrayUtils {

 public static <T> boolean contains(final T[] array, final T v) {
    if (v == null) {
        for (final T e : array) {
            if (e == null)
                return true;
        }
    } else {
        for (final T e : array) {
            if (e == v || v.equals(e))
                return true;
        }
    }

    return false;
}
    public static <T> T[] concatenate(final T[] a, final T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] rtn = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, rtn, 0, aLen);
        System.arraycopy(b, 0, rtn, aLen, bLen);

        return rtn;
    }
}

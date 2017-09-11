package com.ctrip.framework.vi.util;

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
            if (e.equals(v) || v.equals(e))
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

    public static int[] mergeSortedArray(int[] a,int[] b){
        int[] rtn = new int[a.length+b.length];
        int i = a.length -1, j= b.length - 1, k=rtn.length;

        while (k>0){
           rtn[--k] = (j<0 || (i >=0 && a[i] >= b[j])) ? a[i--]:b[j--];
        }
        return rtn;
    }

    public static int[] subtractSortedArray(int[] source,int[] part){

        int[] left = new int[source.length-part.length];

        int k=0, last = part[part.length-1] ,first = part[0];

        for (int aSource : source) {
            boolean needDel = false;
            if(aSource>first && aSource<last ) {
                for (int aPart : part) {
                    if (aSource == aPart) {
                        needDel = true;
                        break;
                    }
                }
            }else if(aSource == first || aSource == last){
                needDel = true;
            }
            if (!needDel) {
                left[k++] = aSource;
            }
        }

        return left;
    }
}

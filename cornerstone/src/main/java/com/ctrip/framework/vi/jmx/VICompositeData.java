package com.ctrip.framework.cornerstone.jmx;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import java.util.Collection;

/**
 * Created by jiang.j on 2016/10/13.
 */
public class VICompositeData implements CompositeData {
    @Override
    public CompositeType getCompositeType() {
        return null;
    }

    @Override
    public Object get(String key) {
        return null;
    }

    @Override
    public Object[] getAll(String[] keys) {
        return new Object[0];
    }

    @Override
    public boolean containsKey(String key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Collection<?> values() {
        return null;
    }
}

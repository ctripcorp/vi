package com.ctrip.framework.cs.code.debug;

/**
 * @author keli.wang
 * Modified by jiang.j
 */
public class ClassField {
    private final int access;
    private final String name;
    private final String desc;

    public ClassField(final int access, final String name, final String desc) {
        this.access = access;
        this.name = name;
        this.desc = desc;
    }

    public int getAccess() {
        return access;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return "access:"+access+", name:"+name+", desc:"+desc;
    }

}

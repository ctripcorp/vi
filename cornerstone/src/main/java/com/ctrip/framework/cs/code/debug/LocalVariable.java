package com.ctrip.framework.cs.code.debug;

/**
 * @author keli.wang
 * Modified by jiang.j
 */
public class LocalVariable {
    private final String name;
    private final String desc;
    private transient final int start;
    private transient final int end;
    private transient final int index;

    LocalVariable(final String name,
                  final String desc,
                  final int start,
                  final int end, final int index) {
        this.name = name;
        this.desc = desc;
        this.start = start;
        this.end = end;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return " name:"+name+", desc:"+desc+", start:"+start+", end:"+end+", index:"+index;
    }

}

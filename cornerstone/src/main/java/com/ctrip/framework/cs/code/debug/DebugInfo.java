package com.ctrip.framework.cs.code.debug;


/**
 * Created by jiang.j on 2017/7/13.
 */
public class DebugInfo {
    private final int lineNum;
    private final String owner;
    private final String traceId;
    private final Condition[] conditions;
    public DebugInfo(final int lineNum,final String traceId){
        this(lineNum,traceId,null,null);
    }

    public DebugInfo(final int lineNum,final String traceId,Condition[] conditions,String owner){
        this.lineNum = lineNum;
        this.traceId = traceId;
        this.conditions = conditions;
        this.owner = owner;

    }

    public int getLineNum(){
        return lineNum;
    }

    public String getTraceId(){
        return traceId;
    }

    public Condition[] getConditions(){
        return this.conditions;
    }
}

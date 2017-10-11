package com.ctrip.framework.cs.code.debug;

/**
 * Created by jiang.j on 2017/8/8.
 */
public class MethodRange {
    private int start;
    private int end;

    public void update(int lineNum){
        if(lineNum<start || start==0){
           start = lineNum;
        }

        if(lineNum > end){
            end = lineNum;
        }
    }

    public boolean isInRange(int lineNum){
        return lineNum>= this.start && lineNum <= this.end;
    }

    public int getStart(){
        return start;
    }

    public int getEnd(){
        return end;
    }
}

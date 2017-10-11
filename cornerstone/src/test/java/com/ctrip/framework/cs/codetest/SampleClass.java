package com.ctrip.framework.cs.codetest;


import com.ctrip.framework.cs.code.Debugger;
import com.ctrip.framework.cs.code.DebuggerManager;
import com.ctrip.framework.cs.code.debug.DebugTool;

import java.util.Date;
import java.util.Map;

/**
 * Created by jiang.j on 2017/7/12.
 */
public class SampleClass {

    public SampleClass(){

        Debugger debugger = DebuggerManager.getCurrent();
        System.out.println("current debugger "+debugger);
        if(debugger != null) {
            try {
                debugger.triggerBreakpoint("traceid");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
    public void doSome(){
        int i = 120;
        float f = 12.3f;
        short s = 100;
        char c = 'z';
        boolean b = false;
        Integer obj = 10;
        String str = "many ....";
        String nulStr = null;
        SampleInfo info;
        info= new SampleInfo();
        info.currentDate = new Date();
        info.isTrue = false;
        info.name = "samle info";

        if(i>100){
            System.out.println(i);
        }

        if(str != null){
            System.out.println(str);
        }

        if(str.length() > 10){
            System.out.println(str);
        }

        if("many ....".equals(str)){
            System.out.println(str);
        }

        if(f<=12.3){
            System.out.println(f);
        }

        System.out.println("sample class do some");
        System.out.println(i);
    }

    public Map<String,Object> getDebugResult(){

        return DebugTool.viewCurrentTrace("traceid");
    }

    public static class SampleInfo{
        public Date currentDate;
        public boolean isTrue;
        private String name;
    }

}

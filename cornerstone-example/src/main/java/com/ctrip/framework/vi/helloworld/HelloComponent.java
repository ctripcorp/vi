package com.ctrip.framework.vi.helloworld;

import com.ctrip.framework.vi.annotation.ComponentStatus;
import com.ctrip.framework.vi.annotation.FieldInfo;

/**
 * Created by jiang.j on 2016/4/12.
 */
@ComponentStatus(id="hellocomponent",name="Hello world",description = "应用基本信息")
public class HelloComponent{

    @FieldInfo(name = "Total Cost",description = "total spend")
     private String totalCost;

    @FieldInfo(name = "Sub Environment", description = "dddfdf")
    private  String subenv;

    private transient String privateVal;

    public HelloComponent(){
        totalCost="a lot ... component";
        subenv="need new row\r\n new row";

    }
}

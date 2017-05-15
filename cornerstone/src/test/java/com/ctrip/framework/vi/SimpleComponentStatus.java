package com.ctrip.framework.cornerstone;

import com.ctrip.framework.cornerstone.annotation.ComponentStatus;

import java.util.Date;

/**
 * Created by jiang.j on 2016/6/17.
 */
@ComponentStatus(id="vi.component.testmethod",name="name",description = "test")
public class SimpleComponentStatus {

    public static class HelloReq{
        public String name;
        public Date date;
    }
    public static String hello(){
        return "hello world";
    }

    public static String hello(HelloReq req){
        return "hello "+req.name+" ,"+req.date.toString();
    }
}

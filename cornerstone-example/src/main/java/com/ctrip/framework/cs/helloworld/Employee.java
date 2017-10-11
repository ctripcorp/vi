package com.ctrip.framework.cs.helloworld;

import com.ctrip.framework.cs.annotation.Sensitive;

import java.util.List;

/**
 * Created by jiang.j on 2017/6/7.
 */
public class Employee {

    public String name;
    public int age;
    @Sensitive
    public int creditNo;
    public Country country;
    public String email;
    public List<Employee> subordinates;
}

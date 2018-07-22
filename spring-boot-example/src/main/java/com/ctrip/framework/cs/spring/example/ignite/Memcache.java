package com.ctrip.framework.cs.spring.example.ignite;

import com.ctrip.framework.cs.cacheRefresh.CacheCell;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang.j on 2016/5/17.
 */
public class Memcache implements CacheCell {
    private String id;
    private static String REFRESHTIME = "refreshTime";
    public final static Date CREATEDATE = new Date();
    Map<String,Object> rtn = new HashMap<>();
    Map<String,Employee> container = new HashMap<>();
    public Memcache(String id){
        this.id = id;
        rtn.put(REFRESHTIME,new Date());
        rtn.put("hitCount",1233);
        rtn.put("createDate",CREATEDATE);
        rtn.put("lastOperator","ctrip");

        Country china = new Country();
        china.name = "中国";
        china.no = 1;
        Country american = new Country();
        american.name = "American";
        american.no = 2;
        Employee employee = new Employee();
        employee.name="peter";
        employee.age = 30;
        employee.creditNo =1223989;
        employee.email="peter@company.com";
        employee.country = american;
        container.put("peter",employee);
        Employee employee1 = new Employee();
        employee1.name="张笑笑";
        employee1.age = 20;
        employee1.creditNo =1223989;
        employee1.email="zhangxiaoxiao@company.com";
        employee1.country = china;
        container.put("zhaoxiaoxiao",employee1);
        Employee employee2 = new Employee();
        employee2.name="李明";
        employee2.age = 25;
        employee2.creditNo =2223989;
        employee2.email="liming@company.com";
        employee2.country = american;
        container.put("zhaoxiaoxiao",employee2);
        Employee employee3 = new Employee();
        employee3.name="刘姥姥";
        employee3.age = 55;
        employee3.creditNo =3333989;
        employee3.email="liulaolao@company.com";
        employee3.country = china;
        employee3.subordinates = new ArrayList<>();
        employee3.subordinates.add(employee);
        employee3.subordinates.add(employee1);
        employee3.subordinates.add(employee2);
        container.put("liulaolao",employee3);

    }
    @Override
    public String id() {
        return this.id;
    }

    @Override
    public boolean refresh() {
        rtn.put(REFRESHTIME,new Date());
        return true;
    }

    @Override
    public Map<String, Object> getStatus() {
        return  rtn;
    }

    @Override
    public Object getByKey(String key) {
        return container.get(key);
    }

    @Override
    public Iterable<String> keys() {
        return container.keySet();
    }


    @Override
    public int size() {
        return container.size();
    }
}

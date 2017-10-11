package com.ctrip.framework.cs.helloworld;

import com.ctrip.framework.cs.annotation.ComponentStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiang.j on 2016/6/15.
 */

@ComponentStatus(id="example.tablestyle",name="simple table display component status",description = "自定义组件状态实例,表格",custom = true)
public class TableCustomComponent {
    public class Hotel{
        String name;
        String score;
        String type;
        float price;
        String city;

    }
    List<Hotel> hotels= new ArrayList<>();
    String[] types = {"豪华","经济","舒适"};
    public TableCustomComponent(){

        for (int i = 0; i < 100; i++) {
            Hotel hotel = new Hotel();
            hotel.name="酒店"+i;
            hotel.score=i+"分";
            hotel.type=types[i%3];
            hotel.price=500+i;
            hotel.city="上海";
            hotels.add(hotel);
        }
    }
}

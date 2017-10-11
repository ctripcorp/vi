package com.ctrip.framework.cs.helloworld;

import com.ctrip.framework.cs.annotation.ComponentStatus;
import com.ctrip.framework.cs.annotation.FieldInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jiang.j on 2016/7/12.
 */
@ComponentStatus(id="com.ctrip.dal.client.DatabasesetConfig",name="com.ctrip.dal.client.databasesetconfig",description = "DAL Database Set config",list = true)
public class ListStatusComponent {

    @FieldInfo(name = "Name",description = "姓名")
    private String name;
    @FieldInfo(name = "Year",description = "年份")
    private Date year;

    @FieldInfo(name = "Name1",description = "姓名")
    private String name1;
    @FieldInfo(name = "Year1",description = "年份")
    private String year1;
    @FieldInfo(name = "Name2",description = "姓名")
    private String name2;
    private int yeaR3;
    public static List<ListStatusComponent> list(){

        List<ListStatusComponent> rtn = new ArrayList<>();
        ListStatusComponent comp1 = new ListStatusComponent();
        comp1.name="ctrip";
        comp1.year= new Date();
        rtn.add(comp1);
        comp1 = new ListStatusComponent();
        comp1.name="ctrip";
        comp1.year= new Date();
        comp1.yeaR3 =2011;
        rtn.add(comp1);
        for(int i=0;i<100;i++) {
            comp1 = new ListStatusComponent();
            comp1.name = "ctrip"+i;
            comp1.year = new Date();
            comp1.name2 = "java"+i;
            comp1.yeaR3 =2011+i;
            rtn.add(comp1);
        }
        return rtn;
    }
}

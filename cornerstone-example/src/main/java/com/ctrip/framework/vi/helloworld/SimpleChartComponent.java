package com.ctrip.framework.cornerstone.helloworld;

import com.ctrip.framework.cornerstone.annotation.ComponentStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiang.j on 2016/6/17.
 */
@ComponentStatus(id="example.simplechart",name = "simple chart",description = "图形示例",custom = true)
public class SimpleChartComponent {

    public static class GetPointsReq{
        public String name;
        public int maxCount;
    }

    public static List<String> getHotPoints(GetPointsReq req){
        ArrayList<String> rtn = new ArrayList<>();
        for(int i=0;i<req.maxCount;i++){
            rtn.add(req.name+"@"+i);
        }
        return rtn;
    }

}

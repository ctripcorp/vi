package com.ctrip.framework.cornerstone;

import com.ctrip.framework.cornerstone.component.ComponentHandler;
import com.ctrip.framework.cornerstone.component.ComponentManager;
import com.google.gson.Gson;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2016/6/17.
 */
public class ComponentHandlerTest {

    public ComponentHandlerTest(){
        ComponentManager.add(SimpleComponentStatus.class);
    }

    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Test
    public void testCustomComponentMethod() throws Exception {
        ComponentHandler handler = new ComponentHandler();
        Map<String,Object> paras = new HashMap<>();
        SimpleComponentStatus.HelloReq req= new SimpleComponentStatus.HelloReq();
        req.name="john";
        req.date=new Date();
        Gson gson = new Gson();
        paras.put("req",gson.toJson(req));
        Object rtn = handler.execute(handler.getStartPath()+"vi.component.testmethod/hello","me",1,logger,paras);
        assertTrue(rtn.toString().contains("john"));
        paras.remove("req");
        rtn = handler.execute(handler.getStartPath()+"vi.component.testmethod/hello","me",1,logger,paras);
       assertTrue(rtn.toString().contains("world"));

    }
}

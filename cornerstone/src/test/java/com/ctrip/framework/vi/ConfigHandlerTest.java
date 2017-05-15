package com.ctrip.framework.cornerstone;

import com.ctrip.framework.cornerstone.configuration.ConfigHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2016/6/20.
 */
public class ConfigHandlerTest {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Test
    public void testGetAll() throws Exception {
        ConfigHandler configHandler = new ConfigHandler();
       Map<String,Map<String,String>> rtn = (Map<String, Map<String, String>>) configHandler.execute(configHandler.getStartPath()+"all","test",1,logger,new HashMap<String, Object>());

        assertTrue(rtn.size() > 0);
        assertTrue(rtn.containsKey("test"));
        assertTrue(rtn.get("test").containsKey("test.chinese"));
    }

    @Test
    public void testUpdate() throws Exception {
        ConfigHandler configHandler = new ConfigHandler();
        Map<String,Object> para = new HashMap<>();
        String key="test.chinese";
        String value = "good";
        para.put(key,value);
        configHandler.execute(configHandler.getStartPath()+"update","test",1,logger,para);
        Map<String,Map<String,String>> rtn = (Map<String, Map<String, String>>) configHandler.execute(configHandler.getStartPath()+"all","test",1,logger,new HashMap<String, Object>());

        assertTrue(rtn.size() > 0);
        assertTrue(rtn.containsKey("test"));
        assertTrue(rtn.get("test").containsKey(key));
        assertEquals(value,rtn.get("test").get(key));

    }

    @Test
    public void testJsonUpdate() throws Exception {
        ConfigHandler configHandler = new ConfigHandler();
        Map<String,Object> paras = new HashMap<>();
        String key="test.chinese";
        String value = "good";
        paras.put(key,value);
        Gson gson = new Gson();

        Type paraMap = new TypeToken<Map<String, JsonPrimitive>>(){}.getType();
        Map<String,Object> params = gson.fromJson(gson.toJson(paras),paraMap);
        configHandler.execute(configHandler.getStartPath()+"update","test",1,logger,params);
        Map<String,Map<String,String>> rtn = (Map<String, Map<String, String>>) configHandler.execute(configHandler.getStartPath()+"all","test",1,logger,new HashMap<String, Object>());

        assertTrue(rtn.size() > 0);
        assertTrue(rtn.containsKey("test"));
        assertTrue(rtn.get("test").containsKey(key));
        assertEquals(value,rtn.get("test").get(key));

    }
}

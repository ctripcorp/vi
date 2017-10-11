package com.ctrip.framework.cs;

import com.ctrip.framework.cs.IgniteManager;
import com.ctrip.framework.cs.IgniteStatus;
import com.ctrip.framework.cs.analyzer.AnalyzerHandler;
import com.ctrip.framework.cs.ignite.Status;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2017/7/4.
 */
public class SelfCheckTest {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Test
    public void testSelfCheck() throws Exception {

        IgniteManager.ignite();
        IgniteStatus status = IgniteManager.getStatus();

        while (status.getStatus()== Status.Running) {
            Thread.sleep(300);
        }
        assertEquals(Status.Success,status.getStatus());

        AnalyzerHandler handler = new AnalyzerHandler();
        Map<String,Object> params = new HashMap<>();
        params.put("id","vi.ignite1");
        params.put("uid",handler.execute("/analyzer/selfcheck","test",1,logger,params));
        params.put("index", "0");
        Thread.sleep(1000);
        Object rtn = (handler.execute("/analyzer/getSelfCheckMsgs","test",1,logger,params));
        assertTrue(rtn instanceof List);
        assertTrue(((List) rtn).size()>0);
        assertTrue(((List) rtn).get(0).toString().contains("pluginId"));


    }

    @Test
    public void testGetPluginIds() throws Exception {
        IgniteManager.ignite();
        IgniteStatus status = IgniteManager.getStatus();

        while (status.getStatus()== Status.Running) {
            Thread.sleep(300);
        }
        assertTrue(status.getStatus() == Status.Success);

        AnalyzerHandler handler = new AnalyzerHandler();
        Map<String,Object> params = new HashMap<>();
        Object rtn =(handler.execute("/analyzer/allIgnitePlugins", "test", 1, logger, params));
        assertTrue(rtn instanceof Set);
        assertEquals(1, ((Set) rtn).size());

    }
}

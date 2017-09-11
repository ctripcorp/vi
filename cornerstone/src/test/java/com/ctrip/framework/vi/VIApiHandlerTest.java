package com.ctrip.framework.vi;

import com.ctrip.framework.vi.threading.ThreadingManager;
import org.junit.Test;

import java.lang.management.ThreadInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2016/5/9.
 */
public class VIApiHandlerTest {
    private final String user="vi-user";
    VIApiHandler handler = new VIApiHandler();
    @Test
    public void testConfig(){
        String path = "/config/all";
        VIApiHandler.ExeResult rtn = handler.executeService(path, user, null);
        Map<String,Object> data = (Map<String, Object>) rtn.getData();
        assertTrue(data.size()>0);
        assertTrue(data.containsKey("test"));

    }
    @Test
    public void testThreading(){
        String path = "/threading/all";
        VIApiHandler.ExeResult rtn = handler.executeService(path, user, null);
        List<ThreadingManager.TInfo> data = (List<ThreadingManager.TInfo>) rtn.getData();
        assertTrue(data.size()>0);

    }

    @Test
    public void testGetThreadInfo(){
        long threadId = 1;
        String path = "/threading/detail/"+threadId;
        VIApiHandler.ExeResult rtn = handler.executeService(path, user, null);
        ThreadInfo info = (ThreadInfo) rtn.getData();
        assertEquals(threadId, info.getThreadId());
        assertEquals("main", info.getThreadName());
        final int maxDepth = 2;
        rtn = handler.executeService(path, user, new HashMap<String, Object>(){
            {
                put("maxdepth",maxDepth+"");
            }
        });
        info = (ThreadInfo) rtn.getData();
        assertEquals(maxDepth,info.getStackTrace().length);
        rtn = handler.executeService(path, user, new HashMap<String, Object>(){
            {
                put("maxdepth","abc");
            }
        });
        info = (ThreadInfo) rtn.getData();
        assertEquals(3,info.getStackTrace().length);

    }

    @Test
    public void testThreadStats(){
        String path = "/threading/stats";
        VIApiHandler.ExeResult rtn = handler.executeService(path, user, null);
        Map<String,Number> info = (Map<String, Number>) rtn.getData();
        assertTrue(info.containsKey("currentThreadCount"));
        assertTrue(info.get("currentThreadCount").intValue()>0);
        assertTrue(info.containsKey("daemonThreadCount"));
        assertTrue(info.get("daemonThreadCount").intValue()>0);
        assertTrue(info.containsKey("totalStartedThreadCount"));
        assertTrue(info.get("totalStartedThreadCount").longValue() > 0);
        assertTrue(info.containsKey("peakThreadCount"));
        assertTrue(info.get("peakThreadCount").intValue()>0);
    }

}

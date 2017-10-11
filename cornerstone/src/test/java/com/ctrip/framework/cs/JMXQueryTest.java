package com.ctrip.framework.cs;

import com.ctrip.framework.cs.util.JMXQuery;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2016/6/6.
 */
public class JMXQueryTest {

    @Test
    public void testQuery(){

        JMXQuery jmxQuery = new JMXQuery();
        List<Map<String, Object>> rtn = jmxQuery.query(JMXQuery.JAVALANG, "MemoryPool", new String[]{"Name", "Usage"});
        assertTrue(rtn.size() > 0);
        assertTrue(rtn.get(0).containsKey("Name"));
        assertTrue(rtn.get(0).containsKey("Usage"));
    }

}

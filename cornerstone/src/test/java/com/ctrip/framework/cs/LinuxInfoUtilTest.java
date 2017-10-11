package com.ctrip.framework.cs;

import com.ctrip.framework.cs.util.LinuxInfoUtil;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * Created by jiang.j on 2016/7/27.
 */
public class LinuxInfoUtilTest {

    @Test
    public void testAvailabeMem() throws IOException {
        Map<String,String> meminfo = new HashMap<>();
        meminfo.put("MemAvailable","7377708 KB");
        meminfo.put("MemFree","400  KB");
        meminfo.put("Buffers","  1400  KB");
        meminfo.put("Cached","4000  KB");
        assertEquals(7377708, LinuxInfoUtil.getAvailableMemKB(meminfo));
        meminfo.remove("MemAvailable");
        assertEquals(400 + 1400 + 4000, LinuxInfoUtil.getAvailableMemKB(meminfo));
    }
}

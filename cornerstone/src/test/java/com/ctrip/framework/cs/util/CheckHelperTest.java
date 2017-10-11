package com.ctrip.framework.cs.util;

import com.ctrip.framework.cs.IgniteManager;
import com.ctrip.framework.cs.util.CheckHelper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2017/7/7.
 */
public class CheckHelperTest {

    @Test
    public void testPing() throws IOException {

        IgniteManager.SimpleLogger logger =new IgniteManager.SimpleLogger();
        CheckHelper checkHelper = CheckHelper.create(logger);
        boolean result = checkHelper.telnet("www.ctrip.com", 80);

        assertTrue(logger.getMsgs(0).size()>0);

    }
}

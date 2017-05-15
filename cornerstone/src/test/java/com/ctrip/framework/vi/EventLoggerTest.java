package com.ctrip.framework.cornerstone;

import com.ctrip.framework.cornerstone.watcher.EventLogger;
import com.ctrip.framework.cornerstone.watcher.EventLoggerFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2016/12/16.
 */
public class EventLoggerTest {


    static final String msg = "hello world";
    static int fireCount = 0;
    public static class TestEventLogger implements EventLogger{

        @Override
        public void fireEvent(String message, Object... args) {

            assertEquals(msg, message);
            assertTrue(args[0] instanceof String);
            assertTrue(args[1] instanceof Object[]);
            fireCount++;
        }
    }

    @Test
    public void testLogger() throws InterruptedException {
        EventLoggerFactory.addLogger(TestEventLogger.class);
        EventLogger logger = EventLoggerFactory.getLogger(getClass());

        logger.fireEvent(msg,"great","good");
        Thread.sleep(200);
        assertTrue(fireCount > 0);
    }
}

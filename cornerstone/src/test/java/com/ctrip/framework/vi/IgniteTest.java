package com.ctrip.framework.cornerstone;

import com.ctrip.framework.cornerstone.annotation.EventSource;
import com.ctrip.framework.cornerstone.configuration.ConfigurationManager;
import com.ctrip.framework.cornerstone.configuration.InitConfigurationException;
import com.ctrip.framework.cornerstone.ignite.Status;
import com.ctrip.framework.cornerstone.watcher.EventLogger;
import com.ctrip.framework.cornerstone.watcher.EventLoggerFactory;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2016/8/22.
 */
public class IgniteTest {

    @Test
    public void testIgnite() throws InterruptedException, InitConfigurationException {

        ConfigurationManager.getConfigKeys("ignite").remove("com.ctrip.framework.cornerstone.plugins.TestIginite5");
        IgniteManager.ignite();
        IgniteStatus status = IgniteManager.getStatus();

        while (status.getStatus()== Status.Running) {
            Thread.sleep(300);
        }
        assertTrue(status.getStatus() == Status.Success);
    }

}

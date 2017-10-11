package com.ctrip.framework.cs;

import com.ctrip.framework.cs.IgniteManager;
import com.ctrip.framework.cs.IgniteStatus;
import com.ctrip.framework.cs.configuration.ConfigurationManager;
import com.ctrip.framework.cs.configuration.InitConfigurationException;
import com.ctrip.framework.cs.ignite.Status;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2016/9/12.
 */
public class IgniteFailTest {

    @Test
    public void testIgniteFail() throws InterruptedException, InitConfigurationException {
        ConfigurationManager.getConfigKeys("ignite").add("TestIginite5");
        IgniteManager.ignite();
        IgniteStatus status = IgniteManager.getStatus();

        while (status.getStatus()== Status.Running) {
            System.out.println(status.getStatus());
            Thread.sleep(300);
        }
        assertTrue(status.getStatus() == Status.Failure);
    }
}

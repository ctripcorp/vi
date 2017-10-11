package com.ctrip.framework.cs;

import com.ctrip.framework.cs.IgniteManager;
import com.ctrip.framework.cs.IgniteStatus;
import com.ctrip.framework.cs.configuration.ConfigurationManager;
import com.ctrip.framework.cs.configuration.InitConfigurationException;
import com.ctrip.framework.cs.ignite.Status;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2016/8/22.
 */
public class IgniteTest {

    public class Mytest{
        String someVal;
        Mytest mytest;
        boolean isTrue;
        int[] someInts;
    }
    public void some(){

        Mytest mytest = new Mytest();
        if(mytest.isTrue && mytest.someInts != null && mytest.someInts[0] ==0 && mytest.mytest !=null && mytest.mytest.someVal!=null && mytest.mytest.someVal.length()>0){

        }

    }
    @Test
    public void testIgnite() throws InterruptedException, InitConfigurationException, IOException {

        ConfigurationManager.getConfigKeys("ignite").remove("TestIginite5");
        IgniteManager.ignite();
        IgniteStatus status = IgniteManager.getStatus();

        while (status.getStatus()== Status.Running) {
            Thread.sleep(300);
        }
        assertTrue(status.getStatus() == Status.Success);
    }

}

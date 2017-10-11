package com.ctrip.framework.cs;

import com.ctrip.framework.cs.analyzer.JVMSampler;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jiang.j on 2016/6/14.
 */
public class SamplerTest {

    @Test
    public void testHeapHisto() throws IOException, InterruptedException {
        JVMSampler.setMaxWaitingTime(1000);
        List<Object[]> heaphisto = JVMSampler.getCurrentHeapHisto();
        if(heaphisto==null || heaphisto.isEmpty()) {
            Thread.sleep(500);
            heaphisto = JVMSampler.getCurrentHeapHisto();
        }
        assertTrue(heaphisto.size() > 0);
        boolean hasSamplerTest=false;
        for(Object[] instanceInfo:heaphisto){
            if(instanceInfo[0].toString().contains("SamplerTest")){
                hasSamplerTest=true;
            }
        }
        assertTrue(hasSamplerTest);
        heaphisto = JVMSampler.getCurrentHeapHisto();
        assertTrue(heaphisto.size() > 0);
        Thread.sleep(2300);
        heaphisto = JVMSampler.getCurrentHeapHisto();
        assertEquals(null,heaphisto);
        if(heaphisto==null || heaphisto.isEmpty()) {
            Thread.sleep(500);
            heaphisto = JVMSampler.getCurrentHeapHisto();
        }
        assertTrue(heaphisto.size() > 0);
    }
}

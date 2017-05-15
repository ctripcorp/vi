package com.ctrip.framework.cornerstone;

import com.ctrip.framework.cornerstone.configuration.ConfigurationManager;
import com.ctrip.framework.cornerstone.configuration.InitConfigurationException;
import com.ctrip.framework.cornerstone.localLog.LocalLogManager;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by jiang.j on 2017/2/22.
 */
public class LocalLogManagerTest {

    @Test
    public void testGetLogList() throws IOException, InitConfigurationException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("test-logs/");
        ConfigurationManager.getConfigInstance().setProperty("app.localLog.path",url.getFile());
        List<LocalLogManager.FileLogInfo> logList = LocalLogManager.getLogList();
        boolean haveTestTxt = false;
        boolean haveGz = false;
        for(LocalLogManager.FileLogInfo info:logList){
            String name = info.name;
            int sIndex = name.indexOf("|");
            if(sIndex>0){
                name = name.substring(0,sIndex);
            }
            System.out.println(name);
            System.out.println(info.size);

           switch (name){
               case "test.txt":
                   haveTestTxt = true;
                   break;
               case "gztest.log.gz":
                   haveGz =true;
                   break;
           }
        }


        assertEquals("Can't get .txt file",true,haveTestTxt);
        assertEquals("Can't get .gz file",true,haveGz);


    }
}

package com.ctrip.framework.cornerstone.component.defaultComponents;

import com.ctrip.framework.cornerstone.annotation.ComponentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Set;

/**
 * Created by jiang.j on 2016/6/6.
 */
@ComponentStatus(id = "vi.tomcatrequestinfo",name = "tomcat request info",description = "")
public class TomcatRequestInfo {

    Logger _logger = LoggerFactory.getLogger(this.getClass());
    Set<ObjectName> names;
    public TomcatRequestInfo(){
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            final ObjectName globalName = new ObjectName("Catalina:type=RequestProcessor,*");
            names= mbs.queryNames(globalName, null);
        }catch (Throwable e){
            _logger.warn("get tomcat run info failed",e);

        }
    }
}

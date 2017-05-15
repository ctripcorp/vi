package com.ctrip.framework.cornerstone.component.defaultComponents;

import com.ctrip.framework.cornerstone.annotation.ComponentStatus;
import com.ctrip.framework.cornerstone.util.JMXQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by jiang.j on 2016/4/29.
 */
@ComponentStatus(id = "vi.tomcatmonitor", name = "tomcat monitor", custom = true,description = "tomcat 监控")
public class TomcatInfo  extends HashMap<String,Object>{

    Logger _logger = LoggerFactory.getLogger(this.getClass());
    public TomcatInfo(){
        try {
            JMXQuery jmxQuery = new JMXQuery();

            this.putAll(jmxQuery.query(JMXQuery.CATALINA,".*http.*","GlobalRequestProcessor",new String[]{
                    "requestCount",
                    "errorCount",
                    "bytesSent",
                    "bytesReceived",
                    "processingTime",
                    "maxTime"
            }).get(0));
            this.putAll(jmxQuery.query(JMXQuery.CATALINA,".*http.*","ThreadPool",new String[]{
                    "maxThreads",
                    "currentThreadCount",
                    "currentThreadsBusy",
                    "backlog"
            }).get(0));
        }catch (Throwable e){
            _logger.warn("get tomcat run info failed",e);

        }
    }
}

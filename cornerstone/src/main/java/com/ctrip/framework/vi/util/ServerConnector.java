package com.ctrip.framework.cornerstone.util;

import com.ctrip.framework.cornerstone.component.defaultComponents.HostInfo;
import com.ctrip.framework.cornerstone.enterprise.EnFactory;
import com.sun.corba.se.impl.util.Version;

import java.util.List;
import java.util.Map;

/**
 * Created by jiang.j on 2016/11/24.
 */
public class ServerConnector {

    static String serverPort;
    static {

        if(HostInfo.isTomcat()) {
            JMXQuery jmxQuery = new JMXQuery();
            List<Map<String, Object>> connectors = jmxQuery.query(JMXQuery.CATALINA, "Connector", new String[]{"port", "protocol"});

            for (Map<String, Object> item : connectors) {

                String protocol = String.valueOf(item.get("protocol")).toLowerCase();
                if (protocol.contains("http")) {
                    serverPort = String.valueOf(item.get("port"));
                    break;
                }
            }
        }else{
            serverPort = System.getProperty("vi.context.path");
        }
    }
    public static String getPath(String version){

        return EnFactory.getEnBase().getAppId() +"/" + EnFactory.getEnHost().getDataCenter()+"-"+ version +"/"+ EnFactory.getEnHost().getHostName();
    }

    public static String getPort(){
        return serverPort;
    }

    public static String getServiceLink(){
        String rtn;
        if(HostInfo.isTomcat()) {
            rtn = serverPort + System.getProperty("vi.context.path");
        }else{
            rtn = serverPort;
        }
        return EnFactory.getEnHost().getHostAddress() + ":" +rtn;
    }

}

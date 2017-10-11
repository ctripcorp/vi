package com.ctrip.framework.cs.util;

import com.ctrip.framework.cs.Version;
import com.ctrip.framework.cs.component.defaultComponents.HostInfo;
import com.ctrip.framework.cs.configuration.ConfigurationManager;
import com.ctrip.framework.cs.configuration.InitConfigurationException;
import com.ctrip.framework.cs.SysKeys;
import com.ctrip.framework.cs.enterprise.EnFactory;

import java.util.ArrayList;
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

        }

        if(serverPort == null){
            serverPort = System.getProperty(SysKeys.SPRINGBOOTPORTKEY);
        }
        if(serverPort == null){
            serverPort = System.getProperty(SysKeys.SERVERIDPROKEY);
        }
    }

    public static String getPort(){
        return serverPort;
    }


    public static List<String> getRegistryAddresses() throws InitConfigurationException {

        List<String> rtn = new ArrayList<>(2);

        String etcdUrl = ConfigurationManager.getConfigInstance().getString(SysKeys.ETCDKEY);
        String contextPath = System.getProperty(SysKeys.TOMCATCONTEXTPATH);
        String viServerPort = System.getProperty(SysKeys.SERVERIDPROKEY);
        String defaultTTL = "&ttl=1000";
        String hostAddress  = EnFactory.getEnHost().getHostAddress() +":";

        String keyPath = EnFactory.getEnBase().getAppId() +"/" + EnFactory.getEnHost().getDataCenter()+"-"+ Version.VERSION +"/"+ EnFactory.getEnHost().getHostName();

        if(HostInfo.isTomcat()){
            if( contextPath != null) {
                rtn.add(etcdUrl + keyPath + "?value=" + hostAddress + serverPort + contextPath + defaultTTL);
            }else{
                rtn.add(etcdUrl + keyPath+"-SB" + "?value=" + hostAddress + System.getProperty(SysKeys.SPRINGBOOTPORTKEY) + defaultTTL);
            }

        }

        if(viServerPort != null){
            rtn.add(etcdUrl+keyPath+"-S?value="+hostAddress + viServerPort+defaultTTL);
        }

        return rtn;
    }

}
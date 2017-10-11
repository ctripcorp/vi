package com.ctrip.framework.cs.enterprise;

import com.ctrip.framework.cs.SysKeys;
import com.ctrip.framework.cs.code.DebuggerManager;
import com.ctrip.framework.cs.code.Debugger;
import com.ctrip.framework.cs.configuration.ConfigurationManager;
import com.ctrip.framework.cs.configuration.InitConfigurationException;
import com.ctrip.framework.cs.util.HttpUtil;
import com.ctrip.framework.cs.util.IOUtils;
import com.ctrip.framework.cs.util.ServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by jiang.j on 2016/10/19.
 */
public class DefaultEnApp implements EnApp {
    Logger logger = LoggerFactory.getLogger(getClass());
    Properties properties = new Properties();
    public DefaultEnApp(){

        try(InputStream is =Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/app.properties")) {
            if(is!=null) {
                try(InputStreamReader reader =new InputStreamReader(is, Charset.defaultCharset())) {
                    properties.load(reader);
                }
            }
        } catch (Throwable e) {
            logger.warn("read app info error!", e);
        }
    }


    class EtcdResult{
        EtcdNode node;
    }
    class EtcdNode{
        String key;
        String value;
        boolean dir;
        EtcdNode[] nodes;
    }

    @Override
    public String getVersion() {
        return properties.getProperty("app.version");
    }

    @Override
    public String getName() {
        return properties.getProperty("app.name");
    }

    @Override
    public String getChineseName() {
        return properties.getProperty("app.chineseName");
    }

    @Override
    public String getOwner() {
        return properties.getProperty("app.owner");
    }

    @Override
    public String getOwnerContact() {
        return properties.getProperty("app.ownerContact");
    }

    @Override
    public String getBackup() {
        return properties.getProperty("app.backup");
    }

    @Override
    public String getBuildTime() {
        return properties.getProperty("app.buildTime");
    }

    public static List<ServerInfo> getAppServers(String etcdUrl,String appId) throws Exception {

        List<ServerInfo> rtn = new ArrayList<>();


        URL url = new URL(etcdUrl+ appId + "?recursive=true");
        EtcdResult result = HttpUtil.doGet(url, EtcdResult.class);
        for(EtcdNode idc:result.node.nodes){
            if(idc.dir && idc.nodes != null){
                for(EtcdNode server:idc.nodes){
                    if(!server.dir) {
                        ServerInfo serverInfo = new ServerInfo();

                        String[] keyInfos = idc.key.split("/");
                        serverInfo.setIdc(keyInfos[3]);
                        serverInfo.setName(server.key.substring(idc.key.length() + 1));
                        String[] tmp = server.value.split(":");
                        serverInfo.setIp(tmp[0]);
                        serverInfo.setUrl(tmp.length>1 && !tmp[1].equalsIgnoreCase("null")?
                                (tmp[1].startsWith("null/")?"8080"+tmp[1].substring(4):tmp[1]):"8080");
                        rtn.add(serverInfo);
                    }
                }

            }
        }
        return rtn;
    }
    @Override
    public List<ServerInfo> getAllServers() {
         List<ServerInfo> rtn = new ArrayList<>();

        try {
            rtn =getAppServers(ConfigurationManager.getConfigInstance().getString(SysKeys.ETCDKEY),EnFactory.getEnBase().getAppId());
        }catch (Throwable e){
            logger.warn("get all servers info failed!",e);
        }
        return rtn;
    }

    @Override
    public Map<String, String> getHelpLinks() {
        return new HashMap<>();
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public void register() {

        List<String> addresses = null;
        try {
            addresses = ServerConnector.getRegistryAddresses();
        } catch (InitConfigurationException e) {
            logger.error("init config failed!",e);
            return;
        }
        for(String address:addresses) {
            try {
                URL url = new URL(address);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setConnectTimeout(500);
                conn.setReadTimeout(500);
                conn.connect();

                try (InputStream inputStream = (conn.getInputStream())) {
                    logger.debug(IOUtils.readAll(inputStream));
                }
            }catch (Throwable e){
                logger.warn("self registration failed! address:"+address,e);
            }
        }
    }

    @Override
    public boolean trace(String traceId) {

        Debugger debugger = DebuggerManager.getCurrent();
        if(debugger != null) {
            try {
                return debugger.triggerBreakpoint(traceId);
            } catch (Throwable e) {
                logger.warn("trigger breakpoint failed", e);
            }
        }
        return false;
    }

    @Override
    public String getGitCommitId() {
        return null;
    }

    @Override
    public String getGitPrjPath() {
        return null;
    }

    @Override
    public void refresh() {

    }
}

package com.ctrip.framework.cornerstone.enterprise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
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
                properties.load(new InputStreamReader(is, Charset.defaultCharset()));
            }
        } catch (Throwable e) {
            logger.warn("read app info error!", e);
        }
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

    @Override
    public List<ServerInfo> getAllServers() {
        return new ArrayList<>();
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

    }

    @Override
    public void refresh() {

    }
}

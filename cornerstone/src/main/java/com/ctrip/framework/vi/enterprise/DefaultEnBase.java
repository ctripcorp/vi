package com.ctrip.framework.vi.enterprise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * Created by jiang.j on 2016/12/1.
 */
public class DefaultEnBase implements EnBase {

    Logger logger = LoggerFactory.getLogger(getClass());
    Properties properties = new Properties();
    public DefaultEnBase(){

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
    @Override
    public String getAppId() {
            return properties.getProperty("app.id");
    }

    @Override
    public String getEnvType() {
        if(properties.containsKey("env")){
            return properties.getProperty("env");
        }else{
            return "dev";
        }
    }
}

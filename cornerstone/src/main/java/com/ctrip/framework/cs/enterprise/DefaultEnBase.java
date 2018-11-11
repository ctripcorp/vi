package com.ctrip.framework.cs.enterprise;

import com.ctrip.framework.cs.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

/**
 * Created by jiang.j on 2016/12/1.
 */
public class DefaultEnBase implements EnBase {


   private String appId = "NULL";
   private String env = "dev";

    public DefaultEnBase(){

        Logger logger = LoggerFactory.getLogger(getClass());
        Properties properties = new Properties();
        try(InputStream is =Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/app.properties")) {
            if(is!=null) {
                try(InputStreamReader reader =new InputStreamReader(is, "utf-8")){

                    properties.load(reader);
                    appId = properties.getProperty("app.id");
                }
            }
        } catch (Throwable e) {
            logger.warn("read app info error!", e);
        }


        String userHome = System.getProperty("user.home");
        String envFilePath = userHome + System.getProperty("file.separator") +  "server.properties";

        String vmEnv = System.getProperty("env");

        if(vmEnv == null) {

           properties = new Properties();
           try (InputStream is = new FileInputStream(new File(envFilePath))) {
              try (InputStreamReader reader = new InputStreamReader(is, "utf-8")) {
                 properties.load(reader);
                 if (properties.containsKey("env")) {
                    env = properties.getProperty("env");
                 }

              }

           } catch (Throwable e) {
              logger.warn("read server.properties info error!", e);
           }
        }else{
           env = vmEnv;
        }

    }
    @Override
    public String getAppId() {
            return appId;
    }

    @Override
    public String getEnvType() {
        return env;
    }
}

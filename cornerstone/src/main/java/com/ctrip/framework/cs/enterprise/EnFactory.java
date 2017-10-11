package com.ctrip.framework.cs.enterprise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.util.Properties;

/**
 * Created by jiang.j on 2016/10/19.
 */
public class EnFactory {

    private static EnBase enBase = new DefaultEnBase();
    private static EnApp enApp = new DefaultEnApp();
    private static EnHost enHost = new DefaultEnHost();
    private static EnAuthentication enAuthentication = new DefaultEnAuthentication();
    private static EnFC enFC = new DefaultEnFC();
    private static EnMaven enMaven = new DefaultEnMaven();
    private static EnUI enUI = new DefaultEnUI();
    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static String initError=null;
    static {
        Properties enPros = new Properties();

        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("vi_en.properties");
            if(inputStream!=null){
                enPros.load(inputStream);

                EnBase currentEnBase = null;
                if(enPros.containsKey("enBase")) {
                    currentEnBase = (EnBase) Class.forName(enPros.getProperty("enBase")).newInstance();
                    currentEnBase.getAppId();
                    currentEnBase.getEnvType();
                }

                if(currentEnBase !=null) {
                    enBase = currentEnBase;
                }

                String appId = enBase.getAppId();
                if(appId!=null) {
                    System.setProperty("app.id", appId);
                }

                if(enPros.containsKey("enHost")) {
                    enHost = (EnHost) Class.forName(enPros.getProperty("enHost")).newInstance();
                }

                if(enPros.containsKey("enUI")) {
                    enUI = (EnUI) Class.forName(enPros.getProperty("enUI")).newInstance();
                }

                if(enPros.containsKey("enAuthentication")) {
                    enAuthentication = (EnAuthentication) Class.forName(enPros.getProperty("enAuthentication")).newInstance();
                }

                if(enPros.containsKey("enFC")) {
                    enFC = (EnFC) Class.forName(enPros.getProperty("enFC")).newInstance();
                }

                if(enPros.containsKey("enMaven")) {
                    enMaven = (EnMaven) Class.forName(enPros.getProperty("enMaven")).newInstance();
                }

                if(appId == null){
                    throw new NoAppIdException();
                }


                if(enPros.containsKey("enApp")) {
                    enApp = (EnApp) Class.forName(enPros.getProperty("enApp")).newInstance();
                }


            }else{
                logger.info("can't find vi_en.properties");
            }
        }catch (Throwable e){
            e.printStackTrace();
            logger.warn("init enFactory failed",e);
            StringWriter writer = new StringWriter();
             e.printStackTrace(new PrintWriter(writer));
            initError = writer.toString();
        }
    }


    public static EnBase getEnBase(){

        return enBase;
    }
    public static EnApp getEnApp(){

        return enApp;
    }

    public static EnHost getEnHost(){

        return enHost;
    }

    public static EnAuthentication getEnAuthentication(){
        return enAuthentication;
    }

    public static EnFC getEnFC(){
        return enFC;
    }

    public static EnMaven getEnMaven(){
        return enMaven;
    }

    public static EnUI getEnUI(){
        return enUI;
    }

    public static String getInitError(){
        return initError;
    }
}

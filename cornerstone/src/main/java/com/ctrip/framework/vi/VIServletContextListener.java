package com.ctrip.framework.cornerstone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Created by jiang.j on 2016/3/25.
 */

@WebListener
public class VIServletContextListener implements ServletContextListener {

    public static String ServerInfo;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public void contextInitialized(ServletContextEvent sce) {

        logger.info("Begin Init VI");
        ServletContext context = sce.getServletContext();
        ServerInfo = context.getServerInfo();

        System.setProperty("vi.context.path",context.getContextPath());

        try {
            Class.forName("javax.servlet.ServletRegistration");
            ServletRegister.regiesterVIServlet(context, logger);
        }catch (ClassNotFoundException e){
            logger.warn("servlet version below 3.0",e);
        }

        logger.info("End Init VI");

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}

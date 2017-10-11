package com.ctrip.framework.cs;

import com.ctrip.framework.cs.code.debug.DebugTool;
import com.ctrip.framework.cs.enterprise.EnFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by jiang.j on 2016/3/25.
 */

@WebListener
public class VIServletContextListener implements ServletContextListener {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        logger.info("Begin Init VI");
        final ServletContext context = sce.getServletContext();
        System.setProperty(SysKeys.ServerInfo,context.getServerInfo());

        context.addListener(new ServletRequestListener() {
            @Override
            public void requestDestroyed(ServletRequestEvent sre) {

            }

            @Override
            public void requestInitialized(ServletRequestEvent sre) {
                HttpServletRequest httpServletRequest = (HttpServletRequest) sre.getServletRequest();
                EnFactory.getEnApp().trace(httpServletRequest.getHeader(DebugTool.getTraceIdKey()));
            }
        });
        System.setProperty(SysKeys.TOMCATCONTEXTPATH,context.getContextPath());

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

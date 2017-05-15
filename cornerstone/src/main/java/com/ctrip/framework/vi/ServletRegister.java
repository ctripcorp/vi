package com.ctrip.framework.cornerstone;

import org.slf4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

/**
 * Created by jiang.j on 2016/9/7.
 */
final class ServletRegister {

protected static final void regiesterVIServlet(ServletContext context,Logger logger){

        try {
            ServletRegistration.Dynamic asr = context.addServlet("VIApiServlet", VIApiServlet.class);
            asr.setLoadOnStartup(Integer.MAX_VALUE);

            if (asr != null) {
                asr.addMapping("/@in/api/*");
            } else {
                logger.warn("Servlet VIApiServlet already exists");
            }

            ServletRegistration ssr = context.addServlet("VIHttpServlet", StaticContentServlet.class);
            if (ssr != null) {
                ssr.addMapping("/@in/*");
            } else {
                logger.warn("Servlet VIHttpServlet already exists");
            }

        }catch (Throwable e){
            logger.error("VI register servlet failed",e);
        }
    }
}

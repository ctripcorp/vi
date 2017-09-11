package com.ctrip.framework.vi;

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

            if (asr != null) {
                asr.setLoadOnStartup(Integer.MAX_VALUE);
                asr.addMapping("/vi/api/*");
            } else {
                logger.warn("Servlet VIApiServlet already exists");
            }

            ServletRegistration ssr = context.addServlet("VIHttpServlet", StaticContentServlet.class);
            if (ssr != null) {
                ssr.addMapping("/vi/*");
            } else {
                logger.warn("Servlet VIHttpServlet already exists");
            }

        }catch (Throwable e){
            logger.error("VI register servlet failed",e);
        }
    }
}

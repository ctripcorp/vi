package com.ctrip.framework.vi.servlet;

import com.ctrip.framework.vi.StaticContentServlet;
import com.ctrip.framework.vi.VIApiServlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by jiang.j on 2017/2/17.
 */
public class VIFilter implements Filter {
    VIApiServlet apiServlet;
    StaticContentServlet staticContentServlet;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        apiServlet  = new VIApiServlet();
        apiServlet.init();
        staticContentServlet =  new StaticContentServlet();
        staticContentServlet.init();

    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contextPath = httpRequest.getContextPath();
        if (contextPath == null) {
            contextPath = "";
        }


        String path = httpRequest.getRequestURI();
        if (!path.startsWith(contextPath + "/vi/") && !path.equalsIgnoreCase(contextPath + "/vi")) {
            chain.doFilter(request,response);
        }else{
            if(path.startsWith(contextPath+"/vi/api/")){
                apiServlet.service(request,response);
            }else{
                staticContentServlet.service(request,response);
            }
        }
    }

    @Override
    public void destroy() {
        apiServlet = null;
        staticContentServlet = null;

    }
}

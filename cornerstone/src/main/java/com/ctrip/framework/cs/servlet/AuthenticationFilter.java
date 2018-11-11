package com.ctrip.framework.cs.servlet;

import com.ctrip.framework.cs.util.SecurityUtil;
import com.ctrip.framework.cs.util.HttpUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by jiang.j on 2017/1/5.
 */
public class AuthenticationFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contextPath = httpRequest.getContextPath();
        if(contextPath==null){
            contextPath = "";
        }
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();
        if(!path.startsWith(contextPath+"/@in/") && !path.equalsIgnoreCase(contextPath+"/@in")) {

            String user = SecurityUtil.getValidUserName(httpRequest);
            String reqUrl = httpRequest.getRequestURL().toString().toLowerCase();

            if (user == null) {
                String uri= contextPath+"/@in/index.html";
                HttpUtil.addCookie(httpResponse, SecurityUtil.JUMPKEY, reqUrl, "/", -1);
                httpResponse.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                httpResponse.setHeader("Location", uri);
                return;
            }
        }
        chain.doFilter(request,response);
    }

    @Override
    public void destroy() {

    }
}

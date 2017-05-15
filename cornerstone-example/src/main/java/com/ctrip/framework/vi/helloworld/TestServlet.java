package com.ctrip.framework.cornerstone.helloworld;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by jiang.j on 2016/3/21.
 */
public class TestServlet extends HttpServlet {

    @Override
    public void service(ServletRequest req, ServletResponse res)
            throws ServletException, IOException
    {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpRes = (HttpServletResponse) res;

        if(httpReq.getPathInfo().contains("abcd")){

            httpRes.setHeader("Content-type","text/html;charset=UTF-8");
            httpRes.getOutputStream().write("it's a test!!!!!!".getBytes("UTF-8"));
        }

    }
}

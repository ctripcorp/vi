package com.ctrip.framework.cs.security;

import com.ctrip.framework.cs.util.IPUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by jiang.j on 2017/5/22.
 */
public class GuardFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        if(req != null){
            int clientId = req.getHeader("clientId").hashCode();
            if(RequestGuard.isNeedBlock(BlockType.BlackClientID, clientId)){
                dealResponse(true,"clientid",response);

            }else if(RequestGuard.isNeedBlock(BlockType.GrayClientID, clientId)){
                dealResponse(false,"clientid",response);
            }
            int clientToken = req.getHeader("clientToken").hashCode();
            if(RequestGuard.isNeedBlock(BlockType.BlackClientToken, clientToken)){
                dealResponse(true,"clienttoken",response);

            }else if(RequestGuard.isNeedBlock(BlockType.GrayClientToken, clientToken)){
                dealResponse(false,"clienttoken",response);
            }

            int ip = IPUtil.getIPV4(req);

            if(RequestGuard.isNeedBlock(BlockType.BlackIP, ip)){
                dealResponse(true,"ip",response);
            }else if(RequestGuard.isNeedBlock(BlockType.GrayIP, ip)){
                dealResponse(false,"ip",response);
            }

        }

        chain.doFilter(request,response);
    }

    void dealResponse(boolean isBlack,String source, ServletResponse resp){

    }

    @Override
    public void destroy() {

    }
}

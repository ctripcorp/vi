package com.ctrip.framework.cs.enterprise;

import java.util.Map;

/**
 * Created by jiang.j on 2016/10/19.
 */
public class DefaultEnAuthentication implements EnAuthentication {

    @Override
    public String authentication(String user, String token, String ip,String requestUrl, Map<String, Object> parameters) throws AuthenticationFailedException {
        return "admin";
    }

    @Override
    public String getJumpUrl(String reqUrl) {
        return reqUrl;
    }

    @Override
    public String getLogoutUrl(String reqUrl) {
        return reqUrl.substring(0,reqUrl.lastIndexOf('/'));
    }
}

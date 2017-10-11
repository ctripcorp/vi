package com.ctrip.framework.cs.enterprise;

import java.util.Map;

/**
 * Created by jiang.j on 2016/10/19.
 */
public interface EnAuthentication {

    String authentication(String user,String token,String ip,String requestUrl,Map<String,Object> parameters) throws AuthenticationFailedException;
    String getJumpUrl(String reqUrl);
    String getLogoutUrl(String reqUrl);

}

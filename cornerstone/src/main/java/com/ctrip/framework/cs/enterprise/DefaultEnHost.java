package com.ctrip.framework.cs.enterprise;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by jiang.j on 2016/10/19.
 */
public class DefaultEnHost implements EnHost {
    @Override
    public String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    @Override
    public String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    @Override
    public String getDataCenter() {
        return "LOCAL";
    }

}

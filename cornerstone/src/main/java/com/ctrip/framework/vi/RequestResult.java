package com.ctrip.framework.vi;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang.j on 2016/12/26.
 */
public class RequestResult {
    public byte[] content = null;
    public String jumpUrl;
    public int responseCode;
    public boolean needCleanCookie = false;
    public String contentType;
    public String user;
    public String token;
    public Map<String,String> headers = new HashMap<>();
    public InputStream streamContent = null;
}

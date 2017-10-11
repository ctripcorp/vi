package com.ctrip.framework.cs.netty;

import com.ctrip.framework.cs.netty.http.Cookie;
import com.ctrip.framework.cs.util.IOUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jiang.j on 2016/12/20.
 */
public class NettyUtils {

    public final static String getCookieValueByName(Set<Cookie> cookies,String name){
        String rtn = null;
        for(Cookie cookie:cookies){
            String path = cookie.path();
            if(cookie.name().equals(name) && (path == null || "/".equals(path))){
                rtn = cookie.value();
                break;
            }
        }
        return rtn;
    }

    public static <T> T checkNotNull(T arg, String text) {
        if (arg == null) {
            throw new NullPointerException(text);
        }
        return arg;
    }


    public static Map<String,Object> loadPostReqParams(HttpContent content){

        Map<String,Object> params =null;

        try {
            Gson gson = new Gson();
            Type paraMap = new TypeToken<Map<String, JsonElement>>(){}.getType();
            ByteBufInputStream in = new ByteBufInputStream(content.content());
            String rawJson = IOUtils.readAll(in);
            params = gson.fromJson(rawJson,paraMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return params;
    }
    public final static Map<String,Object> getReqParams(HttpRequest req,String reqIP){

        Map<String,Object> params = new HashMap<>();
        QueryStringDecoder decoder = new QueryStringDecoder(req.getUri());


        for(Map.Entry<String,List<String>> para :decoder.parameters().entrySet()){

            params.put(para.getKey(),para.getValue().get(0));
        }
        params.put("req_ip",reqIP);
        return params;
    }
}

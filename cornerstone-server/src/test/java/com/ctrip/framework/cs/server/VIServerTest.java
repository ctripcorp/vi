package com.ctrip.framework.cs.server;

import com.ctrip.framework.cs.IgniteManager;
import com.ctrip.framework.cs.util.IOUtils;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by jiang.j on 2016/6/24.
 */
public class VIServerTest {
    public class CInfo{
        public String id;
    }
    @Test
    public void testVIServer() throws Exception {
        int port = 1998;
        VIServer server = new VIServer(port);
        server.start();
        URL url = new URL("http://localhost:"+port+"/@in/api/component/meta");
        Gson gson = new Gson();
        List<CInfo> infos= new ArrayList<>();
        String content = IOUtils.readAll((InputStream)url.getContent());
        System.out.println(IgniteManager.getStatus().getMessages());
        infos =gson.fromJson(content, infos.getClass());
        Assert.assertTrue(infos.size()>=3);
        server.stop();
        try{
            infos =gson.fromJson(IOUtils.readAll((InputStream)url.getContent()), infos.getClass());
            Assert.fail("can't reach there");
        }catch (Exception e){
            Assert.assertTrue(e instanceof ConnectException);
        }
    }
}


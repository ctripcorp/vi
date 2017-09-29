package com.ctrip.framework.vi.spring.example;

import com.ctrip.framework.vi.IgniteManager;
import com.ctrip.framework.vi.annotation.Ignite;
import com.ctrip.framework.vi.ignite.AbstractIgnitePlugin;
import com.ctrip.framework.vi.ignite.IgnitePlugin;
import com.ctrip.framework.vi.instrument.AgentTool;
import sun.net.www.http.HttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jiang.j on 2017/9/28.
 */
@Ignite(id="cornerstone.spring.test",auto = true)
public class TestIgnitePlugin implements IgnitePlugin{
    @Override
    public boolean run(IgniteManager.SimpleLogger logger) {

	logger.info("only for test");
        return true;
    }
}

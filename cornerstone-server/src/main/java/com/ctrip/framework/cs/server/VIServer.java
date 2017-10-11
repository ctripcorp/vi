package com.ctrip.framework.cs.server;

import com.ctrip.framework.cs.*;
import com.ctrip.framework.cs.servlet.AuthenticationFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.lang.invoke.MethodHandles;
import java.util.EnumSet;

/**
 * Created by jiang.j on 2016/6/24.
 */
public class VIServer {
    private volatile boolean running = false;
    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    Server server;
    ServletHandler handler;

    public static ServletHandler bind(Server server,String id){
        return bind(server,id,false);
    }
    public static ServletHandler bind(Server server,String id,boolean useVIAuthentication){
        ServletHandler handler = null;
        System.setProperty(SysKeys.TOMCATCONTEXTPATH, id);
        try {
            handler = new ServletHandler();
            server.setHandler(handler);

            handler.addServletWithMapping(VIApiServlet.class, "/@in/api/*");
            handler.addServletWithMapping(StaticContentServlet.class, "/@in/*");
            if(useVIAuthentication){
                handler.addFilterWithMapping(AuthenticationFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
            }
            IgniteManager.ignite();
        }catch (Exception e){
            logger.error("start vi server failed!",e);
        }
        return handler;
    }

    public VIServer(int listenPort,boolean useVIAuthentication) throws Exception {

        server = new Server(listenPort);
        handler = bind(server, String.valueOf(listenPort),useVIAuthentication);

    }

    public VIServer(int listenPort) throws Exception {
        this(listenPort,false);
    }

    public Server getInnerServer(){
        return server;
    }

    public ServletHandler getHandler(){
        return handler;
    }

    synchronized public void start() throws Exception {
        if(!running){
            server.start();
            running=true;
        }
    }

    synchronized public void stop() throws Exception {
        if(running){
            server.stop();
            running =false;
        }
    }
}

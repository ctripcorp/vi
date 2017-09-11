package com.ctrip.framework.vi.util;

import com.ctrip.framework.vi.IgniteManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

/**
 * Created by jiang.j on 2017/7/6.
 */
public class CheckHelper {

    private IgniteManager.SimpleLogger logger;
    private final int DEFAULT_TIMEOUT= 10000;
    private CheckHelper(IgniteManager.SimpleLogger logger){
        this.logger = logger;
    }

    public static CheckHelper create(IgniteManager.SimpleLogger logger){
        return new CheckHelper(logger);
    }

    public boolean telnet(String host,int port) throws IOException {
        if(host.startsWith("http")){
            host = new URL(host).getHost();
        }
        String tag = "telnet";
        logger.info(LogHelper.beginBlock(tag , new String[]{"host" , host , "port",  String.valueOf(port)}));
        InetAddress[] addresses;
        boolean canReach = false;
        try {
            addresses = InetAddress.getAllByName(host);
            for (InetAddress address : addresses) {
                InetSocketAddress socketAddress = new InetSocketAddress(address, port);
                logger.info("telnet " + address.getHostName() + "[" + address.getHostAddress() + "]");

                boolean reachable = false;
                try (Socket soc = new Socket()) {
                    soc.connect(socketAddress, DEFAULT_TIMEOUT);
                    reachable = true;
                    canReach = true;
                } catch (Throwable e) {
                    logger.error("connect failed!",e);

                }
                logger.info(address.getHostAddress() + " is " + (reachable ? "reachable" : "unreachable"));
            }
        }finally {
            logger.info(LogHelper.endBlock(tag ,new String[]{"isReachable",  String.valueOf(canReach)}));

        }

        return false;
    }
}

package com.ctrip.framework.vi.netty.example;

/**
 * Created by jiang.j on 2016/12/20.
 */
import com.ctrip.framework.vi.netty.VINettyHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpServerCodec;

public class HttpHelloWorldServerInitializer extends ChannelInitializer<SocketChannel> {


    public HttpHelloWorldServerInitializer() {
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpRequestDecoder());
        p.addLast(new HttpObjectAggregator(20248));
        p.addLast(new VINettyHandler());
        p.addLast(new HttpHelloWorldServerHandler());
    }
}

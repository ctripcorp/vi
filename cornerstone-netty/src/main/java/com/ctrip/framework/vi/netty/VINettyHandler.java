package com.ctrip.framework.vi.netty;

import com.ctrip.framework.vi.APIContentExecutor;
import com.ctrip.framework.vi.IgniteManager;
import com.ctrip.framework.vi.RequestResult;
import com.ctrip.framework.vi.StaticContentExecutor;
import com.ctrip.framework.vi.enterprise.EnFactory;
import com.ctrip.framework.vi.netty.http.Cookie;
import com.ctrip.framework.vi.netty.http.DefaultCookie;
import com.ctrip.framework.vi.netty.http.ServerCookieDecoder;
import com.ctrip.framework.vi.netty.http.ServerCookieEncoder;
import com.ctrip.framework.vi.util.SecurityUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.ssl.SslHandler;


import java.io.InputStream;
import java.net.*;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;
/**
 * Created by jiang.j on 2016/12/28.
 */
public class VINettyHandler extends ChannelInboundHandlerAdapter {

    static {
        IgniteManager.ignite();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private String getRemoteAddress(Channel channel){
        InetSocketAddress addr = (InetSocketAddress) channel.remoteAddress();
        return addr.getAddress().getHostAddress();
    }

    private boolean isSecure(Channel channel){

        return channel.pipeline().get(SslHandler.class) != null;
    }

    private String getRequestURL(Channel channel,HttpRequest req){
        StringBuffer url = new StringBuffer();
        String scheme = isSecure(channel)?"https":"http";
        InetSocketAddress addr = (InetSocketAddress)channel.localAddress();
        int port = addr.getPort();
        String urlPath = req.getUri();


        url.append(scheme); // http, https
        url.append("://");
        url.append(EnFactory.getEnHost().getHostAddress());
        if (("http".equalsIgnoreCase(scheme) && port != 80)
                || ("https".equalsIgnoreCase(scheme) && port != 443)) {
            url.append(':');
            url.append(port);
        }

        url.append(urlPath);
        return url.toString();
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) msg;
            URI reqUri = URI.create(req.getUri());
            String path = reqUri.getPath();

            if(!(path.equalsIgnoreCase("/vi") || path.startsWith("/vi/"))){
                ctx.fireChannelRead(msg);
                return;
            }

            HttpMethod httpMethod = req.getMethod();
            boolean isPost = httpMethod.equals(HttpMethod.POST);
            String rawCookie = req.headers().get((CharSequence)HttpHeaders.Names.COOKIE);
            Set<Cookie> cookies;

            if(rawCookie != null) {
                cookies = ServerCookieDecoder.LAX.decode(rawCookie);
            }else{
                cookies = new HashSet<>();
            }


            String user = NettyUtils.getCookieValueByName(cookies, SecurityUtil.USERKEY);
            String token = NettyUtils.getCookieValueByName(cookies, SecurityUtil.TOKENKEY);
            String ip = getRemoteAddress(ctx.channel());
            String autoJumpUrl = NettyUtils.getCookieValueByName(cookies, SecurityUtil.JUMPKEY);
            String reqUrl = getRequestURL(ctx.channel(), req);

            Map<String,Object> params = NettyUtils.getReqParams(req, ip);
            RequestResult exeResult =null;
            String callPath = null;
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
            try {
                if (path.startsWith("/vi/api/")) {
                    if (path.length() > 8) {
                        callPath = path.substring(7);
                    }

                    if(isPost){
                        params = NettyUtils.loadPostReqParams(req);
                        exeResult =
                                APIContentExecutor.doPost(callPath, params, user, token, ip);
                    }else {
                        exeResult =
                                APIContentExecutor.doGet(callPath, params, user, token, ip);
                    }

                } else {
                    if (path.length() > 3) {
                        callPath = path.substring(3);
                    }
                    exeResult =
                            StaticContentExecutor.getContent(reqUrl, "/vi", callPath, params, user, token, ip, autoJumpUrl);

                }
            }catch (Throwable e){

                response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                ctx.write(response);
                return;

            }

            if(exeResult == null){

                response.setStatus(HttpResponseStatus.NOT_FOUND);
                response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                ctx.write(response);
                return;
            }

            if(exeResult.user != null && exeResult.token != null){
                Cookie userCookie = new DefaultCookie(SecurityUtil.USERKEY,exeResult.user);
                userCookie.setPath("/");
                response.headers().add((CharSequence)SET_COOKIE, ServerCookieEncoder.STRICT.encode(userCookie));
                Cookie tokenCookie = new DefaultCookie(SecurityUtil.TOKENKEY,exeResult.token);
                tokenCookie.setPath("/");
                response.headers().add((CharSequence)SET_COOKIE, ServerCookieEncoder.STRICT.encode(tokenCookie));
            }

            if(exeResult.jumpUrl != null){
                response.setStatus(HttpResponseStatus.MOVED_PERMANENTLY);

                response.headers().set((CharSequence)"Location", exeResult.jumpUrl);
                if(exeResult.jumpUrl.equalsIgnoreCase(autoJumpUrl)){
                    Cookie jumpCookie = new DefaultCookie(SecurityUtil.JUMPKEY,"");
                    jumpCookie.setMaxAge(0);
                    jumpCookie.setPath("/");
                    response.headers().add((CharSequence)SET_COOKIE, ServerCookieEncoder.STRICT.encode(jumpCookie));
                }
            }else{

                for(Map.Entry<String,String> entry:exeResult.headers.entrySet()){
                    response.headers().add((CharSequence)entry.getKey(),(CharSequence)entry.getValue());
                }
                if(exeResult.content!=null) {
                    response.headers().set((CharSequence)CONTENT_TYPE, exeResult.contentType + ";charset=utf-8");
                    response.headers().set((CharSequence)TRANSFER_ENCODING,"chunked");
                    response.headers().set((CharSequence)SERVER,"vi netty");
                    response.setStatus(HttpResponseStatus.valueOf(exeResult.responseCode));
                    try {
                        response.content().writeBytes(exeResult.content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else if(exeResult.streamContent != null){
                    try(InputStream inputStream = exeResult.streamContent){
                       byte[] buf=new byte[8192];
                        //HttpChunkedInput httpChunkedInput = new HttpChunkedInput(new ChunkedStream(exeResult.streamContent,8192));
                        //ChannelFuture sendFuture = ctx.write(httpChunkedInput);
                        int bytesread = 0, bytesBuffered = 0;
                        while( (bytesread = inputStream.read( buf )) > -1 ) {
                            response.content().writeBytes(buf, 0, bytesread);
                            bytesBuffered += bytesread;
                            if (bytesBuffered > 2* 1024 * 1024) { //max file is 2MB
                                break;
                            }
                        }
                    }catch (Throwable e){
                        APIContentExecutor.logWarn("get " + reqUrl + " failed!", e);
                    }
                }

                response.headers().set((CharSequence)CONTENT_LENGTH, response.content().readableBytes());
            }
            ctx.write(response);

        }
    }
}

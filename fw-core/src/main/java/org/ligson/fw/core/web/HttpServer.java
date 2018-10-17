package org.ligson.fw.core.web;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.ligson.fw.core.Context;
import org.ligson.fw.core.vo.Bean;
import org.ligson.fw.core.web.annotation.EnableWeb;
import org.ligson.fw.core.web.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpServer {
    private EnableWeb enableWeb;
    private List<Bean> controllers = new ArrayList<>();
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private ServerBootstrap serverBootstrap;
    private Map<String, HttpInvoke> httpInvokeMap = new HashMap<>();
    private Context context;

    private void scanRequestMapping() {
        for (Bean controller : controllers) {
            Class target = controller.getTargetClass();
            Annotation type = target.getDeclaredAnnotation(RequestMapping.class);
            String url = "";
            if (type != null) {
                url = ((RequestMapping) type).value();
            }
            Method[] methods = target.getDeclaredMethods();
            for (Method method : methods) {
                Annotation methodDeclaredAnnotation = method.getDeclaredAnnotation(RequestMapping.class);
                if (methodDeclaredAnnotation != null) {
                    RequestMapping requestMapping = ((RequestMapping) methodDeclaredAnnotation);
                    HttpInvoke httpInvoke = new HttpInvoke(controller, method, url + requestMapping.value(), requestMapping.method());
                    httpInvokeMap.put(httpInvoke.getUrl() + "-" + requestMapping.method().name(), httpInvoke);
                }
            }
        }
    }

    public HttpServer(EnableWeb enableWeb, List<Bean> controllers, Context context) {
        this.context = context;
        this.enableWeb = enableWeb;
        this.controllers = controllers;
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        // server端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
                        ch.pipeline().addLast(new HttpResponseEncoder());
                        // server端接收到的是httpRequest，所以要使用HttpRequestDecoder进行解码
                        ch.pipeline().addLast(new HttpRequestDecoder());
                        ch.pipeline().addLast(new HttpServerInboundHandler(httpInvokeMap, context));
                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        scanRequestMapping();

    }

    public void start() throws Exception {
        try {
            ChannelFuture f = serverBootstrap.bind(enableWeb.port()).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }
}

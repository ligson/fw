package org.ligson.fw.core.web;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.ligson.fw.core.Context;
import org.ligson.fw.core.vo.Bean;
import org.ligson.fw.core.web.util.URLParser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpServerInboundHandler extends SimpleChannelInboundHandler<HttpObject> {
    private HttpRequest request;
    private Map<String, HttpInvoke> httpInvokeMap;
    private Context context;

    public HttpServerInboundHandler(Map<String, HttpInvoke> httpInvokeMap, Context context) {
        this.httpInvokeMap = httpInvokeMap;
        this.context = context;
    }

    private Object convertArg2Value(Class argType, List<String> values) {
        Object object;

        if (argType.isArray()) {
            return null;
        } else {
            if (CollectionUtils.isEmpty(values)) {
                if (argType == int.class) {
                    object = 0;
                } else if (argType == long.class) {
                    object = 0L;
                } else if (argType == short.class) {
                    object = 0;
                } else if (argType == double.class) {
                    object = 0f;
                } else if (argType == String.class) {
                    object = null;
                } else {
                    object = null;
                }
            } else {
                String string = values.get(0);
                if (argType == int.class) {
                    object = Integer.parseInt(string);
                } else if (argType == long.class) {
                    object = Long.parseLong(string);
                } else if (argType == short.class) {
                    object = Short.parseShort(string);
                } else if (argType == double.class) {
                    object = Double.parseDouble(string);
                } else if (argType == String.class) {
                    object = string;
                } else {
                    try {
                        Constructor stringConstructor = argType.getDeclaredConstructor(String.class);
                        object = stringConstructor.newInstance(string);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return object;
    }

    private Object[] convertMethodArgs(Method method, Map<String, List<String>> param) {
        Parameter[] parameters = method.getParameters();
        Object[] objects = new Object[parameters.length];
        int i = 0;
        ClassPool pool = ClassPool.getDefault();
        CtClass ct = null;
        try {
            ct = pool.get(method.getDeclaringClass().getName());
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        CtClass[] paramTypes = new CtClass[parameters.length];
        for (int i1 = 0; i1 < parameters.length; i1++) {
            try {
                paramTypes[i1] = pool.get(parameters[i1].getType().getName());
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        CtMethod ctMethod = null;
        try {
            ctMethod = ct.getDeclaredMethod(method.getName(), paramTypes);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        MethodInfo methodInfo = ctMethod.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attribute = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        for (Parameter parameter : parameters) {
            String name = attribute.variableName(i + 1);
            List<String> values = param.get(name);
            Object object = convertArg2Value(parameter.getType(), values);
            objects[i++] = object;
        }
        return objects;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            String uri = request.uri();
            System.out.println("Uri:" + uri);
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            ByteBuf buf = content.content();
            System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));
            buf.release();
            String path = URLParser.getPath(request.uri());
            Map<String, List<String>> param = URLParser.getParam(request.uri());
            HttpInvoke httpInvoke = httpInvokeMap.get(path + "-" + request.method().name());
            if (httpInvoke == null) {
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                        HttpResponseStatus.NOT_FOUND, Unpooled.wrappedBuffer("404".getBytes("UTF-8")));
                ctx.write(response);
                ctx.flush();
                return;
            }
            Bean bean = httpInvoke.getBean();
            Method method = httpInvoke.getMethod();
            Object[] paramValues = convertMethodArgs(method, param);
            if (bean.getInstance() == null) {
                context.get(bean.getTargetClass());
            }
            Object result = method.invoke(bean.getInstance(), paramValues);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                    OK, Unpooled.wrappedBuffer(result.toString().getBytes("UTF-8")));
            response.headers().set(CONTENT_TYPE, "text/plain");
            response.headers().set(CONTENT_LENGTH,
                    response.content().readableBytes());
            if (HTTP_1_1.isKeepAliveDefault()) {
                response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
            ctx.write(response);
            ctx.flush();
        }
    }
}

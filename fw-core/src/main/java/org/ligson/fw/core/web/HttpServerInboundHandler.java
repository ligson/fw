package org.ligson.fw.core.web;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ligson.fw.core.Context;
import org.ligson.fw.core.util.ReflectUtil;
import org.ligson.fw.core.web.annotation.RequestBody;
import org.ligson.fw.core.web.annotation.RequestMapping;
import org.ligson.fw.core.web.util.URLParser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
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

    private HttpInvoke checkNotFound(ChannelHandlerContext ctx, String path) {
        HttpInvoke httpInvoke = httpInvokeMap.get(path + "-" + request.method().name());
        if (httpInvoke == null) {
            FullHttpResponse response = null;
            try {
                response = new DefaultFullHttpResponse(HTTP_1_1,
                        HttpResponseStatus.NOT_FOUND, Unpooled.wrappedBuffer("404".getBytes("UTF-8")));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            response.headers().set(CONTENT_TYPE, "text/plain");
            response.headers().set(CONTENT_LENGTH,
                    response.content().readableBytes());
            ctx.write(response);
            ctx.flush();
        }
        return httpInvoke;
    }

    private Object[] convertParamValue(HttpContent content, String path, HttpInvoke httpInvoke) {
        ByteBuf buf = content.content();
        String body = buf.toString(io.netty.util.CharsetUtil.UTF_8);
        log.debug("接收到body:{}", body);
        if (httpInvoke.getMethod().getParameters().length > 0) {
            RequestBody requestBody = httpInvoke.getMethod().getParameters()[0].getDeclaredAnnotation(RequestBody.class);
            if (requestBody != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                Object object = null;
                try {
                    object = objectMapper.readValue(body, httpInvoke.getMethod().getParameters()[0].getType());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return new Object[]{object};
            }
        }
        buf.release();
        String fullPath = path;
        if (StringUtils.isNotBlank(body)) {
            if (fullPath.contains("?")) {
                fullPath += "&" + body;
            } else {
                fullPath += "?" + body;
            }
        }
        Map<String, List<String>> param = URLParser.getParam(fullPath);
        Method method = httpInvoke.getMethod();
        return convertMethodArgs(method, param);
    }

    private byte[] convertResult2Bytes(String contentType, Object result) {
        if (contentType.contains("json")) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(result).getBytes("UTF-8");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (contentType.contains("text")) {
            try {
                return result.toString().getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else if (result instanceof byte[]) {
            return (byte[]) result;
        } else {
            throw new RuntimeException("未知返回类型");
        }
    }

    private void writeResponse(ChannelHandlerContext ctx, Object result, Method method) {
        RequestMapping requestMapping = ReflectUtil.getMethodAnnontation(method, RequestMapping.class);
        assert requestMapping != null;
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                OK, Unpooled.wrappedBuffer(convertResult2Bytes(requestMapping.acceptType(), result)));

        response.headers().set(CONTENT_TYPE, requestMapping.acceptType());
        response.headers().set(CONTENT_LENGTH,
                response.content().readableBytes());
        if (HTTP_1_1.isKeepAliveDefault()) {
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.write(response);
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            String uri = request.uri();
            log.debug("访问地址:{},method:{}", uri, request.method().name());
        }
        if (msg instanceof HttpContent) {
            String path = URLParser.getPath(request.uri());
            HttpInvoke httpInvoke = checkNotFound(ctx, path);
            if (httpInvoke != null) {
                if (httpInvoke.getBean().getInstance() == null) {
                    context.get(httpInvoke.getBean().getTargetClass());
                }
                HttpContent content = (HttpContent) msg;
                Object[] paramValues = convertParamValue(content, path, httpInvoke);
                Object result = httpInvoke.getMethod().invoke(httpInvoke.getBean().getInstance(), paramValues);
                if (httpInvoke.getMethod().getReturnType().getName().equals("void")) {

                } else {
                    writeResponse(ctx, result, httpInvoke.getMethod());
                }

            }
        }
    }
}

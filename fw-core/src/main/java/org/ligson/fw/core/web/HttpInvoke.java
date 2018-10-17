package org.ligson.fw.core.web;

import org.ligson.fw.core.vo.Bean;
import org.ligson.fw.core.web.enums.HttpMethod;

import java.lang.reflect.Method;

public class HttpInvoke {
    private Bean bean;
    private Method method;
    private String url;
    private HttpMethod httpMethod;

    public HttpInvoke(Bean bean, Method method, String url, HttpMethod httpMethod) {
        this.bean = bean;
        this.method = method;
        this.url = url;
        this.httpMethod = httpMethod;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Bean getBean() {
        return bean;
    }

    public void setBean(Bean bean) {
        this.bean = bean;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

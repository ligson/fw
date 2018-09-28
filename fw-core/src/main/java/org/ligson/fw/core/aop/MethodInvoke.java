package org.ligson.fw.core.aop;

import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class MethodInvoke {
    private Method method;
    private MethodProxy methodProxy;

    public MethodInvoke(Method method) {
        this.method = method;
    }

    public MethodInvoke(MethodProxy methodProxy) {
        this.methodProxy = methodProxy;
    }

    public Object invoke(Object object, Object[] args) throws Throwable {
        if (method != null) {
            return method.invoke(object, args);

        }
        if (methodProxy != null) {
            return methodProxy.invokeSuper(object, args);
        }
        return null;
    }
}

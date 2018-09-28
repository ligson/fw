package org.ligson.fw.core.aop;

import java.lang.reflect.Method;

public abstract class AopFilter {
    public abstract String pattern();

    public void before(Object object, Method method, Object[] args) {

    }

    public void after(Object object, Method method, Object[] args) {

    }

    public Object around(Object object, Method method, Object[] args, MethodInvoke methodInvoke) throws Throwable {
        return methodInvoke.invoke(object, args);
    }


}

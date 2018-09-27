package org.ligson.fw.core.aop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class AopFilter {
    public abstract String pattern();

    public void before(Object object, Method method, Object[] args) {

    }

    public void after(Object object, Method method, Object[] args) {

    }

    public Object around(Object object, Method method, Object[] args) {
        try {
            return method.invoke(object, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}

package org.ligson.fw.core;

import org.ligson.fw.core.aop.AopFilter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class InvocationHandlerImpl implements InvocationHandler {
    private Object target;
    private List<AopFilter> aopFilters = new ArrayList<>();

    public InvocationHandlerImpl(Object target, List<AopFilter> aopFilters) {
        this.target = target;
        this.aopFilters = aopFilters;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        String methodName = target.getClass().getName() + "." + method.getName();
        //before
        for (AopFilter aopFilter : aopFilters) {
            if (methodName.matches(aopFilter.pattern())) {
                aopFilter.before(target, method, args);
            }
        }
        //around
        for (AopFilter aopFilter : aopFilters) {
            if (methodName.matches(aopFilter.pattern())) {
                if (result == null) {
                    result = aopFilter.around(target, method, args);
                } else {
                    break;
                }
            }
        }
        if (aopFilters.size() == 0) {
            result = method.invoke(target, args);
        }
        //after
        for (AopFilter aopFilter : aopFilters) {
            if (methodName.matches(aopFilter.pattern())) {
                aopFilter.after(target, method, args);
            }
        }
        return result;
    }
}

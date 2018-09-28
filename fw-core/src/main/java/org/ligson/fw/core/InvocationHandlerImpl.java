package org.ligson.fw.core;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.ligson.fw.core.aop.AopFilter;
import org.ligson.fw.core.aop.MethodInvoke;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class InvocationHandlerImpl implements InvocationHandler, MethodInterceptor {
    private Object target;
    private List<AopFilter> aopFilters = new ArrayList<>();

    public InvocationHandlerImpl(Object target, List<AopFilter> aopFilters) {
        this.target = target;
        this.aopFilters = aopFilters;
    }

    public InvocationHandlerImpl(List<AopFilter> aopFilters) {
        this.aopFilters = aopFilters;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    private Object invoke(Object proxy, Method method, Object[] args, MethodInvoke methodInvoke) throws Throwable {
        Object result = null;
        String methodName = target.getClass().getName() + "." + method.getName();
        System.out.println("invoke method:" + methodName);
        System.out.println(target instanceof AopFilter);
        if (target instanceof AopFilter) {
            return method.invoke(target, args);
        }
        //before
        System.out.println("之前1");
        for (AopFilter aopFilter : aopFilters) {
            if (methodName.matches(aopFilter.pattern())) {
                aopFilter.before(target, method, args);
            }
        }
        System.out.println("之前2");
        //around
        for (AopFilter aopFilter : aopFilters) {
            if (methodName.matches(aopFilter.pattern())) {
                if (result == null) {
                    result = aopFilter.around(proxy, method, args, methodInvoke);
                } else {
                    break;
                }
            }
        }
        if (aopFilters.size() == 0) {
            result = method.invoke(target, args);
        }
        System.out.println("exc success");
        //after
        for (AopFilter aopFilter : aopFilters) {
            if (methodName.matches(aopFilter.pattern())) {
                aopFilter.after(target, method, args);
            }
        }
        System.out.println("exc after");
        return result;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodInvoke methodInvoke = new MethodInvoke(method);
        return invoke(target, method, args, methodInvoke);
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        MethodInvoke methodInvoke = new MethodInvoke(proxy);
        return invoke(obj, method, args, methodInvoke);
    }
}

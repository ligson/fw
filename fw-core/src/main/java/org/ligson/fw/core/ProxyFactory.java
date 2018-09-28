package org.ligson.fw.core;

import net.sf.cglib.proxy.Enhancer;
import org.apache.commons.lang3.ArrayUtils;
import org.ligson.fw.core.annotation.Autowire;
import org.ligson.fw.core.aop.AopFilter;
import org.ligson.fw.core.util.PropertyMapperUtil;
import org.ligson.fw.core.vo.Bean;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

public class ProxyFactory {
    private boolean enableCglibProxy;

    public ProxyFactory(boolean enableCglibProxy) {
        this.enableCglibProxy = enableCglibProxy;
    }

    private Object fillField(Object object, Class targetClazz, Map<String, Bean> map, Map<Class, List<Class>> interfaceImpl, List<AopFilter> aopFilters) {
        Field[] fields = targetClazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                Autowire autowire = field.getDeclaredAnnotation(Autowire.class);
            } catch (Exception e) {
                continue;
            }
            field.setAccessible(true);
            String camelName = PropertyMapperUtil.convert2CamelCase(field.getType().getSimpleName());
            if (field.getType().isInterface()) {
                List<Class> clazz = interfaceImpl.get(field.getType());
                Object fieldObj = proxy(clazz.get(0), map, interfaceImpl, aopFilters);
                try {
                    field.set(object, fieldObj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                Bean bean = map.get(camelName);
                if (bean.getInstance() == null) {
                    Object fieldObject = proxy(field.getType(), map, interfaceImpl, aopFilters);
                    try {
                        field.set(object, fieldObject);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }
        return object;
    }

    private Object cglibProxy(Class targetClazz, Map<String, Bean> map, Map<Class, List<Class>> interfaceImpl, List<AopFilter> aopFilters) {
        System.out.println("生成proxy:" + targetClazz.getName());
        if (targetClazz.getSuperclass() == AopFilter.class) {
            return jdkProxy(targetClazz, map, interfaceImpl, aopFilters);
        }
        InvocationHandlerImpl invocationHandler = new InvocationHandlerImpl(aopFilters);
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClazz);
        enhancer.setCallback(invocationHandler);
        enhancer.setInterfaces(targetClazz.getInterfaces());
        Object object = enhancer.create();
        invocationHandler.setTarget(object);
        object = fillField(object, targetClazz, map, interfaceImpl, aopFilters);
        return object;
    }

    private Object jdkProxy(Class targetClazz, Map<String, Bean> map, Map<Class, List<Class>> interfaceImpl, List<AopFilter> aopFilters) {
        Object object = null;
        try {
            object = targetClazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        object = fillField(object, targetClazz, map, interfaceImpl, aopFilters);
        System.out.println(targetClazz.getName());
        if (ArrayUtils.isNotEmpty(targetClazz.getInterfaces())) {
            InvocationHandlerImpl invocationHandler = new InvocationHandlerImpl(object, aopFilters);
            return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), object.getClass().getInterfaces(), invocationHandler);
        } else {
            return object;
        }
    }

    public Object proxy(Class targetClazz, Map<String, Bean> map, Map<Class, List<Class>> interfaceImpl, List<AopFilter> aopFilters) {
        return enableCglibProxy ? cglibProxy(targetClazz, map, interfaceImpl, aopFilters) : jdkProxy(targetClazz, map, interfaceImpl, aopFilters);
    }
}

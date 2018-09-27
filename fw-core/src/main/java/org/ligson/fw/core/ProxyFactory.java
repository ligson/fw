package org.ligson.fw.core;

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

    public Object proxy(Class targetClazz, Map<String, Bean> map, Map<Class, List<Class>> interfaceImpl, List<AopFilter> aopFilters) {
        Object object = null;
        try {
            object = targetClazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
        if (ArrayUtils.isNotEmpty(targetClazz.getInterfaces())) {
            InvocationHandlerImpl invocationHandler = new InvocationHandlerImpl(object, aopFilters);
            return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), object.getClass().getInterfaces(), invocationHandler);
        } else {
            return object;
        }
    }
}

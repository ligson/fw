package org.ligson.fw.core;

import org.apache.commons.lang3.ArrayUtils;
import org.ligson.fw.core.annotation.Autowire;
import org.ligson.fw.core.vo.Bean;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class Context {
    private Map<Class, Bean> map = new HashMap<>();

    public Object initBean(Class clazz) {
        if (clazz.isInterface()) {
            return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{clazz}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return method.invoke(proxy, args);
                }
            });
        } else {
            Object object = null;
            try {
                object = clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return object;
        }
    }

    public void put(Class clazz) {
        Bean bean = new Bean();
        bean.setId(clazz + "-0");
        bean.setaClass(clazz);
        bean.setInstance(initBean(clazz));
        map.put(clazz, bean);
    }

    public Bean get(Class clazz) {
        Bean bean = null;
        if (clazz.isInterface()) {
            for (Class aClass : map.keySet()) {
                if (ArrayUtils.contains(aClass.getInterfaces(), clazz)) {
                    bean = map.get(aClass);
                    break;
                }
            }
        } else {
            bean = map.get(clazz);
        }
        Field[] fields = bean.getaClass().getDeclaredFields();
        for (Field field : fields) {
            Autowire autowire = null;
            try {
                autowire = field.getDeclaredAnnotation(Autowire.class);
            } catch (Exception e) {
                continue;
            }
            try {
                field.setAccessible(true);
                if (field.get(bean.getInstance()) == null) {
                    Bean bean1 = get(field.getType());
                    field.set(bean.getInstance(), bean1.getInstance());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return bean;
    }

}

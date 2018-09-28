package org.ligson.fw.core;

import org.ligson.fw.core.annotation.FWApp;
import org.ligson.fw.core.aop.AopFilter;
import org.ligson.fw.core.util.PropertyMapperUtil;
import org.ligson.fw.core.vo.Bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {
    private Map<String, Bean> beanMap = new HashMap<>();
    private ProxyFactory proxyFactory;
    private Map<Class, List<Class>> interfaceImpl = new HashMap<>();
    private List<AopFilter> aopFilters = new ArrayList<>();
    private FWApp fwApp;

    public Context(FWApp fwApp) {
        this.fwApp = fwApp;
        proxyFactory = new ProxyFactory(fwApp.enableCglibProxy());
    }

    public void initAopFilters() {
        for (String beanId : beanMap.keySet()) {
            Bean bean = beanMap.get(beanId);
            if (bean.getTargetClass().getSuperclass() == AopFilter.class) {
                Object object = proxyFactory.proxy(bean.getTargetClass(), beanMap, interfaceImpl, aopFilters);
                if (object == null) {
                    throw new RuntimeException("create bean error");
                }
                bean.setInstance(object);
                aopFilters.add((AopFilter) object);
            }
        }
    }

    public void put(Class clazz) {
        if (clazz.isInterface()) {
            if (!interfaceImpl.containsKey(clazz)) {
                interfaceImpl.put(clazz, new ArrayList<>());
            }
        } else {
            Class[] inters = clazz.getInterfaces();
            for (Class inter : inters) {
                if (!interfaceImpl.containsKey(inter)) {
                    interfaceImpl.put(inter, new ArrayList<>());
                }
                List<Class> impls = interfaceImpl.get(inter);
                if (!impls.contains(clazz)) {
                    impls.add(clazz);
                }
            }
            Bean bean = new Bean();
            bean.setId(PropertyMapperUtil.convert2CamelCase(clazz.getSimpleName()));
            bean.setTargetClass(clazz);
            //bean.setInstance(initBean(clazz));
            beanMap.put(bean.getId(), bean);
        }
    }


    public Bean get(Class clazz) {
        Bean bean = null;
        if (clazz.isInterface()) {
            List<Class> impls = interfaceImpl.get(clazz);
            Class impl = impls.get(0);
            for (String name : beanMap.keySet()) {
                Class aClass = beanMap.get(name).getTargetClass();
                if (aClass == impl) {
                    bean = beanMap.get(name);
                    break;
                }
            }
        } else {
            String name = PropertyMapperUtil.convert2CamelCase(clazz.getSimpleName());
            bean = beanMap.get(name);
        }
        if (bean == null) {
            return null;
        }
        if (bean.getInstance() == null) {
            Object object = proxyFactory.proxy(bean.getTargetClass(), beanMap, interfaceImpl, aopFilters);
            if (object == null) {
                return null;
            }
            bean.setInstance(object);
        }
        return bean;
    }

}

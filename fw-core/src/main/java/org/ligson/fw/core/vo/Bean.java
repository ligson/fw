package org.ligson.fw.core.vo;

public class Bean {
    private String id;
    private Class targetClass;
    private Object instance;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }
}

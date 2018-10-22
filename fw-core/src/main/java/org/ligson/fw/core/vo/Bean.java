package org.ligson.fw.core.vo;

import lombok.Data;

@Data
public class Bean {
    private String id;
    private Class targetClass;
    private Object instance;
}

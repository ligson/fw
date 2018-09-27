package org.ligson.fw.core.ex;

import java.lang.reflect.Field;

public class UninitException extends Exception {
    private Field field;

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
}

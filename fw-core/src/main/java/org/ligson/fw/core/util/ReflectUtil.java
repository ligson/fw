package org.ligson.fw.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class ReflectUtil {
    public static <T extends Annotation> T getMethodAnnontation(Method method, Class<T> annontationType) {
        return method.getDeclaredAnnotation(annontationType);
    }
}

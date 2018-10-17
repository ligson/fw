package org.ligson.fw.core.web.annotation;


import org.ligson.fw.core.web.enums.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    String value();

    HttpMethod method() default HttpMethod.GET;

    /***
     * header Content-Type
     * @return
     */
    String contentType() default "application/json";

    /***
     * header Accept
     * @return
     */
    String acceptType() default "application/json";

}

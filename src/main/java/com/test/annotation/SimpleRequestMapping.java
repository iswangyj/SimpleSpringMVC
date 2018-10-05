package com.test.annotation;

import java.lang.annotation.*;

/**
 * @author wyj
 * @date 2018/10/5
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SimpleRequestMapping {
    /**
     * 访问该方法的url
     *
     * @return
     */
    String value() default "";
}

package com.test.annotation;

import java.lang.annotation.*;

/**
 * @author wyj
 * @date 2018/10/5
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SimpleController {
    /**
     * 为controller注册别名
     *
     * @return
     */
    String value() default "";
}

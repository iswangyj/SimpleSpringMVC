package com.test.annotation;

import java.lang.annotation.*;

/**
 * @author wyj
 * @date 2018/10/5
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SimpleRequestParam {
    /**
     * 表示参数别名，必需填写
     *
     * @return
     */
    String value();
}

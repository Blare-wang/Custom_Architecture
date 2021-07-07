package com.itblare.archetype.support.annotation;

import java.lang.annotation.*;

/**
 * 响应结果封装注解
 *
 * @author Blare
 * @version 1.0.0
 * @since 2021/7/7 10:54
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseWrapper {
}

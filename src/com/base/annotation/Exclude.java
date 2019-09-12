package com.loit.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * @Auther: zhaoke
 * @Date: 2019/8/7 0007 15:17
 * @Description: 自定义注解：过滤非数据库字段
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Exclude {
}

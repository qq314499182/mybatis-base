package com.loit.base.annotation;

import java.lang.annotation.*;

/**
 * @Auther: zhaoke
 * @Date: 2019/8/7 0007 16:17
 * @Description: 自定义注解：设置数据库表面
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TableName {

    String name() default "";
}

package com.beiwei.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestParameter {
    String value();
    /**
     *  @MyRequestParameter注解
     *      1.相当于SpringMVC中的@RequestParameter注解.
     *      2.作用于方法参数上面,用来表示当前传入参数的别名.必须
     *      3.相当于前端请求传入一个username,而方法名中用name,如果不用注解,那Springmvc不会自动放值,此时需要给参数name
     *      加上注解@RequestParameter("username");
     */
}

package com.beiwei.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {
    String value() default "";
}


// springmvc中比较重要的注解,通过给Action添加@controller注解,用来处理DispatcherServlet转发的请求;
// 还有类似的 @service--服务层,@repository--仓库,@component--其他成分;
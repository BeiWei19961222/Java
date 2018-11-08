package com.beiwei.annotation;

import java.lang.annotation.*;

/**
 * @MyRequestMapping注解:
 *      1.类似于SpringMVC中的RequestMapping.
 *      2.作用在类,方法上.
 *      3.用来表示当前类or方法请求路径,比如说方法的请求路径为: url:端口/项目名/类请求路径/方法请求路径
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {
    String value() default "";
}

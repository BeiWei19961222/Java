package com.beiwei.annotation;

import java.lang.annotation.*;
import java.lang.reflect.Method;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface test {
//        String id();
//        String name();
//        String print () default "haha";
        String value();
}

/**
 * 注解:
 *    1.注解本身是一个接口类.
 *    2.注解在java.lang包中,java自带了几个常用的注解.比如说@override,@documented等等..
 *    3.声明注解常用的三个注解标志.
 *           1.@Target:表示当前注解所应用到的作用范围,可以有多个(类:TYPE,方法:METHOD,域:FIELD,参数:PARAMETER..)
 *           2.@Retention:表示标签的有效区域(java文件:SOURCE,class编译时候:CLASS,运行时候:RUNTIME)
 *           3.Documented:表示将当前注解加入到doc文档中;
 *    4.注解跟接口一样,使用时需要通过(方法名=参数)'实现'方法.带有默认值的不需要.
 *    5.注解应该是有个默认的父类,有其他方法.
 *    6.注解如果是value()方法,则直接用@注解(值)即可.
 */


@test("1")
class AnnotationTest{
    /**
     * 注解的实现有很大部分是跟反射挂钩的.
     * class类可以通过反射来获取到当前类的注解.
     * 注解也是一个类,然后通过注解类获取到方法等等重要信息;
     */
    public void printInfo(){
        // 反射reflection.通过反射可以获取类的所有属性(注解,方法,参数,成员变量)
        Annotation[] annotations = this.getClass().getAnnotations();
        for(Annotation obj:annotations){
            Class<? extends Annotation> clazz = obj.annotationType();
            Method[] methods = clazz.getMethods();
            for(Method method:methods){
                System.out.println("方法名:"+method.getName());
            }
        }
    }

    public static void main(String[] args) {
        new AnnotationTest().printInfo();
    }
}

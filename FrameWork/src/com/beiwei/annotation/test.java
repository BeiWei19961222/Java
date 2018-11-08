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
 * ע��:
 *    1.ע�Ȿ����һ���ӿ���.
 *    2.ע����java.lang����,java�Դ��˼������õ�ע��.����˵@override,@documented�ȵ�..
 *    3.����ע�ⳣ�õ�����ע���־.
 *           1.@Target:��ʾ��ǰע����Ӧ�õ������÷�Χ,�����ж��(��:TYPE,����:METHOD,��:FIELD,����:PARAMETER..)
 *           2.@Retention:��ʾ��ǩ����Ч����(java�ļ�:SOURCE,class����ʱ��:CLASS,����ʱ��:RUNTIME)
 *           3.Documented:��ʾ����ǰע����뵽doc�ĵ���;
 *    4.ע����ӿ�һ��,ʹ��ʱ��Ҫͨ��(������=����)'ʵ��'����.����Ĭ��ֵ�Ĳ���Ҫ.
 *    5.ע��Ӧ�����и�Ĭ�ϵĸ���,����������.
 *    6.ע�������value()����,��ֱ����@ע��(ֵ)����.
 */


@test("1")
class AnnotationTest{
    /**
     * ע���ʵ���кܴ󲿷��Ǹ�����ҹ���.
     * class�����ͨ����������ȡ����ǰ���ע��.
     * ע��Ҳ��һ����,Ȼ��ͨ��ע�����ȡ�������ȵ���Ҫ��Ϣ;
     */
    public void printInfo(){
        // ����reflection.ͨ��������Ի�ȡ�����������(ע��,����,����,��Ա����)
        Annotation[] annotations = this.getClass().getAnnotations();
        for(Annotation obj:annotations){
            Class<? extends Annotation> clazz = obj.annotationType();
            Method[] methods = clazz.getMethods();
            for(Method method:methods){
                System.out.println("������:"+method.getName());
            }
        }
    }

    public static void main(String[] args) {
        new AnnotationTest().printInfo();
    }
}

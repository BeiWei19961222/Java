package com.beiwei.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {
    String value() default "";
}


// springmvc�бȽ���Ҫ��ע��,ͨ����Action���@controllerע��,��������DispatcherServletת��������;
// �������Ƶ� @service--�����,@repository--�ֿ�,@component--�����ɷ�;
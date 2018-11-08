package com.beiwei.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestParameter {
    String value();
    /**
     *  @MyRequestParameterע��
     *      1.�൱��SpringMVC�е�@RequestParameterע��.
     *      2.�����ڷ�����������,������ʾ��ǰ��������ı���.����
     *      3.�൱��ǰ��������һ��username,������������name,�������ע��,��Springmvc�����Զ���ֵ,��ʱ��Ҫ������name
     *      ����ע��@RequestParameter("username");
     */
}

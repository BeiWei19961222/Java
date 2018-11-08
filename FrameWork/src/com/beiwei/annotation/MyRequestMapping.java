package com.beiwei.annotation;

import java.lang.annotation.*;

/**
 * @MyRequestMappingע��:
 *      1.������SpringMVC�е�RequestMapping.
 *      2.��������,������.
 *      3.������ʾ��ǰ��or��������·��,����˵����������·��Ϊ: url:�˿�/��Ŀ��/������·��/��������·��
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {
    String value() default "";
}

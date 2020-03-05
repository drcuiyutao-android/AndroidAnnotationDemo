package com.drcuiyutao.lib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 属性只支持int, boolean, long, float, double, String, 注入属性必须为static final
@Target(value = {ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Insert {
    Class target();

    String name() default "";

    // 注入方法是否在object中
    boolean sorceInKotlinObject() default false;

    // 目标方法是否在companion object中
    boolean targetInKotlinCompanion() default false;

    // 注入方法是否在companion object中
    boolean sorceInKotlinCompanion() default false;

    int position() default 0;

    boolean beforesuper() default false;

    boolean replace() default false;

    String addCatch() default "";

    String fieldClzName() default "";
}

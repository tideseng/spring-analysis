package com.tideseng.spring.framework.aop.aspect;

import java.lang.reflect.Method;

public interface MyJoinPoint {

    Object getThis(); // 被代理类

    Method getMethod(); // 被代理方法

    Object[] getArguments(); // 被代理方法参数

    void setUserAttribute(String key, Object value);

    Object getUserAttribute(String key);

}

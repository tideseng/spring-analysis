package com.tideseng.spring.framework.aop;

/**
 * 代理顶层设计
 */
public interface MyAopProxy {

    Object getProxy();

    Object getProxy(ClassLoader classLoader);

}

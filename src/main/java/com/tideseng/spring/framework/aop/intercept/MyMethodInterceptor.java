package com.tideseng.spring.framework.aop.intercept;

public interface MyMethodInterceptor {

    Object invoke(MyMethodInvocation mi) throws Throwable;

}

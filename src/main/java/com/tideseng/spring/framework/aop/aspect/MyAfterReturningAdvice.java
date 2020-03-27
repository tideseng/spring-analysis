package com.tideseng.spring.framework.aop.aspect;

import com.tideseng.spring.framework.aop.intercept.MyMethodInterceptor;
import com.tideseng.spring.framework.aop.intercept.MyMethodInvocation;

import java.lang.reflect.Method;

public class MyAfterReturningAdvice extends MyAbstractAspectJAdvice implements MyMethodInterceptor {

    private MyJoinPoint joinPoint;

    public MyAfterReturningAdvice(Object aspectTarget, Method aspectMethod) {
        super(aspectTarget, aspectMethod);
    }

    @Override
    public Object invoke(MyMethodInvocation mi) throws Throwable {
        Object returnValue = mi.proceed(); // 继续执行拦截器链
        this.joinPoint = mi;
        this.afterReturn(mi.getThis(), mi.getMethod(), mi.getArguments(), returnValue);
        return returnValue;
    }

    public void afterReturn(Object target, Method method, Object[] args, Object returnValue) throws Throwable {
        super.invokeAdviceMethod(this.joinPoint, returnValue, null); // 传送织入参数
    }

}

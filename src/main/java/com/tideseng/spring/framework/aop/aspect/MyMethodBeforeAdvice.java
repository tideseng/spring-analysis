package com.tideseng.spring.framework.aop.aspect;

import com.tideseng.spring.framework.aop.intercept.MyMethodInterceptor;
import com.tideseng.spring.framework.aop.intercept.MyMethodInvocation;

import java.lang.reflect.Method;

public class MyMethodBeforeAdvice extends MyAbstractAspectJAdvice implements MyMethodInterceptor {

    private MyJoinPoint joinPoint;

    public MyMethodBeforeAdvice(Object aspectTarget, Method aspectMethod) {
        super(aspectTarget, aspectMethod);
    }

    @Override
    public Object invoke(MyMethodInvocation mi) throws Throwable {
        this.joinPoint = mi; // MethodInterceptor也是JoinPoint
        this.before(mi.getThis(), mi.getMethod(), mi.getArguments());
        return mi.proceed(); // 继续执行拦截器链
    }

    public void before(Object target, Method method, Object[] args) throws Throwable {
        super.invokeAdviceMethod(this.joinPoint, null, null); // 传送织入参数
    }

}

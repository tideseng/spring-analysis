package com.tideseng.spring.framework.aop.aspect;

import com.tideseng.spring.framework.aop.intercept.MyMethodInterceptor;
import com.tideseng.spring.framework.aop.intercept.MyMethodInvocation;

import java.lang.reflect.Method;

public class MyAspectJAfterAdvice extends MyAbstractAspectJAdvice implements MyMethodInterceptor {

    private MyJoinPoint joinPoint;

    public MyAspectJAfterAdvice(Object aspectTarget, Method aspectMethod) {
        super(aspectTarget, aspectMethod);
    }

    @Override
    public Object invoke(MyMethodInvocation mi) throws Throwable {
        try{
            return mi.proceed(); // 继续执行拦截器链
        } finally {
            this.joinPoint = mi;
            this.after(mi.getThis(), mi.getMethod(), mi.getArguments());
        }
    }

    public void after(Object target, Method method, Object[] args) throws Throwable {
        super.invokeAdviceMethod(this.joinPoint, null, null); // 传送织入参数
    }

}

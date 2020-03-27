package com.tideseng.spring.framework.aop.aspect;

import com.tideseng.spring.framework.aop.intercept.MyMethodInterceptor;
import com.tideseng.spring.framework.aop.intercept.MyMethodInvocation;

import java.lang.reflect.Method;

public class MyAspectJAfterThrowingAdvice extends MyAbstractAspectJAdvice implements MyMethodInterceptor {

    private String throwingName;
    private MyJoinPoint joinPoint;

    public MyAspectJAfterThrowingAdvice(Object aspectTarget, Method aspectMethod) {
        super(aspectTarget, aspectMethod);
    }

    public MyAspectJAfterThrowingAdvice(Object aspectTarget, Method aspectMethod, String throwingName) {
        this(aspectTarget, aspectMethod);
        this.throwingName = throwingName;
    }

    @Override
    public Object invoke(MyMethodInvocation mi) throws Throwable {
        try{
            return mi.proceed();
        } catch(Throwable e){
            this.joinPoint = mi;
            this.afterThrow(mi.getThis(), mi.getMethod(), mi.getArguments(), e);
            throw e;
        }
    }

    public void afterThrow(Object target, Method method, Object[] args, Throwable e) throws Throwable {
        super.invokeAdviceMethod(this.joinPoint, null, e); // 传送织入参数
    }
}

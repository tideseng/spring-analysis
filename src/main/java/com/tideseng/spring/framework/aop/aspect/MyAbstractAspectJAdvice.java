package com.tideseng.spring.framework.aop.aspect;

import java.lang.reflect.Method;

public abstract class MyAbstractAspectJAdvice implements MyAdvice {

    private Object aspectTarget;
    private Method aspectMethod;

    public MyAbstractAspectJAdvice(Object aspectTarget, Method aspectMethod) {
        this.aspectTarget = aspectTarget;
        this.aspectMethod = aspectMethod;
    }

    /**
     * 调用切面的对应方法
     * @param joinPoint
     * @param returnValue
     * @param e
     * @return
     * @throws Throwable
     */
    public Object invokeAdviceMethod(MyJoinPoint joinPoint, Object returnValue, Throwable e) throws Throwable{
        Class<?>[] paramTypes = this.aspectMethod.getParameterTypes();
        if(paramTypes == null || paramTypes.length == 0){
            return this.aspectMethod.invoke(aspectTarget);
        }else{
            Object[] args = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i ++) {
                if(paramTypes[i] == MyJoinPoint.class){
                    args[i] = joinPoint;
                }else if(paramTypes[i] == Object.class){
                    args[i] = returnValue;
                }else if(paramTypes[i] == Throwable.class){
                    args[i] = e;
                }
            }
            return this.aspectMethod.invoke(aspectTarget, args);
        }
    }

}

package com.tideseng.spring.framework.aop.intercept;

import com.tideseng.spring.framework.aop.aspect.MyJoinPoint;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 方法执行器
 */
public class MyMethodInvocation implements MyJoinPoint {

    private Object proxy; // 代理类
    private Object target; // 被代理类
    private Method method; // 被代理方法
    private Object[] arguments; // 被代理方法参数
    private Class<?> targetClass; // 被代理类字节码
    private List<Object> interceptorsAndDynamicMethodMatchers; // 代理方法的拦截器链
    private Map<String, Object> userAttributes;

    // 定义索引，从-1开始来记录当前拦截器执行的位置
    private int currentInterceptorIndex = -1;

    public MyMethodInvocation(
            Object proxy, Object target, Method method, Object[] arguments,
            Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers) {
        this.proxy = proxy;
        this.target = target;
        this.targetClass = targetClass;
        this.method = method;
        this.arguments = arguments;
        this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
    }

    /**
     * 执行拦截器链（有未执行的拦截器则执行拦截器、没有时调用被代理方法）
     * @return
     * @throws Throwable
     */
    public Object proceed() throws Throwable{
        // 如果Interceptor执行完了，则执行joinPoint
        if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
            return this.method.invoke(this.target, this.arguments);
        }

        Object interceptorOrInterceptionAdvice = this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
        // 如果要动态匹配joinPoint
        if (interceptorOrInterceptionAdvice instanceof MyMethodInterceptor) {
            MyMethodInterceptor mi = (MyMethodInterceptor) interceptorOrInterceptionAdvice;
            // 动态匹配：运行时参数是否满足匹配条件
            return mi.invoke(this);
        } else {
            // 动态匹配失败时，略过当前Intercetpor调用下一个Interceptor
            return proceed();
        }
    }

    @Override
    public Object getThis() {
        return this.target;
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public Object[] getArguments() {
        return this.arguments;
    }

    @Override
    public void setUserAttribute(String key, Object value) {
        if (value != null) {
            if (this.userAttributes == null)
                this.userAttributes = new HashMap<>();
            this.userAttributes.put(key, value);
        }
        else {
            if (this.userAttributes != null)
                this.userAttributes.remove(key);
        }
    }

    @Override
    public Object getUserAttribute(String key) {
        return this.userAttributes != null ? this.userAttributes.get(key) : null;
    }

}

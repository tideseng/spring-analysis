package com.tideseng.spring.framework.aop.support;

import com.tideseng.spring.framework.aop.aspect.MyAfterReturningAdvice;
import com.tideseng.spring.framework.aop.aspect.MyAspectJAfterAdvice;
import com.tideseng.spring.framework.aop.aspect.MyAspectJAfterThrowingAdvice;
import com.tideseng.spring.framework.aop.aspect.MyMethodBeforeAdvice;
import com.tideseng.spring.framework.aop.config.MyAopConfig;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyAdvisedSupport {

    private MyAopConfig config;
    private Object target;
    private Class<?> targetClass;
    private Pattern pointCutClassPattern;
    private transient Map<Method, List<Object>> methodCache = new HashMap<>();;

    public MyAdvisedSupport(MyAopConfig config) {
        this.config = config;
    }

    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) throws Exception {
        List<Object> cached = this.methodCache.get(method);
        if(cached == null){
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes()); // 获取实现类方法
            cached = this.methodCache.get(m);
            this.methodCache.put(method, cached); //  对代理方法进行一个兼容处理
        }
        return cached;
    }

    public boolean pointCutMatch(){
        return this.pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    private void setPointCutClassPattern(String pointCut){
        String pointCutClassRegex = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        this.pointCutClassPattern = Pattern.compile("class " + pointCutClassRegex.substring(pointCutClassRegex.lastIndexOf(" ")+1));
    }

    private void parse() throws Exception {
        // pointCut=public .* com.tideseng.spring.demo.service.impl.*ServiceImpl..*(.*)
        String pointCut = config.getPointCut()
                .replaceAll("\\.","\\\\.")
                .replaceAll("\\\\.\\*",".*")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");
        // pointCut=public .* com\.tideseng\.spring\.demo\.service\.impl\..*ServiceImpl\..*\(.*\)

        setPointCutClassPattern(pointCut);

        Pattern pointCutMethodPattern = Pattern.compile(pointCut);
        Class<?> aspectClass = this.getAspectClass();
        Object aspectTarget = aspectClass.newInstance();
        Map<String, Method> aspectMethods = this.getAspectMethods(aspectClass);
        for (Method method : this.targetClass.getMethods()) { // 给每一个方法封装拦截器链
            String methodString = method.toString();
            if(methodString.contains("throws"))
                methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();

            // public java.lang.String com.tideseng.spring.demo.service.impl.UserServiceImpl.get(java.lang.String)
            Matcher matcher = pointCutMethodPattern.matcher(methodString);
            if(matcher.matches()) { // 类方法满足切点表达式则将切面方法添加到执行器链中
                List<Object> advices = new LinkedList<>(); // 执行器链
                //把每一个方法包装成 MethodInterceptor
                if(StringUtils.isNotBlank(this.config.getAspectBefore())) // before
                    advices.add(new MyMethodBeforeAdvice(aspectTarget, aspectMethods.get(this.config.getAspectBefore())));
                if(StringUtils.isNotBlank(this.config.getAspectAfter())) // after
                    advices.add(new MyAspectJAfterAdvice(aspectTarget, aspectMethods.get(this.config.getAspectAfter())));
                if(StringUtils.isNotBlank(this.config.getAspectAfterReturn())) // afterReturning
                    advices.add(new MyAfterReturningAdvice(aspectTarget, aspectMethods.get(this.config.getAspectAfterReturn())));
                if(StringUtils.isNotBlank(this.config.getAspectAfterThrow())) // afterThrowing
                    advices.add(new MyAspectJAfterThrowingAdvice(aspectTarget, aspectMethods.get(this.config.getAspectAfterThrow()), this.config.getAspectAfterThrowingName()));
                this.methodCache.put(method, advices);
            }
        }

    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class<?> targetClass) throws Exception {
        this.targetClass = targetClass;
        parse();
    }

    private Class<?> getAspectClass() throws Exception {
        return Class.forName(this.config.getAspectClass());
    }

    private Map<String,Method> getAspectMethods(Class<?> aspectClass){
        Map<String,Method> aspectMethods = new HashMap<String,Method>();
        for (Method method : aspectClass.getMethods()) {
            aspectMethods.put(method.getName(), method);
        }
        return aspectMethods;
    }

}

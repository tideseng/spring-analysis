package com.tideseng.spring.framework.webmvc.servlet;

import com.tideseng.spring.framework.annotation.MyRequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MyHandlerAdapter {

    private Map<String, Integer> paramIndexMapping = new HashMap<String,Integer>(); // key为参数名，value为参数index

    public boolean supports(Object handler){
        return (handler instanceof MyHandlerMapping);
    }

    public MyModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        MyHandlerMapping handlerMapping = (MyHandlerMapping) handler;

        putParamIndexMapping(handlerMapping);

        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(), getArgs(request, response, handlerMapping));
        if(result == null || result instanceof  Void) return null;

        if(handlerMapping.getMethod().getReturnType() == MyModelAndView.class)
            return (MyModelAndView) result;
        else if(handlerMapping.getMethod().getReturnType() == String.class)
            return new MyModelAndView((String) result, new HashMap<>());
        return null;
    }

    private void putParamIndexMapping(MyHandlerMapping handlerMapping) {
        // 提取方法中加了注解的参数
        Annotation[][] parameterAnnotations = handlerMapping.getMethod().getParameterAnnotations(); // 获取方法中加带注解的参数列表（一维数组是参数列表，二维数组是注解列表）
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for(Annotation annotation : parameterAnnotations[i]){
                if(annotation instanceof MyRequestParam){
                    String paramName = ((MyRequestParam) annotation).value();
                    this.paramIndexMapping.put(paramName, i);
                }
            }
        }

        // 提取方法中的request和response参数
        Class<?> [] paramsTypes = handlerMapping.getMethod().getParameterTypes();
        for (int i = 0; i < paramsTypes.length ; i ++) {
            Class<?> type = paramsTypes[i];
            if(type == HttpServletRequest.class || type == HttpServletResponse.class){
                this.paramIndexMapping.put(type.getName(), i);
            }
        }
    }

    private Object[] getArgs(HttpServletRequest req, HttpServletResponse resp, MyHandlerMapping handlerMapping) {
        Map<String, String[]> parameterMap = req.getParameterMap(); // 请求参数
        Class<?>[] parameterTypes = handlerMapping.getMethod().getParameterTypes(); // 形参列表
        Object[] args = new Object[parameterTypes.length]; // 实参列表

        if(this.paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
            Integer reqIndex = this.paramIndexMapping.get(HttpServletRequest.class.getName());
            args[reqIndex] = req;
        }
        if(this.paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
            Integer respIndex = this.paramIndexMapping.get(HttpServletResponse.class.getName());
            args[respIndex] = resp;
        }
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String paramValue = Arrays.toString(entry.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
            if(!this.paramIndexMapping.containsKey(entry.getKey())) continue;
            Integer index = this.paramIndexMapping.get(entry.getKey());
            args[index] = convert(parameterTypes[index], paramValue);
        }
        return args;
    }

    private Object convert(Class<?> parameterType, String paramValue) {
        if(parameterType == Integer.class || parameterType == int.class)
            return Integer.parseInt(paramValue);
        return paramValue;
    }

}

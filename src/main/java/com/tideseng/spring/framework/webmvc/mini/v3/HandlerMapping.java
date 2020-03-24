package com.tideseng.spring.framework.webmvc.mini.v3;

import lombok.Data;
import com.tideseng.spring.framework.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Data
public class HandlerMapping {

    private String url; // url
    private Pattern pattern; // url匹配规则
    private Method method; // 映射的方法
    private Object controller;
    private Map<String, Integer> paramIndexMapping; // 参数名对应的顺序
    private Class<?>[] parameterTypes;

    public HandlerMapping(String url, Method method, Object controller) {
        this.url = url;
        this.method = method;
        this.controller = controller;
        this.paramIndexMapping = new HashMap<String,Integer>(); // key为参数名，value为参数index
        this.parameterTypes = method.getParameterTypes();
        putParamIndexMapping(method);
    }

    public HandlerMapping(Pattern pattern, Method method, Object controller) {
        this.pattern = pattern;
        this.method = method;
        this.controller = controller;
        this.paramIndexMapping = new HashMap<String,Integer>(); // key为参数名，value为参数index
        this.parameterTypes = method.getParameterTypes();
        putParamIndexMapping(method);
    }

    private void putParamIndexMapping(Method method) {
        // 提取方法中加了注解的参数
        Annotation[][] parameterAnnotations = method.getParameterAnnotations(); // 获取方法中加带注解的参数列表（一维数组是参数列表，二维数组是注解列表）
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for(Annotation annotation : parameterAnnotations[i]){
                if(annotation instanceof MyRequestParam){
                    String paramName = ((MyRequestParam) annotation).value();
                    paramIndexMapping.put(paramName, i);
                }
            }
        }

        // 提取方法中的request和response参数
        Class<?> [] paramsTypes = method.getParameterTypes();
        for (int i = 0; i < paramsTypes.length ; i ++) {
            Class<?> type = paramsTypes[i];
            if(type == HttpServletRequest.class || type == HttpServletResponse.class){
                paramIndexMapping.put(type.getName(), i);
            }
        }
    }

}

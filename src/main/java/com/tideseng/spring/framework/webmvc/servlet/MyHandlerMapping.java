package com.tideseng.spring.framework.webmvc.servlet;

import lombok.Getter;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

@Getter
public class MyHandlerMapping {

    private Pattern pattern; // url匹配规则
    private Method method; // 映射的方法
    private Object controller;

    public MyHandlerMapping(Pattern pattern, Method method, Object controller) {
        this.pattern = pattern;
        this.method = method;
        this.controller = controller;
    }

}

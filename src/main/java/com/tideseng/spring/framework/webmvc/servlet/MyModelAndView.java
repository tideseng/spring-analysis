package com.tideseng.spring.framework.webmvc.servlet;

import lombok.Getter;

import java.util.Map;

@Getter
public class MyModelAndView {

    private String viewName;
    private Map<String, ?> model;

    public MyModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public MyModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }
}

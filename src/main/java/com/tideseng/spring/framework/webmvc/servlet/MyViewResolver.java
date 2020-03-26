package com.tideseng.spring.framework.webmvc.servlet;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Locale;

public class MyViewResolver {

    private final String DEFAULT_TEMPLATE_SUFFX = ".html";
    private File templateRootDir;

    public MyViewResolver(File templateRootDir){
        this.templateRootDir = templateRootDir;
    }

    public MyView resolveViewName(String viewName, Locale locale) {
        if(StringUtils.isBlank(viewName)) return null;
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFX) ? viewName : viewName + DEFAULT_TEMPLATE_SUFFX;
        File templateFile = new File((templateRootDir.getPath() + "/" + viewName).replaceAll("/+","/"));
        return new MyView(templateFile);
    }

}

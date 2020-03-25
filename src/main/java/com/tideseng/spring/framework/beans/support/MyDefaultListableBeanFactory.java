package com.tideseng.spring.framework.beans.support;

import com.tideseng.spring.framework.beans.config.MyBeanDefinition;
import com.tideseng.spring.framework.context.support.MyAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyDefaultListableBeanFactory extends MyAbstractApplicationContext {

    // 存储注册信息的BeanDefinition（伪IOC容器）
    protected final Map<String, MyBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

}

package com.tideseng.spring.framework.beans.config;

import lombok.Data;

/**
 * Bean的配置对象，存储配置文件中的信息
 */
@Data
public class MyBeanDefinition {

    private String factoryBeanName;
    private String beanClassName;
    private boolean lazyInit;

    public MyBeanDefinition() {
    }

    public MyBeanDefinition(String factoryBeanName, String beanClassName) {
        this.factoryBeanName = factoryBeanName;
        this.beanClassName = beanClassName;
    }

    public MyBeanDefinition(String factoryBeanName, String beanClassName, boolean lazyInit) {
        this.factoryBeanName = factoryBeanName;
        this.beanClassName = beanClassName;
        this.lazyInit = lazyInit;
    }

}

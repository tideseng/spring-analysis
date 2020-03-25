package com.tideseng.spring.framework.beans.config;

public class MyBeanPostProcessor {

    /**
     * 在Bean的初始化前提供回调入口
     * @param bean
     * @param beanName
     * @return
     * @throws Exception
     */
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        return bean;
    }

    /**
     * 在Bean的初始化之后提供回调入口
     * public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception
     * @param bean
     * @param beanName
     * @return
     * @throws Exception
     */
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        return bean;
    }

}

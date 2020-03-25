package com.tideseng.spring.framework.beans;

/**
 * 工厂的顶层设计
 */
public interface MyBeanFactory {

    /**
     * 根据beanName从IOC容器中获得一个实例Bean，也是依赖注入的入口
     * @param beanName
     * @return
     * @throws Exception
     */
    Object getBean(String beanName) throws Exception;

    /**
     * 根据beanClass从IOC容器中获得一个实例Bean
     * @param beanClass
     * @return
     * @throws Exception
     */
    Object getBean(Class<?> beanClass) throws Exception;

}

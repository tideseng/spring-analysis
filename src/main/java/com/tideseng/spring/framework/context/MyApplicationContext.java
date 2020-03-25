package com.tideseng.spring.framework.context;

import com.tideseng.spring.framework.beans.MyBeanFactory;
import com.tideseng.spring.framework.beans.MyBeanWrapper;
import com.tideseng.spring.framework.beans.config.MyBeanDefinition;
import com.tideseng.spring.framework.beans.support.MyBeanDefinitionReader;
import com.tideseng.spring.framework.beans.support.MyDefaultListableBeanFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IOC核心容器
 */
public class MyApplicationContext extends MyDefaultListableBeanFactory implements MyBeanFactory {

    private String[] configLocations;

    // 单例IOC容器缓存
    private Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    // 通用IOC容器
    private Map<String, MyBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

    public MyApplicationContext(String... configLocations) throws Exception {
        this.configLocations = configLocations;
        refresh();
    }

    /**
     * 定位、加载、注册，预初始化
     * @throws Exception
     */
    @Override
    protected void refresh() throws Exception {
        //1、定位配置文件（扫描相关的类）
        MyBeanDefinitionReader reader = new MyBeanDefinitionReader(this.configLocations);

        //2、加载配置文件，封装成BeanDefinition
        List<MyBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        //3、注册，把配置信息放到容器里面（伪IOC容器）
        doRegisterBeanDefinition(beanDefinitions);

        //4、把不是延时加载的类，提前初始化
        doAutowired();
    }

    private void doRegisterBeanDefinition(List<MyBeanDefinition> beanDefinitions) throws Exception {
        for(MyBeanDefinition beanDefinition : beanDefinitions){
            if(super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName()))
                throw new Exception("The"+beanDefinition.getFactoryBeanName()+" is exists");
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }
    }

    private void doAutowired() throws Exception {
        for(Map.Entry<String, MyBeanDefinition> beanDefinitionEntry : super.beanDefinitionMap.entrySet()){
            if(beanDefinitionEntry.getValue().isLazyInit()) continue;
            getBean(beanDefinitionEntry.getKey());
        }
    }

    /**
     * bean初始化和依赖注入入口
     * @param beanName
     * @return
     * @throws Exception
     */
    @Override
    public Object getBean(String beanName) throws Exception {

        return null;
    }

    @Override
    public Object getBean(Class<?> beanClass) throws Exception {
        return getBean(beanClass.getName());
    }

    private MyBeanWrapper instantiateBean(String beanName, MyBeanDefinition beanDefinition) {

        return null;
    }

    private void populateBean(String beanName, MyBeanDefinition mbd, MyBeanWrapper beanWrapper){

    }

}

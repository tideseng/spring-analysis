package com.tideseng.spring.framework.context;

import com.tideseng.spring.framework.annotation.MyAutowired;
import com.tideseng.spring.framework.annotation.MyController;
import com.tideseng.spring.framework.annotation.MyService;
import com.tideseng.spring.framework.beans.MyBeanFactory;
import com.tideseng.spring.framework.beans.MyBeanWrapper;
import com.tideseng.spring.framework.beans.config.MyBeanDefinition;
import com.tideseng.spring.framework.beans.config.MyBeanPostProcessor;
import com.tideseng.spring.framework.beans.support.MyBeanDefinitionReader;
import com.tideseng.spring.framework.beans.support.MyDefaultListableBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IOC核心容器
 */
public class MyApplicationContext extends MyDefaultListableBeanFactory implements MyBeanFactory {

    private String[] configLocations;
    private MyBeanDefinitionReader reader;

    // 单例IOC容器缓存（key为beanClassName和factoryBeanName）
    private Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<>();
    // 通用IOC容器（key为beanDefinitionMap的ke'y）
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
        reader = new MyBeanDefinitionReader(this.configLocations);

        //2、加载配置文件，封装成BeanDefinition
        List<MyBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        //3、注册，把配置信息放到容器里面（伪IOC容器）
        doRegisterBeanDefinition(beanDefinitions);

        //4、把不是延时加载的类，提前初始化
        doAutowired();
    }

    /**
     * 将BeanDefinition的List集合封装到BeanDefinition的Map集合中，key为factoryBeanName（实际为类的全类名和类名、接口全类名）
     * @param beanDefinitions
     * @throws Exception
     */
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
//        MyBeanPostProcessor beanPostProcessor = new MyBeanPostProcessor();
        MyBeanDefinition beanDefinition = super.beanDefinitionMap.get(beanName);

        Object instance = instantiateBean(beanName, beanDefinition);
//        beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
        //3、把这个对象封装到BeanWrapper中
        MyBeanWrapper beanWrapper = new MyBeanWrapper(instance);
        //4、把BeanWrapper存到IOC容器里面
//        if(this.factoryBeanInstanceCache.containsKey(beanName)) throw new Exception("The"+beanName+"is exists");
        this.factoryBeanInstanceCache.put(beanName, beanWrapper);
//        beanPostProcessor.postProcessAfterInitialization(instance, beanName);

        populateBean(beanName, beanDefinition, beanWrapper);

        return beanWrapper.getWrappedInstance();
    }

    @Override
    public Object getBean(Class<?> beanClass) throws Exception {
        return getBean(beanClass.getName());
    }

    private Object instantiateBean(String beanName, MyBeanDefinition beanDefinition) throws Exception {
        //1、拿到要实例化的对象的类名
        String beanClassName = beanDefinition.getBeanClassName();

        //2、反射实例化，得到一个对象
        Object instance;
        if(this.factoryBeanObjectCache.containsKey(beanClassName))
            instance = this.factoryBeanObjectCache.get(beanClassName);
        else {
            instance = Class.forName(beanClassName).newInstance();
            this.factoryBeanObjectCache.put(beanClassName, instance);
            this.factoryBeanObjectCache.put(beanDefinition.getFactoryBeanName(), instance);
        }
        return instance;
    }

    private void populateBean(String beanName, MyBeanDefinition beanDefinition, MyBeanWrapper beanWrapper) throws Exception {
        Class<?> clazz = beanWrapper.getWrappedClass();
        if(clazz.isAnnotationPresent(MyController.class) || clazz.isAnnotationPresent(MyService.class)) {
            for(Field field : clazz.getDeclaredFields()){ // 获取class的所有属性
                if(!field.isAnnotationPresent(MyAutowired.class)) continue;
                MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                String autowiredbeanName = autowired.value();
                if("".equals(autowiredbeanName)) autowiredbeanName = field.getType().getName(); // 注入时不指定名称，以全类名获取
                field.setAccessible(true);
                if(this.factoryBeanInstanceCache.get(autowiredbeanName) != null){
                    field.set(beanWrapper.getWrappedInstance(), this.factoryBeanInstanceCache.get(autowiredbeanName).getWrappedInstance()); // 注入值
                } else { // 当还未实列化是特殊处理
                    field.set(beanWrapper.getWrappedInstance(), getBean(autowiredbeanName)); // 注入值
                }
            }
        }
    }

}

package com.tideseng.spring.framework.beans;

import lombok.Data;

/**
 * Bean的实例对象
 */
@Data
public class MyBeanWrapper {

    private Object wrappedInstance;
    private Class<?> wrappedClass;

    public MyBeanWrapper(Object wrappedInstance){
        this.wrappedInstance = wrappedInstance;
        this.wrappedClass = wrappedInstance.getClass();
    }

}

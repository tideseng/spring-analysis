package com.tideseng.spring.framework.aop;

import com.tideseng.spring.framework.aop.support.MyAdvisedSupport;

public class MyCglibAopProxy implements MyAopProxy {

    private MyAdvisedSupport advised;

    public MyCglibAopProxy(MyAdvisedSupport advised) {
        this.advised = advised;
    }

    @Override
    public Object getProxy() {
        return null;
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return null;
    }
}

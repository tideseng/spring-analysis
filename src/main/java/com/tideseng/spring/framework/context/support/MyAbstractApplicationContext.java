package com.tideseng.spring.framework.context.support;

public abstract class MyAbstractApplicationContext {

    /**
     * 受保护方法，只提供给子类重写
     * @throws Exception
     */
    protected void refresh() throws Exception {}

}

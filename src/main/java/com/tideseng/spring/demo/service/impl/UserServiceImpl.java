package com.tideseng.spring.demo.service.impl;

import com.tideseng.spring.demo.service.IUserService;
import com.tideseng.spring.framework.annotation.MyService;
import lombok.SneakyThrows;

@MyService
public class UserServiceImpl implements IUserService {

    @Override
    public String get(String name) {
        return "My name is " + name;
    }

    @Override
    public void todo() {
        System.out.println("todo");
    }

    @Override
    public void error() throws Exception {
        throw new Exception("故意出错");
    }

}

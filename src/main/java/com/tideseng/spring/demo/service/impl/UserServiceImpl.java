package com.tideseng.spring.demo.service.impl;

import com.tideseng.spring.demo.service.IUserService;
import com.tideseng.spring.framework.annotation.MyService;

@MyService
public class UserServiceImpl implements IUserService {

    @Override
    public String get(String name) {
        return "My name is " + name;
    }

}

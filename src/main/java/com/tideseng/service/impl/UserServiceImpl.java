package com.tideseng.service.impl;

import com.tideseng.service.IUserService;
import springmvc.annotation.MyService;

@MyService
public class UserServiceImpl implements IUserService {

    @Override
    public String get(String name) {
        return "My name is " + name;
    }

}

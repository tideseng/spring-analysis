package com.tideseng.spring.demo.controller;

import com.tideseng.spring.demo.service.IUserService;
import com.tideseng.spring.framework.annotation.MyAutowired;
import com.tideseng.spring.framework.annotation.MyController;
import com.tideseng.spring.framework.annotation.MyRequestMapping;
import com.tideseng.spring.framework.annotation.MyRequestParam;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@MyController
@MyRequestMapping("/user")
public class UserController {

  	@MyAutowired
	private IUserService userService;

	@MyRequestMapping("/find")
	public void find(HttpServletRequest req, HttpServletResponse resp,
					  @MyRequestParam("name") String name){
		String result = userService.get(name);
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@MyRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@MyRequestParam("a") Integer a, @MyRequestParam("b") Integer b){
		try {
			resp.getWriter().write(a + "+" + b + "=" + (a + b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@MyRequestMapping("/remove")
	public String remove(@MyRequestParam("id") Integer id){
		return "success";
	}

}

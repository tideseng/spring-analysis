package com.tideseng.controller;

import com.tideseng.service.IUserService;
import springmvc.annotation.MyAutowired;
import springmvc.annotation.MyController;
import springmvc.annotation.MyRequestMapping;
import springmvc.annotation.MyRequestParam;

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

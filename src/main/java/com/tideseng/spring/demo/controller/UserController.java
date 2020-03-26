package com.tideseng.spring.demo.controller;

import com.tideseng.spring.demo.service.IUserService;
import com.tideseng.spring.framework.annotation.MyAutowired;
import com.tideseng.spring.framework.annotation.MyController;
import com.tideseng.spring.framework.annotation.MyRequestMapping;
import com.tideseng.spring.framework.annotation.MyRequestParam;
import com.tideseng.spring.framework.webmvc.servlet.MyModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@MyController
@MyRequestMapping("/user")
public class UserController {

  	@MyAutowired
	private IUserService userService;

	@MyRequestMapping("/find")
	public MyModelAndView find(HttpServletRequest req, HttpServletResponse resp,
					  @MyRequestParam("name") String name){
		String result = userService.get(name);
		Map<String, Object> map = new HashMap<>();
		map.put("data", result);
		return new MyModelAndView("200", map);
	}

	@MyRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@MyRequestParam("a") Integer a, @MyRequestParam("b") Integer b){
		out(resp, a + "+" + b + "=" + (a + b));
	}

	@MyRequestMapping("/remove")
	public String remove(@MyRequestParam("id") Integer id){
		return "200";
	}

	private void out(HttpServletResponse resp, String result){
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

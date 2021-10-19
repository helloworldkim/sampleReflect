package com.test.reflect.controller;

import com.test.reflect.anno.MyRequestMapping;
import com.test.reflect.dto.JoinDTO;
import com.test.reflect.dto.LoginDTO;

public class MainController {
	
	@MyRequestMapping(value = "/login")
	public String login(LoginDTO logindto) {
		System.out.println("login() 호출");
		System.out.println(logindto);
		return "/WEB-INF/login.jsp";
	}
	@MyRequestMapping("/index")
	public String index() {
		System.out.println("index() 호출");
		return "/WEB-INF/index.jsp";
	}
	@MyRequestMapping("/join")
	public String join(JoinDTO joindto) {
			System.out.println("join() 호출");
			System.out.println(joindto);
			return "/WEB-INF/join.jsp";
	}

}

package com.test.reflect.filter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.test.reflect.anno.MyRequestMapping;
import com.test.reflect.controller.MainController;

public class Dispatcher implements Filter{

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		//URL주소 매핑을 위해 파싱하기(replace를 수행하면 기존 contextpath인 /reflect 를 제외한걸 가져옴.
		String endPoint  = request.getRequestURI().replaceAll(request.getContextPath(), "");
		System.out.println(endPoint);//실제 호출한 주소 ex) /login
		MainController mainController = new MainController();
		
		Method[] methods =  mainController.getClass().getDeclaredMethods();//해당 클래스에 존재하는 모든 메서드를 가져온다
		for(Method method : methods) {
			//어노테이션이 한개인경우
			/*
			 Annotation annotation =method.getDeclaredAnnotation(MyRequestMapping.class);
			 */
			//어노테이션이 여러개인경우
			Annotation[] annotations =method.getDeclaredAnnotations();
			Annotation annotation=null;
			for(Annotation anno : annotations) {
				if(anno.annotationType().equals(MyRequestMapping.class)) {
					annotation = anno;
				}
				break;
			}
			MyRequestMapping myRequestMapping = (MyRequestMapping) annotation;
			String value = myRequestMapping.value(); //어노테이션에 선언된 value값(URL주소 매핑을 위한 값을 받는부분)
			if(value.equals(endPoint)) {
				try {
					Parameter[] params  = method.getParameters();
					String path = null;
					if(params.length!=0) {//파라미터 있을때
						Object dtoInstance=null;
						for (Parameter param : params) {
							dtoInstance = param.getType().newInstance();//해당 클래스로 만들어준다
							setData(dtoInstance, request);
						}
						path = (String) method.invoke(mainController,dtoInstance);
					}else {//없을때
						path = (String) method.invoke(mainController);
					}
					RequestDispatcher dis = request.getRequestDispatcher(path);
					dis.forward(request, response);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			
		}
		
	}
	private <T> void setData(T instance, HttpServletRequest request) {
		//모든 파라미터의 이름들을 가져온다. ex) username,password
		Enumeration<String> keys = request.getParameterNames();
		//key--> set으로 변형 ex) username --> setUsername
		while(keys.hasMoreElements()) {
			String key = keys.nextElement();
			String methodKey = keyToMethodKey(key);
			Method[] methods = instance.getClass().getDeclaredMethods();
			for (Method method : methods) {
				//만약 username의 파라미터가 존재한다면 setUsername메서드를 찾아서 해당값을 넘겨주고 메서드를 실행시킨다!
				if(method.getName().equals(methodKey)) {
					try {
						method.invoke(instance, request.getParameter(key));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}//methods
		}//while
	}
	/**
	 * 파라미터 값을 받으면 setter의 기본형태로 만들어서 string을 return한다
	 * @param key
	 * @return String methodKey
	 */
	private String keyToMethodKey(String key) {
		String firstKey = "set";
		String upperKey = key.substring(0,1).toUpperCase();
		String remainKey = key.substring(1);
		return firstKey+upperKey+remainKey;
	}

}

package com.lwtx.wisdom.service;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class RequestService {

	public HttpServletRequest currentRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}

	public ServletContext currentServletContext() {
		return currentRequest().getServletContext();
	}

	public String currentAppPath() {
		return currentServletContext().getRealPath("");
	}
}

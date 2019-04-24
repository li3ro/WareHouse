package com.webo.warehouse.utils;

import io.jsonwebtoken.lang.Assert;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class WebUtils {

	
	/**
	 * Source = https://github.com/spring-projects/spring-framework/blob/master/spring-web/src/main/java/org/springframework/web/util/WebUtils.java
	 * @param request
	 * @param name
	 * @return
	 */
	public static Cookie getCookie(HttpServletRequest request, String name) {
		Assert.notNull(request, "Request must not be null");
		Cookie cookies[] = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (name.equals(cookie.getName())) {
					return cookie;
				}
			}
		}
		return null;
	}
}

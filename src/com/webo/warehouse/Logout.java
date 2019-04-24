package com.webo.warehouse;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webo.warehouse.sso.CookieUtil;
import com.webo.warehouse.sso.JwtUtil;

@WebServlet("/logout")
public class Logout extends HttpServlet {

	private static final long serialVersionUID = 8257392557643859838L;

	private static final String jwtTokenCookieName = "JWT-TOKEN";
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
	
		JwtUtil.invalidateRelatedTokens(request);
	    CookieUtil.clear(response, jwtTokenCookieName);
	    
	    response.getWriter().print("{\"status\":\"OKAY\"}");
	}
    
}

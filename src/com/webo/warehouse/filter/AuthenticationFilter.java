package com.webo.warehouse.filter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webo.warehouse.sso.JwtUtil;



@WebFilter( filterName = "AuthenticationFilter", urlPatterns = {"/*"} )
public class AuthenticationFilter implements Filter {
	
	static Logger logger = Logger.getLogger(AuthenticationFilter.class.getName());
	private static final String jwtTokenCookieName = "JWT-TOKEN";
    private static final String signingKey = "signingKey";
    
    public void init(FilterConfig fConfig) throws ServletException {
    	logger.log(Level.FINE, "[AuthenticationFilter]  -  AuthenticationFilter initialized");
    }
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    	
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        
        String username = JwtUtil.parseToken(req, jwtTokenCookieName, signingKey);
        
        String uri = req.getRequestURI();
        
        if(username == null && 
        		!(uri.endsWith("/login") || 
        		uri.endsWith("login.html") || 
        		uri.endsWith("Login") ||
        		uri.contains("resources/js/") || 
        		uri.contains("resources/css/") || 
        		uri.contains("resources/images/") 
        		)) {
        	logger.log(Level.FINE, "[AuthenticationFilter]  -  Unauthorized access request. Redirecting to //login");
        	resp.sendRedirect("login.html");
        } else{
            req.setAttribute("username", username);
            chain.doFilter(req, resp);
        }
        
    }

	@Override
	public void destroy() {
		
	}
 

}
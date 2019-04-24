package com.webo.warehouse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.webo.warehouse.db.QueryManager;
import com.webo.warehouse.sso.CookieUtil;
import com.webo.warehouse.sso.JwtUtil;


@WebServlet("/login")
public class Login extends HttpServlet {
	
	private static final long serialVersionUID = 2507443989271487930L;
	static Logger logger = Logger.getLogger(Login.class.getName());
	private static final String jwtTokenCookieName = "JWT-TOKEN";
    private static final String signingKey = "signingKey";
    public static final String DEPLOYMENT_DOMAIN = "antilopa.zapto.org";
    private static AtomicInteger reqId = new AtomicInteger(1);
    
    @Override
	public void init() throws ServletException {
		logger.info("[init()] - started.");
		Thread.currentThread().setName("LogMeIn");
		
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new SimpleFormatter());
		handler.setLevel(Level.ALL);
		logger.addHandler(handler);
		
		try {
			QueryManager.verifyIndexesCreation();
			QueryManager.addDefaultAdminUserAccount();
			QueryManager.addDefaultAdminUserRole();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "[init()] - Failed to access the database. Reason: ", e);
		}
			
		logger.info("[init()] - ended.");
	}
	
    
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		response.setHeader("Location", "/login");
		response.sendRedirect("login.html");
    }
	
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int requestId = reqId.incrementAndGet();
    	String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        HttpSession session = request.getSession(false);
		SessionInfo sessionInfo = null;
        
		try {
			
			session = checkSession(request, requestId);
			if (session == null) {
				return;
			}
			sessionInfo = (SessionInfo)session.getAttribute("SessionInfo");
			sessionInfo.setAttribute(requestId, "HttpServletRequest", request);
			sessionInfo.setAttribute(requestId, "HttpServletResponse", response);
			
			UserEntity userEntity = null;
			
			if(username != null && username.length() > 0 && password != null && password.length() > 0) {
				userEntity = QueryManager.authenticate(username , password);
	        	if(userEntity == null) {
	        		logger.log(Level.SEVERE, "[Login.doPost()] - Failed to authenticate!");
					response.getWriter().print("{\"status\":\"FAIL\", \"reason\":\"Failed to authenticate\"}");
					return;
	        	}
	        } else {
	        	logger.log(Level.INFO, "[Login.doPost()] - Failed! username or password are empty.");
				response.getWriter().print("{\"status\":\"FAIL\", \"reason\":\"username or password are empty.\"}");
				return;
	        }
	        logger.log(Level.INFO, "[Login.doPost()] - Authenticated!");
		
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception message: {}" , e);
			response.getWriter().print("{\"status\":\"FAIL\", \"error\":\"exception caught. check the server logs..\"}");
			return;
		}
		
		
		
		
		
		
        if(username==null || username.length()==0 || password==null || password.length()==0) {
        	response.getWriter().print("{\"status\":\"FAIL\"}");
			return;
        }
        
        String token = JwtUtil.generateToken(signingKey, username);
        CookieUtil.create(response, jwtTokenCookieName, token, false, -1, DEPLOYMENT_DOMAIN);
    	response.getWriter().print("{\"status\":\"OKAY\"}");
    	
    }
	
	
    
    protected HttpSession checkSession(HttpServletRequest req, int requestId) {
		boolean isSessionValid = false;
		HttpSession session = null;
		SessionInfo sessionInfo = null;
		
		try {
			
			isSessionValid = req.isRequestedSessionIdValid();
			if (isSessionValid) {
				session = req.getSession(false);
				
				sessionInfo = (SessionInfo) session.getAttribute("SessionInfo");
				if (sessionInfo == null) {
					isSessionValid = false;
				} else {
					sessionInfo.setSessionId(session.getId());
					Sessions.INSTANCE.addSession(sessionInfo);
				}
			} else {
				logger.log(Level.FINE, "[checkSession()]  -  Session is Invalid.  req.isRequestedSessionIdValid() = false");
			}
			
		} catch (NullPointerException npe) {
			logger.log(Level.SEVERE, "",npe);
			isSessionValid = false;
		}
		
		if (isSessionValid) {
			logger.log(Level.FINE, "checkSession():  SessionID="+session.getId()+"  is Valid for requestID= "+requestId);
			
		} else {
			
			logger.info("checkSession(): Creating new session and sessionInfo.");
			session = req.getSession(true);		// create new session
			
			sessionInfo = new SessionInfo(session);
			Sessions.INSTANCE.addSession(sessionInfo);
			sessionInfo.setSessionId(session.getId());
			session.setAttribute("SessionInfo", sessionInfo);
			sessionInfo.setSession(session);
		}
		
		return session;
	}
}

package com.webo.warehouse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;


public enum Sessions implements Runnable {
	INSTANCE;
	
	static Logger logger = Logger.getLogger(Sessions.class.getName());
	private Thread thread;
	private HashMap fieldSessions= new HashMap(1);
	public static ThreadLocal threadLocal = new ThreadLocal();
	private long fieldSessionInactivityTimeout = 1800000;		// half an hour
	
	private Sessions() {
		thread = new Thread(this);
		thread.setName("Sessions");
		thread.setDaemon(true);
		thread.start();
	}
	
	
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(5L*60L*1000L);	//5 minutes between checks
				purgeSessions();			// This is for Garbage collection
			}
			catch (Exception e) {
				logger.log(Level.SEVERE, "[Sessions.run()] - Something went wrong. Reason: ",e);
			}
		}
	}

	
	public SessionInfo getSession(String sessionId) {
		if (fieldSessions.containsKey(sessionId)) {
			return (SessionInfo) fieldSessions.get(sessionId);
		}
		return null;
	}
	
	public synchronized SessionInfo addSession(SessionInfo userSessionInfo) {
		fieldSessions.put(userSessionInfo.getSession().getId(), userSessionInfo);
		return userSessionInfo;
	}
	
	public synchronized SessionInfo removeSession(SessionInfo userSessionInfo) {
		if(fieldSessions == null || userSessionInfo == null || userSessionInfo.getSession() == null)
			return userSessionInfo;
		if (fieldSessions.remove(userSessionInfo.getSession().getId()) == null) {
			logger.log(Level.INFO, "[removeSession()]  -  Session.removeSession could not find session id {}" , userSessionInfo.getSession().getId());		
		} else {
//			success!
			logger.info("[removeSession()]  -  Successfully removed sessionInfo");
		}
		return userSessionInfo;
	}
	
	public void invalidateAll() {
		String[] sessions = new String[fieldSessions.size()];
		int counter=0;
		for (Iterator iter= fieldSessions.keySet().iterator(); iter.hasNext();) {
			String element= (String) iter.next();
			sessions[counter]=element;
			counter++;
		}
		for (int i= 0; i < sessions.length; i++) {
			SessionInfo si = getSession(sessions[i]);
			si.invalidate();
		}
	}
	
	
	public synchronized void purgeSessions() throws Exception {
		SessionInfo sessionInfo;
		HttpSession session;
		Iterator sessionsIterator = fieldSessions.values().iterator();
		long currentTimeMillis = System.currentTimeMillis();
		
		String id;
		while (sessionsIterator.hasNext()) {
			sessionInfo = (SessionInfo)sessionsIterator.next();

			session = sessionInfo.getSession();
			if (session != null) {
				try {
//					session.setMaxInactiveInterval(180);	// invalidate after 3 min (FOR TESTS ONLY)
					long elapsed = (currentTimeMillis-session.getLastAccessedTime())/1000;
					if (elapsed > session.getMaxInactiveInterval()) {
						session.invalidate();
						sessionsIterator.remove();	
						
						logger.log(Level.FINE, "[purgeSessions()] - markSessionAsInactive due to elapsed ("+elapsed+") > session.getMaxInactiveInterval() ("+session.getMaxInactiveInterval()+")");
						
					}
				} catch (IllegalStateException ise) {
					// silently ignore - do not show in out log
					sessionsIterator.remove();
					logger.log(Level.FINE, "[purgeSessions()] - IllegalStateException caught. session is invalid. removing session from sessions container. marking session as inactive.", ise.toString());
					
				} catch (Throwable t) {
					logger.log(Level.SEVERE, "",t);
					//if anything goes wrong trying to see if session is invalid it probably means the session
					//is in fact invalid, so go ahead and treat it as such
					sessionsIterator.remove();		
					
					logger.log(Level.FINE, "[purgeSessions()] - markSessionAsInactive due to Catch Throwable: {}", t.toString());					
				}
			} else {
				sessionsIterator.remove();
			}
		}
	}
}

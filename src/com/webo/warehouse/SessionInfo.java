package com.webo.warehouse;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;



public class SessionInfo {
	static Logger logger = Logger.getLogger(SessionInfo.class.getName());
	private Hashtable<Integer,HashMap<String,Object>> fieldRequestLevelObjects = new Hashtable<Integer,HashMap<String,Object>>();
	private UserEntity userEntity;
	private transient HttpSession session;
	private String sessionId;
	private Date fieldLastUsed = null;
	
	public SessionInfo(HttpSession session) {
		this.session = session;
	}
	
	public Object getAttribute(int reqId , String key) {
		if (getRequestLevelObjects().containsKey(reqId)) {
			return getRequestLevelObjects().get(reqId).get(key);
		}
		return null;
	}
	
	public synchronized void setAttribute(int reqId ,String key, Object value) {
		HashMap<String,Object> map = getRequestLevelObjects().get(reqId);
		if(map==null) {
			map = new HashMap<String,Object>();
			getRequestLevelObjects().put(reqId, map);
		}
		map.put(key, value);
	}
	
	public int size() {
		return getRequestLevelObjects().size();
	}
	
	protected Hashtable<Integer,HashMap<String,Object>> getRequestLevelObjects() {
		return fieldRequestLevelObjects;
	}
	
	protected void setRequestLevelObjects(Hashtable<Integer,HashMap<String,Object>> requestLevelObjects) {
		fieldRequestLevelObjects= requestLevelObjects;
	}
	
	public synchronized void invalidate() {
		getRequestLevelObjects().clear();
	}
	
	public void removeAttribute(int reqId,String key) {
		if(getRequestLevelObjects().contains(reqId))
			getRequestLevelObjects().get(reqId).remove(key);
	}

	public UserEntity getUserEntity() {
		return userEntity;
	}

	public void setUserEntity(UserEntity userEntity) {
		this.userEntity = userEntity;
	}
	
	public Date getLastUsed() {
		if (fieldLastUsed == null) {
			fieldLastUsed = new Date();
		}
		return fieldLastUsed;
	}
	public void setLastUsed(Date lastUsed) {
		fieldLastUsed = lastUsed;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String gateWaySessionId) {
		this.sessionId = gateWaySessionId;
	}

	public HttpSession getSession() {
		return session;
	}

	public void setSession(HttpSession session) {
		this.session = session;
	}

}

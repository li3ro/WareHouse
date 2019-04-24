package com.webo.warehouse.sso;

import java.util.HashSet;
import java.util.Set;

public enum LoggedState {
	INSTANCE;
	
	Set<String> logged = new HashSet<String>();
	

	public synchronized void add(String user_id) {
		logged.add(user_id);
	}

	public boolean isMember(String user_id) {
		return logged.contains(user_id);
	}

	public synchronized void remove(String user_id) {
		logged.remove(user_id);
	}
}

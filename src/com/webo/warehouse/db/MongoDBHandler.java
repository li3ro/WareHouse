package com.webo.warehouse.db;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.webo.warehouse.Consts;

public enum MongoDBHandler {
	INSTANCE;
	
	private MongoClient mongoClient = null;
	
	private MongoDBHandler() {
		try {
			if(mongoClient == null) {
				ServerAddress serverAddress = new ServerAddress(Consts.DB_SERVER_ADDR , Consts.DB_SERVER_PORT);
				List<MongoCredential> credentials = new ArrayList<MongoCredential>();
				credentials.add(MongoCredential.createCredential(Consts.DB_USERNAME, Consts.DB_NAME, Consts.DB_PASSWORD));
				mongoClient = new MongoClient(serverAddress , credentials);
			}
		} catch (Exception e) {
			Logger.getLogger(MongoDBHandler.class.getName()).log(Level.SEVERE ,"Failed to connect to DB: ", e);
		}
	}
	
	public MongoDatabase getDB() {
		if(mongoClient == null) {
			return null;
		}
		return mongoClient.getDatabase(Consts.DB_NAME);
	}
	public void close() {
		if(mongoClient == null) {
			mongoClient.close();
		}
	}

	
}

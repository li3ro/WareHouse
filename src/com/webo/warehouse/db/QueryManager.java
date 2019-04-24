package com.webo.warehouse.db;

import static com.mongodb.client.model.Filters.eq;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.webo.warehouse.Consts;
import com.webo.warehouse.UserEntity;

public class QueryManager {

	static Logger logger = Logger.getLogger(QueryManager.class.getName());

	
	/**
	 * Make sure all the mongoDB indexes are setup and in place (Like 'username' field uniqueness in 'users' collection)
	 */
	public static void verifyIndexesCreation() {
		IndexOptions indexOptions = new IndexOptions().unique(true);
		MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_USERS).createIndex(Indexes.ascending("username"), indexOptions);
		MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_ROLES).createIndex(Indexes.ascending("role_id"), indexOptions);
	}
	public static void addDefaultAdminUserAccount() {
		try {
			MongoCollection<Document> sessions = MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_USERS);
			Document newDoc = new Document("username" , "admin" )
				.append("password", "4155")
				.append("created", System.currentTimeMillis())
				.append("display_name", "The King!")
				.append("role_id", 1);
			sessions.insertOne(newDoc);
		} catch (Exception e) {
			if(e.getMessage().contains("duplicate key"))
				return;
			else {
				logger.log(Level.SEVERE, "[QueryManager.addDefaultAdminUserAccount()] - Something went wrong. Reason: ", e);
			}
		}
	}
	public static void addDefaultAdminUserRole() {
		try {
			MongoCollection<Document> sessions = MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_ROLES);
			Document newDoc = new Document("role_id" , 1)
				.append("is_master", true)
				.append("created", System.currentTimeMillis())
				.append("view_all_users", true)
				.append("view_all_products", true)
				.append("view_all_orders", true)
				.append("add_users", true)
				.append("remove_users", true)
				.append("edit_all_users", true)
				.append("edit_all_orders", true)
				.append("edit_product", true)
				.append("add_product", true)
				.append("add_order", true)
				.append("remove_orders", true);
			sessions.insertOne(newDoc);
		} catch (Exception e) {
			if(e.getMessage().contains("duplicate key"))
				return;
			else {
				logger.log(Level.SEVERE, "[QueryManager.addDefaultAdminUserAccount()] - Something went wrong. Reason: ", e);
			}
		}
	}
	
	
	/** Authenticate and return UserEntity or null... 
	 * @param username
	 * @param password 
	 * @return UserEntity **/
	public static UserEntity authenticate(final String username, final String password) {
		MongoCollection<Document> users = MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_USERS);
		FindIterable<Document> resp1 = users.find(com.mongodb.client.model.Filters.and(eq("username", username), eq("password", password)));
		if(resp1 == null)
			return null;
		
		Document queryResponse = resp1.first();
		if(queryResponse == null) {
			return null;
		} else {
			final UserEntity userEntity = new UserEntity();
			
			logger.log(Level.INFO, "[QueryManager.authenticate()] - queryResponse=" + queryResponse.toString());
			resp1.forEach(new Block<Document>() {
			    @Override
			    public void apply(final Document document) {
			    	logger.log(Level.INFO, "[QueryManager.authenticate()]  -  "+document.toJson());
			    	userEntity.username = username;
			    	userEntity.roleId = document.getInteger("role_id");
			    	userEntity.displayName = document.getString("display_name");
			    }
			});
			
			return userEntity;
		}
	}


	
	
//	public static boolean isFoundUserInSessionsTable(SessionInfo sessionInfo) {
//		MongoCollection<Document> sessions = MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_SESSIONS);
//		FindIterable<Document> resp = sessions.find(eq("UDID", sessionInfo.getUserEntity().getUDID()));
//		if(resp == null) {
//			return false;
//		}
//		Document udid = resp.first();
//		if(udid == null) {
//			// not found
//			return false;
//		}
//		return true;
//	}
//
//	public static boolean isSessionInDB(SessionInfo sessionInfo) {
//		MongoCollection<Document> sessions = MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_SESSIONS);
//		Document s_id = sessions.find(eq("session_id", sessionInfo.getSessionId())).first();
//		if(s_id == null) {
//			// not found
//			return false;
//		}
//		return true;
//	}
//
//	public static void updateSessionTime(SessionInfo sessionInfo) {
//		MongoCollection<Document> sessions = MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_SESSIONS);
//		Document newUpdate = new Document("$set", new Document("created", System.currentTimeMillis()));
//		Document searchDoc = new Document("UDID", sessionInfo.getUserEntity().getUDID()).append("session_id", sessionInfo.getSessionId());
//		sessions.updateOne(searchDoc, newUpdate);
//	}
//
//	public static void addSessionEmptyData(SessionInfo sessionInfo) {
//		MongoCollection<Document> sessions = MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_SESSIONS);
//		Document newDoc = new Document("UDID" , sessionInfo.getUserEntity().getUDID() )
//			.append("session_id", sessionInfo.getSessionId())
//			.append("created", System.currentTimeMillis())
//			.append("is_active", true)
//			.append("last_level_played", 1);
//		sessions.insertOne(newDoc);
//	}
//
//	public static List<SongPojo> getAvailableSongs(SessionInfo sessionInfo, int level, String genre, boolean forced) {
//		// 1. get list of viewed questions(songs) for the UDID
//		// 2. return the list of all the songs that were not viewed yet.
//		// 3. if forced == true >> get the list of songs in level X (dont query DB_COLLECTION_VIEWED_QUESTIONS)
//		ArrayList<SongPojo> filtered = new ArrayList<SongPojo>();
//		Set<String> viewed_song_ids = new HashSet<String>();
//		if(!forced) {
//			MongoCollection<Document> viewedQs = MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_VIEWED_QUESTIONS);
//			MongoCursor<Document> cursor = viewedQs.find(new Document("UDID",sessionInfo.getUserEntity().getUDID())).iterator();
//			try {
//				Document doc;
//			    while (cursor.hasNext()) {
//			    	doc = cursor.next();
//			    	viewed_song_ids.add(doc.getString("song_id"));
//			    }
//			} finally {
//			    cursor.close();
//			}
//		}
//		
//		MongoCollection<Document> songsCollection = MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_SONGS);
//		Document query = new Document("song_level", level);
//		if(viewed_song_ids.size() > 0) {
//			for(String song_id : viewed_song_ids)
//				query.append("$ne", new Document("song_id", song_id));
//		}
//		
//		MongoCursor<Document> cursor = songsCollection.find(query).iterator();
//		try {
//			Document doc;
//		    while (cursor.hasNext()) {
//		    	doc = cursor.next();
//		    	SongPojo data = new SongPojo();
//		    	data.setSongId(doc.getString("song_id"));
//		    	data.setArtist(doc.getString("artist"));
//		    	data.setGenre(doc.getString("genre"));
//		    	data.setLevel(doc.getInteger("song_level"));
//		    	data.setSongLink(doc.getString("song_link"));
//		    	data.setOwnerUDID(doc.getString("chorus_link"));
//		    	filtered.add(data);
//		    }
//		} finally {
//		    cursor.close();
//		}
//		
//		return filtered;
//	}
//	
//	public static void setSongAsViewed(SessionInfo sessionInfo, String song_id) {
//		MongoCollection<Document> viewedQs = MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_VIEWED_QUESTIONS);
//		boolean found = false;
//		MongoCursor<Document> cursor = viewedQs.find(new Document("UDID",sessionInfo.getUserEntity().getUDID()).append("song_id", song_id)).iterator();
//		try {
//		    while (cursor.hasNext()) {
//		    	cursor.next();
//		    	found = true;
//		        break;
//		    }
//		} finally {
//		    cursor.close();
//		}
//		
//		if(!found) {// not found. add the question..
//			viewedQs.insertOne(new Document("UDID",sessionInfo.getUserEntity().getUDID()).append("song_id", song_id));
//		}
//	}
//
//	public static void setLastLevelPlayed(SessionInfo sessionInfo, int level) {
//		MongoCollection<Document> sessions = MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_SESSIONS);
//		Document newUpdate = new Document("$set", new Document("last_level_played", level));
//		Document searchDoc = new Document("UDID", sessionInfo.getUserEntity().getUDID()).append("session_id", sessionInfo.getSessionId());
//		sessions.updateOne(searchDoc, newUpdate);
//	}
//
//	public static void setTopLevel(SessionInfo sessionInfo, int level) {
//		MongoCollection<Document> users = MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_USERS);
//		Document newUpdate = new Document("$set", new Document("top_level", level));
//		Document searchDoc = new Document("UDID", sessionInfo.getUserEntity().getUDID());
//		users.updateOne(searchDoc, newUpdate);
//	}
//	public static int getTopLevel(SessionInfo sessionInfo) {
//		MongoCollection<Document> users = MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_USERS);
//		Document user = users.find(eq("UDID", sessionInfo.getUserEntity().getUDID())).first();
//		return user.getInteger("top_level", 1);
//	}
//	
//	
//	public static void addCandidate(String songURL, String UDID, String artist, String songName, String playerName, long currentTimeMillis) {
//		MongoCollection<Document> users = MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_USERS);
//		MongoCollection<Document> candidates = MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_CANDIDATES);
//		MongoCursor<Document> cursor = users.find(new Document("UDID",UDID)).iterator();
//		String lang="UNKNOWN" , country="UNKNOWN", nicks = null;
//		try {
//		    while (cursor.hasNext()) {
//		    	Document d = cursor.next();
//		    	lang = d.getString("language");
//		    	country = d.getString("country");
//		    	nicks = d.getString("nicks");
//		        break;
//		    }
//		} finally {
//		    cursor.close();
//		}
//		
//		if(playerName != null && playerName.length() > 0) {
//			if(nicks == null) {
//				nicks = playerName;
//			} else if(!nicks.contains(playerName)) {
//				nicks += (nicks.length()==0 ? "" : ",") + playerName;
//			}
//			// update nicks in Users Collection
//			Document newUpdate = new Document("$set", new Document("nicks", nicks));
//			Document searchDoc = new Document("UDID", UDID);
//			users.updateOne(searchDoc, newUpdate);
//		}
//		
//		candidates.insertOne(new Document("owner",UDID) //singer
//								.append("songId", (Math.random()*999999999 + Math.random()*999999999))
//								.append("songURL", songURL)
//								.append("songName", songName)
//								.append("artist", artist)
//								.append("language", lang)
//								.append("country", country)
//								.append("createdTimeMillis", currentTimeMillis));
//		
//	}
//	
//	
//	public static List<CandidatePojo> getAvailableCandidates(SessionInfo sessionInfo) {
//		List<CandidatePojo> list = new ArrayList<CandidatePojo>();
//		MongoCollection<Document> songsCollection = MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_CANDIDATES);
//		Document query = new Document();
//		int i=10;
//		MongoCursor<Document> cursor = songsCollection.find(query).iterator();
//		try {
//			Document doc;
//		    while (cursor.hasNext() && i>0) {
//		    	i--;
//		    	doc = cursor.next();
//		    	CandidatePojo data = new CandidatePojo();
//		    	data.setSongId(doc.getDouble("songId") != null ? doc.getDouble("songId").toString() : "");
//		    	data.setSongLink(doc.getString("songURL"));
//		    	data.setName(doc.getString("songName"));
//		    	data.setArtist(doc.getString("artist"));
//		    	data.setLanguage(doc.getString("language"));
//		    	data.setCountry(doc.getString("country"));
//		    	data.setCreatedTimeMillis(doc.getLong("createdTimeMillis").toString());
//		    	list.add(data);
//		    }
//		} finally {
//		    cursor.close();
//		}
//		
//		return list;
//	}
//	
//	
//	public static List<QuestionPojo> getRound(SessionInfo sessionInfo) {
//		List<QuestionPojo> q_list = new ArrayList<QuestionPojo>();
//		MongoCollection<Document> songsCollection = MongoDBHandler.INSTANCE.getDB().getCollection(Consts.DB_COLLECTION_CANDIDATES);
//		Document query = new Document();
//		int i=10;
//		MongoCursor<Document> cursor = songsCollection.find(query).iterator();
//		try {
//			Document doc;
//		    while (cursor.hasNext() && i>0) {
//		    	i--;
//		    	doc = cursor.next();
////		    	CandidatePojo c = new CandidatePojo();
////		    	c.setSongId(doc.getDouble("songId") != null ? doc.getDouble("songId").toString() : "");
////		    	c.setLanguage(doc.getString("language"));
////		    	c.setCountry(doc.getString("country"));
////		    	c.setCreatedTimeMillis(doc.getLong("createdTimeMillis").toString());
//		    	
//		    	
//		    	QuestionPojo qp = new QuestionPojo();
//		    	qp.setName("סיבוב ראשון");
//		    	qp.setPointAddedForCorrectAnswer(5);
//		    	qp.setTimeLimitInSeconds(22);
//
//		    	QuestionMetadata qm = qp.new QuestionMetadata();
//		    	qm.setQuestionText("מי האומן");
//		    	qm.setSongURL(doc.getString("songURL"));
//		    	Answer a1 = qp.new Answer();
//		    	a1.setAnswerText(doc.getString("artist"));
//		    	a1.setCorrect(true);
//		    	Answer a2 = qp.new Answer();
//		    	a2.setAnswerText("יהודה פוליקר");
//		    	a2.setCorrect(false);
//		    	Answer a3 = qp.new Answer();
//		    	a3.setAnswerText("גל גדות השווה");
//		    	a3.setCorrect(false);
//		    	Answer a4 = qp.new Answer();
//		    	a4.setAnswerText("צלילי הכרם");
//		    	a4.setCorrect(false);
//		    	List<Answer> answers = new ArrayList<QuestionPojo.Answer>();
//		    	answers.add(a3);
//		    	answers.add(a1);
//		    	answers.add(a4);
//		    	answers.add(a2);
//		    	qm.setAnswers(answers);
//		    	
//		    	QuestionMetadata qm2 = qp.new QuestionMetadata();
//		    	qm2.setQuestionText("זהה את שם השיר");
//		    	qm2.setSongURL(doc.getString("songURL"));
//		    	a1 = qp.new Answer();
//		    	a1.setAnswerText(doc.getString("songName"));
//		    	a1.setCorrect(true);
//		    	a2 = qp.new Answer();
//		    	a2.setAnswerText("אימונים זה לחלשים");
//		    	a2.setCorrect(false);
//		    	answers = new ArrayList<QuestionPojo.Answer>();
//		    	answers.add(a1);
//		    	answers.add(a2);
//		    	qm2.setAnswers(answers);
//		    	
//		    	List<QuestionMetadata> qms = new ArrayList<QuestionPojo.QuestionMetadata>();
//		    	qms.add(qm);
//		    	qms.add(qm);
//		    	qp.setQuestions(qms);
//		    	q_list.add(qp);
//		    }
//		} finally {
//		    cursor.close();
//		}
//		
//		return q_list;
//	}
}

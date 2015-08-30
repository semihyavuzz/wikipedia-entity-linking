package com.semihyavuz.app;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.Driver;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;

/*
 * Database class serves as a bridging between JWPL Wikipedia database content,
 * and 
 */
public class Database {
	
	private String url, dbName, user, password;
	private Connection connection;
	
	public Database (String url, String dbName, String user, String pass) {
		this.url = url;
		this.dbName = dbName;
		this.user = user;
		this.password = pass;
	}
	
	public DatabaseConfiguration getDbConfig() {
		DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setHost(url);
        dbConfig.setDatabase(dbName);
        dbConfig.setUser(user);
        dbConfig.setPassword(password);
        dbConfig.setLanguage(Language.english);
        
        return dbConfig;
	}
	
	
	public void connect () throws MalformedURLException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		//DriverManager.registerDriver(driver);
		String dbUrl = "jdbc:mysql://"+this.url+":3306/"+ this.dbName;
		this.connection = DriverManager.getConnection(dbUrl, user, password);
	}
	
	public int fetchDocumentFrequency (String word, String table) throws SQLException {
		
		String sqlQuery = "SELECT weight FROM "+table+" WHERE id=?";
		PreparedStatement st = connection.prepareStatement(sqlQuery);
		st.setString(1, word);
		
		ResultSet rs = st.executeQuery();
		int result = rs.next() ? rs.getInt(1) : 0;
		
		rs.close();
		
		return result;
	}
	
	
	public void updateFrequency (String word) throws SQLException {
		
		String sqlQuery = "INSERT INTO DocumentFrequency (id, weight) VALUES (?, 1)"+ 
						"ON DUPLICATE KEY UPDATE weight=weight+1";
		
		PreparedStatement st = connection.prepareStatement(sqlQuery);
		st.setString(1, word);
		st.executeUpdate();
	}
	
	public void deleteOnlineTable () throws SQLException {
		String sqlDelete = "DELETE FROM TempFrequency WHERE 1=1";
		PreparedStatement st = connection.prepareStatement(sqlDelete);
		st.executeUpdate();
	}
	
	public void updateOnlineTable (String onlineWord) throws SQLException {
		String sqlQuery = "INSERT INTO TempFrequency (id, weight) VALUES (?, 1)"+ 
				"ON DUPLICATE KEY UPDATE weight=weight+1";

		PreparedStatement st = connection.prepareStatement(sqlQuery);
		st.setString(1, onlineWord);
		st.executeUpdate();
	}
	
	public void updateTrainingTable (Double scalar, Double accuracy) throws SQLException {
		String sqlTraining = "INSERT INTO TrainingParameters(scalar, accuracy) VALUES (?, ?)";
		
		PreparedStatement st = connection.prepareStatement(sqlTraining);
		st.setDouble(1, scalar);
		st.setDouble(2, accuracy);
		st.executeUpdate();
		System.out.println("-------------------------------UPDATE FOR "+scalar+" is flushed to DATABASE-----------------------------");
	}
 	
	
	public void close() throws SQLException {
		this.connection.close();
	}
}

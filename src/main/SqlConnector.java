package main;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlConnector {
	
	private boolean hasConnected;
	
	private Connection dbConnection;
	
	private static SqlConnector instance;
	public static SqlConnector getInstance(){
		if (instance == null) {
			instance = new SqlConnector();
		}
		return instance;
	}
	public boolean hasConnected(){
		return hasConnected;
	}
	public void connect(String hostname, String username, String password, String dbName){
		try {
			dbConnection = DriverManager.getConnection("jdbc:mysql://" + hostname + "/"+ dbName +"?" + "user="+username +"&password="+password);
		} catch (SQLException e) {
			System.out.println("Unable to establish a connection to the SQL server:");
			e.printStackTrace();
			return;
		}
		hasConnected = true;
	}
	public void connect(String hostname, int port, String username, String password, String dbName){
		try {
			dbConnection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/"+ dbName +"?" + "user="+username +"&password="+password);
		} catch (SQLException e) {
			System.out.println("Unable to establish a connection to the SQL server:");
			e.printStackTrace();
			return;
		}
		hasConnected = true;
	}
	public String sendSelectQuery(String query){
		String[] results = sendSelectQueryArr(query);
		String result = "";
		for(String var : results){
			result = result.concat(", " + var);
		}
		return result.substring(2);
	}
	private void listException(Exception e){
		SimpleBot.instance.unreadExceptions.add(e.getMessage());
	}
	public String[] sendSelectQueryArr(String query){
		
		List<String> results = new ArrayList<String>();
		
		Statement statement = null;
		try {
			statement = dbConnection.createStatement();
			ResultSet result = statement.executeQuery(query);
			while(result.next()){
				results.add(result.getString(1));
			}
		} catch (SQLException e) {
			listException(e);
			e.printStackTrace();
		}finally{
			try {
				statement.close();
			} catch (SQLException e) {
				listException(e);
				e.printStackTrace();
			}
		}
		return results.toArray(new String[results.size()]);
	}
	public String sendQuery(String query){
		if(!hasConnected){
			return "Please connect to a database before attempting to query it.";
		}
		java.sql.PreparedStatement statement = null;
		try {
			statement = dbConnection.prepareStatement(query);
			statement.execute();
		} catch (SQLException e) {
			listException(e);
			e.printStackTrace();
		}finally{
			try {
				statement.close();
			} catch (SQLException e) {
				listException(e);
				e.printStackTrace();
			}
		}
		return "Done";
	}
	public void tryIncrementLastUsedBy(String table, String primary, String primaryValue, String column, String user, String insert){
		tryIncrement(table, primary, primaryValue, column, insert);
		sendQuery("UPDATE " + table + " SET last_used_by = '" + user + "' " + " WHERE "+ primary + " = '" + primaryValue + "'");
	}
	
	public void tryIncrement(String table, String primary, String primaryValue, String column, int amount, String insert) {
		boolean created;
		String result = sendSelectQuery("SELECT COUNT(*) " + primary + " FROM " + table + " WHERE "+ primary + " = '" + primaryValue + "'");
		created = (Integer.parseInt(result) > 0);

		if (created) {
			sendQuery("UPDATE " + table + " SET `" + column + "` = `" + column+ "` + " + amount + " WHERE " + primary + " = '"+ primaryValue + "'");
		} else {
			sendQuery("INSERT INTO " + table + " VALUES (" + insert + ")");
		}
	}

	// Increments a value in a column behind the primary key with value
	// 'primaryValue' without checking if the primary key exists
	public void increment(String table, String primary, String primaryValue, String column) {
		SqlConnector.getInstance().sendQuery("UPDATE " + table + " SET `" + column + "` = `" + column+ "` + 1 WHERE " + primary + " = '" + primaryValue+ "'");

	}

	// Increments a value in a column behind the primary key with value
	// 'primaryValue', and creates the primary key if it doens't exist yet.
	public void tryIncrement(String table, String primary, String primaryValue, String column, String insert) {
		tryIncrement(table, primary, primaryValue, column, 1, insert);
	}
	public void disconnect() {
		try {
			dbConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}

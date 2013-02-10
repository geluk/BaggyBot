package main;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 * Interfaces with a MySQL database using Strings for input and output.
 * Also provides some shorthand methods for commonly used features.
 */

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
	
	// Attempts to connect to the MySQL database.
	public void connect(String hostname, String username, String password, String dbName){
		try {
			dbConnection = DriverManager.getConnection("jdbc:mysql://" + hostname + "/"+ dbName +"?" + "user="+username +"&password="+password);
		} catch (SQLException e) {
			System.out.println("Unable to establish a connection to the SQL server:");
			e.printStackTrace();
			BaggyBot.instance.addException(e);
			return;
		}
		hasConnected = true;
	}
	// Attempts to connect to the MySQL database using a port. 
	// I know, duplicate code, ugly and all that. yeah. I know. Shush.
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
	
	// Sends a 'select' query and returns a string containing all the results, separated by commas and spaces.
	// This method will work if you have more than one result, but in that case using the sendSelectQueryArr (which returns a string array)
	// would be recommended.
	public String sendSelectQuery(String query){
		/*String[] results = sendSelectQueryArr(query);
		String result = "";
		for(int i = 0; i < results.length; i++){
			if(i == 0){
				result = results[i];
			}else{
				result = result.concat(", " + results[i]);
			}
		}
		return result;*/
		return sendSelectQueryArr(query)[0];
	}
	public String sendSelectQueryMultiresult(String query){
		String[] results = sendSelectQueryArr(query);
		String result = "";
		for(int i = 0; i < results.length; i++){
			if(i == 0){
				result = results[i];
			}else{
				result = result.concat(", " + results[i]);
			}
		}
		return result;
	}
	
	// Adds an exception to the list of unread exceptions.
	private void registerException(Exception e){
		BaggyBot.instance.addException(e);
	}
	// Returns a string array containing all results. Only use this when your SQL query will return one column.
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
			registerException(e);
			e.printStackTrace();
		}finally{
			try {
				statement.close();
			} catch (SQLException e) {
				registerException(e);
				e.printStackTrace();
			}
		}
		// Turn the list into an array
		return results.toArray(new String[results.size()]);
	}
	// Attempts to send a query. Don't use this to send a select query, since it won't return anything.
	// It returns a string containing information about the status.
	public String sendQuery(String query){
		if(!hasConnected){
			return "Please connect to a database before attempting to query it.";
		}
		java.sql.PreparedStatement statement = null;
		String status = "Done";
		try {
			statement = dbConnection.prepareStatement(query);
			statement.execute();
		} catch (SQLException e) {
			System.out.println("Failed: " + query);
			registerException(e);
			e.printStackTrace();
			status = "Failed";
		}finally{
			try {
				statement.close();
			} catch (SQLException e) {
				registerException(e);
				e.printStackTrace();
				status = "Failed";
 			}
		}
		return status;
	}
	public void tryIncrementLastUsedBy(String table, String primary, String primaryValue, String column, String user, String insert){
		tryIncrement(table, primary, primaryValue, column, insert);
		sendQuery("UPDATE " + table + " SET last_used_by = '" + user + "' " + " WHERE "+ primary + " = '" + primaryValue + "'");
	}
	
	public void tryIncrement(String table, String primary, String primaryValue, String column, int amount, String insert) {
		boolean created;
		String query = "SELECT COUNT(`" + primary + "`) FROM `" + table + "` WHERE `"+ primary + "` = '" + primaryValue + "'";
		String result = sendSelectQuery(query);
		created = (Integer.parseInt(result) > 0);

		if (created) {
			sendQuery("UPDATE " + table + " SET `" + column + "` = `" + column+ "` + " + amount + " WHERE `" + primary + "` = '"+ primaryValue + "'");
		} else {
			sendQuery("INSERT INTO " + table + " VALUES (" + insert + ")");
		}
	}

	// Increments a value in a column behind the primary key with value
	// 'primaryValue' without checking if the primary key exists
	public void increment(String table, String primary, String primaryValue, String column) {
		SqlConnector.getInstance().sendQuery("UPDATE " + table + " SET `" + column + "` = `" + column+ "` + 1 WHERE " + primary + " = '" + primaryValue+ "'");

	}
	public void tryIncrementVaria(String key){
		tryIncrement("varia", "key", key, "value", "'" + key + "', 1");
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

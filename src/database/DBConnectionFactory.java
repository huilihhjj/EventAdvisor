package database;

import database.mysql.MySQLDBConnection;

public class DBConnectionFactory {

	// This should change based on the pipeline. 
	private static final String DEFAULT_DB = "mysql";
	// Create a DBConnection based on given db type.
	public static DBConnection getDBConnection(String db) {
		switch (db) { 
			case "mysql":
				return new MySQLDBConnection();
			case "mongodb":
				return null;
			default:
				throw new IllegalArgumentException("Invalid db " + db);
		} 
	}
	// This is overloading not overriding.
	public static DBConnection getDBConnection() {
		return getDBConnection(DEFAULT_DB);
	}
}

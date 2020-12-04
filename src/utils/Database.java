package utils;
import java.sql.Connection;


import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


/**
 * Database class that holds methods relating to the database.
 */
public class Database {

	//Connection variables
	private Connection conn;
	//private final String tableName = "students";
	private String userName = "root";
	private String password = "";
	private String serverName = "localhost";
	private int portNumber = 3306;
	private String dbName = "Assign2";


	/**
	 * Empty Constructor for the Database Class
	 */
	public Database() { }


	/**
	 * Connection method for creating a connection between the Database and our app
	 *
	 * @return Connection to the database
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		Properties connectionProps = new Properties();

		connectionProps.put("user", userName);
		connectionProps.put("password", password);
		conn = DriverManager.getConnection("jdbc:mysql://"
				+ serverName + ":" + portNumber + "/" + dbName,
				connectionProps);

		return conn;
	}
}

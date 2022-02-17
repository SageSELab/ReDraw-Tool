package cs435.guiproto;

import java.sql.*;

/**
 * A Java MySQL PreparedStatement INSERT example.
 * Demonstrates the use of a SQL INSERT statement against a
 * MySQL database, called from a Java program, using a
 * Java PreparedStatement.
 * 
 * Created by Alvin Alexander, http://alvinalexander.com
 */
public class DatabaseInsert {
    private String compquery = " insert into views (parent_id, view, x, y, width,"
      + " height, text) values (?, ?, ?, ?, ?, ?, ?)";
	private int parent_id, x, y, width, height;
	private String view, text;
	
	/**
	 * Constructor
	 * @param parent_id
	 * @param view
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param text
	 */
    public DatabaseInsert(int parent_id, String view, int x, int y, int width, 
			int height, String text) {
    	this.parent_id = parent_id;
    	this.view = view;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.text = text;
	}
    
    /**
     * Compiles a query to send to the database, then sends the query.
     * @param password
     */
    
    public void execute(String password) {
    	try {
    		// create the connection
    		String myUrl = "jdbc:mysql://localhost";
    		Class.forName("com.mysql.jdbc.Driver");
    		Connection conn = DriverManager.getConnection(myUrl, "root", password);
    		
    		// create the insert prepared statement
    		Statement accStmt = conn.createStatement();
    		accStmt.execute("use android_db");
    		
    		PreparedStatement preparedStmt = null;
    		
    		if (view != null) {
    			preparedStmt = conn.prepareStatement(compquery);
    			
    			preparedStmt.setInt (1, parent_id);
    			preparedStmt.setString (2, view);
    			preparedStmt.setInt (3, x);
    			preparedStmt.setInt (4, y);
    			preparedStmt.setInt (5, width);
    			preparedStmt.setInt (6, height);
    			preparedStmt.setString(7, text);
    		}
    		
    		
    		// execute the prepared statement
    		preparedStmt.execute();
    		
    		conn.close();
    	}
    	
    	catch (Exception e) {
    		System.err.println("Got an exception!");
    		System.err.println(e.getMessage());
    	}
    }
}
package cs435.guiproto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseTest {
	   // JDBC driver name and database URL
	   static final String JDBC_DRIVER = "mysql-connector-java-5.1.41-bin.jar";  
	   static final String DB_URL = "jdbc:mysql://localhost/";

	   //  Database credentials
	   static final String USER = "root";
	   static final String PASS = "B1zShn1p5"; 
	   
	   private static boolean checkDBExists(String dbName){
		   Connection conn = null;
		   
		   try{
			   Class.forName("com.mysql.jdbc.Driver"); 
			   
			   conn = DriverManager.getConnection(DB_URL, USER, PASS);
			   ResultSet resultSet = conn.getMetaData().getCatalogs();
			   
			   while (resultSet.next()) {
				   String databaseName = resultSet.getString(1);
				   
				   if(databaseName.equals(dbName)){
					   return true;
		           
				   }
		       
			   }
			   resultSet.close();
		   
		   }
		   
		   catch(Exception e){
			   e.printStackTrace();

		   }

		    return false;
		    
	   }
	   
	   
	   public static void main (String args[]) {
		   Connection conn = null;
		   Statement stmt = null;
		   
		   try{
			   //STEP 2: Register JDBC driver
			   Class.forName("com.mysql.jdbc.Driver").newInstance();

			   //STEP 3: Open a connection
			   conn = DriverManager.getConnection(DB_URL, USER, PASS);

			   //STEP 4: Execute a query
			   stmt = conn.createStatement();
			   
			   if (checkDBExists("squirrel")==false){
				   conn.setAutoCommit(false);
				   String sql = "CREATE DATABASE squirrel";
				   String use = "USE squirrel";
				   String comp = "CREATE TABLE views (id int(11) not null primary key auto_increment,"
				   + "parent_id int(10), view varchar(20) not null, x int(8), y int(8), width int(8), height int(8), text varchar(100))";
				   
				   stmt.addBatch(sql);
				   stmt.addBatch(use);
				   stmt.addBatch(comp);
				   stmt.executeBatch();
				   conn.commit();
				   conn.setAutoCommit(true);
				   
			   }
			   
			   DatabaseInsert dbInsert = new DatabaseInsert(1, "x", 1, 1, 1, 1, "x");
			   dbInsert.execute(PASS);
			   
		   }catch(SQLException se){
			   //Handle errors for JDBC
			   se.printStackTrace();
			   
		   }catch(Exception e){
			   //Handle errors for Class.forName
			   e.printStackTrace();
			   
		   }finally{
			   try{
				   if(stmt!=null)
					   stmt.close();
				   
			   }catch(SQLException se2){}
			   
			   try{
				   if(conn!=null)
					   conn.close();
				   }catch(SQLException se){
					   se.printStackTrace();
					   }
			   }
		   System.out.println("Goodbye!");
		   
	   }
	   
}
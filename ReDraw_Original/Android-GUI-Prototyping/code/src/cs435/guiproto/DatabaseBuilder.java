package cs435.guiproto;

import java.sql.*;

/**
 * Builds a mySQL database to store Android views. 
 * If a database by the same name already exists, ends.
 *
 */
public class DatabaseBuilder {
   // JDBC driver name and database URL
   static final String JDBC_DRIVER = "mysql-connector-java-5.1.41-bin.jar";  
   static final String DB_URL = "jdbc:mysql://localhost/";

   //  Database credentials
   static final String USER = "root";
   static String PASS = "";
   
   /**
    * Checks to see if the database with the name dbName exists.
    * @param dbName
    * @return
    */
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
   
   /**
    * Creates a mySQL database.
    * @param password
    */
   public void MakeDatabase(String password) {
	   Connection conn = null;
	   Statement stmt = null;
	   
	   try{
		   Class.forName("com.mysql.jdbc.Driver").newInstance();

		   PASS = password;
		   conn = DriverManager.getConnection(DB_URL, USER, PASS);

		   stmt = conn.createStatement();
		   
		   if (checkDBExists("android_db")==false){
			   conn.setAutoCommit(false);
			   String sql = "CREATE DATABASE android_db";
			   String use = "USE android_db";
			   String view = "CREATE TABLE views (id int(11) not null primary key auto_increment,"
			   + "parent_id int(10), view varchar(20) not null, x int(8), y int(8), width int(8), height int(8), text varchar(100))";
			   
			   stmt.addBatch(sql);
			   stmt.addBatch(use);
			   stmt.addBatch(view);
			   stmt.executeBatch();
			   conn.commit();
			   conn.setAutoCommit(true);
			   
		   }
		   
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
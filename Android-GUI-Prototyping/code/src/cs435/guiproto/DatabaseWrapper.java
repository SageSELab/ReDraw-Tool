
package cs435.guiproto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
/**
 * Provides a wrapper for putting activities into and getting activities out of the database.
 * 
 * While activities and their components could be manipulated directly from the database, it's
 * usually more natural to use Java classes, Lists, and what-not. To that end, this will provide
 * a simple interface for getting activities and components from the database without having to
 * muck about in SQL queries. 
 */
public class DatabaseWrapper {

	public int tempIntString;
	private String password = "";
	
	public DatabaseWrapper() {

	}

	/**
	 * Call DatabaseBuilder to access mySQL and build the database 'android_db'. 
	 * Nothing happens if the database already exists.
	 */
	public void build(){
		DatabaseBuilder builder = new DatabaseBuilder();
		builder.MakeDatabase(password);
	}

	/**
	 * Change the existing mySQL password to newpassword.
	 * @param newpassword
	 */
	public void setPassword(String newpassword) {
		password = newpassword;
	}
	
	/**
	 * Scans through the view structure and adds each one to the database.
	 * Results in a similar listing order to the xml file.
	 * @param parent
	 */
	protected void traverseViewTree(ViewGroup parent){
		List <View> siblings = parent.getChildren();
		
		for (int i=0; i < siblings.size(); i++){
			View node = siblings.get(i);
			
			addView(parent.id, node);
			if (node instanceof ViewGroup) {
				traverseViewTree((ViewGroup) node);
			}
		}
		
	}
	
	/**
	 * Accesses the database to add a new view.
	 * @param parent_id
	 * @param node
	 */
	private void addView(int parent_id, View node) {
		DatabaseInsert comp = new DatabaseInsert(parent_id, node.getName(), node.getX(), node.getY(), node.getWidth(), node.getHeight(), node.getText());
		comp.execute(password);
	}
}
package cs435.extra;

/**
 * Print out the name of the current user. If you "register" your user name and Android SDK path in AcceptanceTests,
 * then you won't have to specify it every time you run the program!
 * @author Ben
 *
 */
public class GetUserName {
	
	public static void main(String[] args) {
		String name = System.getProperty("user.name");
		System.out.println("      HELLO");
		System.out.println("    My Name Is");
		System.out.println(name);
	}
	
}

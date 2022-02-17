package cs435.guiproto;

import java.io.File;

public class AGPMain {
	
	public static int SCREEN_WIDTH = 1200;
	public static int SCREEN_HEIGHT = 1920;
	public static int SCREEN_DPI = 323;
	public static void main(String args[]) {

		//command line functionality for testing
		if (args.length != 0){
			System.out.println("Command line functionality invoked");
			Title title = new Title(false);
			title.setInputFile(args[0]);
			title.setOutputFile(args[1]);
			title.setSdkLocation(args[2]);
			title.setScreenshot(args[3]);
			boolean useAbsolutePositioning = Boolean.parseBoolean(args[4]);
			Constants.setGUIconstants(
					SCREEN_DPI,
					SCREEN_WIDTH,
					SCREEN_HEIGHT,
					SCREEN_WIDTH,
					SCREEN_HEIGHT
			);
			//clear out the test directory
			deleteFolder(new File(args[1]));
			title.makeEntireProject(useAbsolutePositioning);
			
		} else {
			new Title(true);
		}
	}
	/**
	 * Method to recursively delete folders from test directory. Code obtained from
	 * http://stackoverflow.com/questions/7768071/how-to-delete-directory-content-in-java
	 * @author NCode
	 * @param folder
	 */
	public static void deleteFolder(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}
}

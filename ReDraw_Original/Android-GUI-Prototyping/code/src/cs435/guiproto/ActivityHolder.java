/**
 * 
 */
package cs435.guiproto;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cs435.extra.BackgroundGetter;

/**
 * Represents a single activity, or "page", of an Android app.
 */
public class ActivityHolder {
	
	private static int nextId = 0;
	private int id;
	private String className;
	private String layoutName;
	private Path screenshotPath;
	private View root;
	
	/**
	 * Generate a new activity.
	 * 
	 * @param className Name of the generated activity's Java class. Should be in CamelCase
	 * @param components  Root component of the activity, usually a Layout or descendant thereof
	 * @throws IOException 
	 */
	public ActivityHolder(String className, Path screenshot, View root) throws IOException {
		id = nextId;
		nextId++;
		
		this.className= className;
		this.screenshotPath = screenshot;
		this.root = root;
		// Layout name is snake_case version of className
		layoutName = className.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase() + ".xml";
		if (screenshot == null) {
			throw new IllegalArgumentException("Screenshot cannot be null");
		}
		// Add child views to this activity
		root.setActivity(this);
	}
	
	/**
	 * Return this activity's XML layout tree.
	 * @return
	 */
	public Document buildLayoutXMLDocument(boolean abs) {
		Document doc;
		try {
			doc = DocumentBuilderFactory
					.newInstance()
					.newDocumentBuilder()
					.newDocument();
			
			// Set header attributes
			Element header;
			if (abs){
				//set to zero because it is the root element
				header = getRoot().getLayoutElementAbsolute(doc, getRoot().getX(), getRoot().getY());
			}
			else{
				header = getRoot().getLayoutElement(doc);
			}
			header.setAttribute("xmlns:android", "http://schemas.android.com/apk/res/android");
			header.setAttribute("android:layout_width", "match_parent");
		 	header.setAttribute("android:layout_height","match_parent");
		 	header.setAttribute("android:background", "@drawable/" + getBackgroundName());
			doc.appendChild(header);
		} catch (ParserConfigurationException e) {
			System.err.println("This should never happen. If it does, we'll crash.");
			e.printStackTrace();
			System.exit(1);
			return null;
		}
		return doc;
	}
	
	/**
	 * Create all drawables (and other resources) needed to run this activity, including
	 * the resources needed by the activity's various views.
	 * 
	 * @param projectRoot
	 * @throws IOException
	 */
	public void generateResources(Path projectRoot) throws IOException {
		final Path drawableFile = projectRoot.resolve("src/main/res/drawable-xxhdpi/" + getBackgroundName() + ".png");
		BackgroundGetter.generateBackground(this, drawableFile);
		
		for (View c : getViewList()) {
			c.generateResources(projectRoot);
		}
	}
	
	/**
	 * Get this activity's unique ID.
	 * @return
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Return the .png screenshot file that this activity is associated with.
	 * @return
	 */
	public Path getScreenshotPath() {
		return screenshotPath;
	}
	
	/**
	 * Get the class name associated with the layout file, e.g. ActivityMain
	 * @return
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * Get the name of the layout file, e.g. activity_main.xml
	 */
	public String getLayoutName() {
		return layoutName;
	}
	
	/**
	 * Get a list of every component in the activity
	 */
	public View getRoot() {
		return root;
	}
	
	/**
	 * Get the view tree as a list; good for iteration
	 * @return
	 */
	public List<View> getViewList() {
		return getViewListFrom(root);
	}
	
	/**
	 * Recursive part of getViewList()
	 * @param root
	 * @return
	 */
	private List<View> getViewListFrom(View root) {
		if (root instanceof ViewDecorator) {
			root = ((ViewDecorator) root).getView();
		}
		
		List<View> out = new ArrayList<>();
		out.add(root);
		
		if (root instanceof ViewGroup) {
			for (View child : ((ViewGroup) root).getChildren()) {
				out.addAll(getViewListFrom(child));
			}
		}
		return out;
	}
	
	/**
	 * Get the name of the background drawable
	 * @return
	 */
	private String getBackgroundName() {
		return "background" + getId();
	}

}

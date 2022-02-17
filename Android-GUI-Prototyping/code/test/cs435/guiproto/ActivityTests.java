package cs435.guiproto;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit tests for ActivityHolder's various functions.
 * 
 * @author bdpowell
 *
 */
public class ActivityTests {
	
	static final Path SCREENSHOT = Paths.get("resources/simple-dump.png");
	
	private ActivityHolder activity;
	private LinearLayout root;
	private RelativeLayout relative;
	private DummyViewGroup dummy;
	private Button button1, button2, button3;
	
	/**
	 * Create a sample activity by hard-coding in components (avoid using the importer)
	 * @throws IOException
	 */
	@Before
	public void setUp() throws IOException {
		/*
		 * LinearLayout:
		 *   Button1
		 *   RelativeLayout:
		 *     Button2
		 *   DummyLayout:
		 *     Button3
		 *     
		 * These layouts DO NOT correspond with simple-dump.uix or any other
		 * real layout; they're just there so the code doesn't get mad at me for
		 * supplying components with no values.
		 */
		button1 = (Button) ViewBuilder.buildComponent("Button");
		button1.setValues(0, 0, 24, 24);
		button2 = (Button) ViewBuilder.buildComponent("Button");
		button2.setValues(24, 0, 24, 24);
		button3 = (Button) ViewBuilder.buildComponent("Button");
		button3.setValues(48, 0, 24, 24);
		
		root = (LinearLayout) ViewBuilder.buildViewGroup("LinearLayout");
		root.setValues(0, 0, 1600, 2560/2);
		root.orient = LinearLayout.Orientation.Vertical;
		root.addToChildren(button1);
		
		relative = (RelativeLayout) ViewBuilder.buildViewGroup("RelativeLayout");
		root.setValues(0, 2560/2, 1600, 2560/2);
		relative.addToChildren(button2);
		root.addToChildren(relative);
		
		dummy = (DummyViewGroup) ViewBuilder.buildViewGroup("this layout type doesn't exist");
		dummy.setValues(0, 24, 1600, 24);
		dummy.addToChildren(button3);
		root.addToChildren(dummy);
		
		activity = new ActivityHolder("TestClass", SCREENSHOT, root);
	}
	
	/**
	 * Test that building a project generates a background resource
	 * that shows up in the XML layout file.
	 * 
	 * @throws IOException 
	 */
	@Test
	public void testActivityBuildsABackground() throws IOException {
		Path tempRoot = Files.createTempDirectory("project-root");
		Path drawable = Files.createDirectories(tempRoot.resolve("src/main/res/drawable-xxhdpi/"));
		
		activity.generateResources(tempRoot);
		Document layout = activity.buildLayoutXMLDocument(true);
		
		// Confirm that background is in the right place
		final Path expectedBackground = drawable.resolve("background" + activity.getId() + ".png");
		if (!Files.exists(expectedBackground)) {
			fail("Background file never generated");
		}
		
		// Confirm that header element exists
		Element header = (Element) layout.getFirstChild();
		String backgroundName = header.getAttribute("android:background");
		assertNotEquals("Background attribute doesn't exist", null, backgroundName);
		assertEquals("Background attribute incorrect", "@drawable/background" + activity.getId(), backgroundName);
	}
	
	/**
	 * Test that getViewList returns all of an activity's non-decorator components,
	 * without duplicates or missing views
	 */
	@Test
	public void testGetViewListReturnsAllComponents() {
		HashMap<View, Boolean> foundViews = new HashMap<>();
		foundViews.put(root,     false);
		foundViews.put(relative, false);
		foundViews.put(dummy,    false);
		foundViews.put(button1,  false);
		foundViews.put(button2,  false);
		foundViews.put(button3,  false);
		
		for (View child : activity.getViewList()) {
			if (foundViews.containsKey(child)) {
				if (foundViews.get(child) == true) {
					fail("Found duplicate view: " + child);
				} else {
					foundViews.put(child, true);
				}
			} else {
				fail("View list has element not in original list: " + child);
			}
		}
		
		Iterator<Entry<View, Boolean>> it = foundViews.entrySet().iterator();
		while (it.hasNext()) {
			Entry<View, Boolean> e = it.next();
			if (e.getValue() == false) {
				fail("View list did not return " + e.getKey());
			}
		}
	}
	
}

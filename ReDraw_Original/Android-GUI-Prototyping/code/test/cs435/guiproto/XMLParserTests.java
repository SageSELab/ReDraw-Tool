package cs435.guiproto;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests related to the XMLParser class.
 * 
 * @author bdpowell
 *
 */
public class XMLParserTests {
	
	/*
	 * Original representation:
<hierarchy rotation="2">
    <node class="android.widget.FrameLayout" >
        <node  class="android.view.ViewGroup" >
            <node  class="android.widget.FrameLayout" >
                <node  class="android.view.ViewGroup" >
                    <node  class="android.widget.TextView" >
                </node>
            </node>
            <node  class="android.widget.FrameLayout" >
                <node class="android.widget.LinearLayout" >
                    <node  class="android.widget.TextView" >
                    <node  class="android.widget.TextView" >
                    <node  class="android.widget.Button" >
                    <node  class="android.widget.ImageButton" >
                </node>
            </node>
        </node>
    </node>
</hierarchy>

	 * New representation:
<hierarchy rotation="2">
        <node  class="android.view.ViewGroup" >
            <node  class="android.view.ViewGroup" >
                    <node  class="android.widget.TextView" >
            </node>
            <node class="android.widget.LinearLayout" >
                    <node  class="android.widget.TextView" >
                    <node  class="android.widget.TextView" >
                    <node  class="android.widget.Button" >
                    <node  class="android.widget.ImageButton" >
            </node>
        </node>
</hierarchy>
	 */
	
	static final Path UI_DUMP = Paths.get("resources/simple-dump.uix");
	static final Path SCREENSHOT = Paths.get("resources/simple-dump.png");
	static ActivityHolder activity;
	
	@BeforeClass 
	public static void setUpBeforeClass() throws Exception {
		// Load in our unit test
		activity = XMLParser.parseActivityFromFile(UI_DUMP, SCREENSHOT, "TestActivity");
	}
	
	@Test
	public void testAllViewsHaveTheRightActivity() {
		assertIfActivityDoesNotMatch(activity.getRoot());
	}
	
	private void assertIfActivityDoesNotMatch(View root) {
		assertEquals("View's saved activity does not match its parent's", activity, root.activity);
		if (root instanceof ViewGroup) {
			for (View child : ((ViewGroup) root).getChildren()) {
				assertIfActivityDoesNotMatch(child);
			}
		} else if (root instanceof ViewDecorator) {
			assertEquals("Decorator's internal activity doesn't match", activity, ((ViewDecorator) root).view.activity);
 		}
	}
	
	/**
	 * Check to see if "redundant" layouts are removed. These are:
	 *  - Layouts with one child in them
	 *  - Layouts with the same size as their child
	 */
	@Test
	public void testEliminateDuplicateViewGroups() {
		// TODO Test is broken, not sure how
		View root = activity.getRoot();
		// Root node should be ViewGroup, not FrameLayout
		assertEquals("Root's duplicate node was not eliminated", "ViewGroup", getRealName(root));
		// First child should be a ViewGroup with a TextView
		List<View> children = ((ViewGroup) activity.getRoot()).getChildren();
		ViewGroup firstChild = (ViewGroup) children.get(0);
		assertEquals("First child's duplicate node was not eliminated", "ViewGroup", getRealName(firstChild));
		assertEquals("First child does not have the right number of children", 1, firstChild.getChildren().size());
		assertEquals("First child does not have the right child", "TextView", getRealName(firstChild.getChildren().get(0)));
		// Second child should be a LinearLayout with four further children
		ViewGroup secondChild = (ViewGroup) children.get(1);
		assertEquals("Second child's duplicate node was not eliminated", "LinearLayout", getRealName(secondChild));
		assertEquals("First child does not have the right number of children", 4, secondChild.getChildren().size());
	}
	
	private String getRealName(View v) {
		if (v instanceof DummyComponent) {
			return ((DummyComponent) v).getOriginalName();
		} else if (v instanceof DummyViewGroup) {
			return ((DummyViewGroup) v).getOriginalName();
		} else {
			return v.getName();
		}
	}

	
}

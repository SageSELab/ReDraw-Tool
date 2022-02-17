package cs435.guiproto;

import static org.junit.Assert.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Unit tests for StyleFragment.
 * 
 * @author bdpowell
 */
public class StyleFragmentTests {
	
	Document doc;
	
	@Before
	public void setUp() throws ParserConfigurationException {
		doc = DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder()
				.newDocument();
	}
	
	/**
	 * Test a single StyleFragment by adding several attributes and
	 * checking the generated XML element.
	 * @throws ParserConfigurationException 
	 */
	@Test
	public void testSingleStyleFragmentWithAttributes() {
		StyleFragment frag = new StyleFragment();
		frag.addStringAttribute("sname1", "svalue1");
		frag.addStringAttribute("sname2", "svalue2");
		frag.addColorAttribute("red", new Color(255, 0, 0));
		
		Element xml = frag.getStyleXML(doc);
		assertEquals("Start of name is wrongly derived", "Style", xml.getAttribute("name").substring(0,5));
		// Not sure if this should be AppTheme, android:style/AppTheme, or what
		//assertEquals("Parent is wrongly derived", "AppTheme", xml.getAttribute("parent"));
		
		// Get element nodes from children
		Map<String, String> items = getStyleItems(xml);
		
		// Hopefully these are in order...
		assertEquals("Incorrect number of items generated", 3, items.size());
		
		assertEquals("First item's value is wrong", "svalue1", items.get("sname1"));
		assertEquals("Second item's value is wrong", "svalue2", items.get("sname2"));
		assertEquals("Third item's value is wrong", "#ff0000", items.get("red").toLowerCase());
	}
	
	/**
	 * Adding the same attribute twice to one StyleFragment will result
	 * in an IllegalArgumentException
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testCannotAddTheSameStringAttributeTwice() {
		StyleFragment frag = new StyleFragment();
		frag.addStringAttribute("a", "1");
		frag.addStringAttribute("a", "2");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCannotAddTheSameColorAttributeTwice() {
		StyleFragment frag = new StyleFragment();
		frag.addColorAttribute("a", Color.RED);
		frag.addColorAttribute("a", Color.BLUE);
	}
	
	@Test
	public void testMergingStringAndColorAttributes() {
		final String midpoint = ImagesHelper.c2hex(new Color(255/2, 255/2, 255/2));
		
		// When merging string attributes, the first attribute is kept, and the second discarded
		StyleFragment s1 = new StyleFragment();
		s1.addStringAttribute("string", "first");
		s1.addColorAttribute("color", Color.BLACK);
		
		// When merging color attributes, the colors are averaged
		StyleFragment s2 = new StyleFragment();
		s2.addStringAttribute("string", "first");
		s2.addColorAttribute("color", Color.WHITE);
		
		StyleFragment merged = s1.mergeFragments(s2);
		Element xml = merged.getStyleXML(doc);
		Map<String, String> items = getStyleItems(xml);
		
		assertEquals("Wrong number of items", 2, items.size());
		assertEquals("Incorrect string attribute (first should be kept, second discarded)", "first", items.get("string"));
		assertEquals("Incorrect color attribute (black and white should average to gray", midpoint, items.get("color"));
	}
	
	/**
	 * Return a Map of a style's items from the XML.
	 * 
	 * For example, this item:
	 * 
	 * <item name="sname1">svalue1</item>
	 * 
	 * Becomes {"sname1": "svalue1"}
	 * 
	 * @param style
	 * @return
	 */
	private Map<String, String> getStyleItems(Element style) {
		Map<String, String> items = new HashMap<>();
		
		NodeList nodes = style.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++) {
			if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) nodes.item(i);
				String name = e.getAttribute("name");
				String value = ((Text) e.getFirstChild()).getTextContent();
				
				if (items.containsKey(name)) {
					fail("Duplicate item: tried to insert " + value + ", but already has " + items.get(name));
				}
				items.put(name, value);
			}
		}
		
		return items;
	}
	
}
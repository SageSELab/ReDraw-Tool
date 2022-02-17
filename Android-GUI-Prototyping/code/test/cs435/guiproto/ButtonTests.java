package cs435.guiproto;

import static org.junit.Assert.*;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Tests related to the button component - specifically tests related to the color-grabbing
 * functionality.
 * @author bdpowell
 */
public class ButtonTests {
	
	private static final Path dumps  = Paths.get("resources/dumps");
	private static final Path ux     = dumps.resolve("colored-buttons.uix");
	private static final Path screen = dumps.resolve("colored-buttons.png");
	
	private Document document;
	
	/*
	 * 0: red background,   white text
	 * 1: green background, black text
	 * 2: blue background,  white text,
	 * 3: black background, white text
	 */
	private List<View> components;
	
	@Before
	public void setUp() throws ParserConfigurationException, SAXException, IOException {
		document = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder()
				.newDocument();
		
		ActivityHolder main = XMLParser.parseActivityFromFile(ux, screen, "MainActivity");
		components = main.getViewList();
	}
	
	@Test
	public void testBrightSingleColorButtons() throws SAXException, IOException, ParserConfigurationException {
		for (View c: components) {
			Color background;
			Color text;
			String backgroundName;
			String textName;
			switch (c.getText()) {
			case "EVERY TIME WE TOUCH":
				background = Color.RED;
				text = Color.WHITE;
				backgroundName = "Red";
				textName = "White";
				break;
			case "I GET THIS FEELING":
				background = Color.GREEN;
				text = Color.BLACK;
				backgroundName = "Green";
				textName = "Black";
				break;
			case "EVERY TIME WE KISS":
				background = Color.BLUE;
				text = Color.WHITE;
				backgroundName = "Blue";
				textName = "White";
				break;
			case "I FEEL EMPTY INSIDE":
				background = Color.BLACK;
				text = Color.WHITE;
				backgroundName = "Black";
				textName = "White";
				break;
			default:
				continue;
			}
			Element button = c.getLayoutElement(document);
			// Check background color
			String backgroundAttr = button.getAttribute("android:background");
			assertNotEquals("Background attribute doesn't exist", backgroundAttr, "");
			
			Color actualBackground = Helpers.colorFromHex(backgroundAttr);
			Helpers.assertColorsClose(backgroundName + "'s background doesn't match",
					actualBackground,
					background);
			// Check text color
			String textAttr = button.getAttribute("android:textColor");
			assertNotEquals("Text attribute doesn't exist", textAttr, "");
			
			Color actualText = Helpers.colorFromHex(textAttr);
			Helpers.assertColorsClose(textName + "'s text doesn't match", actualText, text);
		}
	}
	
}

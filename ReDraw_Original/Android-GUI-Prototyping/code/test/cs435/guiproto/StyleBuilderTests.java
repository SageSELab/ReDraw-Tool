package cs435.guiproto;

import static org.junit.Assert.*;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class StyleBuilderTests {
	
	// TODO No longer correct, update this later (by which I mean never)
	static final String STYLES_STRING = 
		  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
		+ "<resources>\n\n"
		+ "    <style name=\"AppTheme\" parent=\"Theme.NoTitleBar\">\n"
		+ "        <item name=\"android:buttonStyle\">@style/ButtonStyle</item>\n"
		+ "        <item name=\"android:textViewStyle\">@style/TextViewStyle</item>\n"
		+ "    </style>\n\n"
		+ "    <style name=\"ButtonStyle\" parent=\"android:style/Widget.Button\">\n"
		+ "        <item name=\"android:textSize\">19sp</item>\n"
		+ "        <item name=\"android:backgroundTint\">#ff0000</item>\n"
		+ "    </style>\n\n"
		+ "    <style name=\"TextViewStyle\" parent=\"android:style/Widget.TextView\">\n"
		+ "        <item name=\"android:textColor\">#ffffff</item>\n"
		+ "        <item name=\"android:textSize\">19sp</item>\n"
		+ "    </style>\n\n"
		+ "</resources>\n";
	
	static Document expectedStyles;
	
	@BeforeClass
	public static void setUpClass() throws ParserConfigurationException, SAXException, IOException {
		InputSource stream = new InputSource(new StringReader(STYLES_STRING));
		
		expectedStyles = DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder()
				.parse(stream);
		expectedStyles.normalizeDocument();
	}
	
	// TODO Fix this test
	@Test
	public void testBuildStyleXML() throws IOException, TransformerException {
		List<View> views = new ArrayList<>();
		
		StyleFragment textView = new StyleFragment();
		textView.addStringAttribute("android:textSize", "19sp");
		textView.addColorAttribute("android:textColor", new Color(255, 255, 255));
		views.add(new StyleViewStub("TextView", textView));
		
		StyleFragment button = new StyleFragment();
		button.addStringAttribute("android:textSize", "19sp");
		button.addColorAttribute("android:backgroundTint", new Color(255, 0, 0));
		views.add(new StyleViewStub("Button", button));
		
		//Document actualStyles = StyleBuilder.buildStyleXML(views);
		//actualStyles.normalizeDocument();
		
		/*
		 * Having fun reading my crappy test code? It's because I didn't realize there are
		 * libraries out there for comparing and testing XML documents! If you have to write
		 * more tests for this library (and you will), I recommend checking you XMLUnit,
		 * which purports to painlessly compare two XML documents and point out the
		 * discrepancies.
		 */
//		if (!actualStyles.isEqualNode(expectedStyles)) {
//			System.err.println("Style documents don't match");
//			System.err.println("\nEXPECTED\n\n");
//			printDocument(expectedStyles, System.err);
//			System.err.println("\n\nACTUAL\n\n");
//			printDocument(actualStyles, System.err);
//			fail("Documents not equal, see console for more");
//		}
	}
	
	// Code unceremoniously stolen from:
	// http://stackoverflow.com/questions/2325388/what-is-the-shortest-way-to-pretty-print-a-org-w3c-dom-document-to-stdout
	public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	    transformer.transform(new DOMSource(doc), 
	         new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}
	
	/**
	 * Mock class that, instead of generating a style fragment, just passes one on
	 * @author bdpowell
	 *
	 */
	class StyleViewStub extends View {
		public StyleViewStub(String name, StyleFragment fragment) {
			super(name);
			this.fragment = fragment;
		}
		
		@Override
		public StyleFragment getStyleFragment() {
			return fragment;
		}
	}
	
}
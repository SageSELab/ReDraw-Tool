
package cs435.guiproto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Parses UIAutomatorViewer files into ActivityHolders.
 * 
 * @author jacobharless, benpowell
 */
public class XMLParser {

	/**
	 * Parses an XML file and returns an Activity.
	 * @param uxDump Path to the .uix or .xml file
	 * @param screenshot Path to the screenshot image
	 * @param name Name of the resulting activity's class; should be CamelCase.
	 * @return New activity witht he components from uxDump
	 * @throws SAXException
	 * @throws IOException
	 */
	public static ActivityHolder parseActivityFromFile(Path uxDump, Path screenshot, String name) 
			throws SAXException, IOException {
		View componentTree = XMLParser.getViewTree(uxDump.toFile());
		return new ActivityHolder(name, screenshot, componentTree);
	}
	
	/**
	 * Get the view tree from a UIAutomatorViewer file.
	 * @param in
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	private static View getViewTree(File in) throws SAXException, IOException{
		DocumentBuilderFactory docbuildfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docbuilder;
		try {
			docbuilder = docbuildfactory.newDocumentBuilder();
			Document doc = docbuilder.parse(in);

			Node root = doc.getElementsByTagName("node").item(0);
			return makeViewTree(root);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Return a view tree from a UIAutomatorViewer root node.
	 * @param node
	 * @return
	 */
	private static View makeViewTree(Node node){
		// Remove duplicate nodes (does not account for overlapping Views)
		if (node.hasChildNodes()) {
			Node child = node.getFirstChild();
			boolean replaced = true;
			if (node.getChildNodes().getLength()>1)
			{
				replaced = false;
			}
			

			while (replaced && child != null && child.getNodeType() == Node.ELEMENT_NODE) {
				String bounds      = node.getAttributes().getNamedItem("bounds").getNodeValue();
				String childBounds = child.getAttributes().getNamedItem("bounds").getNodeValue();
				if (bounds.equals(childBounds)) {
					node = node.getFirstChild();
					
					child = node.getFirstChild();
					if (node.getChildNodes().getLength()>1)
					{
						replaced = false;
					}
				}
				else{
					replaced =false;
				}
			}
		}
		
		if (node.hasChildNodes()) {
			/*
			 * Something weird about getChildNodes() is that it returns both 
			 * the node's attributes and its elements. I think it also returns
			 * all children regardless of depth.
			 * 
			 * Instead, we use getFirstChild() and getNextSibling(), which works
			 * just as well.
			 */
			ViewGroup layout = (ViewGroup) makeView(node);
			Node child = node.getFirstChild();
			while (child != null) {
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					layout.addToChildren(makeViewTree(child));
				}
				child = child.getNextSibling();
			}
			layout.pack();
			return (View) layout; 
		} else {
			return makeView(node);
		}
	}
	
	/**
	 * Convert a single XML node into a view.
	 * @param node
	 * @returns a view
	 */
	private static View makeView(Node node){
		String text = node.getAttributes().getNamedItem("text").getNodeValue();
		String klass = node.getAttributes().getNamedItem("class").getNodeValue();
		List<String> arr = new ArrayList<String>(Arrays.asList(klass.split("\\.")));
		String component = arr.get(arr.size()-1);

		String bounds = node.getAttributes().getNamedItem("bounds").getNodeValue();
		
		//following code get the x and y coordinates from the bounds
		bounds = bounds.replaceAll("]",",");
		bounds = bounds.substring(1);
		bounds = bounds.replace("[","");
		List<String> list = new ArrayList<String>(Arrays.asList(bounds.split(",")));
		//System.out.println(list);
		int xstart;
		int ystart;
		int xend;
		int yend;
		xstart = Integer.parseInt(list.get(0));
		ystart = Integer.parseInt(list.get(1));
		xend   = Integer.parseInt(list.get(2));
		yend   = Integer.parseInt(list.get(3));
		int width = (xend-xstart);
		int height = (yend-ystart);
		
		// Add nodes to list
		View out;
		if (node.hasChildNodes()) {
			out = ViewBuilder.buildViewGroup(component);
		} else {
			out = ViewBuilder.buildComponent(component);
		}
		out.setValues((int) Constants.scale(xstart, true),(int)  Constants.scale(ystart, false), 
				(int) Constants.scale(width, true), (int) Constants.scale(height, false), text);
		return out;
	}
}



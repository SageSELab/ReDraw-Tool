/*******************************************************************************
 * Copyright (c) 2016, SEMERU
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those
 * of the authors and should not be interpreted as representing official
 * policies,
 * either expressed or implied, of the FreeBSD Project.
 *******************************************************************************/

package edu.wm.semeru.redraw.pipeline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.redraw.uiautomator.tree.AttributePair;
import com.redraw.uiautomator.tree.BasicTreeNode;
import com.redraw.uiautomator.tree.RootUINode;
import com.redraw.uiautomator.tree.UiTreeNode;

import edu.semeru.redraw.synthetic.SyntheticHelper;
import edu.semeru.redraw.synthetic.SyntheticViolation;
import edu.semeru.redraw.synthetic.SyntheticViolation.SyntheticBuilder;

/**
 * This class will read in an Android-GUI .xml file and parse the contents into
 * a data structure called a BasicTreeNode.
 * 
 * @author KevinMoran, Carlos modified by Boyang
 */
public class UIDumpParser {

	private RootUINode mRootNode;

	public UIDumpParser() {
	}

	public ArrayList<UiTreeNode> getLeafNodes() {
		return mRootNode.getLeafNodes();
	}

	public void transform() {
		mRootNode.transform();
	}

	public void printOutTree() {
		mRootNode.printOutTree();
	}

	/**
	 * Description: Uses a SAX parser to process XML dump. This method will read
	 * in an Android-GUI .xml file and parse the contents into a data structure
	 * called a BasicTreeNode. This data structure can be further parsed in the
	 * high-level DynGuiComponent Value Object which encapsulates a large number
	 * of the attributes of the nodes of the graph.
	 * 
	 * @param xmlPath
	 * @return
	 */
	public RootUINode parseXml(String xmlPath) {
		mRootNode = null;
		// standard boilerplate to get a SAX parser
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = null;
		try {
			parser = factory.newSAXParser();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		} catch (SAXException e) {
			e.printStackTrace();
			return null;
		}
		// handler class for SAX parser to receiver standard parsing events:
		// e.g. on reading "<foo>", startElement is called, on reading "</foo>",
		// endElement is called
		DefaultHandler handler = new DefaultHandler() {
			BasicTreeNode mParentNode;
			BasicTreeNode mWorkingNode;

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
				boolean nodeCreated = false;
				// starting an element implies that the element that has not yet
				// been closed
				// will be the parent of the element that is being started here
				mParentNode = mWorkingNode;
				if ("hierarchy".equals(qName)) {
					mWorkingNode = new RootUINode(attributes.getValue("windowName"), attributes.getValue("rotation"));
					nodeCreated = true;
				} else if ("node".equals(qName)) {
					UiTreeNode tmpNode = new UiTreeNode();
					// System.out.println("-------");
					for (int i = 0; i < attributes.getLength(); i++) {
						// System.out.println(attributes.getQName(i) + " - " +
						// attributes.getValue(i));
						tmpNode.addAtrribute(attributes.getQName(i), attributes.getValue(i));
					}
					mWorkingNode = tmpNode;
					nodeCreated = true;
				}
				// nodeCreated will be false if the element started is neither
				// "hierarchy" nor "node"
				if (nodeCreated) {
					if (mRootNode == null) {
						// this will only happen once
						mRootNode = (RootUINode) mWorkingNode;
					}
					if (mParentNode != null) {
						mParentNode.addChild(mWorkingNode);
					}
				}
			}

			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {
				// mParentNode should never be null here in a well formed XML
				if (mParentNode != null) {
					// closing an element implies that we are back to working on
					// the parent node of the element just closed, i.e. continue
					// to
					// parse more child nodes
					mWorkingNode = mParentNode;
					mParentNode = mParentNode.getParent();
				}
			}
		};
		try {
			parser.parse(new File(xmlPath), handler);
		} catch (SAXException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return mRootNode;
	}
	
	/**
	 * @param output
	 * @param tree
	 * @param violations
	 * @param builderRulesXml
	 */
	public void buildXml(String output, BasicTreeNode tree, StringBuilder builderXml,
			SyntheticViolation... violations) {
		if (tree != null) {
			boolean singleLine = false;
			if (tree.getChildCount() == 0) {
				singleLine = true;
			}
			boolean isMissing = false;
			// Update tree
			for (SyntheticViolation violation : violations) {
				UiTreeNode node = violation.getNode();
				if (tree instanceof UiTreeNode && node.getX() == tree.getX() && node.getY() == tree.getY()
						&& node.getWidth() == tree.getWidth() && node.getHeight() == node.getHeight()) {
					switch (violation.getInjectionType()) {
					case SyntheticHelper.LOCATION:
						((UiTreeNode) tree).addAtrribute("bounds", "[" + violation.getNewX() + "," + violation.getNewY()
						+ "][" + (violation.getOriginalWidth() + Integer.parseInt(violation.getNewX())) + "," + (violation.getOriginalHeight() + Integer.parseInt(violation.getNewY())) + "]");
						break;
					case SyntheticHelper.NUMBER_COMPONENTS:
						isMissing = true;
						break;
					case SyntheticHelper.SIZE:
						((UiTreeNode) tree).addAtrribute("bounds", "[" + node.getX() + "," + node.getY() + "]["
								+ (Integer.parseInt(violation.getNewWidth()) + node.getX()) + "," + (Integer.parseInt(violation.getNewHeight()) + node.getY()) + "]");
						break;
					case SyntheticHelper.TEXT_CONTENT:
						((UiTreeNode) tree).addAtrribute("text", violation.getNewText());
						break;
					case SyntheticHelper.TEXT_FONT:
					case SyntheticHelper.TEXT_COLOR:
					case SyntheticHelper.IMAGE:
					case SyntheticHelper.IMAGE_COLOR:
					case SyntheticHelper.COMPONENT_COLOR:
						// Do nothing
						break;
					default:
						break;
					}
					break;
				}
			}

			if(!isMissing) {
				String attributes = getAttributes(tree);
				if (singleLine) {
					builderXml.append("<node " + attributes + "/>");
				} else {
					if (tree instanceof RootUINode) {
						builderXml.append("<hierarchy " + attributes + ">");
					} else {
						builderXml.append("<node " + attributes + ">");
					}
				}
			}

			// For loop
			//            for (BasicTreeNode child : tree.getChildren()) {
			//                buildXml(output, child, builderXml, violations);
			//            }
			for(int i = 0; i < tree.getChildCount(); i++) {
				buildXml(output, tree.getChildren().get(i), builderXml, violations);
			}
			// Close node
			if(!isMissing) {
				if (!singleLine) {
					if (tree instanceof RootUINode) {
						builderXml.append("</hierarchy>");
					} else {
						builderXml.append("</node>");

					}
				}
			}
		}

	}

	/**
	 * @param tree
	 * @return
	 */
	private String getAttributes(BasicTreeNode tree) {
		Object[] attributesArray = tree.getAttributesArray();
		String result = "";
		for (int i = 0; i < attributesArray.length; i++) {
			AttributePair attribute = (AttributePair) attributesArray[i];
			result += attribute.key + "=\"" + ((attribute.value != null) ? attribute.value.replace("\"", "&quot;") : "")
					+ "\" ";
		}
		return result;
	}
}

/*******************************************************************************
 * Copyright (c) 2016, SEMERU
 * All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 *******************************************************************************/
/**
 * XmlHelper.java
 * 
 * Created on Jun 26, 2014, 1:12:26 AM
 * 
 */
package edu.wm.cs.semeru.core.helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import edu.wm.cs.semeru.core.model.no_db.XmlComponent;



/**
 * {Insert class description here}
 * 
 * @author Carlos Bernal
 * @since Jun 26, 2014
 */
public class XmlHelper {

    private static XMLStreamReader reader;

    /**
     * 
     */
    public XmlHelper(String pathReader) {
	super();
	XMLInputFactory factory = XMLInputFactory.newInstance();
	try {
	    reader = factory.createXMLStreamReader(new FileReader(pathReader));
	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	} catch (XMLStreamException e1) {
	    e1.printStackTrace();
	}
    }

    public static XMLStreamReader getByTagName(String name) {
	try {
	    while (reader.hasNext()) {
		if (reader.hasName() && reader.isStartElement()) {
		    // String prefix = reader.getPrefix();
		    String localName = reader.getLocalName();
		    if (localName.equalsIgnoreCase(name)) {
			return reader;
		    }
		    System.out.println();
		}
		reader.next();
	    }
	} catch (XMLStreamException e) {
	    e.printStackTrace();
	}
	return null;
    }

    public static String getAttibute(XMLStreamReader tag, String attribute) {
	for (int i = 0; i < tag.getAttributeCount(); i++) {
	    // String prefix = tag.getAttributePrefix(i);
	    String localName = tag.getAttributeLocalName(i);
	    String value = tag.getAttributeValue(i);
	    if (attribute.equalsIgnoreCase(localName)) {
		return value;
	    }

	}
	return null;
    }

    public static List<String> getAttibutes(XMLStreamReader tag, String... attributes) {
	String[] result = new String[attributes.length];
	for (int i = 0; i < attributes.length; i++) {
	    String attibute = getAttibute(tag, attributes[i]);
	    if (attibute != null) {
		result[i] = attibute;
	    }
	}
	return Arrays.asList(result);
    }



    public static void main(String[] args) {
	// String pathReader = "output/resources/AndroidManifest.xml";
	// XmlHelper helper = new XmlHelper(pathReader);
	// helper.getAppInfo();
	// getGUIComponents("/Users/charlyb07/Documents/workspace/semeru/APK-analyzer/output/info.staticfree.android.units/res/res/layout/numpad_2.xml");
	String a = "out/string.apk";
	int i = a.lastIndexOf(File.separator);
	System.out.println(a.substring(i + 1, a.length()));
    }

    public static XmlComponent getGUIComponents(String path) {
	SAXBuilder builder = new SAXBuilder();
	XmlComponent processNode = null;
	try {

	    Document document = (Document) builder.build(new File(path));
	    Element rootNode = document.getRootElement();

	    processNode = processNode(rootNode);
	    // Set name of file
	    int index = path.lastIndexOf(File.separator);
	    processNode.setFile(path.substring(index + 1, path.length()));
	} catch (IOException io) {
	    System.out.println(io.getMessage());
	} catch (JDOMException jdomex) {
	    System.out.println(jdomex.getMessage());
	}
	return processNode;
    }

    private static XmlComponent processNode(Element rootNode) {
	XmlComponent node = getNode(rootNode);
	processNode(rootNode, node);
	return node;
    }

    /**
     * @param children
     */
    private static void processNode(Element rootNode, XmlComponent parent) {
	List<Element> children = rootNode.getChildren();
	for (Element child : children) {
	    XmlComponent xmlChild = getNode(child);
	    parent.addXmlComponents(xmlChild);
	    processNode(child, xmlChild);
	}
	// return node;
    }

    /**
     * @param element
     */
    private static XmlComponent getNode(Element element) {
	XmlComponent gui = new XmlComponent();
	gui.setName(element.getName());
	// Get attributes
	String attributes = getAttributes(element);
	gui.setAttributes(attributes);
	// Get id xml
	boolean id1 = attributes.contains("@id");
	boolean id2 = attributes.contains("@*android:id");
	boolean clazz = attributes.contains("class");
	if (id1 || id2) {
	    int i;
	    if (id1) {
		i = attributes.indexOf("@id") + 4;
	    } else {
		i = attributes.indexOf("@*android:id") + 13;
	    }
	    int i2 = attributes.substring(i).indexOf("|");
	    gui.setId(attributes.substring(i, i + i2));
	}
	if (clazz){
	    
	}
	
	return gui;
    }

    /**
     * @param element
     */
    private static String getAttributes(Element element) {
	List<Attribute> attributes = element.getAttributes();
	String result = "";
	for (Attribute attribute : attributes) {
	    String prefix = attribute.getNamespacePrefix().length() == 0 ? "" : attribute.getNamespacePrefix() + ":";
	    result += prefix + attribute.getName() + "=" + attribute.getValue() + "|";
	}
	// System.out.println(result);
	return result;
    }
}

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
 * XmlComponent.java
 * 
 * Created on Jul 6, 2014, 6:28:20 PM
 * 
 */
package edu.wm.cs.semeru.core.model.no_db;

import java.util.ArrayList;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Carlos Bernal
 * @since Jul 6, 2014
 */
public class XmlComponent {

    private String name;
    private String id;
    private String attributes;
    private String file;
    private List<XmlComponent> xmlComponents = new ArrayList<XmlComponent>();

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * @return the id
     */
    public String getId() {
	return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
	this.id = id;
    }

    /**
     * @return the attributes
     */
    public String getAttributes() {
	return attributes;
    }

    /**
     * @param attributes
     *            the attributes to set
     */
    public void setAttributes(String attributes) {
	this.attributes = attributes;
    }

    /**
     * @return the xmlComponents
     */
    public List<XmlComponent> getComponents() {
	return xmlComponents;
    }

    /**
     * @param xmlComponents
     *            the xmlComponents to set
     */
    public void setComponents(List<XmlComponent> xmlComponents) {
	this.xmlComponents = xmlComponents;
    }

    /**
     * @return the xmlComponents
     */
    public List<XmlComponent> getXmlComponents() {
	return xmlComponents;
    }

    /**
     * @param xmlComponents
     *            the xmlComponents to set
     */
    public void addXmlComponents(XmlComponent xmlComponent) {
	this.xmlComponents.add(xmlComponent);
    }

    /**
     * @return the file
     */
    public String getFile() {
	return file;
    }

    /**
     * @param file
     *            the file to set
     */
    public void setFile(String file) {
	this.file = file;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "XmlComponent [name=" + name + ", id=" + id + ", attributes=" + attributes + ", file=" + file
		+ ", xmlComponents=" + xmlComponents + "]";
    }

}

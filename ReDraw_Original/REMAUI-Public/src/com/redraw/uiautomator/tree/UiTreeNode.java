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
package com.redraw.uiautomator.tree;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.redraw.uiautomator.tree.BasicTreeNode;
import com.redraw.uiautomator.tree.AttributePair;

import edu.wm.cs.semeru.redraw.Position;
import edu.wm.cs.semeru.redraw.Size;

/**
 * This class represents a node in an UI tree.
 * @author KevinMoran, Carlos
 * modified by Boyang, Sprague
 */
public class UiTreeNode extends BasicTreeNode {
	private static final Pattern BOUNDS_PATTERN = Pattern
			.compile("\\[-?(\\d+),-?(\\d+)\\]\\[-?(\\d+),-?(\\d+)\\]");
	// use LinkedHashMap to preserve the order of the attributes
	private final Map<String, String> mAttributes = new LinkedHashMap<String, String>();
	private String mDisplayName = "ShouldNotSeeMe";
	private Object[] mCachedAttributesArray;

	private String id;
	private Position pos;
	private Size size;
	private String name, type;
	private String elementFile;  //file name for the element
	private Element element;   // xml element in the res folder

	public UiTreeNode(){
	}

	public UiTreeNode(int x, int y, int width, int height){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public UiTreeNode(RootUINode convert)
	{
		this.x= convert.x;
		this.y=convert.y;
		this.width = convert.width;
		this.height = convert.height;
		mChildren = convert.mChildren;
//		mAttributes = convert.getAttributesArray();
	}
	
	public UiTreeNode( List<UiTreeNode> dchildren)
	{

		
		for(UiTreeNode child: dchildren)
		{
			 addChild(child);
		}
		
		FixTightBounds();
		type = "RelativeLayout";

		addAtrribute("bounds",  "[" + x + "," +  y + "][" + ( width + x)
				+ "," + (height + y) + "]");
		addAtrribute("class","RelativeLayout");
		addAtrribute("text","");
		
	}

	public String getElementFile(){
		return elementFile;
	}

	public void setElementFile(String file){
		this.elementFile = file;
	}

	public String getElementString(){
		if(element == null){
			return "";
		}else{
			return element.toString();
		}
	}

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
	}

	public String getId() {
		return id;
	}

	public Position getPos() {
		return pos;
	}

	public Size getSize() {
		return size;
	}

	public String getName() {
		return name;
	}



	/**
	 * @return the centric
	 */
	public Position getCentric() {
		return new Position(x+ width/2,  y +  height/2);
	}


	public void addAtrribute(String key, String value) {
		mAttributes.put(key, value);
		updateDisplayName();
		if ("bounds".equals(key)) {
			updateBounds(value);
		}
	}

	public Map<String, String> getAttributes() {
		return Collections.unmodifiableMap(mAttributes);
	}

	/**
	 * Builds the display name based on attributes of the node
	 */
	private void updateDisplayName() {
		String className = mAttributes.get("class");
		if (className == null)
			return;
		String text = mAttributes.get("text");
		if (text == null)
			return;
		String contentDescription = mAttributes.get("content-desc");
		if (contentDescription == null)
			return;
		String index = mAttributes.get("index");
		if (index == null)
			return;
		String bounds = mAttributes.get("bounds");
		if (bounds == null) {
			return;
		}
		// shorten the standard class names, otherwise it takes up too much space on UI
		//		className = className.replace("android.widget.", "");
		//		className = className.replace("android.view.", "");
		className = className.substring(className.lastIndexOf(".") + 1, className.length());
		type = className;
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		builder.append(index);
		builder.append(") ");
		builder.append(className);
		if (!text.isEmpty()) {
			builder.append(':');
			builder.append(text);
		}
		if (!contentDescription.isEmpty()) {
			builder.append(" {");
			builder.append(contentDescription);
			builder.append('}');
		}
		builder.append(' ');
		builder.append(bounds);
		mDisplayName = builder.toString();
		//this.pos = boundToStartPos(bounds);

		id = mAttributes.get("resource-id");
		if (id == null) {
			return;
		}

		name = mAttributes.get("text");
		if (id == null) {
			return;
		}

	}


	private void updateBounds(String bounds) {
		Matcher m = BOUNDS_PATTERN.matcher(bounds);
		if (m.matches()) {
			x = Integer.parseInt(m.group(1));
			y = Integer.parseInt(m.group(2));
			width = Integer.parseInt(m.group(3)) - x;
			height = Integer.parseInt(m.group(4)) - y;
			this.pos = new Position(x, y);
			this.size = new Size(width, height);
			mHasBounds = true;
		} else {
			throw new RuntimeException("Invalid bounds: " + bounds);
		}
	}


	public void FixBounds()
	{
		for(int i = 0; i<mChildren.size();i++)
		{
			if (mChildren.get(i).getX() < x)
			{
				x = mChildren.get(i).getX();
			}
			if (mChildren.get(i).getY() < y)
			{
				y = mChildren.get(i).getY();
			}
			if (mChildren.get(i).getHeight() > height)
			{
				height = mChildren.get(i).getHeight();
			}
			if (mChildren.get(i).getWidth() > width)
			{
				width = mChildren.get(i).getWidth();
			}
		}
	}
	
	public void FixTightBounds()
	{
		Rectangle box = findTightBounds();
		
		x = box.x;
		y = box.y;
		height = box.height;
		width = box.width;
		
	}
	
	private Rectangle findTightBounds() {
		// TODO Auto-generated method stub
		if (mChildren.size() <=0)
		{
			return new Rectangle(0,0,0,0);
		}
		int[] bounds = new int[4];
		bounds[0]= mChildren.get(0).getX();
		bounds[1]= mChildren.get(0).getY();
		bounds[2]= mChildren.get(0).getRight();
		bounds[3]= mChildren.get(0).getBottom();
		for(int i = 1; i<mChildren.size(); i++)
		{
			if(bounds[0]> mChildren.get(i).getX())
			{
				bounds[0] = mChildren.get(i).getX();
			}
			if(bounds[1]> mChildren.get(i).getY())
			{
				bounds[1] = mChildren.get(i).getY();
			}
			if(bounds[2]< mChildren.get(i).getRight())
			{
				bounds[2] = mChildren.get(i).getRight();
			}
			if(bounds[3]< mChildren.get(i).getBottom())
			{
				bounds[3] = mChildren.get(i).getBottom();
			}
			
		}
		
		return new Rectangle(bounds[0],bounds[1],bounds[2]-bounds[0],bounds[3]-bounds[1]);
	}
	
	
	
	public String getAttribute(String key) {
		return mAttributes.get(key);
	}

	@Override
	public Object[] getAttributesArray() {
		// this approach means we do not handle the situation where an attribute is added
		// after this function is first called. This is currently not a concern because the
		// tree is supposed to be readonly
		if (mCachedAttributesArray == null) {
			mCachedAttributesArray = new Object[mAttributes.size()];
			int i = 0;
			for (String attr : mAttributes.keySet()) {
				mCachedAttributesArray[i++] = new AttributePair(attr, mAttributes.get(attr));
			}
		}
		return mCachedAttributesArray;
	}

	public String printOutGUICheckerFormat(){
		return "[id, " + id + "]"
				+ "[start position, " + pos + "]"
				+ "[rectangle, " + size + "]"
				+ "[name, " + name + "] ";
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


	public void setName(String name) {
		this.name = name;
	}


	@Override
	public String toString() {
		//return mDisplayName;
		return "(" + x + "," + y + "," + width + "," + height + "); " +  name +"   type: " +  type;
	}
}

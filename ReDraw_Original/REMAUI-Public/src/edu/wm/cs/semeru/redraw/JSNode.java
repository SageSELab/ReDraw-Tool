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
 *******************************************************************************/



package edu.wm.cs.semeru.redraw;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.redraw.uiautomator.tree.AndroidUtilities;
import com.redraw.uiautomator.tree.BasicTreeNode;
import com.redraw.uiautomator.tree.RootDesignNode;

//import edu.semeru.android.guichecker.AndroidGUIChecker;
import edu.wm.cs.semeru.redraw.ConstantSettings;
import edu.wm.cs.semeru.redraw.helpers.ImagesHelper;

/**
 * This class represents one node in a DS file
 * @author Boyang Li
 * Created on Aug 28, 2016
 */
public class JSNode extends BasicTreeNode{

	private String orignalString = "";  //store the original string in the JS file
	private String id; //same as src
	private String name = "", html = "";
	private String type, sharedStyleType, sharedStyle, fontFamily, fontSize, color,  textAlign;   //We could parse style as well if it's needed


	private int z;


	private int styleWidth, styleHeight;



	//private Position centric;  //The center of the node


	public JSNode(int x, int y, int width, int height){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public JSNode(int x, int y, int width, int height, String id){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.id = id;
	}

	/**
	 * @param str
	 */
	public JSNode(String str){
		//"id":"8D3BCE4C-45AB-4AEB-BC4C-698390CB5B86","src":"8D3BCE4C-45AB-4AEB-BC4C-698390CB5B86","name":"","type":"MSTextLayer",
		//"x":828,"y":834,"zIndex":244,"width":209,"height":56,"sharedStyleType":"TEXTSHAREDSTYLE","sharedStyle":"@Property / Text",
		//"html":"text-color%3A%20%23000000%20100%25%0Aopacity%3A%20100%25%0Afont-size%3A%2011sp%0Afont-face%3A%20FZLTZHUNHK--GBK1-0",
		//"style":{"font-family":"STHeitiSC-Light","font-size":"14px","color":"#ffffff"}
		orignalString = str;
		String[] attributes = str.split(",\"");

		for(String attr: attributes){
			String[] ret = parseAttribute(attr);
			if(ret[0].equals("\"id")){
				this.id = ret[1].substring(1, ret[1].length()-1);
			}
			if(ret[0].equals("name")){
				//this.name = ret[1].substring(1, ret[1].length()-1);
				String tempString = ret[1].substring(1, ret[1].length()-1);
				try {
					this.name  = URLDecoder.decode(tempString, "utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(ret[0].equals("type")){
				this.type = ret[1].substring(1, ret[1].length()-1);
			}
			if(ret[0].equals("sharedStyleType")){
				this.sharedStyleType = ret[1].substring(1, ret[1].length()-1);
			}
			if(ret[0].equals("sharedStyle")){
				this.sharedStyle = ret[1].substring(1, ret[1].length()-1);
			}
			if(ret[0].equals("html")){
				//this.html = ret[1].substring(1, ret[1].length()-1);
				String tempString = ret[1].substring(1, ret[1].length()-1);
				try {
					this.html  = URLDecoder.decode(tempString, "utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(ret[0].contains("font-family")){
				this.fontFamily = ret[1].substring(1, ret[1].length()-1);
			}
			if(ret[0].equals("font-size")){
				this.fontSize = ret[1].substring(1, ret[1].length()-1);
			}
			if(ret[0].equals("color")){
				this.color = ret[1].substring(1, ret[1].length()-2);
			}

			if(ret[0].equals("text-align")){
				this.textAlign = ret[1].substring(1, ret[1].length()-2);
			}
			if(ret[0].equals("x")){
				this.x = Integer.parseInt(ret[1]);
			}
			if(ret[0].equals("y")){
				this.y =  Integer.parseInt(ret[1]);
			}
			if(ret[0].equals("zIndex")){
				this.z =  Integer.parseInt(ret[1]);
			}
			if(ret[0].equals("width")){
				if(ret[1].contains("px")){
					this.styleWidth =  Integer.parseInt(ret[1].replaceAll("px", "").replace("\"", "").replaceAll("\\}",""));
				}else{
					this.width =  Integer.parseInt(ret[1]);
				}
			}
			if(ret[0].equals("height")){
				if(ret[1].contains("px")){
					this.styleHeight =   Integer.parseInt(ret[1].replaceAll("px", "").replace("\"", "").replaceAll("\\}",""));
				}else{
					this.height =  Integer.parseInt(ret[1]);
				}
			}
		}

		//Screen crop
//
//		try {
//			//Make sure the folder is exist
//			String designImgFolder = AndroidGUIChecker.outputFolder + "design";
//			File dir = new File(designImgFolder);
//			if(!dir.exists()){
//				dir.mkdir();
//			}
//			ImagesHelper.cropImageAndSave(AndroidGUIChecker.mockUpImageJpg, dir + File.separator + this.hashCode() + ".jpg",
//					this.x, this.y, this.width, this.height, "jpg");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		//compute centric
		//this.centric = new Position(x+ width/2,  y +  height/2);
	}


	/**
	 * @return the centric
	 */
	public Position getPxCentric() {
		return  new Position(getPxX()+ getPxWidth()/2,  getPxY() +  getPxHeight()/2);
	}

	/**
	 * @return the styleWidth
	 */
	public int getStyleWidth() {
		return styleWidth;
	}

	/**
	 * @param styleWidth the styleWidth to set
	 */
	public void setStyleWidth(int styleWidth) {
		this.styleWidth = styleWidth;
	}

	/**
	 * @return the styleHeight
	 */
	public int getStyleHeight() {
		return styleHeight;
	}

	/**
	 * @param styleHeight the styleHeight to set
	 */
	public void setStyleHeight(int styleHeight) {
		this.styleHeight = styleHeight;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		if(id.equals("000")){
			return "ScreenNode000";
		}else{
			return id;
		}
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the sharedStyleType
	 */
	public String getSharedStyleType() {
		return sharedStyleType;
	}

	/**
	 * @param sharedStyleType the sharedStyleType to set
	 */
	public void setSharedStyleType(String sharedStyleType) {
		this.sharedStyleType = sharedStyleType;
	}

	/**
	 * @return the sharedStyle
	 */
	public String getSharedStyle() {
		return sharedStyle;
	}

	/**
	 * @param sharedStyle the sharedStyle to set
	 */
	public void setSharedStyle(String sharedStyle) {
		this.sharedStyle = sharedStyle;
	}

	/**
	 * @return the html
	 */
	public String getHtml() {
		return html;
	}

	/**
	 * @param html the html to set
	 */
	public void setHtml(String html) {
		this.html = html;
	}

	/**
	 * @return the fontFamily
	 */
	public String getFontFamily() {
		return fontFamily;
	}

	/**
	 * @param fontFamily the fontFamily to set
	 */
	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}

	/**
	 * @return the fontSize
	 */
	public String getFontSize() {
		return fontSize;
	}

	/**
	 * @param fontSize the fontSize to set
	 */
	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}

	/**
	 * @return the color
	 */
	public String getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(String color) {
		this.color = color;
	}

	/**
	 * @return the z
	 */
	public int getZ() {
		return z;
	}

	/**
	 * @param z the z to set
	 */
	public void setZ(int z) {
		this.z = z;
	}


	/**
	 * @return the x in pixel
	 * Now, we assume no converting is needed.
	 */
	public int getPxX(){
		return x;
		//ConstantSettings settings = ConstantSettings.getInstance();
		//return AndroidUtilities.convertDpCoordinates(settings.getTargetDeviceDensity(), getDpX());
	}

	/**
	 * @return the y in pixel
	 */
	public int getPxY(){
		return y;
		//ConstantSettings settings = ConstantSettings.getInstance();
		//return AndroidUtilities.convertDpCoordinates(settings.getTargetDeviceDensity(), getDpY());
	}

	/**
	 * @return the width in pixel
	 */
	public int getPxWidth(){
		return width;
		//ConstantSettings settings = ConstantSettings.getInstance();
		//return AndroidUtilities.convertDpCoordinates(settings.getTargetDeviceDensity(), getDpWidth());
	}

	/**
	 * @return the height in pixel
	 */
	public int getPxHeight(){
		return height;
		//ConstantSettings settings = ConstantSettings.getInstance();
		//return AndroidUtilities.convertDpCoordinates(settings.getTargetDeviceDensity(), getDpHeight());
	}



//	/**
//	 * @return the x in dp
//	 */
//	public float getDpX() {
//		ConstantSettings settings = ConstantSettings.getInstance();
//		return (float)x/settings.getDpRatio();
//	}


//	/**
//	 * @return the y in dp
//	 */
//	public float getDpY() {
//		ConstantSettings settings = ConstantSettings.getInstance();
//		return (float)y/settings.getDpRatio();
//	}


//	/**
//	 * @return the width in dp
//	 */
//	public float getDpWidth() {
//		ConstantSettings settings = ConstantSettings.getInstance();
//		return (float)width/settings.getDpRatio();
//	}



//	/**
//	 * @return the height in dp
//	 */
//	public float getDpHeight() {
//		ConstantSettings settings = ConstantSettings.getInstance();
//		return (float)height/settings.getDpRatio();
//	}



	/**
	 * @return the textAlign
	 */
	public String getTextAlign() {
		return textAlign;
	}

	/**
	 * @param textAlign the textAlign to set
	 */
	public void setTextAlign(String textAlign) {
		this.textAlign = textAlign;
	}


	private String[] parseAttribute(String str){
		String [] ret = new String[2];
		int pos = str.lastIndexOf("\":");
		//String [] temp = str.split("\":");
		ret[0] = str.substring(0, pos);
		ret[1] = str.substring(pos+2, str.length());
		return ret;

	}

	/**
	 * @return the content
	 */
	public String getContent() {
		//return name;
		//name: 娑�锟斤拷锟斤拷锟界��锟� copy   html:锟藉��锟芥��锟斤�锟斤拷
		if(!html.equals("")){
			return html;
		}else{
			return name;
		}
	}


	/**
	 * Check if the current Node contains comparedNode
	 * @param comparedNode
	 * @return
	 */
	public boolean contains(JSNode comparedNode) {
		return (comparedNode.x >= this.x) && (comparedNode.y >= this.y) &&
				(comparedNode.x + comparedNode.width <= this.x + this.width)
				&& (comparedNode.y + comparedNode.height <= this.y + this.height);
	}
	
	public boolean containsWithThreshold(JSNode comparedNode, double t) {
		return (comparedNode.x >= this.x - this.width * t) && (comparedNode.y >= this.y - this.height *t) &&
				(comparedNode.x + comparedNode.width <= this.x + this.width * (1+t))
				&& (comparedNode.y + comparedNode.height <= this.y + this.height * (1+t));
	}

	public boolean equals(JSNode comparedNode){
		return (comparedNode.x == this.x) && (comparedNode.y == this.y) &&
				(comparedNode.width == this.width)
				&& (comparedNode.height == this.height);
	}


	/**
	 * @return the orignalString
	 */
	public String getOrignalString() {
		return orignalString;
	}

	public String toString(){
		return "(" + x + "," + y + "," + width + "," + height + "); " +  name;
	}


}

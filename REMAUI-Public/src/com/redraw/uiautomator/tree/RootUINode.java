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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.jsoup.nodes.Element;

import com.redraw.uiautomator.tree.AttributePair;
import com.redraw.uiautomator.tree.BasicTreeNode;

//import edu.semeru.android.guichecker.AndroidGUIChecker;
import edu.wm.cs.semeru.redraw.ConstantSettings;
import edu.wm.cs.semeru.redraw.ElementDocfile;
import edu.wm.cs.semeru.redraw.JSNode;
import edu.wm.cs.semeru.redraw.helpers.ImagesHelper;


/**
 * This class represents a root node in a UI tree
 * @author KevinMoran, Carlos,
 * modified by Boyang, Sprague
 */
public class RootUINode extends BasicTreeNode {//TODO In the future see what happens if this inherits from UiTreeNode instead. It may make things simpler.

	private final String mWindowName;
	private final String mRotation;
	private Object[] mCachedAttributesArray;
	private PrintWriter writer;
	private ArrayList<UiTreeNode> leafNodes = new  ArrayList<UiTreeNode> ();
	public String type= "";

	public RootUINode() {
		mWindowName = "";
		mRotation = "";
	}


	public RootUINode(String windowName, String rotation,String intype) {
		mWindowName = windowName;
		mRotation = rotation;
		type= intype;
	}
	public RootUINode(String windowName, String rotation) {
		mWindowName = windowName;
		mRotation = rotation;
	}

	public RootUINode(String windowName, String rotation ,int x, int y, int width, int height) {
		super(x,y,width,height);
		mWindowName = windowName;
		mRotation = rotation;
	}
	public RootUINode(String windowName, String rotation ,int x, int y, int width, int height,String inType) {
		super(x,y,width,height);
		mWindowName = windowName;
		mRotation = rotation;
		type = inType;
	}
	
	public RootUINode(UiTreeNode node)
	{	
		super(0,0,1200,1920);
		//mCachedAttributesArray=node.getAttributesArray();

		mWindowName="";
		mRotation ="";
		type = "RelativeLayout";
		
		mCachedAttributesArray = node.getAttributesArray();
		
		addChild(node);
		
		//List<BasicTreeNode> childList = node.getChildrenList();
	//	node.clearAllChildren();
		//for(BasicTreeNode child:childList)
		//{
		//	addChild(child);
		//}
		
		
		
		
	}
	public RootUINode(BasicTreeNode node)
	{	
		super(0,0,1200,1920);
		//super(node.getX(),node.getY(),node.getWidth(),node.getHeight());

		mCachedAttributesArray = node.getAttributesArray();
		mWindowName="";
		mRotation ="";
		addChild(node);
		
		
		
		
		
	}

	/**
	 * @return the rootDesign
	 */
	public RootDesignNode getRootDesign() {
		return (RootDesignNode)correspondingNode;
	}


	/**
	 * @param rootDesign the rootDesign to set
	 */
	public void setRootDesign(RootDesignNode rootDesign) {
		this.correspondingNode = rootDesign;
	}


	/**
	 * get LeafNodes
	 */
	public ArrayList<UiTreeNode> getLeafNodes(){
		leafNodes.clear();
		for(BasicTreeNode node : this.mChildren){
			getLeafNodesHelper(node);
		}
		return leafNodes;
	}
	
	


	/**
	 * A helper function to get leaf nodes
	 * @param node
	 */
	private void getLeafNodesHelper(BasicTreeNode node){
		if(node.mChildren.size() == 0 && node.merged != true){
			leafNodes.add((UiTreeNode)node);
		}
		for(BasicTreeNode child : node.mChildren){
			getLeafNodesHelper(child);
		}
	}


	@Override
	public String toString() {
		return mWindowName + " - " + mRotation;
	}


	
	@Override
	public Object[] getAttributesArray() {
		if (mCachedAttributesArray == null) {
			mCachedAttributesArray = new Object[] { new AttributePair("window-name", mWindowName),
					new AttributePair("rotation", mRotation)};
		}
		return mCachedAttributesArray;
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
	

	/**
	 * Print out nodes to UIWindowPrintOut.txt
	 */
	public void iterateAllNodes(){
		try {
			writer = new PrintWriter("UIWindowPrintOut.txt", "UTF-8");
			for (BasicTreeNode child : this.getChildren()) {
				iterateSubNodes(child);
			}
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void iterateSubNodes(BasicTreeNode node){
		if(node instanceof UiTreeNode){
			UiTreeNode uiNode = (UiTreeNode) node;
			String str = uiNode.printOutGUICheckerFormat();
			writer.println(str);
			System.out.println(str);
		}
		for (BasicTreeNode child : node.getChildren()) {
			iterateSubNodes(child);
		}
	}


	/**
	 * Transform the tree based on heuristic rules
	 */
	public void transform(){
		//Delete node if it has same size as its parent node
		removeRedundantNode();
	}


	public void assignRes(HashMap<String, ElementDocfile> mapIDElem){
		ArrayList<UiTreeNode> leafNodes = this.getLeafNodes();
		for(UiTreeNode uiNode : leafNodes){
			int index = uiNode.getId().indexOf(":id/");
			if(index == -1){
				continue;
			}
			String nodeID = uiNode.getId().substring(index + 4);
			ElementDocfile elemDoc = mapIDElem.get("@id/" + nodeID);
			if(elemDoc != null){
				uiNode.setElement(elemDoc.getEle());
				uiNode.setElementFile(elemDoc.getDoc());
			}
		}
	}


	//	/**
	//	 * Find predefined root node that Implementation tree started with (without i.e. battery log)
	//	 * @return a BasicTreeNode as a new root
	//	 */
	//	private RootUINode findDefinedFrame(){
	//		Queue <BasicTreeNode> queue = new LinkedList <BasicTreeNode>();
	//		queue.add(this);
	//		ArrayList <BasicTreeNode> candidateFrames = new  ArrayList <BasicTreeNode> ();
	//		while(!queue.isEmpty()){
	//			BasicTreeNode node = queue.poll();
	//			if(node.x == ConstantSettings.IMPLEMENTATIONFRAME[0] && node.y == ConstantSettings.IMPLEMENTATIONFRAME[1]
	//					&& node.width == ConstantSettings.IMPLEMENTATIONFRAME[2] && node.height == ConstantSettings.IMPLEMENTATIONFRAME[3]){
	//				candidateFrames.add(node);
	//			}
	//			for(BasicTreeNode child: node.mChildren){
	//				queue.add(child);
	//			}
	//		}
	//
	//
	//		if(candidateFrames.size() > 0) {
	//			//Found the frame and return the last one.
	//			RootUINode newRoot = new RootUINode();
	//			BasicTreeNode frame =  candidateFrames.get(candidateFrames.size()-1);
	//			newRoot.x  = frame.x;
	//			newRoot.y = frame.y;
	//			newRoot.width = frame.width;
	//			newRoot.height = frame.height;
	//			newRoot.mChildren = frame.mChildren;
	//			for(BasicTreeNode child : newRoot.mChildren){
	//				child.setParent(newRoot);
	//			}
	//			return newRoot;
	//		}else{
	//			// validation checking
	//			try {
	//				throw new Exception();
	//			} catch (Exception e) {
	//				e.printStackTrace();
	//				System.out.println("Error: did not find the predefined frame in implementation ui file. Please check ConstantSettings.IMPLEMENTATIONFRAME");
	//				System.exit(0);
	//			}
	//			return null;
	//		}
	//	}


	private ArrayList<BasicTreeNode> redundantNodes = new ArrayList<BasicTreeNode> ();

	/**
	 * Delete node if it has same size as its only child
	 */
	private void removeRedundantNode(){
		for(BasicTreeNode child : this.mChildren){
			detectRedundantNodeHelper(child);
		}


		//remove nodes
		for(BasicTreeNode node : redundantNodes){
			BasicTreeNode parent = node.getParent();
			int index = parent.mChildren.indexOf(node);
			parent.mChildren.remove(node);
			if(index == -1){
				continue;
			}
			parent.mChildren.addAll(index, node.mChildren);
			for(BasicTreeNode child : node.mChildren){
				child.mParent = parent;
			}
		}
	}


	/**
	 * Helper - Delete node if it has same size as its only child
	 */
	private void detectRedundantNodeHelper(BasicTreeNode node){

		if(node.mChildren.size() == 1){
			//BasicTreeNode child = node.mChildren.get(0);
			//1. if same size, definitely delete
			//if(node.x == child.x && node.y == child.y
			//		&& node.width == child.width && node.height == child.height){
			//	//remove node
			//	redundantNodes.add(node);
			//}
			//2. if the only child and child is only has one child

			redundantNodes.add(node);
		}
		//start commented 12/30/16-Boyang
		//		UiTreeNode tn = (UiTreeNode) node;
		//		if(node.mChildren.size() == 0 && tn.getType().equals("View")){
		//			UiTreeNode parent = (UiTreeNode)node.mParent;
		//			if(node.x == parent.x && node.y == parent.y
		//					&& node.width == parent.width && node.height == parent.height){
		//				//remove node
		//				redundantNodes.add(node);
		//			}
		//			//redundantNodes.add(node);
		//		}
		//end commented 12/30/16-Boyang

		if(node.x == 0 && node.y == 0
				&& node.width == 0 && node.height == 0){
			redundantNodes.add(node);
		}

		int[][] ignoredNodes = ConstantSettings.getInstance().getIgnoredCompDesign();

		if(ignoredNodes != null){
			for(int[] iNode : ignoredNodes){
				BasicTreeNode tempNode = new BasicTreeNode(iNode[0], iNode[1], iNode[2]-iNode[0],iNode[3]-iNode[1]);
				if(tempNode.contains(node) || tempNode.equals(node)||tempNode.containsWithThreshold(node,0.2)){
				//if(node.x == iNode[0] && node.y == iNode[1]
				//		&& node.width == iNode[2] && (node.height >= iNode[3] - 4 && node.height <= iNode[3] + 4)){
					redundantNodes.add(node);
				}
			}
		}

		/*Combining Leaf Text Components with a Shared Container Component
		 *if there are two Text Components (e.g. TextViews), that share a
		 *single parent container component, then the text components should
		 *be combined into a single component*/
		//		if(node.mChildren.size() >= 2){
		//			boolean allViewAndChecked = true;
		//			String textInfo = "";
		//			int newWidth = 0;
		//			int horizontal = node.mChildren.get(0).y;
		//			for(BasicTreeNode child : node.mChildren){
		//				UiTreeNode tnChild = (UiTreeNode) child;
		//				if(!tnChild.getType().contains("View")){
		//					allViewAndChecked = false;
		//					break;
		//				}
		//				if(tnChild.y != horizontal){
		//					allViewAndChecked = false;
		//					break;
		//				}
		//				textInfo += tnChild.getName();
		//				newWidth += tnChild.getWidth();
		//			}
		//			//Passed all checks
		//			if(allViewAndChecked == true){
		//				redundantNodes.add(node);
		//				UiTreeNode newNode = (UiTreeNode)node.mChildren.get(0);
		//				newNode.setName(textInfo);
		//				newNode.width = newWidth;
		//				node.mChildren.clear();
		//				node.mChildren.add(newNode);
		//			}
		//		}

		for(BasicTreeNode child : node.mChildren){
			detectRedundantNodeHelper(child);
		}
	}


	/**
	 * crop all nodes in the implementation from the input file
	public void cropScreen(){
		//Make sure the folder is exist
		String designImgFolder = AndroidGUIChecker.outputFolder + "implement";
		File dir = new File(designImgFolder);
		if(!dir.exists()){
			dir.mkdir();
		}
		
		String bwPID = AndroidGUIChecker.outputFolder + "pidbw";
		dir = new File(bwPID);
		if(!dir.exists()){
			dir.mkdir();
		}
		for(BasicTreeNode child : this.mChildren){
			cropScreenHelper(child);
		}
	}

	 */
//TODO see if crop methods are necessary
	/**
	 * Helper -  crop all nodes in the implementation from the input file
	private void cropScreenHelper(BasicTreeNode node){
		//Crop
		try {
			String designImgFolder = AndroidGUIChecker.outputFolder + "implement";
			File dir = new File(designImgFolder);
			//ImagesHelper.cropImageAndSave(AndroidGUIChecker.appScreenShot, dir + File.separator + node.hashCode() + ".png",
			//		node.x, node.y, node.width, node.height);
			ImagesHelper.cropImageAndSave(AndroidGUIChecker.appScreenShotJpg, dir + File.separator + node.hashCode() + ".jpg",
					node.x, node.y, node.width, node.height, "jpg");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(BasicTreeNode child : node.mChildren){
			cropScreenHelper(child);
		}
	}
	 */
}

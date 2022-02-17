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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.redraw.uiautomator.tree.Edge;

//import edu.semeru.android.guichecker.InvalidOverlap;
import edu.wm.cs.semeru.redraw.JSNode;
import edu.wm.cs.semeru.redraw.REMAUI;
//import edu.semeru.android.guichecker.ui.UiTreeNode;
import edu.wm.cs.semeru.redraw.helpers.ImagesHelper;

/**
 * This class represents a Basic node in the tree
 * @author KevinMoran, Carlos, Boyang
 * 
 */
public class BasicTreeNode {

	//The corresponding node   UI node -> Design or Design node -> UI.
	public BasicTreeNode correspondingNode;

	//true if the matching is complete (grand)children are matching as well.
	public boolean completeMatching = false;

	protected BasicTreeNode mParent;
	protected ArrayList<BasicTreeNode> mChildren = new ArrayList<BasicTreeNode>();
	protected int x, y, width, height;


	protected boolean merged = false;  //true if the current node is merged into a super node

	// whether the boundary fields are applicable for the node or not
	// RootWindowNode has no bounds, but UiNodes should
	protected boolean mHasBounds = false;

	private float PIDDiffRatio;   //PID difference in percentage

	public BasicTreeNode(){
	}


	public BasicTreeNode(int x, int y, int width, int height){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void addChild(BasicTreeNode child) {
		if (child == null) {
			throw new NullPointerException("Cannot add null child");
		}
		if (mChildren.contains(child)) {
			throw new IllegalArgumentException("node already a child");
		}
		mChildren.add(child);
		child.mParent = this;
	}


	/**
	 * Gets an ancestor in the path (level above), returns null if nothing find.
	 * @param level
	 * @return
	 */
	public BasicTreeNode getAncestor(int level){
		BasicTreeNode currentNode = this;
		for(int i = 0; i < level; i++){
			if(currentNode == null){
				return null;
			}
			currentNode = currentNode.mParent;
		}
		return currentNode;
	}


	public boolean hasCorrespondingMatch(){
		if(this.correspondingNode == null){
			return false;
		}else{
			return true;
		}
	}

	public List<BasicTreeNode> getChildrenList() {
		return Collections.unmodifiableList(mChildren);
	}

	public ArrayList<BasicTreeNode> getChildren() {
		return this.mChildren;
	}

	public void setChildren(ArrayList<BasicTreeNode> children) {
		this.mChildren = children;
	}

	public void setParent(BasicTreeNode parent) {
		this.mParent = parent;
	}

	public BasicTreeNode getParent() {
		return mParent;
	}

	public boolean hasChild() {
		return mChildren.size() != 0;
	}

	public int getChildCount() {
		return mChildren.size();
	}
	
	public List<BasicTreeNode> getAllChildrenList(){
		List<BasicTreeNode> allChildren = new ArrayList<BasicTreeNode>();
		
		for(BasicTreeNode child: mChildren)
		{
			allChildren.add(child);
			allChildren.addAll(child.getAllChildrenList());
		}
		
		return allChildren;
	}

	public void clearAllChildren() {
		for (BasicTreeNode child : mChildren) {
			child.clearAllChildren();
		}
		mChildren.clear();
	}


	/**
	 * @return the correspondingNode
	 */
	public BasicTreeNode getCorrespondingNode() {
		return correspondingNode;
	}


	/**
	 *
	 * Find nodes in the tree containing the coordinate
	 *
	 * The found node should have bounds covering the coordinate, and none of
	 * its children's bounds covers it. Depending on the layout, some app may
	 * have multiple nodes matching it, the caller must provide a
	 * {@link IFindNodeListener} to receive all found nodes
	 *
	 * @param px
	 * @param py
	 * @return
	 */
	public boolean findLeafMostNodesAtPoint(int px, int py,
			IFindNodeListener listener) {
		boolean foundInChild = false;
		for (BasicTreeNode node : mChildren) {
			foundInChild |= node.findLeafMostNodesAtPoint(px, py, listener);
		}
		// checked all children, if at least one child covers the point, return
		// directly
		if (foundInChild)
			return true;
		// check self if the node has no children, or no child nodes covers the
		// point
		if (mHasBounds) {
			if (x <= px && px <= x + width && y <= py && py <= y + height) {
				listener.onFoundNode(this);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public Object[] getAttributesArray() {
		return null;
	};

	public static interface IFindNodeListener {
		void onFoundNode(BasicTreeNode node);
	}


	/**
	 * Check if the current Node contains comparedNode
	 * @param comparedNode
	 * @return
	 */
	public boolean contains(BasicTreeNode comparedNode) {
		return (comparedNode.x >= this.x) && (comparedNode.y >= this.y) &&
				(comparedNode.x + comparedNode.width <= this.x + this.width)// + ConstantSettings.ADJUSTPIXELINDESIGN)
				&& (comparedNode.y + comparedNode.height <= this.y + this.height);// + ConstantSettings.ADJUSTPIXELINDESIGN);
		/*
		 * intersects
		  return !((comparedNode.x + comparedNode.width <= this.x) ||
	                (comparedNode.y + comparedNode.height <= this.y) ||
	                (comparedNode.x >= this.x + this.width) ||
	                (comparedNode.y >= this.y + this.height));

		 */
	}


	/**
	 * Check if the current Node contains comparedNode with 0.2 threshold
	 * @param comparedNode
	 * @return
	 */
	public boolean containsWithThreshold(BasicTreeNode comparedNode) {
		return (comparedNode.x >= this.x - this.width * 0.2) && (comparedNode.y >= this.y - this.height * 0.2) &&
				(comparedNode.x + comparedNode.width <= this.x + this.width * 1.2)
				&& (comparedNode.y + comparedNode.height <= this.y + this.height * 1.2);
	}
	
	public boolean containsWithThreshold(BasicTreeNode comparedNode, double t) {
		return (comparedNode.x >= this.x - this.width * t) && (comparedNode.y >= this.y - this.height *t) &&
				(comparedNode.x + comparedNode.width <= this.x + this.width * (1+t))
				&& (comparedNode.y + comparedNode.height <= this.y + this.height * (1+t));
	}


	public boolean intersect(BasicTreeNode comparedNode){
		return !((comparedNode.x + comparedNode.width <= this.x) ||
				(comparedNode.y + comparedNode.height <= this.y) ||
				(comparedNode.x >= this.x + this.width) ||
				(comparedNode.y >= this.y + this.height));
	}

	public boolean equals(BasicTreeNode comparedNode){
		return (comparedNode.x == this.x) && (comparedNode.y == this.y) &&
				(comparedNode.width == this.width)
				&& (comparedNode.height == this.height);
	}


	/**
	 * Add node in to this object
	 * Node: this always contains node
	 * @param node
	 */
	public void addNode(BasicTreeNode node){
		BasicTreeNode appendToChild = null;
		List<BasicTreeNode> found = new ArrayList<BasicTreeNode>();

		for(BasicTreeNode child : mChildren){
			if(node.equals(child)){
				return;
			}else if(node.contains(child)){
				found.add(child);
				node.addNode(child);
			}else if(child.contains(node)){
				//Important debug messages for detecting overlap exceptions. Do not delete.
				//				System.out.println("=================");
				//				System.out.println(appendToChild);
				//				System.out.println(child);
				//				System.out.println("=================");
				if(appendToChild != null){
					try {
						throw new Exception();
					} catch (Exception e) {
						if(child instanceof JSNode && appendToChild instanceof JSNode){
						//TODO is this right or do I need to implement this?	AndroidGUIChecker.warnings.add(new InvalidOverlap((JSNode)child, (JSNode)appendToChild));
						}
//						e.printStackTrace();
//						System.out.println("Error: do not allow two children contain the added node +"
//								+ "(strictly nested relations)");
//						System.exit(0);
					}
				}
				appendToChild = child;
			}else if(child.intersect(node)){
				if(child instanceof JSNode && node instanceof JSNode){
					//TODO Does this need to be implemented? AndroidGUIChecker.warnings.add(new InvalidOverlap((JSNode)child, (JSNode)node));
					System.out.println("Node 1: " + child);
					System.out.println("Node 2: " + node);
					System.out.println("Error: do not allow intercection");
				}
			}
		}

		mChildren.removeAll(found);

		if(appendToChild == null){
			mChildren.add(node);
			node.mParent = this;
		}else{
			appendToChild.addNode(node);
			//node should not have children
		}
	}


	/**
	 * need 4 integers to indicate pos and size
	 */
	public void deleteChildByPosSize(int [] posSize){
		for(BasicTreeNode child : this.mChildren){
			if(child.x == posSize[0] && child.y == posSize[1] && child.width == posSize[2] && child.height == posSize[3]){
				mChildren.remove(child);
				return;
			}
		}

	}


	PrintWriter writer;

	/**
	 * Depth first
	 */
	public void printOutTree(){
		//try {
		System.out.print("|");
		//writer = new PrintWriter("DesignPrintOutTree-Marketch.txt", "UTF-8");
		//} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		//	e.printStackTrace();
		//} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		int level = -1;
		for(BasicTreeNode node : this.mChildren){
			printOutTreeHelper(level+1, node);
		}
		//writer.close();

	}





	public void printOutTreeHelper(int level, BasicTreeNode node){
		System.out.print("|level " + level+ "|");
		//writer.print("|");
		for(int i = 0; i < level ; i ++){
			System.out.print("--------");
			//writer.print("--------");
		}
		System.out.print("Start Pos:" + node.x + "," + node.y);
		//writer.print("Start Pos:" + node.x + "," + node.y);
		System.out.print("\trectangle:" + node.width + "," + node.height);
		//writer.print("\trectangle:" + node.width + "," + node.height);

		if(node instanceof JSNode){
			JSNode dn = (JSNode)node;
			System.out.print("\tID:" + dn.getId());
			//writer.print("\tID:" + dn.getId());
			System.out.print("\t name:" + dn.getName());
			//writer.print("\t name:" + dn.getName());
		}
		if(node instanceof UiTreeNode){
			UiTreeNode un = (UiTreeNode)node;
			System.out.print("\tID:" + un.getId());
			//writer.print("\tID:" + un.getId());
			System.out.print("\t name:" + un.getName());
			//writer.print("\t name:" + un.getName());
			System.out.print("\t type(class):" + un.getType());
			//writer.print("\t type(class):" + un.getType());
			System.out.println(un.getElementString());
		}
		System.out.print("\n");
		//writer.print("\n");
		for(BasicTreeNode child : node.getChildren()){
			printOutTreeHelper(level+1, child);
		}
	}


	/**
	 * Depth first
	 */
	public void printOutCorrespondingTree(){
		int level = -1;
		for(BasicTreeNode node : this.mChildren){
			printOutCorrespondingTreeHelper(level+1, node);
		}
	}

	public void printOutCorrespondingTreeHelper(int level, BasicTreeNode nodeOrg){
		BasicTreeNode node = nodeOrg.correspondingNode;
		if(node == null){
			return;
		}
		System.out.print("|level " + level+ "|");

		for(int i = 0; i < level ; i ++){
			System.out.print("--------");
		}
		System.out.print("Start Pos:" + node.x + "," + node.y);
		System.out.print("\trectangle:" + node.width + "," + node.height);


		if(node instanceof JSNode){
			JSNode dn = (JSNode)node;
			System.out.print("\tID:" + dn.getId());
			System.out.print("\t name:" + dn.getName());
		}
		if(node instanceof UiTreeNode){
			UiTreeNode un = (UiTreeNode)node;
			System.out.print("\tID:" + un.getId());
			System.out.print("\t name:" + un.getName());
			System.out.print("\t type(class):" + un.getType());
			JSNode dsNode = (JSNode)nodeOrg;
			System.out.print("\t org node name:" + dsNode.getContent() + "start: " + dsNode.x +","+ dsNode.y);
			System.out.print("\t complete:" + dsNode.completeMatching);
		}
		System.out.print("\n");
		for(BasicTreeNode child : nodeOrg.getChildren()){
			printOutCorrespondingTreeHelper(level+1, child);
		}
	}



	private HashSet<BasicTreeNode> bnSet = new HashSet<BasicTreeNode>();


	/**
	 * Return all sub-nodes under this root.
	 * @return
	 */
	public HashSet<BasicTreeNode> getAllNodes(){
		bnSet = new HashSet<BasicTreeNode>();
		for (BasicTreeNode child : this.getChildren()) {
			getAllNodesHelper(child);
		}
		return this.bnSet;
	}


	/**
	 * A helper function for returning all sub-nodes under
	 * @param node
	 */
	public void getAllNodesHelper(BasicTreeNode node){

		this.bnSet.add(node);
		for (BasicTreeNode child : node.getChildren()) {
			getAllNodesHelper(child);
		}
	}


	//	/**
	//	 * Return all  getAllLeafNodes nodes under this.
	//	 * @return
	//	 */
	//	public HashSet<BasicTreeNode> getAllLeafNodes(){
	//		bnSet = new HashSet<BasicTreeNode>();
	//		for (BasicTreeNode child : this.getChildren()) {
	//			getAllLeafNodesHelper(child);
	//		}
	//		return this.bnSet;
	//	}
	//
	//
	//	/**
	//	 * A helper function for returning all getAllLeafNodes under
	//	 * @param node
	//	 */
	//	public void getAllLeafNodesHelper(BasicTreeNode node){
	//		if(node.mChildren.size() == 0){
	//			this.bnSet.add(node);
	//		}
	//		for (BasicTreeNode child : node.getChildren()) {
	//			getAllLeafNodesHelper(child);
	//		}
	//	}


	/**
	 * Return all  getAllLeafNodes nodes under this.
	 * @return
	 */
	public HashSet<BasicTreeNode> getAllIncompletedNodes(){
		bnSet = new HashSet<BasicTreeNode>();
		for (BasicTreeNode child : this.getChildren()) {
			getAllIncompletedNodesHelper(child);
		}
		return this.bnSet;
	}


	/**
	 * A helper function for returning all getAllLeafNodes under
	 * @param node
	 */
	public void getAllIncompletedNodesHelper(BasicTreeNode node){
		if(node.completeMatching == false){
			this.bnSet.add(node);
		}else{
			for (BasicTreeNode child : node.getChildren()) {
				getAllIncompletedNodesHelper(child);
			}
		}
	}


	public String getCroppedImg(){
		if(this instanceof JSNode){
			
			String fileStr = "C://design" + File.separator + this.hashCode() + ".jpg";
//TODO Look into the outputFolder//			String fileStr = AndroidGUIChecker.outputFolder + "design" + File.separator + this.hashCode() + ".jpg";
			File file = new File(fileStr);
			if(file.exists()){
				return fileStr;
			}else{
				return "";
			}

		}

		if(this instanceof UiTreeNode){
			String fileStr = "C://implement" + File.separator + this.hashCode() + ".jpg";
			//TODO Look into the outputFolder//			String fileStr = AndroidGUIChecker.outputFolder + "implement" + File.separator + this.hashCode() + ".jpg";
			File file = new File(fileStr);
			if(file.exists()){
				return fileStr;
			}else{
				return "";
			}
		}

		return null;
	}


	public String[] getTop3Colors(){
		String imgLocation = this.getCroppedImg();
		String [] returnHex = new String[3];
		try {
			Color[] colors = ImagesHelper.quantizeImageAndGetColors(imgLocation, 3);
			int pos = 0;
			for(Color color : colors){
				if(color != null ) {
				String hex = "#"+Integer.toHexString(color.getRGB()).substring(2);
				returnHex[pos++] = hex;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnHex;
	}


	/**
	 * Get the top line (vertical value)
	 * @return
	 */
	public int getTop(){
		return y;
	}

	/**
	 * Get the left line (horizontal value)
	 * @return
	 */
	public int getLeft(){
		return x;
	}

	/**
	 * Get the right line (horizontal value)
	 * @return
	 */
	public int getRight(){
		return x + width;
	}

	/**
	 * Get the bottom line (vertical value)
	 * @return
	 */
	public int getBottom(){
		return y + height;
	}



	/**
	 * For ui node (most cases)
	 * @return
	 */
	public int getEdgePosition(Edge edge){
		switch(edge){
		case Top:
			return y;
		case Left:
			return x;
		case Bottom:
			return y + height;
		case Right:
			return x + width;
		default:
			return -1;
		}
	}



	/**
	 * Get the top line (vertical value)
	 * @return
	 */
	public int getX(){
		return x;
	}

	/**
	 * Get the left line (horizontal value)
	 * @return
	 */
	public int getY(){
		return y;
	}

	/**
	 * Get the right line (horizontal value)
	 * @return
	 */
	public int getWidth(){
		return width;
	}

	/**
	 * Get the bottom line (vertical value)
	 * @return
	 */
	public int getHeight(){
		return height;
	}

	public float getPIDDiffRatio() {
		return PIDDiffRatio;
	}

	public void setPIDDiffRatio(float pIDDiffRatio) {
		PIDDiffRatio = pIDDiffRatio;
	}


	public boolean isMerged() {
		return merged;
	}

	public void setMerged(boolean merged) {
		this.merged = merged;
	}

}

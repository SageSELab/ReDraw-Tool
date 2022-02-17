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

package com.redraw.uiautomator.tree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.redraw.uiautomator.tree.BasicTreeNode;

//import edu.semeru.android.guichecker.AndroidGUIChecker;
import edu.wm.cs.semeru.redraw.ConstantSettings;
import edu.wm.cs.semeru.redraw.Utils;
import edu.wm.cs.semeru.redraw.JSNode;
import edu.wm.cs.semeru.redraw.helpers.ImagesHelper;

/**
 * This class represents a root node in the design tree.
 * @author Boyang Li
 */
public class RootDesignNode extends BasicTreeNode{

	/**
	 * An helper element for preProcessSplitter function
	 */
	private ArrayList<JSNode> leafNodes = new ArrayList<JSNode>();


	/**
	 * An helper element for preProcessSplitter function
	 */
	private List<BasicTreeNode> detectedChildren = new ArrayList<BasicTreeNode>();


	/**
	 * @return the rootUI
	 */
	public RootUINode getRootUI() {
		return (RootUINode)correspondingNode;
	}


	/**
	 * @param rootUI the rootUI to set
	 */
	public void setRootUI(RootUINode rootUI) {
		this.correspondingNode = rootUI;
	}



	public RootDesignNode(){
		ConstantSettings settings = ConstantSettings.getInstance();
		int[] board = settings.getDSBoard();
		x = board[0];
		y = board[1];
		width = board[2];
		height = board[3];
	}



	/**
	 * Build bidirection between parent and children
	 */
	public void buildBidirection(){
		for(BasicTreeNode node : this.mChildren){
			node.mParent = this;
			buildBidirectiontHelper(node);
		}
	}


	private void buildBidirectiontHelper(BasicTreeNode node){
		for(BasicTreeNode child : node.mChildren){
			child.mParent = node;
			buildBidirectiontHelper(child);
		}
	}


	/**
	 * Get LeafNodes
	 */
	public ArrayList<JSNode> getLeafNodes(){
		leafNodes.clear();
		for(BasicTreeNode node : this.mChildren){
			getLeafNodesHelper(node);
		}
		return leafNodes;
	}


	private void getLeafNodesHelper(BasicTreeNode node){
		//ignore nodes if it's a split line
		if(node.getWidth() < 3 || node.getHeight() < 3){
			return;
		}

		//leaf and it's not merged
		if(node.mChildren.size() == 0 && node.merged != true){
			leafNodes.add((JSNode)node);
		}

		for(BasicTreeNode child : node.mChildren){
			getLeafNodesHelper(child);
		}
	}



	/**
	 * Remove special sub components for icons, images, etc
	 */
	public void deletReduandant(){
		ArrayList<JSNode> leafNodes = getLeafNodes();
		for(JSNode node : leafNodes){
			JSNode curNode = node;
			while(curNode != null){
				if(curNode.getName().startsWith("Oval") || curNode.getName().startsWith("Mask")
						|| curNode.getName().startsWith("icon_") || curNode.getName().startsWith("imare_")
						|| curNode.getName().startsWith("btn_") || curNode.getName().startsWith("search_")
						|| curNode.getName().toLowerCase().startsWith("layout_")
						){
					for(BasicTreeNode child : curNode.mChildren){
						child.setParent(null);
					}
					curNode.mChildren.clear();
					break;
				}else{
					BasicTreeNode pNode = curNode.getParent();
					if(pNode instanceof JSNode){
						curNode = (JSNode)pNode;
					}else{
						break;
					}
				}
			}
		}
	}

	/**
	 * preprocess splitter. move one level up if it's too close to an edge of a container
	 */
	public void preProcessSplitter(){
		for(BasicTreeNode node : this.mChildren){
			preProcessSplitterHelper(node);
		}

		//node.mChildren.removeAll(deleteChildren);
		//move one level up
		for(BasicTreeNode detectedNode: detectedChildren){
			BasicTreeNode parent =  detectedNode.mParent;
			BasicTreeNode grandparent = detectedNode.mParent.mParent;
			grandparent.mChildren.add(detectedNode);
			parent.mChildren.remove(detectedNode);
			detectedNode.mParent = grandparent;
		}
	}

	/**
	 * Helper function for removing redundant nodes
	 * @param node
	 */
	private void preProcessSplitterHelper(BasicTreeNode node){
		//1.
		for(BasicTreeNode child : node.getChildren()){
			JSNode dsNodeChild = (JSNode)child;
			if(dsNodeChild.getContent().contains("Line") || dsNodeChild.getContent().contains("Rectangle")){
				if(dsNodeChild.width < 3){
					//Vertical
					if(Utils.isConnected(dsNodeChild.getLeft(), node.getLeft()) || Utils.isConnected(dsNodeChild.getRight(), node.getRight())){
						//move one level up
						detectedChildren.add(dsNodeChild);
					}
				}else if(dsNodeChild.height < 3){
					//Horizontal
					if(Utils.isConnected(dsNodeChild.getTop(), node.getTop()) || Utils.isConnected(dsNodeChild.getBottom(), node.getBottom())){
						//move one level up
						detectedChildren.add(dsNodeChild);
					}
				}
			}

		}


		//2. recursive
		for(BasicTreeNode child : node.getChildren()){
			sortTreeHelper(child);
		}
	}



	/**
	 * Sort the tree based on components' positions
	 */
	public void sortTree(){
		for(BasicTreeNode node : this.mChildren){
			sortTreeHelper(node);
		}

		//Sort current node's children
		ArrayList<BasicTreeNode> sortedChildren = new ArrayList<BasicTreeNode>();
		for(BasicTreeNode child : this.getChildren()){
			int childY = child.y;
			int childX = child.x;
			int count = 0;
			boolean inserted = false;

			for(BasicTreeNode itChild : sortedChildren){
				int itChildY = itChild.y;
				int itChildX = itChild.x;
				if(itChildY > childY){
					sortedChildren.add(count, child);
					inserted = true;
					break;
				}
				if(itChildY == childY && itChildX > childX){
					sortedChildren.add(count, child);
					inserted = true;
					break;
				}
				count++;
			}
			if(inserted == false){
				sortedChildren.add(child);
			}

		}
		this.setChildren(sortedChildren);
	}


	/**
	 * Helper function for sorting
	 * @param node
	 */
	private void sortTreeHelper(BasicTreeNode node){
		//2. recursively
		for(BasicTreeNode child : node.getChildren()){
			sortTreeHelper(child);
		}

		//1. sort current node's children
		ArrayList<BasicTreeNode> sortedChildren = new ArrayList<BasicTreeNode>();
		for(BasicTreeNode child : node.getChildren()){
			int childY = child.y;
			int childX = child.x;
			int count = 0;
			boolean inserted = false;

			for(BasicTreeNode itChild : sortedChildren){
				int itChildY = itChild.y;
				int itChildX = itChild.x;
				if(itChildY > childY){
					sortedChildren.add(count, child);
					inserted = true;
					break;
				}
				if(itChildY == childY && itChildX > childX){
					sortedChildren.add(count, child);
					inserted = true;
					break;
				}
				count++;
			}
			if(inserted == false){
				sortedChildren.add(child);
			}

		}
		node.setChildren(sortedChildren);
	}


	/**
	 * Cut/delete the node list with their subtrees
	 * @param nodes
	 */
	public void cutNodes(int[][] nodes){
		for(int i = 0; i < nodes.length; i++){
			BasicTreeNode cutNodeParent = findParentByPosSize(nodes[i]);
			//delete the node
			if(cutNodeParent != null){
				cutNodeParent.deleteChildByPosSize(nodes[i]);
			}
		}

	}

	private ArrayList<BasicTreeNode> redundantNodes = new ArrayList<BasicTreeNode> ();


	/**
	 * Delete redundant nodes
	 * In design tree, if the component name contains Rectangle and it has only one child.  Delete the child.
	 */
	public void removeRedundantNode(){
		for(BasicTreeNode child : this.mChildren){
			detectRedundantNodeHelper(child);
		}

		//remove nodes
		for(BasicTreeNode node : redundantNodes){
			BasicTreeNode parent = node.getParent();
			int index = parent.mChildren.indexOf(node);
			if(index == -1) {
                continue;
            }
			parent.mChildren.remove(node);
			parent.mChildren.addAll(index, node.mChildren);
			for(BasicTreeNode child : node.mChildren){
				child.mParent = parent;
			}
		}
	}


	/**
	 * Helper - Delete node
	 */
	private void detectRedundantNodeHelper(BasicTreeNode node){
		JSNode dsn = (JSNode)node;
		if(node.mChildren.size() == 1 && dsn.getName().contains("Rectangle")){
			if(node.mChildren.get(0).mChildren.size() == 0) {
                redundantNodes.add(node.mChildren.get(0));
            }
		}

		if(node.mChildren.size() == 1){
			BasicTreeNode child = node.mChildren.get(0);
			//1. if same size, definitely delete
			if(Utils.isConnected(node.x, child.x) && Utils.isConnected(node.y, child.y)
					&& Utils.isConnected(node.width, child.width) && Utils.isConnected(node.height, child.height)){
				//remove node
				redundantNodes.add(node);
			}
		}
		for(BasicTreeNode child : node.mChildren){
			detectRedundantNodeHelper(child);
		}
	}


	/**
	 * Find parent node by position and size
	 * @return
	 */
	public BasicTreeNode findParentByPosSize(int [] PosSize){
		for(BasicTreeNode child : this.getChildren()){
			if(child.x == PosSize[0] && child.y == PosSize[1] && child.width == PosSize[2] && child.height == PosSize[3]){
				return this;
			}
			BasicTreeNode childFinding = findParentByPosSizeHelper(child, PosSize);
			if( childFinding != null){
				return childFinding;
			}
		}
		return null;

	}


	private BasicTreeNode findParentByPosSizeHelper(BasicTreeNode node, int[] PosSize){
		for(BasicTreeNode child : node.getChildren()){
			if(child.x == PosSize[0] && child.y == PosSize[1] && child.width == PosSize[2] && child.height == PosSize[3]){
				return node;
			}
			BasicTreeNode childFinding = findParentByPosSizeHelper(child, PosSize);
			if( childFinding != null){
				return childFinding;
			}
		}
		return null;
	}


		//TODO check for any uses of cropScreen or cropScreenHelper, May not be needed

	/**
	 * crop all nodes in the implementation from the input file
	public void cropScreen(){
		//Make sure the folder is exist
		String implementImgFolder = AndroidGUIChecker.outputFolder + "design";
		File dir = new File(implementImgFolder);
		if(!dir.exists()){
			dir.mkdir();
		}
		for(BasicTreeNode child : this.mChildren){
			cropScreenHelper(child);
		}
	}

	 */

	/**
	 * Helper -  crop all nodes in the implementation from the input file
	private void cropScreenHelper(BasicTreeNode node){
		//Crop
		try {
			String implementImgFolder = AndroidGUIChecker.outputFolder + "design";
			File dir = new File(implementImgFolder);
			//ImagesHelper.cropImageAndSave(AndroidGUIChecker.appScreenShot, dir + File.separator + node.hashCode() + ".png",
			//		node.x, node.y, node.width, node.height);
			ImagesHelper.cropImageAndSave(AndroidGUIChecker.mockUpImageJpg, dir + File.separator + node.hashCode() + ".jpg",
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

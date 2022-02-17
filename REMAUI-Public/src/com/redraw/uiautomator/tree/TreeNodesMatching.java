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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import edu.wm.cs.semeru.redraw.ConstantSettings;
import edu.wm.cs.semeru.redraw.JSNode;
//import edu.semeru.android.guichecker.ui.UiTreeNode;

/**
 * Given two root nodes (from Design and UI), the class help match the nodes between two trees
 * @author Boyang Li
 */
public class TreeNodesMatching {

	RootDesignNode rootD;
	RootUINode rootU;

	/**
	 * Constructor
	 * @param rootD design tree
	 * @param rootU implementation tree
	 */
	public TreeNodesMatching(RootDesignNode rootD, RootUINode rootU){
		this.rootD = rootD;
		this.rootU = rootU;

		//rootD.setRootUI(rootU);
		//rootU.setRootDesign(rootD);
	}

	/**
	 * Matching leaf nodes in both trees
	 */
	public void matching(){
		ConstantSettings settings = ConstantSettings.getInstance();
		ArrayList<JSNode> dsLeafNodes = rootD.getLeafNodes();
		ArrayList<UiTreeNode> uiLeafNodes = rootU.getLeafNodes();
		
		for(int i =0; i < uiLeafNodes.size(); i++) {
			if(uiLeafNodes.get(i).getType().contains("Layout")){
//				System.out.println(uiLeafNodes.get(i).getType());
				uiLeafNodes.remove(i);
			}
		}
		
		RelativePositionConverter converter = new RelativePositionConverter(settings.getDSBoard(), settings.getUIBoard());

		ArrayList<TupleDisNodes> listTuple = new ArrayList<TupleDisNodes> ();

		for(JSNode dsNode : dsLeafNodes){
			for(UiTreeNode uiNode : uiLeafNodes){
				if(uiNode.getWidth() < 3 || uiNode.getHeight() < 3){
					continue;
				}
				float curDis = converter.heuristicDistance(dsNode, uiNode);

				listTuple.add(new TupleDisNodes(curDis, dsNode, uiNode));
			}
		}
		Collections.sort(listTuple);

		for(TupleDisNodes tuple : listTuple ){
			JSNode dsNode = tuple.getDsNode();
			UiTreeNode uiNode = tuple.getUiNode();
			if(tuple.getDis() < settings.getThresholdMatchDistance()){
				//match
//				System.out.println(uiNode.getType() + " - " + uiNode.getX() + "," + uiNode.getY() + "," + uiNode.getWidth() + "," + uiNode.getHeight() + " :: " + dsNode.getX() + "," + dsNode.getY() + "," + dsNode.getWidth() + "," + dsNode.getHeight());
				if(dsNode.correspondingNode == null && uiNode.correspondingNode == null){
//					System.out.println(uiNode.getType() + " - " + uiNode.getX() + "," + uiNode.getY() + "," + uiNode.getWidth() + "," + uiNode.getHeight() + " :: " + dsNode.getX() + "," + dsNode.getY() + "," + dsNode.getWidth() + "," + dsNode.getHeight());
					dsNode.correspondingNode = uiNode;
					uiNode.correspondingNode = dsNode;
				}
			}else{
				break;
			}
		}




		// The second round matching
		//merge dsNodes if it's possible
		for(JSNode dsNode : dsLeafNodes){
			if(dsNode.correspondingNode == null){

				for(TupleDisNodes tuple : listTuple ){
					JSNode dsTupleNode = tuple.getDsNode();
					UiTreeNode uiNode = tuple.getUiNode();

					//find the closest node that has a match in ui
					if( dsTupleNode == dsNode
						&& tuple.getDis() < settings.getThresholdMatchDistance() * 2){

						if(uiNode.containsWithThreshold(dsNode)){
							//merge dsNode and uiNode.corresponding node as a super DSNode
							JSNode dsNode2 = (JSNode)uiNode.correspondingNode;
							if(dsNode2 == null || dsNode == null){
								continue;
							}
							//If those are not in the same container
							if(dsNode2.getParent() != dsNode.getParent()){
								continue;
							}
							int x = Math.min(dsNode.x, dsNode2.x);
							int y = Math.min(dsNode.y, dsNode2.y);
							int right1 = dsNode.x + dsNode.width;
							int right2 = dsNode2.x + dsNode2.width;
							int bottom1 = dsNode.y + dsNode.height;
							int bottom2 = dsNode2.y + dsNode2.height;
							int width = Math.max(right1, right2) - x;
							int height = Math.max(bottom1, bottom2) - y;

							//Create a super node and add to the tree
							JSNode superNode = new JSNode(x, y, width, height);
							superNode.setName("super_node");
							uiNode.correspondingNode.setMerged(true);
							dsNode.setMerged(true);
							dsNode.getParent().addChild(superNode);

							uiNode.correspondingNode = superNode;
							superNode.correspondingNode = uiNode;
						}
					}
				}
			}
		}

		//merge uiNodes if it's possible
		for(UiTreeNode uiNode : uiLeafNodes){
			if(uiNode.correspondingNode == null){
				for(TupleDisNodes tuple : listTuple ){
					JSNode dsNode = tuple.getDsNode();
					UiTreeNode uiTupleNode = tuple.getUiNode();
					//find the closest node that has a match in ui
					if(tuple.getDis() < settings.getThresholdMatchDistance() * 2
							&& uiTupleNode == uiNode){
						if(dsNode.containsWithThreshold(uiNode)){
							//merge dsNode and uiNode.corresponding node as a super DSNode
							UiTreeNode uiNode2 = (UiTreeNode)dsNode.correspondingNode;
							if(uiNode2 == null || uiNode == null){
								continue;
							}
							//If those are not in the same container
							if(uiNode2.getParent() != uiNode.getParent()){
								continue;
							}
							int x = Math.min(uiNode.x, uiNode2.x);
							int y = Math.min(uiNode.y, uiNode2.y);
							int right1 = uiNode.x + uiNode.width;
							int right2 = uiNode2.x + uiNode2.width;
							int bottom1 = uiNode.y + uiNode.height;
							int bottom2 = uiNode2.y + uiNode2.height;
							int width = Math.max(right1, right2) - x;
							int height = Math.max(bottom1, bottom2) - y;

							//Create a super node and add to the tree
							UiTreeNode superNode = new UiTreeNode(x, y, width, height);
							superNode.setName("super_node");
							dsNode.correspondingNode.setMerged(true);
							uiNode.setMerged(true);
							uiNode.getParent().addChild(superNode);

							dsNode.correspondingNode = superNode;
							superNode.correspondingNode = dsNode;
						}
					}
				}
			}
		}

		//Match based on the centric
		for(UiTreeNode uiNode : uiLeafNodes){
			if(uiNode.correspondingNode == null){
				int diffMin = Integer.MAX_VALUE;
				for(JSNode dsNode : dsLeafNodes){
					if(dsNode.correspondingNode == null){
						int diff = (int) dsNode.getPxCentric().computeDistance(uiNode.getCentric());
						if(diff < settings.getThresholdMatchDistance()/10){
							if(diffMin < diff){
								continue;
							}
							uiNode.correspondingNode = dsNode;
							diffMin = diff;
						}
					}
				}
				//link back
				if(uiNode.correspondingNode != null){
					uiNode.correspondingNode.correspondingNode = uiNode;
				}
			}
		}


	}



	//######################### considering deletion ###################
	//	public void matchingTwoTrees(){
	//		//1. Top down first
	//		matchingNodesFrom(rootD, rootU);
	//
	//		//2. then bottom up, text matching
	//		bottomupMatching(rootD, rootU);
	//
	//		//So far, all matchings are sound.
	//		//3. heuristically matching.
	//		//incompletedNodeMatching(rootD, rootU);
	//	}
	//
	//
	//	public void matchingNodesFrom(BasicTreeNode parentD, BasicTreeNode parentU){
	//		matchingHelper(parentD, parentU, 1);
	//		matchingHelper(parentD, parentU, 2);
	//	}
	//
	//
	//
	//	private void incompletedNodeMatching(RootDesignNode rootD, RootUINode rootU){
	//		HashSet<BasicTreeNode> setD = rootD.getAllIncompletedNodes();
	//		//TODO:
	//		for(BasicTreeNode node: setD){
	//			System.out.println(node);
	//		}
	//	}
	//
	//
	//
	//
	//	private void bottomupMatching(RootDesignNode rootD, RootUINode rootU){
	//		ArrayList<DSNode> setD = rootD.getLeafNodes();
	//		ArrayList<UiTreeNode> setU = rootU.getLeafNodes();
	//		for(BasicTreeNode dbtNode : setD){
	//			DSNode dNode = (DSNode)dbtNode;
	//			if(dNode.hasCorrespondingMatch()){
	//				continue;
	//			}
	//			for(BasicTreeNode ubtNode : setU){
	//				UiTreeNode uNode = (UiTreeNode)ubtNode;
	//				if(isTextMatch(dNode, uNode)){
	//					DSNode dTemp = dNode;
	//					UiTreeNode uTemp = uNode;
	//					while(dTemp.hasCorrespondingMatch() == false && uTemp.hasCorrespondingMatch() == false){
	//						dTemp.correspondingNode = uTemp;
	//						uTemp.correspondingNode = dTemp;
	//						if(dTemp.mChildren.size() == 0 || dTemp.mChildren.size() == 1){
	//							dTemp.completeMatching = true;
	//							dTemp.completeMatching = true;
	//						}
	//						if(dTemp.completeMatching == false){
	//							//matchingHelper(dTemp, parentU, 1);
	//							matchingNodesFrom(dTemp, uTemp);
	//						}
	//						if(!(dTemp.mParent instanceof RootDesignNode) && !(uTemp.mParent instanceof RootUINode)){
	//							dTemp = (DSNode)dTemp.mParent;
	//							uTemp = (UiTreeNode)uTemp.mParent;
	//						}
	//					}
	//				}
	//			}
	//		}
	//	}
	//
	//	/**
	//	 * Top down matching
	//	 * # of children are matching, relative positions are matching.
	//	 * @param parentD
	//	 * @param childrenD
	//	 * @param parentU
	//	 * @param childrenU
	//	 */
	//	private void matchingHelper(BasicTreeNode parentD, BasicTreeNode parentU, int depth){
	//		//if already matched
	//		if(parentD.completeMatching == true && parentU.correspondingNode.completeMatching == true && parentD.correspondingNode == parentU){
	//			for(BasicTreeNode child: parentD.mChildren){
	//				matchingHelper(child, child.correspondingNode, depth);
	//			}
	//			return;
	//		}
	//
	//		ArrayList<BasicTreeNode> childrenD =  new ArrayList<BasicTreeNode>();
	//		ArrayList<BasicTreeNode> childrenU =  new  ArrayList<BasicTreeNode>();
	//		childrenD = parentD.mChildren;
	//
	//		//Set up UI part
	//		Queue<BasicTreeNode> queueU = new LinkedList<BasicTreeNode>();
	//		queueU.add(parentU);
	//		while(queueU.isEmpty() != true){
	//			BasicTreeNode currentNode = queueU.poll();
	//			if(currentNode == null){
	//				continue;
	//			}
	//			if(currentNode.getAncestor(depth) == parentU){
	//				childrenU.add(currentNode);
	//			}else{
	//				if(currentNode.mChildren.size() == 0){
	//					childrenU.add(currentNode);
	//				}else{
	//					queueU.addAll(currentNode.mChildren);
	//				}
	//			}
	//		}
	//
	//		if(childrenD.size() ==  childrenU.size()){
	//			//has the same number of children
	//			parentD.completeMatching = true;
	//			parentU.completeMatching = true;
	//			pairNodes(parentD, childrenD, parentU, childrenU);
	//			for(BasicTreeNode child: childrenD){
	//				matchingHelper(child, child.correspondingNode, depth);
	//			}
	//		}
	//	}
	//
	//	/**
	//	 * Pair the child (or grandchild) nodes
	//	 * @param parentD
	//	 * @param childrenD
	//	 * @param parentU
	//	 * @param childrenU
	//	 */
	//	private void pairNodes(BasicTreeNode parentD, ArrayList<BasicTreeNode> childrenD,
	//			BasicTreeNode parentU, ArrayList<BasicTreeNode> childrenU){
	//		RelativePositionConverter converter = new RelativePositionConverter(parentD, parentU);
	//		for(BasicTreeNode dnode : childrenD){
	//			if(dnode.hasCorrespondingMatch()){
	//				continue;
	//			}
	//			BasicTreeNode closestMatch = null;
	//			double lowestDis = Double.MAX_VALUE;
	//			for(BasicTreeNode unode : childrenU){
	//				if(unode.hasCorrespondingMatch() == true){
	//					continue;
	//				}
	//				if(closestMatch == null){
	//					closestMatch = unode;
	//					lowestDis = converter.getConvertDistance(dnode, unode);
	//					continue;
	//				}else{
	//					double currentDis = converter.getConvertDistance(dnode, unode);
	//					if(currentDis < lowestDis){
	//						closestMatch = unode;
	//						lowestDis = currentDis;
	//					}
	//				}
	//			}
	//			if(closestMatch != null){
	//				dnode.correspondingNode = closestMatch;
	//				closestMatch.correspondingNode = dnode;
	//				if(closestMatch.mChildren.size() == 0){
	//					closestMatch.completeMatching = true;
	//				}
	//			}
	//
	//			if(dnode.mChildren.size() == 0){
	//				dnode.completeMatching = true;
	//			}
	//
	//		}
	//	}
	//
	//	/**
	//	 * Check if the two nodes are matched based on text
	//	 * @param dsNode
	//	 * @param uiNode
	//	 * @return
	//	 */
	//	private boolean isTextMatch(DSNode dsNode, UiTreeNode uiNode){
	//		String dsText = dsNode.getContent();
	//		String uiText = uiNode.getName();
	//		return dsText.equals(uiText);
	//	}

}

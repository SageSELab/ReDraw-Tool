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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import com.redraw.uiautomator.tree.RootDesignNode;

import edu.wm.cs.semeru.redraw.ConstantSettings;


/** 
 * A parser for Marketch DS files
 * @author Boyang Li
 */

public class MarketchJSParser {
	public final File DSFile;
	private HashSet <JSNode> DSNodeSet = new HashSet <JSNode>();
	private RootDesignNode treeRoot = null;

	/**
	 * @return the treeRoot
	 */
	public RootDesignNode getTreeRoot() {
		return treeRoot;
	}


	public ArrayList<JSNode>  getLeafNodes() {
		return treeRoot.getLeafNodes();
	}


	/**
	 * @param file
	 */
	public MarketchJSParser(String file){
		this.DSFile = new File(file);
	}


	/**
	 * Run the parser
	 */
	public void runParser(){
		ConstantSettings settings = ConstantSettings.getInstance();
		try {
			BufferedReader brTest = new BufferedReader(new FileReader(DSFile));
			String content = brTest.readLine();
			brTest.close();

			//cut string before "artboard":{
			content = content.substring(content.indexOf("\"artboard\":{") + 1);

			//cut unnecessary info from artboard
			content = content.substring(content.indexOf("[{") + 2);

			//cut tail
			//content = content.substring(0, content.lastIndexOf("}],\"mask\":{}}}}"));
			content = content.substring(0, content.lastIndexOf("}],\"mask\":"));
			String [] allComponent = content.split("\\},\\{");


			//parsing all kinds of components
			for(String component : allComponent){

				JSNode newNode = new JSNode(component);

				//if the component is an avoid node, do not add it
				boolean avoid = false;
				for(int i = 0; i < settings.getIgnoredCompDesign().length; i++){
					JSNode tempDSNode = generateTempNode(settings.getIgnoredCompDesign()[i]);
					
					//delete the node ((i.e. battery))
					if(tempDSNode.contains(newNode) || tempDSNode.equals(newNode) ||tempDSNode.containsWithThreshold(newNode, 0.2)){
						//System.out.println("====avoid===="+newNode);
						avoid = true;
					}
				}
				//To avoid existing node 
				if(nodeExists(newNode)){
					avoid = true;
				}
				
				//Avoid ds node out of the screen
				JSNode boardDSNode = generateTempNode(settings.getDSBoard());
				if(!boardDSNode.contains(newNode)){
					avoid = true;
				}
				if(!avoid){
					DSNodeSet.add(newNode);
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Has same node or has node very similar
	 * @param dsNode
	 * @return
	 */
	private boolean nodeExists(JSNode dsNode){
		for(JSNode tempNode :DSNodeSet){
			if(tempNode.getName().equals(dsNode.getName()) &&
					tempNode.getHeight() == dsNode.getHeight() &&
					tempNode.getWidth() == dsNode.getWidth() &&
					(Math.abs(tempNode.getX()-dsNode.getX()) < 4 &&
							Math.abs(tempNode.getY()-dsNode.getY()) < 4)){
				return true;
			}
		}
		return false;
	}

	private JSNode generateTempNode(int [] tempData){
		JSNode temp = new JSNode(tempData[0], tempData[1], tempData[2]-tempData[0],tempData[3]-tempData[1]);
		return temp;
	}


	/**
	 * Build a tree based on all detected components. One html per time
	 */
	public void buildTree() {
		treeRoot = new RootDesignNode();
		// start with the first node
		for (JSNode node : DSNodeSet) {
			treeRoot.addNode(node);
		}
		treeRoot.sortTree();
		treeRoot.buildBidirection();
		treeRoot.deletReduandant();

		//treeRoot.preProcessSplitter();
		//treeRoot.cutNodes(ConstantSettings.IGNOREDCOMPDESIGN);
		//treeRoot.splitNode();
		//treeRoot.removeRedundantNode();
	}


	public void printOutTree() {
		treeRoot.printOutTree();
	}


	public JSNode findNodeByPosSize(int x, int y, int width, int height){
		JSNode tempNode = new JSNode(x, y, width, height);
		for(JSNode node : this.DSNodeSet){
			if(node.equals(tempNode)){
				return node;
			}
		}
		return null;
	}


	/**
	 * Find the first node matches the size. 
	 * @return
	 */
	public JSNode findNodeBySize(int width, int height){
		for(JSNode node : this.DSNodeSet){
			if(node.getWidth() == width && node.getHeight() == height){
				return node;
			}
		}
		return null;
	}

	/**
	 * @return the dSFile
	 */
	public File getDSFile() {
		return DSFile;
	}


	/**
	 * @return the dSNodeSet
	 */
	public HashSet<JSNode> getDSNodeSet() {
		return DSNodeSet;
	}


}

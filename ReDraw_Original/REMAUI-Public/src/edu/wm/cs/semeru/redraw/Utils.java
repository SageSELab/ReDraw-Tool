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

import java.util.ArrayList;

import com.redraw.uiautomator.tree.BasicTreeNode;
import com.redraw.uiautomator.tree.UiTreeNode;

import edu.wm.cs.semeru.redraw.JSNode;



/**
 * Utility class
 * @author Boyang Li
 *
 */
public class Utils {
	
	/**
	 * Check two postions are close enough
	 * @param pos1
	 * @param pos2
	 * @return
	 */
	public static boolean isConnected(int pos1, int pos2){
		return Math.abs(pos2 - pos1) < 3; 
	}
	
	
	
	/**
	 * Sorts list by Y position and returns a new list 
	 * @param list
	 * @return
	 */
	public static ArrayList <JSNode> sortByYPosition(ArrayList <JSNode> list){
		ArrayList <JSNode> ret = new ArrayList <JSNode>();
		for(JSNode node : list){
			boolean added = false;
			int count = 0;
			for(JSNode sortedNode : ret){
				if(node.getY() <= sortedNode.getY()){
					ret.add(count, node);
					added = true;
					break;
				}		
				count++;
			}
			if(added == false){
				ret.add(node);
			}
		}
		return ret;
	}
	
	
	/**
	 * Sorts list by X position and returns a new list 
	 * @param list
	 * @return
	 */
	public static ArrayList <JSNode> sortByXPosition(ArrayList <JSNode> list){
		ArrayList <JSNode> ret = new ArrayList <JSNode>();
		for(JSNode node : list){
			boolean added = false;
			int count = 0;
			for(JSNode sortedNode : ret){
				if(node.getX() <= sortedNode.getX()){
					ret.add(count, node);
					added = true;
					break;
				}	
				count++;
			}
			if(added == false){
				ret.add(node);
			}
		}
		return ret;
	}
	
	
	/**
	 * Down casts a BasicTreeNode ArrayList to a DSNode ArrayList
	 * @param list
	 * @return
	 */
	public static ArrayList <JSNode> BasicToDesign(ArrayList <BasicTreeNode> list){
		ArrayList <JSNode> returnList = new ArrayList <JSNode>();
		for(BasicTreeNode node : list){
			returnList.add((JSNode)node);
		}
		return returnList;
	}
	
	
	/**
	 * Down casts a BasicTreeNode ArrayList to a DSNode ArrayList
	 * @param list
	 * @return
	 */
	public static ArrayList <UiTreeNode> BasicToUI(ArrayList <BasicTreeNode> list){
		ArrayList <UiTreeNode> returnList = new ArrayList <UiTreeNode>();
		for(BasicTreeNode node : list){
			returnList.add((UiTreeNode)node);
		}
		return returnList;
	}
}

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

import edu.wm.cs.semeru.redraw.ConstantSettings;
import edu.wm.cs.semeru.redraw.JSNode;
//import edu.semeru.android.guichecker.ui.UiTreeNode;

public class RelativePositionConverter {

	private int fromX;
	private int fromY;
	private int toX;
	private int toY;
	private int fromWidth;
	private int fromHeight;
	private int toWidth;
	private int toHeight;

	private float ratioWidth;
	private float ratioHeight;

	public RelativePositionConverter(int[] boardFrom, int[] boardTo){
		fromX = boardFrom[0];
		fromY = boardFrom[1];
		toX = boardTo[0];
		toY = boardTo[1];

		fromWidth = boardFrom[2];
		fromHeight = boardFrom[3];
		toWidth = boardTo[2];
		toHeight = boardTo[3];

		ratioWidth = (float)toWidth/fromWidth;
		ratioHeight = (float)toHeight/fromHeight;
	}


	public float heuristicDistance(JSNode nodeFrom, UiTreeNode nodeTo){
		float rFromX = nodeFrom.getX() * ratioWidth;
		float rFromY = nodeFrom.getY() * ratioHeight;
		float rFromWidth = nodeFrom.getWidth() * ratioWidth;
		float rFromHeight = nodeFrom.getHeight() * ratioHeight;
		float dis = Math.abs(nodeTo.getX() - rFromX);
		dis += Math.abs(nodeTo.getY() - rFromY);
		dis += Math.abs(nodeTo.getWidth() - rFromWidth);
		dis += Math.abs(nodeTo.getHeight() - rFromHeight);
		if(isTextMatch(nodeFrom, nodeTo)){
			int threshold = ConstantSettings.getInstance().getThresholdMatchDistance();
			//decrease the distance by 1/2 of the threshold if we have a text matching
			//dis -=  threshold/2;
			dis *=  0.1;
		}
		return dis;
	}
	
	public float getConvertX(int fromX){
		return  fromX * ratioWidth;	
	}
	
	public float getConvertY(int fromY){
		return  fromY * ratioHeight;	
	}
	
	public float getConvertWidth(int fromWidth){
		return  fromWidth * ratioWidth;	
	}
	
	public float getConvertHeight(int fromHeight){
		return  fromHeight * ratioHeight;	
	}



	/**
	 * Check if the two nodes are matched based on text
	 * @param dsNode
	 * @param uiNode
	 * @return
	 */
	private boolean isTextMatch(JSNode dsNode, UiTreeNode uiNode){
		String dsText = dsNode.getContent();
		String uiText = uiNode.getName();
		return dsText.equals(uiText);
	}

}

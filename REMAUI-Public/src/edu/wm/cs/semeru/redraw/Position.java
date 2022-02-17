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


/**
 * This class represents a position in screen or page
 * @author Boyang Li
 * Created on Aug 13, 2016
 */

public class Position {
	
	private float x;
	
	private float y;
	
	
	public Position(float left, float top){
		this.x = left;
		this.y = top;
	}

	/**
	 * @return the top
	 */
	public float getY() {
		return y;
	}

	/**
	 * @param top the top to set
	 */
	public void setTop(float top) {
		this.y = top;
	}

	/**
	 * @return the left
	 */
	public float getX() {
		return x;
	}

	/**
	 * @param left the left to set
	 */
	public void setLeft(float left) {
		this.x = left;
	}
	
	public String toString(){
		return "(" + x + "," + y + ") ";
	}

	
	public double computeDistance(Position otherPos){
		double p1 = (x - otherPos.getX()) * (x - otherPos.getX());
		double p2 = (y - otherPos.getY()) * (y - otherPos.getY());
		return Math.sqrt(p1+p2);
	}
}

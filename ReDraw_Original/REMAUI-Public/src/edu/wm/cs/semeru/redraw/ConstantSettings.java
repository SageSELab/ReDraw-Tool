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


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * This class stores constant settings
 * @author Boyang Li
 *
 */
public class ConstantSettings{
	
	public final static int MATCHINGRATIO = 4;   //provided by HUAWEI. Non-Changeable by parameters since since we consider it as a experience number. 
	
	private static ConstantSettings instance = null;

	public static ConstantSettings getInstance() {
		if(instance == null) {
			instance = new ConstantSettings();
		}
		return instance;
	}
	private ConstantSettings(){
		//Constructor
	}
	/**
	 * @param instance the instance to set
	 */
	public static void setInstance(ConstantSettings instance) {
		ConstantSettings.instance = instance;
	}

	@SerializedName("IgnoredCompDesign")
	@Expose
	private int[][] ignoredCompDesign = null;
	@SerializedName("UIBoard")
	@Expose
	private int[] uIBoard = null;
	@SerializedName("DSBoard")
	@Expose
	private int[] dSBoard = null;
	@SerializedName("ThresholdMatchDistance")
	@Expose
	private int thresholdMatchDistance;    //TODO: delete thresholdMatchDistance
	@SerializedName("ViolationThreshold")
	@Expose
	private int violationThreshold;
	@SerializedName("ImgDiffThreshold")
	@Expose
	private int ImgDiffThreshold;
	@SerializedName("DynamicCompDesign")
	@Expose
	private int[][] dynamicCompDesign = null;
	private boolean ifAutoDynamic = false;
//	The following variables are currently dprecated as they are used for scaling the dimensions of 
//	a design mock-up to a differently sized screen with different pixel density.
//	@SerializedName("TargetDeviceDensity")
//	@Expose
//	private int targetDeviceDensity;      //480 for Nexus 5; 320 for Nexus 7; 560 for 6P
//	@SerializedName("DpRatio")
//	@Expose
//	private float dpRatio;

	/**
	 * @return the ignoredCompDesign
	 */
	public int[][] getIgnoredCompDesign() {
		return ignoredCompDesign;
	}
	
	public int[][] getDynamicCompDesign(){
		return dynamicCompDesign;
	}
	public boolean getIfAutoDynamic() {
		return ifAutoDynamic;
	}
	public void setIfAutoDynamic(boolean ifAutoDynamic) {
		this.ifAutoDynamic = ifAutoDynamic;
	}
	/**
	 * @param ignoredCompDesign the ignoredCompDesign to set
	 */
	public void setIgnoredCompDesign(int[][] ignoredCompDesign) {
		this.ignoredCompDesign = ignoredCompDesign;
	}
	
	public void setDynamicCompDesign(int[][] dynamicCompDesign) {
		this.dynamicCompDesign = dynamicCompDesign;
	}

	/**
	 * @return the uIBoard
	 */
	public int[] getUIBoard() {
		return uIBoard;
	}

	/**
	 * @param uIBoard the uIBoard to set
	 */
	public void setUIBoard(int[] uIBoard) {
		this.uIBoard = uIBoard;
	}

	/**
	 * @return the dSBoard
	 */
	public int[] getDSBoard() {
		return dSBoard;
	}

	/**
	 * @param dSBoard the dSBoard to set
	 */
	public void setDSBoard(int[] dSBoard) {
		this.dSBoard = dSBoard;
	}


	/**
	 * 
	 * @return
	 * The thresholdMatchDistance
	 */
	public int getThresholdMatchDistance() {
		//return thresholdMatchDistance;
		return (uIBoard[2]+uIBoard[3])/2/MATCHINGRATIO; 
	}


	/**
	 * 
	 * @return
	 * The violationThreshold
	 */
	public int getViolationThreshold() {
		return violationThreshold;
	}

	/**
	 * 
	 * @param violationThreshold
	 * The ViolationThreshold
	 */
	public void setViolationThreshold(int violationThreshold) {
		this.violationThreshold = violationThreshold;
	}

	/**
	 * @return the imgDiffThreshold
	 */
	public int getImgDiffThreshold() {
		return ImgDiffThreshold;
	}
	/**
	 * @param imgDiffThreshold the imgDiffThreshold to set
	 */
	public void setImgDiffThreshold(int imgDiffThreshold) {
		ImgDiffThreshold = imgDiffThreshold;
	}
	
	
// 	Deprecated Getters and Setters for the scaling factors
//	/**
//	 * @return
//	 * The dpRatio
//	 */
//	public float getDpRatio() {
//		return dpRatio;
//	}
//
//	/**
//	 * 
//	 * @param dpRatio
//	 * The DpRatio
//	 */
//	public void setDpRatio(float dpRatio) {
//		this.dpRatio = dpRatio;
//	}
	
//	/**
//	 * 
//	 * @return
//	 * The targetDeviceDensity
//	 */
//	public int getTargetDeviceDensity() {
//		return targetDeviceDensity;
//	}
//
//	/**
//	 * 
//	 * @param targetDeviceDensity
//	 * The TargetDeviceDensity
//	 */
//	public void setTargetDeviceDensity(int targetDeviceDensity) {
//		this.targetDeviceDensity = targetDeviceDensity;
//	}

}

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
/**
 * Created by Kevin Moran on Aug 28, 2016
 */
package com.redraw.uiautomator.tree;


/**
 * @author KevinMoran
 *
 */
public class AndroidUtilities {

	
	 public static String getNewLocation(int x, int y, int sizeX, int sizeY) {
	        double percentageX = 0.05;// 5% * 2 = 10%
	        double percentageY = 0.3;
	        int minX = (int) (sizeX / 2d - (sizeX * percentageX));
	        int minY = (int) (sizeY * percentageY);
	        int maxX = (int) (sizeX / 2d + (sizeX * percentageX));
	        int maxY = sizeY - minY;
	        String result = "";

	        if (y <= maxY) {
	            if (y <= minY) {
	                result = "Top";
	            } else {
	                result = "Center";
	            }
	        } else {
	            result = "Bottom";
	        }

	        if (x <= maxX) {
	            if (x <= minX) {
	                result += " left";
	            } else {
	                result += "";
	            }
	        } else {
	            result += " right";
	        }
	        return result;
	    }
	
	 
	 
	 /**
	  * Name: convertDpCoordinates
	  * Description: This method takes a dp value specified by a Marketch design file and converts it to the proper 
	  * pixel value for a device with a given screen density.
	  * 
	  * @param targetDisplayDensity: This is the target Display density of the Android device.  This can be gathered 
	  * by using the command "adb shell getprop ro.sf.lcd_density".  For our purposes we want to use a value of 
	  * 320 for the Nexus 7.
	  * 
	  * @param dpMeasurement: This is the value of the dp measurement that you want to convert into a pixel value.
	  * 
	  * @return the method returns an integer value of the pixel measurement conversion of the dp value you entered as
	  * a parameter.
	 */
	public static int convertDpCoordinates(int targetDisplayDensity, float f){
		 
		 int result = 0;
		 
		 float displayRatio = (float)targetDisplayDensity / 160;
		 
		 result = (int)((float) f * displayRatio);
		 
		 return result;
	 }
	 
}

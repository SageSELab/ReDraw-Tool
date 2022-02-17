/*******************************************************************************
 * Copyright (c) 2017, SEMERU
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
package edu.wm.semeru.redraw.parsing;

import java.io.File;
/**
 * Splits large data file into k subset so that we can perform k-fold cross validation to test. Note that
 * the number of "left out" files will be equal to numImages mod k (the least nonnegative integer in this
 * equivalence class)
 * @author mjcurcio
 *
 */
public class DataPartioner {

	public static void main(String[] args){
		File dataDir = new File(args[0]);
		int num = Integer.parseInt(args[1]);
		int count;
		
		File[] children = dataDir.listFiles();
		for (count = 0; count < num; count++){
			int i;
			File newDir = new File(dataDir.getAbsolutePath() + "/dataBatch" + Integer.toString(count));
			newDir.mkdir();
			String newDirName = newDir.getAbsolutePath();
			for (i = 0; i < children.length/10; i++){
				File im = children[count * (children.length/num) + i];
				//renameTo also has the ability to change the location of the file
				im.renameTo(new File(newDirName + "/" + im.getName()));
				//for debugging
				System.out.println(i);
			}
			
		}
	}
}

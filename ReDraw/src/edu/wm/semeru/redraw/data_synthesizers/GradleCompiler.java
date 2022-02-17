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
package edu.wm.semeru.redraw.data_synthesizers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import edu.wm.cs.semeru.core.helpers.TerminalHelper;

/**
 * build list of strings of paths to the root of android projects, invoke a slightly modified MutantCompiler.java in the Android-Mutation Project
 * 
 * TODO change the file copier so that the new copied files have the .apk file extension. While this modification is not made, 
 * 		just run the rile renamer script in this package after running this script to change add the extension. I am not doing this now because then I would have to 
 * 		recompile all the apps which takes a lot of time. The apk files must have that extension for an emulator/device to recognize them.
 * @author mjcurcio
 *
 */
public class GradleCompiler {
	
	public static ArrayList<String> rootList;

	public static void main(String[] args) throws IOException{
		
		rootList = new ArrayList<String>();
		File inputDir = new File(args[0]);
		String failedCompDir = args[1];
		String newAPKsDir = args[2];
		String pathToGradle = args[3];
		String pathToAndroid = args[4];
		
		
		BuildStringList(inputDir);
		
		buildMutants(rootList, pathToGradle, newAPKsDir, false);
	}
	
	public static void BuildStringList(File in){
		File[] children = in.listFiles();
		for (File child : children){
			String[] gChildren = child.list();
			if (Arrays.asList(gChildren).contains("gradlew")){
				rootList.add(child.getAbsolutePath());
			}
			else{
				BuildStringList(child);
			}
		}
	}
	/**
	 * stolen from MutantCompiler.java
	 * @param antPath
	 * @param mutantSrcFolder
	 * @param numOfMutants
	 * @param instrument
	 * @throws IOException 
	 */
	public static void buildMutants(ArrayList<String> projects, String gradlePath, String outputDir, boolean instrument) throws IOException{
		
		String cleanCommand = "";
		String cleanCommandOutput = "";
		ArrayList<Integer> failedMutants = new ArrayList<Integer>();
		//only the else clause will be used in this method
//		if(instrument){
//		
//		for(int i = 1; i <= projects.size(); i++){
//			System.out.println("Building Mutant #: " + i + " with Instrumentation");
//			cleanCommand = "(cd " + mutantSrcFolder + File.separator + i + File.separator + "; " + gradlePath + File.separator + "bin/ant emma debug)";
//			System.out.println(cleanCommand);
//			TerminalHelper.executeCommand(cleanCommand);
//			//System.out.println(cleanCommandOutput);
//			//if(cleanCommandOutput.contains("BUILD FAILED")){
//				//failedMutants.add(i);
//			//}
//		}
		
//		}else{
			
			for(int i = 0; i < projects.size(); i++){
				String curProject = projects.get(i);
				File curFile = new File(curProject);
				String curComponent = curFile.getName().replaceAll("[^A-Za-z]", "");
				curComponent = curComponent.substring(0, curComponent.length() - 3);
				
				System.out.println("Building Mutant #: " + i + " without Instrumentation");
				cleanCommand = ("cd " + curProject + ";" + gradlePath + "/gradle build");
				System.out.println(cleanCommand);
				cleanCommandOutput = TerminalHelper.executeCommand(cleanCommand);
				System.out.println(cleanCommandOutput);
				
				//copy file to new apk dir
				String findAPKCommand = "find " + curProject + " -name 'app-debug.apk'";
				String findAPKResult = TerminalHelper.executeCommand(findAPKCommand);
				File apkFile = new File(findAPKResult);
				File newFile = new File(outputDir + "/" + curComponent + "/" + curComponent + "-apk-" + Integer.toString(i) + ".apk");
				FileUtils.copyFile(apkFile, newFile);
			}
			
		}
		//System.out.println("The following mutants failed to compile with Instrumentation:");
		//for (int printFailed : failedMutants){
			//System.out.println(printFailed);
		//}
		
	}

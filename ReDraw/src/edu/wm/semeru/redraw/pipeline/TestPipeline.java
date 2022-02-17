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
package edu.wm.semeru.redraw.pipeline;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabExecutionException;
import com.mathworks.engine.MatlabSyntaxException;

/**
 * {Insert class description here}
 *
 * @author Carlos Bernal
 */
public class TestPipeline {

    public static void main(String[] args) throws MatlabExecutionException, MatlabSyntaxException, ExecutionException, IOException {
    	//argument for python command
    	String sketchFile = "";
    	String imageFile = "";
    	String outputDir = "/Users/KevinMoran/Desktop/TestOutput/"; //args[1];	
//    	String example = args[0];
//    	if (example.equalsIgnoreCase("pandora")){
//    		sketchFile = "D:\\rdata\\ReDraw-REMAUI-Pix2Code-Oracle-Images-and-Xml\\oracle\\codeadore.textgram\\1.xml";
//            imageFile = "D:\\rdata\\ReDraw-REMAUI-Pix2Code-Oracle-Images-and-Xml\\oracle\\codeadore.textgram\\1.png";
//    	}
//    	else{
//    		System.out.println("incorrect example given on command line");
//    		System.exit(0);
//    	}
        String outputLocation = "/Users/KevinMoran/Desktop/TestOutput";
        String pathToAndroidSDK = "/Applications/AndroidSDK/sdk";//need to reinstall Android SDK
        String pathToNet ="/Users/KevinMoran/Dropbox/Documents/My_Graduate_School_Work/SEMERU/git_src_code/gitlab-code/ReDraw/modelDir/frozen_inception_v3.pb"; //"C:\\Users\\Andrew\\Downloads\\ReDraw-Cropped-Fine-Tuning";
//        String pathToKnnData = "C:\\ReDraw\\knnScreens\\container-neighborhood.mat";
        String pathToKnnData = "/Users/KevinMoran/Desktop/ReDraw-Tiers/tiers";
              String pathTesseract = "C:\\ReDraw\\tesseract-win";
        String useKnnAlgo = "true";
        String absolutePositioning = "true";
        String useREMAUI = "false";
        String labelFile = "/Users/KevinMoran/Dropbox/Documents/My_Graduate_School_Work/SEMERU/git_src_code/gitlab-code/ReDraw/modelDir/labels.txt";
        String scriptPath = "/Users/KevinMoran/Dropbox/Documents/My_Graduate_School_Work/SEMERU/git_src_code/gitlab-code/ReDraw/python/redrawClassify.py";
        String inputLayer = "input:0";
        String outputLayer = "InceptionV3/Predictions/Reshape_1:0";
        String mamaFolder = "/Users/KevinMoran/Desktop/Test-ReDraw-Input";
        String MamaOut = "/Users/KevinMoran/Desktop/Test-Output/Final-Output";


		File directory = new File(mamaFolder);
		File[] subdirs = directory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);

		ArrayList<String> parameter= new ArrayList<String>();
		parameter.add(pathToKnnData);
		parameter.add(pathToNet);
		parameter.add(pathToAndroidSDK);
		parameter.add(outputLocation);
		parameter.add(absolutePositioning);
		parameter.add(pathTesseract);
		parameter.add(labelFile);
		parameter.add(scriptPath);
		parameter.add(inputLayer);
		parameter.add(outputLayer);
		parameter.add(useREMAUI);
		parameter.add(useKnnAlgo);
		for (File dir : subdirs) {
			
			int count = 1;
			
			System.out.println("Directory: " + dir.getAbsolutePath());
			File folder = new File(dir.getAbsolutePath());
			File[] listOfFiles = folder.listFiles();
			
			String[] parameters = new String[1] ;
			for(int i = 0; i < listOfFiles.length; i++){
				String filename = listOfFiles[i].getAbsolutePath();
				if(filename.endsWith(".xml")||filename.endsWith(".XML"))
				{
					


					FileUtils.cleanDirectory(new File(outputLocation)); 
					outputDir = MamaOut + File.separator + dir.getName()+ File.separator + count;
					sketchFile = filename;
					imageFile = filename.replace(".xml", ".png");
				
					
					System.out.println(imageFile);
					System.out.println(sketchFile);
					
					 parameters = new String[] {  pathToKnnData, pathToNet, pathToAndroidSDK, outputLocation, 
			        		 absolutePositioning, pathTesseract, labelFile,scriptPath,inputLayer,
			        		 outputLayer, useREMAUI, useKnnAlgo,imageFile, sketchFile, outputDir };
					 parameter.add(imageFile);
					 parameter.add(sketchFile);
					 parameter.add(outputDir);

					
					System.out.println("Completed");
					count+=1;
				}
			}
		}
			        try {
			            GeneratorWrapper.main(Arrays.copyOf(parameter.toArray(),  parameter.size(), 
	                             String[].class));

			        } catch (EngineException | IllegalArgumentException | IllegalStateException | InterruptedException e) {
			            e.printStackTrace();
			        }
    
			
		
    }
}

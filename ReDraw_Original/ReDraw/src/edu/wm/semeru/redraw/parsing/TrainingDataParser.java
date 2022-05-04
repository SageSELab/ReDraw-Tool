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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.semeru.android.core.helpers.ui.UiAutomatorBridge;
import edu.semeru.android.core.model.DynGuiComponentVO;
import edu.wm.cs.semeru.core.helpers.ScreenshotModifier;
import edu.wm.cs.semeru.core.helpers.TerminalHelper;
import edu.wm.semeru.redraw.helpers.ImageHelper;


/**
 * The purpose of this class is to take the preliminary app data that is stored
 * in a particular directory, parse the xml files found within it for instances 
 * of textViews, and then output a .mat file containing relevant screenshot paths
 * and the bounding boxes of each textView so that we can fine-tune our pre-trained Neural
 * Net using transfer learning
 * @author Kevin Moran
 *
 */

public class TrainingDataParser {

	private static int widthScreen = 1200;  //Default Value for Nexus 7
	private static int heightScreen = 1920;	 //Default Value for Nexus 7
	private static int ctr;
	private static ArrayList<String> components;
	private static ArrayList<DynGuiComponentVO> componentList;

	public static void main(String[] args) {

		//perturbTrainingDataColors(args[0], args[1], args[2], Integer.parseInt(args[3]));
		partitionObjectDetectionTrainingData(args[0], args[1]);
		
	}

	public static void cropMultipleTrainingDataImages(String googlePlayData, String syntheticData, String outputDirectory, boolean leafNodes){

		String[] exts = {"png"}; //File Extensions to search for 

		File inputDir = new File(googlePlayData);

		//Use Apache FileUtils to find all screenshots
		Collection<File> screenshotList = FileUtils.listFiles(inputDir, exts, true);

		components = new ArrayList<String>();

		String uiAutomatorFile = "";
		String uiAutomatorExt = "";

		//Iterate through all found Screenshots
		for(File currFile: screenshotList){

			System.out.println(currFile.getAbsolutePath());

			if(!currFile.getAbsolutePath().substring(currFile.getAbsolutePath().lastIndexOf("/")+1,currFile.toString().length()).startsWith(".")){ // Ignore Hidden Files

				//Get base File path
				uiAutomatorFile = currFile.getAbsolutePath().substring(0,currFile.getAbsolutePath().lastIndexOf("/"));

				System.out.println("Processing " + currFile.getAbsolutePath() + " ...");

				//Get number of screenshot
				uiAutomatorExt = currFile.getAbsolutePath().substring(currFile.getAbsolutePath().lastIndexOf("_"));

				// Append number and replace File extension
				uiAutomatorFile = uiAutomatorFile + File.separator + "hierarchy" + uiAutomatorExt.replaceAll(".png", ".xml");

				//Crop out all components from Larger Screenshot.
				cropScreenshotComponents(uiAutomatorFile, currFile.getAbsolutePath(), outputDirectory, leafNodes, false);

			}

		}

		// Folder for Synthetic Data
		inputDir = new File(syntheticData);

		// Use Apache FileUtils to find all screenshots
		screenshotList = FileUtils.listFiles(inputDir, exts, true);

		// Iterate through all found Screenshots
		for (File currFile : screenshotList) {

			if (!currFile.getAbsolutePath()
					.substring(currFile.getAbsolutePath().lastIndexOf("/") + 1, currFile.toString().length())
					.startsWith(".")) { // Ignore Hidden Files

				// Get base File path
				uiAutomatorFile = currFile.getAbsolutePath().substring(0,
						currFile.getAbsolutePath().lastIndexOf("/"));

				System.out.println("Processing " + currFile.getAbsolutePath() + " ...");

				// Get number of screenshot
				uiAutomatorExt = currFile.getAbsolutePath().substring(currFile.getAbsolutePath().lastIndexOf("_"));

				// Append number and replace File extension
				uiAutomatorFile = uiAutomatorFile + File.separator + "hierarchy"
						+ uiAutomatorExt.replaceAll(".png", ".xml");

				// Crop out all components from Larger Screenshot.
				cropScreenshotComponents(uiAutomatorFile, currFile.getAbsolutePath(), outputDirectory,
						leafNodes, true);

			}

		}

		System.out.println("Finished Dataset Processing!");
		System.out.println("Component Type Report: ");

		Multiset<String> set = HashMultiset.create(components);

		for (Multiset.Entry<String> entry : set.entrySet()) {
			System.out.println(entry.getElement() + ": " + entry.getCount());
		}

		System.out.println("Total # of Components: " + ctr);

	}

	public static File[] screenshotFinder(String dirName){
		File dir = new File(dirName);



		return dir.listFiles(new FilenameFilter() { 
			public boolean accept(File dir, String filename)
			{ return filename.endsWith(".png"); }
		} );

	}

	/**
	 * 
	 * Description:  Takes as input a screenshot and a uiautomator xml file specifying the 
	 * screen hierarchy, and crops out all components into individual images of each identified screenshot.
	 * If the leafNodes is set to true, it will only parse out leaf nodes.
	 * 
	 * @param pathToUIAutomatorFile
	 * @param pathToDeviceScreenshot
	 * @param outputPath
	 * @param leafNodes
	 */
	public static void cropScreenshotComponents(String pathToUIAutomatorFile, String pathToDeviceScreenshot, String outputPath, boolean leafNodes, boolean syntheticData){

		//This class uses UiAutomatorBridge, without instantiating the device parameters since most of the 
		//code we need for parsing the uiautomator files are there.

		UiAutomatorBridge uiBridge = new UiAutomatorBridge(null, 0);
		uiBridge.updateTreeFromFile(pathToUIAutomatorFile);

		//Parse the UI-Hierarchy into an ArrayList of components

		ArrayList<DynGuiComponentVO> uiComponentList = new ArrayList<>();
		ArrayList<DynGuiComponentVO> allUiComponentList = new ArrayList<>();
		ArrayList<DynGuiComponentVO> tempUiComponentList = new ArrayList<>();



		if(leafNodes){

			//Get all leaf nodes
			tempUiComponentList = uiBridge.getLeafComponentsNoDevice(widthScreen, heightScreen);
			//Set new array to avoid concurrent modification
			uiComponentList.addAll(tempUiComponentList);

			//The following commented out code is for parsing number pickers from the synthetic components

			//			//Get all components
			//			DynGuiComponentVO rootComponent = uiBridge.getScreenInfoHierarchyNoDevice(new StringBuilder(), widthScreen, heightScreen);
			//			componentList = new ArrayList<DynGuiComponentVO>();
			//			buildComponentList(rootComponent, syntheticData);
			//			allUiComponentList = componentList;
			//			
			//			//Remove NumberPickers (or other components)
			//			for(DynGuiComponentVO tempComp: tempUiComponentList) {
			//				if(tempComp.getName().equals("android.widget.NumberPicker") || tempComp.getName().equals("android.widget.Button") || tempComp.getName().equals("android.widget.EditText")) {
			//					uiComponentList.remove(tempComp);
			//				}
			//			}
			//			
			//			//Add Number Pickers from all components
			//			for(DynGuiComponentVO tempComp: allUiComponentList) {
			//				if(tempComp.getName().equals("android.widget.NumberPicker")) {
			//					uiComponentList.add(tempComp);
			//				}
			//			}

		}else{
			DynGuiComponentVO rootComponent = uiBridge.getScreenInfoHierarchyNoDevice(new StringBuilder(), widthScreen, heightScreen);
			componentList = new ArrayList<DynGuiComponentVO>();
			buildComponentList(rootComponent, syntheticData);
			uiComponentList = componentList;
		}


		for(DynGuiComponentVO currComponent:uiComponentList){

			// Here we get the first child of the current component to do a check in order to 
			// discard components of the same size below
			DynGuiComponentVO firstChild = null;
			if (currComponent.getChildren().size() != 0){
				firstChild = currComponent.getChildren().get(0);
			}

			// This conditional is for discarding the extraneous components in the synthetic examples

			if ((syntheticData
					// ImageView
					&& ((currComponent.getPositionX() == 1104 && currComponent.getPositionY() == 66
					&& currComponent.getWidth() == 80 && currComponent.getHeight() == 96)
							// TextView
							|| (currComponent.getPositionX() == 32 && currComponent.getPositionY() == 87)
							// ImageButton
							|| (currComponent.getPositionX() == 1032 && currComponent.getPositionY() == 1644
							&& currComponent.getWidth() == 160 && currComponent.getHeight() == 184)))) {
				continue;
			}

			//Here we need to check for four main cases:
			// 1) We need to be sure that the component has a class assigned by uiautomator
			// 2) We need to be sure that if either the height or width of the component are zero, we discard it
			// 3) We need to be sure that if any of the positioning parameters are negative (e.g., x,y,width,height) we discard it
			// 4) We need to be sure that if the (x+width) value or (y+height) value exceed the dimensions of the screen, we discard it

			if((currComponent.getName() !=null && !currComponent.getName().isEmpty()) && !(currComponent.getWidth() == 0 || currComponent.getHeight() == 0)  && 
					!(currComponent.getPositionX() < 0 || currComponent.getPositionY() < 0 || currComponent.getWidth() < 0 || currComponent.getHeight() < 0) && 
					!(currComponent.getPositionX() + currComponent.getWidth() > 1200 || currComponent.getPositionY() + currComponent.getHeight() > 1920)){

				//If the component only has one child and that child is the same size as the component, we don't consider this component
				// when we train the net
				if(firstChild != null){
					if(currComponent.getChildren().size() == 1 && currComponent.getPositionX() == firstChild.getPositionX() 
							&& currComponent.getPositionY() == firstChild.getPositionY() && currComponent.getWidth() == firstChild.getWidth()
							&& currComponent.getHeight() == firstChild.getHeight()){
						continue;
					}
				}
				components.add(currComponent.getName());

				ScreenshotModifier.cropScreenshot(pathToDeviceScreenshot,outputPath + File.separator + ctr + "-" + currComponent.getName() + ".png",currComponent.getPositionX(),
						currComponent.getPositionY(),currComponent.getHeight(),currComponent.getWidth());

				ctr ++;
			}
		}

	}//End cropScreenshotComponents()

	public static void buildComponentList(DynGuiComponentVO root, boolean syntheticData){
		if(root.getChildren().size() != 0){
			if((!syntheticData)||( syntheticData && !(root.getPositionX() == 0 && root.getPositionY() == 50 && root.getWidth() == widthScreen && root.getHeight() == 128) && !(root.getPositionX() == 1032 && root.getPositionY() == 1644 && root.getWidth() == 160 && root.getHeight() == 184) && !(root.getPositionX() == 32 && root.getPositionY() == 87))){
				for (DynGuiComponentVO child : root.getChildren()){
					buildComponentList(child, syntheticData);
				}
			}
		}
		if((!syntheticData)||( syntheticData && !(root.getPositionX() == 0 && root.getPositionY() == 50 && root.getWidth() == widthScreen && root.getHeight() == 128) && !(root.getPositionX() == 1032 && root.getPositionY() == 1644 && root.getWidth() == 160 && root.getHeight() == 184) && !(root.getPositionX() == 32 && root.getPositionY() == 87))){
			componentList.add(root);
		}
	}

	/**
	 * Description: Takes as input a path to cropped training data screenshots and produces a prescribed
	 * number of color perturbed images in the output folder. It will randomly select components if there
	 * are more available components than the number to be generated, otherwise it will randomly iterate
	 * through available components.
	 * 
	 * @param pathToCroppedComponents
	 * @param outputPath
	 * @param componentType
	 * @param numberofImages
	 */
	public static void perturbTrainingDataColors(String pathToCroppedComponents, String outputPath, String componentType, int numberofImages){

		String[] exts = {"png"}; //File Extensions to search for 

		File inputDir = new File(pathToCroppedComponents);

		//Use Apache FileUtils to find all screenshots in target directory
		Collection<File> fullScreenshotList = FileUtils.listFiles(inputDir, exts, true);
		ArrayList<File> targetScreenshots = new ArrayList<File>();


		//Derive a list of screenshots matching the target component type		
		for(File currFile: fullScreenshotList) {
			if(currFile.getAbsolutePath().contains(componentType)) {
				targetScreenshots.add(currFile);
			}
		}

		System.out.println("Generating Color Perturbed Images, Please Wait...");

		if(targetScreenshots.size() >= numberofImages) {
			Collections.shuffle(targetScreenshots);
			for(int i = 1; i <= numberofImages; i++) {
				System.out.println(targetScreenshots.get(i).getAbsolutePath() + " " + outputPath + File.separator + i + "-" + componentType);
				ImageHelper.changeImageColor(targetScreenshots.get(i-1).getAbsolutePath(), outputPath + File.separator + i + "-" + componentType + ".png");
			}
		}else {
			for(int i = 1; i <= numberofImages; i++) {
				Collections.shuffle(targetScreenshots);
				System.out.println("test!");
				ImageHelper.changeImageColor(targetScreenshots.get(0).getAbsolutePath(), outputPath + File.separator + i + "-" + componentType + ".png");
			}
		}

		System.out.println("Image Perturbation has completed for "+ numberofImages+"! Please check the output directory");

	}

	/**
	 * Description: This method converts UiAutomator xml files into annotation xml files 
	 * in the same form as the PASCAL-VOC annotation files so that they can be trained with 
	 * DarkFlow.
	 * 
	 * @param pathToUIAutomatorFile
	 * @param pathToDeviceScreenshot
	 * @param datasetFolder
	 * @param outputXMLPath
	 * @param count
	 */
	public static void convertUIAutomatorXMLFile(String pathToUIAutomatorFile, String pathToDeviceScreenshot, String datasetFolder, String outputXMLPath, String count){

		pathToDeviceScreenshot = pathToDeviceScreenshot.substring(pathToDeviceScreenshot.lastIndexOf('/'));

		// This class uses UiAutomatorBridge, without instantiating the device parameters since most of the 
		// code we need for parsing the uiautomator files are there.

		UiAutomatorBridge uiBridge = new UiAutomatorBridge(null, 0);
		uiBridge.updateTreeFromFile(pathToUIAutomatorFile);

		//Parse the UI-Hierarchy into an ArrayList of components

		ArrayList<DynGuiComponentVO> uiComponentList = new ArrayList<>();
		//uiComponentList = uiBridge.getScreenInfoNoDevice(widthScreen, heightScreen, true, false);
		uiComponentList = uiBridge.getLeafComponentsNoDevice(widthScreen, heightScreen);

		// Set up the xml document and write header information

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document screenAnnotationXML = docBuilder.newDocument();

			Element rootElement = screenAnnotationXML.createElement("annotation");
			screenAnnotationXML.appendChild(rootElement);

			Element folder = screenAnnotationXML.createElement("folder");
			folder.appendChild(screenAnnotationXML.createTextNode(datasetFolder));
			rootElement.appendChild(folder);

			Element filename = screenAnnotationXML.createElement("filename");
			filename.appendChild(screenAnnotationXML.createTextNode(count + ".jpg"));
			rootElement.appendChild(filename);

			//Write Database source information

			Element source = screenAnnotationXML.createElement("source");
			rootElement.appendChild(source);

			Element database = screenAnnotationXML.createElement("database");
			database.appendChild(screenAnnotationXML.createTextNode("SEMERU Android GUI Database"));
			source.appendChild(database);

			Element annotation = screenAnnotationXML.createElement("annotation");
			annotation.appendChild(screenAnnotationXML.createTextNode("SEMERU Lab"));
			source.appendChild(annotation);

			Element image = screenAnnotationXML.createElement("image");
			image.appendChild(screenAnnotationXML.createTextNode("Nexus7screenshot"));
			source.appendChild(image);

			//Write Owner Information

			Element owner = screenAnnotationXML.createElement("owner");
			rootElement.appendChild(owner);

			Element name = screenAnnotationXML.createElement("name");
			name.appendChild(screenAnnotationXML.createTextNode("SEMERU"));
			owner.appendChild(name);

			// Write Image Soze information

			Element size = screenAnnotationXML.createElement("size");
			rootElement.appendChild(size);

			Element width = screenAnnotationXML.createElement("width");
			width.appendChild(screenAnnotationXML.createTextNode(Integer.toString(widthScreen)));
			size.appendChild(width);

			Element height = screenAnnotationXML.createElement("height");
			height.appendChild(screenAnnotationXML.createTextNode(Integer.toString(heightScreen)));
			size.appendChild(height);

			Element depth = screenAnnotationXML.createElement("depth");
			depth.appendChild(screenAnnotationXML.createTextNode("3"));
			size.appendChild(depth);

			Element segmented = screenAnnotationXML.createElement("segmented");
			segmented.appendChild(screenAnnotationXML.createTextNode("0"));
			rootElement.appendChild(segmented);



			for(DynGuiComponentVO currComponent:uiComponentList){

				if((currComponent.getName() !=null && !currComponent.getName().isEmpty()) && !(currComponent.getPositionX() == 0 && currComponent.getPositionY() == 0 && currComponent.getWidth() == 0 && currComponent.getHeight() == 0)){

					Element object = screenAnnotationXML.createElement("object");
					rootElement.appendChild(object);

					Element objName = screenAnnotationXML.createElement("name");
					objName.appendChild(screenAnnotationXML.createTextNode(currComponent.getName()));
					object.appendChild(objName);

					Element pose = screenAnnotationXML.createElement("pose");
					pose.appendChild(screenAnnotationXML.createTextNode("unspecified"));
					object.appendChild(pose);

					Element truncated = screenAnnotationXML.createElement("truncated");
					truncated.appendChild(screenAnnotationXML.createTextNode("0"));
					object.appendChild(truncated);

					Element difficult = screenAnnotationXML.createElement("difficult");
					difficult.appendChild(screenAnnotationXML.createTextNode("0"));
					object.appendChild(difficult);

					Element bndbox = screenAnnotationXML.createElement("bndbox");
					object.appendChild(bndbox);

					Element xmin = screenAnnotationXML.createElement("xmin");
					xmin.appendChild(screenAnnotationXML.createTextNode(Integer.toString(currComponent.getPositionX())));
					bndbox.appendChild(xmin);

					Element ymin = screenAnnotationXML.createElement("ymin");
					ymin.appendChild(screenAnnotationXML.createTextNode(Integer.toString(currComponent.getPositionY())));
					bndbox.appendChild(ymin);

					Element xmax = screenAnnotationXML.createElement("xmax");
					xmax.appendChild(screenAnnotationXML.createTextNode(Integer.toString(currComponent.getPositionX()+currComponent.getWidth())));
					bndbox.appendChild(xmax);

					Element ymax = screenAnnotationXML.createElement("ymax");
					ymax.appendChild(screenAnnotationXML.createTextNode(Integer.toString(currComponent.getPositionY()+currComponent.getHeight())));
					bndbox.appendChild(ymax);

				}

			}// End for loop to iterate through components

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource sourceXML = new DOMSource(screenAnnotationXML);
			StreamResult result = new StreamResult(new File(outputXMLPath));

			// Output to console for testing
			//result = new StreamResult(System.out);

			transformer.transform(sourceXML, result);


		} catch (ParserConfigurationException e) {
			System.err.println("There was an error setting up the XML parser!");
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			System.err.println("There was an error configuring the xml Transformer!");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			System.err.println("There was an error performing the XML transformation!");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}// End convertUIAutomatorXMLFile()

	/**
	 * Description: This method will take training data for an RCNN Object detector and partition it 
	 * in 75% Training Data, 15% Validation Data, and 10% Test Data.  The partitioning proceeds by
	 * getting a list of all screens (essentially <png,xml> tuples) shuffling the list and then
	 * copying the data into three different directories labeled Training, Validation, and Test.
	 * 
	 * @param trainingDataDirectory - The directory holding the training data screenshots 
	 * @param outputDirectory
	 */
	public static void partitionObjectDetectionTrainingData(String trainingDataDirectory, String outputDirectory) {
		
		String[] exts = {"png"}; //File Extensions to search for 

		File inputDir = new File(trainingDataDirectory);

		//Use Apache FileUtils to find all screenshots
		Collection<File> screenshotCollection = FileUtils.listFiles(inputDir, exts, true);
		ArrayList<File> screenshotList = new ArrayList<File>();
		
		// Convert File colleciton into list for easy shuffling
		for(File currFile: screenshotCollection) {
			screenshotList.add(currFile);
		}
		
		Collections.shuffle(screenshotList);
		
		int trainingDataSize = (int) (screenshotList.size() * 0.75);
		int validationDataSize = (int) (screenshotList.size() * 0.15);
		int testDataSize = (int) (screenshotList.size() * 0.10);
		
		File finalTrainingDataDirectory = new File(outputDirectory + File.separator + "Training");
		File validationDataDirectory = new File(outputDirectory + File.separator + "Validation");
		File testDataDirectory = new File(outputDirectory + File.separator + "Test");
		
		finalTrainingDataDirectory.mkdirs();
		validationDataDirectory.mkdirs();
		testDataDirectory.mkdirs();
		
		String currSS = "";
		String currHierarchy = "";
		
		//Create Training Data
		System.out.println("Creating Training Data, Please Wait...");
		for (int i = 0; i < trainingDataSize; i++) {
			
			currSS = screenshotList.get(i).getAbsolutePath();
			currHierarchy = currSS.replaceAll(".png", ".xml"); 
			if(currSS.contains("screenshot")) {
			currHierarchy = currHierarchy.replaceAll("screenshot_", "hierarchy_");
			}else {
				currHierarchy = currHierarchy.replaceAll("screen_", "hierarchy_");
			}
			
			String copySSCommand = "cp " + currSS + " " + finalTrainingDataDirectory + File.separator + i + ".png";
			System.out.println(copySSCommand);
			String copyXMLCommand = "cp " + currHierarchy + " " + finalTrainingDataDirectory + File.separator + i + ".xml";
			System.out.println(copyXMLCommand);
			
			TerminalHelper.executeCommand(copySSCommand);
			TerminalHelper.executeCommand(copyXMLCommand);
			
		}
		
		System.out.println("Creating Validation Data, Please Wait...");
		for (int i = trainingDataSize; i < trainingDataSize+validationDataSize; i++) {
			
			currSS = screenshotList.get(i).getAbsolutePath();
			currHierarchy = currSS.replaceAll(".png", ".xml"); 
			if(currSS.contains("screenshot")) {
			currHierarchy = currHierarchy.replaceAll("screenshot_", "hierarchy_");
			}else {
				currHierarchy = currHierarchy.replaceAll("screen_", "hierarchy_");
			}
			
			String copySSCommand = "cp " + currSS + " " + validationDataDirectory+ File.separator + i + ".png";
			String copyXMLCommand = "cp " + currHierarchy + " " + validationDataDirectory + File.separator + i + ".xml";
			System.out.println(copySSCommand);
			System.out.println(copyXMLCommand);
			
			TerminalHelper.executeCommand(copySSCommand);
			TerminalHelper.executeCommand(copyXMLCommand);
			
		}
		
		System.out.println("Creating Test Data, Please Wait...");
		for (int i = trainingDataSize+validationDataSize; i < trainingDataSize+validationDataSize+testDataSize; i++) {
			
			currSS = screenshotList.get(i).getAbsolutePath();
			currHierarchy = currSS.replaceAll(".png", ".xml"); 
			if(currSS.contains("screenshot")) {
			currHierarchy = currHierarchy.replaceAll("screenshot_", "hierarchy_");
			}else {
				currHierarchy = currHierarchy.replaceAll("screen_", "hierarchy_");
			}
			
			String copySSCommand = "cp " + currSS + " " + testDataDirectory+ File.separator + i + ".png";
			String copyXMLCommand = "cp " + currHierarchy + " " + testDataDirectory + File.separator + i + ".xml";
			System.out.println(copySSCommand);
			System.out.println(copyXMLCommand);
			
			TerminalHelper.executeCommand(copySSCommand);
			TerminalHelper.executeCommand(copyXMLCommand);
			
		}
		
	}
	
}

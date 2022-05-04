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

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.json.JSONObject;

import com.android.uiautomator.tree.AttributePair;
import com.redraw.uiautomator.tree.BasicTreeNode;
import com.redraw.uiautomator.tree.RootUINode;
import com.mathworks.engine.MatlabEngine;
import com.mathworks.engine.MatlabExecutionException;
import com.mathworks.engine.MatlabSyntaxException;

import cs435.guiproto.AGPMain;
import edu.semeru.redraw.knn.KnnBuilder;
import edu.wm.cs.semeru.redraw.ConstantSettings;
import edu.wm.cs.semeru.redraw.JSNode;
import edu.wm.cs.semeru.redraw.MarketchJSParser;
//import edu.semeru.android.guichecker.ui.UIDumpParser;
import com.redraw.uiautomator.tree.UiTreeNode;
import edu.wm.cs.semeru.redraw.REMAUI;
import edu.wm.cs.semeru.redraw.ViewHierarchy;
import edu.wm.cs.semeru.redraw.helpers.ImagesHelper;
import edu.wm.semeru.redraw.helpers.TensorHelper;
import edu.wm.cs.semeru.redraw.ocr.OCRImageParser;
//import org.opencv.core.Core;

/**
 * The entry point to the classifer/app generator. Acts as an adapter so that
 * either one of our two approaches work with our software. makes calls to
 * MATLAB to classify/find bounding boxes and then makes call to the app
 * generator to output the .apk file.
 * 
 * @author mjcurcio
 *
 */
public class GeneratorWrapper {

	//set this boolean to true if you want to use the CPU instead of the GPU for
	// detection/classification
	public static boolean USE_CPU = false;

	private static final int WIDTH = 1200;  
	private static final int HEIGHT = 1920;  
	public static String pathToNet;
	public static String pathToWorkingDir;
	public static String pathToKnnData;
	public static String pathToAndroidSDK;
	public static String outputLocation;
	public static String pathToPng;
	public static String nameOfMatlabInstance;
	public static String sourceCodeLocation;
	public static String useAbsolutePositioning;
	public static boolean useUIXFile;
	public static boolean useREMAUI;
	public static boolean existingInstance;
	public static String trimmedIm;
	public static String labelFile;
	public static String scriptPath;
	public static String inputLayerName;
	public static String outputLayerName;
	public static String pythonCommand;
	//X,Y,WIDTH,HEIGHT
	public static int[] CONTENT_VIEW_DIMS = {0, 72, 1200, 1704 };

	/**
	 * FIRST PARAMETER: path to the png file SECOND PARAMETER: path to KNN data.
	 * THIRD PARAMETER: path to .mat file containing either the detector or
	 * classifier (it is assumed that it is called rcnn in the case of the
	 * detector) FOURTH PARAMETER: path to the imported photo editing software
	 * (if applicable)
	 * 
	 * @param args
	 * @throws InterruptedException
	 * @throws IllegalStateException
	 * @throws IllegalArgumentException
	 * @throws ExecutionException 
	 * @throws MatlabSyntaxException 
	 * @throws MatlabExecutionException 
	 * @throws IOException 
	 */
	public static void main(String[] args)
			throws IllegalArgumentException, IllegalStateException, InterruptedException, MatlabExecutionException, MatlabSyntaxException, ExecutionException, IOException {

		//    	List<JSNode> containers = new ArrayList<JSNode>();
		//    	List<UiTreeNode> components = new ArrayList<UiTreeNode>();
		//    	HashMap<String, JSNode> info = parseSketchFile("/Users/KevinMoran/Dropbox/Documents/My_Graduate_School_Work/SEMERU/git_src_code/gitlab-code/ReDraw/Sketch-Examples/com.yelp.android/yelp-1200x1920.png", "/Users/KevinMoran/Dropbox/Documents/My_Graduate_School_Work/SEMERU/git_src_code/gitlab-code/ReDraw/Sketch-Examples/com.yelp.android/marketch-1200x1920/data.js", containers, "/Users/KevinMoran/Desktop/Test-Sketch-Files/");

		/*nameOfMatlabInstance = args[7];
		if (nameOfMatlabInstance.equalsIgnoreCase("false")){
			existingInstance = false;
		}
		else{
			existingInstance = true;
		}*/
		String json = "";
		System.out.println("Started Redraw");
		try
		{
			
		json = new String(Files.readAllBytes(Paths.get(args[0])));
		}
		catch(  IllegalArgumentException e)
		{
			System.out.println("Exception Reading Arguments " + e.getMessage());
		}
		
		JSONObject obj = new JSONObject(json);
        pathToNet = obj.getJSONObject("config").getString("PathToNet");
        pathToKnnData =  obj.getJSONObject("config").getString("PathToKnnData");
        String FinalOutputDirectory =  obj.getJSONObject("config").getString("FinalOutputDirectory");
        String inputFolder =  obj.getJSONObject("config").getString("inputFolder");
        pathToAndroidSDK =  obj.getJSONObject("config").getString("pathToAndroidSDK");
        String pathTesseract =obj.getJSONObject("config").getString("pathToTesseract");
        outputLocation = obj.getJSONObject("config").getString("PreliminaryOutputDirectory");
        labelFile =  obj.getJSONObject("config").getString("labelFile");
        scriptPath =  obj.getJSONObject("config").getString("scriptPath");
        inputLayerName =  obj.getJSONObject("config").getString("inputLayer");
        outputLayerName =  obj.getJSONObject("config").getString("outputLayer");
        useAbsolutePositioning = obj.getJSONObject("config").getString("useAbsolutePositioning");
        useREMAUI = obj.getJSONObject("config").getString("useREMAUI").equals("true");
        pythonCommand = obj.getJSONObject("config").getString("pythonCommand");
		
		
		
		
		ArrayList<String> imagePaths = new ArrayList<String>();
		ArrayList<String> sketchPaths = new ArrayList<String>();
		ArrayList<String> OutPaths = new ArrayList<String>();
		
		
		File directory = new File(inputFolder);
		File[] subdirs = directory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);

		for (File dir : subdirs) {
			
			int count = 1;
			System.out.println("Directory: " + dir.getAbsolutePath());
			File folder = new File(dir.getAbsolutePath());
			File[] listOfFiles = folder.listFiles();
			
			for(int i = 0; i < listOfFiles.length; i++){
				String filename = listOfFiles[i].getAbsolutePath();
				if(filename.endsWith(".xml")||filename.endsWith(".XML"))
				{
					OutPaths.add( FinalOutputDirectory + File.separator+ dir.getName()+ File.separator + count);
					sketchPaths.add(filename);
					imagePaths.add(filename.replace(".xml", ".png"));
				}
			}
		
		}
		
		
		

		pathToWorkingDir = (new File(pathToNet)).getParentFile().getAbsolutePath();

		GenerateRedrawTotal(pathTesseract,imagePaths,sketchPaths,OutPaths);			
		
	}

	public static void GenerateNoObjectDetection(String screenshot, String sketchFile, String pathTesseract, boolean useKnnLayoutGrouping)
			throws IllegalArgumentException, IllegalStateException, InterruptedException, MatlabExecutionException, MatlabSyntaxException, ExecutionException, IOException {

		
		
		//Use the tensor helper to load the checkpoint
		
		
		
		
		if (existingInstance){
		//	eng = MatlabEngine.connectMatlab(nameOfMatlabInstance);
		//	System.out.println("successfully connected to running matlab instance");
		}
		else{
			//if there is no instance, we have to load up the data (could take awhile)
		//	eng = MatlabEngine.startMatlab();
			// load up our workspace containing the detector, set it to a
			// variable called "workspace"
		//	command = "workspace = load('" + pathToNet + "');";
		//	eng.eval(command);
			// set the object detector to a variable in the workspace
		//	command = "classifier = workspace.classifier;";
		//	eng.eval(command);

		//	command = "cd " + pathToWorkingDir;
		//	eng.eval(command);

			if(useKnnLayoutGrouping){
		//		eng.eval(command);
			}
		}

		List<JSNode> containers = new ArrayList<JSNode>();
		List<UiTreeNode> components = new ArrayList<UiTreeNode>();
		HashMap<String, JSNode> info = new HashMap<String, JSNode>();
		if (useREMAUI){
			ArrayList<JSNode> lst = getLeafNodesREMAUI(pathToPng, outputLocation);
			String remauiOutput = outputLocation + File.separator + "toClassify" + File.separator;
			new File(remauiOutput).getAbsoluteFile().mkdirs();
			int count = 0;
			for (JSNode cur: lst){
				String im = remauiOutput + "img-" + count + ".png";
				ImagesHelper.cropImageAndSave(trimmedIm, im, cur.getX(), cur.getY(), cur.getWidth(), cur.getHeight());
				info.put(im, cur);
				count++;
			}
		}else{
			UIDumpParser loader = new UIDumpParser();
			//note that in this scenario, sketchfile is actually xml file
			RootUINode tree = loader.parseXml(sketchFile);

			List<UiTreeNode> nodes = tree.getLeafNodes();
			
			String outputLoc = outputLocation + File.separator + "toClassify" + File.pathSeparator;
			int count = 0;
			
			for(int i = 0; i < nodes.size(); i++)
			{
				JSNode curjs = new JSNode(nodes.get(i).getX(), nodes.get(i).getY(), nodes.get(i).getWidth(), nodes.get(i).getHeight());
				String im = outputLoc + "img-" + count + ".png";
				ImagesHelper.cropImageAndSave(pathToPng, im, curjs.getX(), curjs.getY(), curjs.getWidth(), curjs.getHeight());
				//info.put(im, curjs);
				components = tree.getLeafNodes();
				count++;
				
			}
			
		}
//		else{
//			info = parseSketchFile(screenshot, sketchFile, containers, outputLocation);
//		}
		OCRImageParser ocrParser = new OCRImageParser(pathTesseract);
		String text = null;

		// 1. For loop to iterate images
		// 2. Crop images to classify the component
		// 3. Generate xml

		
		
		try {
			// Change working directory
		//	command = "cd " + pathToWorkingDir;
			int index = 1;

			//for debugging
			for (Entry<String, JSNode> item: info.entrySet()){
				JSNode node = item.getValue();
				System.out.println("x: " + node.getX() +  " y: " + node.getY() + " width: " + node.getWidth() + " height: " + node.getHeight() + " image: " + item.getKey());
			}
			if(useREMAUI)
			{
				
			for (Entry<String, JSNode> item : info.entrySet()) {
				JSNode node = item.getValue();
				// load and read the screenshot
			//	command = "image = imread('" + item.getKey() + "');";
				// resize screenshot!
				//command = "image = imresize(image, [128,128]);";
				// detect objects in the image and record bounding box and
				// labels
//				command = "[pred, conf] = classify(classifier, image);";
				// Change working directory
				// Get label for the classification
//				command = "label = code2name(str2double(cellstr(pred)), false);";
				String label="";// = eng.getVariable("label");
				if (label.toLowerCase().contains("text")||label.toLowerCase().contains("button")) {
					try{
						text = ocrParser.parseImage(item.getKey(),true);
					}
					catch (NullPointerException e){
						System.out.println("WARNING: failed to parse image " + item.getKey() + ", continuing...");
					}
					text = (text == null ? "" : text);
					//                    int ndx = text.lastIndexOf(":");
					//                    text = text.substring(0, ndx);
				} else {
					text = "";
				}
				System.out.println("Text: " + text);
				System.out.println("Label: " + label);
				// Add data to bboxes and labels
				text = text.replace("&", "&amp");
				text = text.replace("'", "&apos");
				text = text.replace("\"", "&quot");
//				command = "text = \"" + text + "\";";
				
				//eng.eval(command);

				UiTreeNode comp = new UiTreeNode(node.getX(), node.getY(), node.getWidth(), node.getHeight());
				comp.addAtrribute("class", "android.widget." + label);
				comp.addAtrribute("text", text);
				comp.setName("component");
				components.add(comp);

				// Add data to bboxes and labels
	//			command = "bboxes(" + index + ",:) = [" + node.getX() + "," + node.getY() + "," + node.getWidth() + "," + node.getHeight() + "];";
				//eng.eval(command);
		//		command = "labels(" + index + ",:) = [label, text];";
				//eng.eval(command);
				//                System.out.println("labels(" + index + ") = '"+eng.getVariable("label")+"';");
				System.out.println("Processing component " + index + ":");
				index++;
			}

			}
			//I believe that the components variable is the one needed for KNNBuilder

			File uixFile = new File(outputLocation + File.separator + "generatedApp.xml");

			
			if(useKnnLayoutGrouping){
				// take the data just obtained, utilize KNN algorithm to set up the
				// hierarchy, finally call app generator
//				command = "knnInput = formatDetectorOutput(bboxes, labels);";
				//eng.eval(command);
	//			command = "newTree = cnh.knn(knnInput);";
				//eng.eval(command);
				// write the xml file
		//		command = "newTree.writeXmlFile('" + outputLocation + File.separator + "generatedApp');";
				//eng.eval(command);

				KnnBuilder kb = new KnnBuilder();
				if(useREMAUI)
				{
					UiTreeNode root = new UiTreeNode();
					root.setName("frame");
					addBounds(0, 0, WIDTH, HEIGHT, root);
					root.setType("android.widget.FrameLayout");
					root.addAtrribute("class", "android.widget.FrameLayout");
					root.addAtrribute("text", "");
					
					RootUINode rn = new RootUINode("","0",0,0,WIDTH,HEIGHT);
					
					kb.screenRoots.add(rn);
				}
				else
				{
					kb.getHeirarchyFromPath(sketchFile, false);
				}

				UIDumpParser parser = new UIDumpParser();
				StringBuilder builderXml = new StringBuilder();
				builderXml.append("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>");
				parser.buildXml(null, kb.screenRoots.get(0), builderXml);
//				kb.getHeirarchy(path, isDirectory)

				try (BufferedWriter bw = new BufferedWriter(new FileWriter(uixFile))) {
					bw.write(builderXml.toString());
					System.out.println("Done");
				} catch (IOException e) {
					e.printStackTrace();
				}
				//eng.close();
			}else{
				// Do the magic here! (just use the info from the sketch file)
				UiTreeNode root = new UiTreeNode();
				root.setName("frame");
				addBounds(0, 0, WIDTH, HEIGHT, root);
				root.setType("android.widget.FrameLayout");
				root.addAtrribute("class", "android.widget.FrameLayout");
				root.addAtrribute("text", "");
				containers.sort(Comparator.comparing(comp -> comp.getPxWidth() * comp.getPxHeight()));

				// Add containers first
				for (int i = 0; i < containers.size(); i++) {
					JSNode jsNode = containers.get(containers.size() - (i + 1));
					System.out.println(jsNode + ":" + (jsNode.getPxWidth() * jsNode.getPxHeight()));
					addNode(root, jsNode);
				}

				// Then add components
				for (UiTreeNode uiNode : components) {
					addNode(root, uiNode);
				}

				UIDumpParser parser = new UIDumpParser();
				StringBuilder builderXml = new StringBuilder();
				builderXml.append("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>");
				builderXml.append("<hierarchy rotation=\"0\">");
				parser.buildXml(null, root, builderXml);
				builderXml.append("</hierarchy>");
				//eng.close();
				// Write the file
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(uixFile))) {
					bw.write(builderXml.toString());
					System.out.println("Done");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// app generator..
			String im;
			if (useREMAUI){im = trimmedIm;}else{im=pathToPng;}
			String[] arguments = new String[] { uixFile.getAbsolutePath(), sourceCodeLocation, pathToAndroidSDK,
					im, useAbsolutePositioning };
			AGPMain.main(arguments);
		} catch (RejectedExecutionException  e) {
			e.printStackTrace();
		}

	}

	public static void GenerateRedrawTotal( String pathTesseract, ArrayList<String> imagePaths, ArrayList<String> sketchPaths, ArrayList<String> outPaths) throws IOException
	{
		//Load tensor checkpoint
//		TensorHelper th = new TensorHelper();
		
		//th.loadPythonCheckpoint(pathToNet);//not for testing
		//TensorHelper.loadPythonCheckpoint(path);
		
		
		KnnBuilder kb = new KnnBuilder();
		
		
		List<String> TierFolders = new ArrayList<String>();

		File folder = new File(pathToKnnData);
		File[] listOfFiles = folder.listFiles();//sort this array
		
		Arrays.sort(listOfFiles);
		
		
		
		for(int i = 0 ; i < listOfFiles.length; i++)
		{
			if(listOfFiles[i].isDirectory())
			{
				TierFolders.add(listOfFiles[i].getAbsolutePath());
			}
		}
		
		if(TierFolders.size()<1)
		{
			System.out.println("Data path is empty for Knn Data, cannot continue");
			return;
		}
		
		kb.loadAndAssembleTierFolders(TierFolders);		

		
		//Testing all folders in directory
		String path = "";
		
		for(int i = 0; i <imagePaths.size(); i++)
		{
			path = sketchPaths.get(i);
			pathToPng = imagePaths.get(i);
			sourceCodeLocation = outPaths.get(i);

			FileUtils.cleanDirectory(new File(outputLocation)); 
			List<UiTreeNode> inputNodes = new ArrayList<UiTreeNode>();
			try {
				inputNodes.addAll(getNodesForKnn(pathTesseract,path));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			kb.knn(inputNodes, path+".out");
		//the xml file should have the hierarchy
		

			String[] arguments = new String[] { path + ".out", sourceCodeLocation, pathToAndroidSDK,
					pathToPng, useAbsolutePositioning };
			AGPMain.main(arguments);		
		
			System.out.println("Completed:"+i);
		}
		System.out.println("finished");
		
		
	}
	
	
	
	private static List<UiTreeNode> getNodesForKnn(String OCRParserLocation, String sketchFile) throws IOException, InterruptedException {
//TODO the nodes are not classified, and need to be classified by REMAUI
		//It looks like the matlab neural network does this, while the neural network loads
		//it has not been set up to run in java yet
		//For the time being I'll use the non-remaui
		List<UiTreeNode> components = new ArrayList<UiTreeNode>();
		
		HashMap<String, JSNode> info = new HashMap<String, JSNode>();
        
		String command = pythonCommand+" "+ scriptPath+ " "+labelFile+" "+ pathToNet + " "+inputLayerName + " "+ outputLayerName;
	
        if (useREMAUI){
			ArrayList<JSNode> lst = getLeafNodesREMAUI(pathToPng, outputLocation);
			String remauiOutput = outputLocation + File.separator + "toClassify" + File.pathSeparator;
			new File(remauiOutput).getAbsoluteFile().mkdirs();
			int count = 0;
			for (JSNode cur: lst){
				String im = remauiOutput + "img-" + count + ".png";
				command += " "+im;
				ImagesHelper.cropImageAndSave(trimmedIm, im, cur.getX(), cur.getY(), cur.getWidth(), cur.getHeight());
				info.put(im, cur);
				count++;
			}
		}else{
			UIDumpParser loader = new UIDumpParser();
			//note that in this scenario, sketchfile is actually xml file
			RootUINode tree = loader.parseXml(sketchFile);

			List<UiTreeNode> nodes = tree.getLeafNodes();
			
			String outputLoc = outputLocation + File.separator + "toClassify" + File.pathSeparator;
			int count = 0;
			
			for(int i = 0; i < nodes.size(); i++)
			{
				JSNode curjs = new JSNode(nodes.get(i).getX(), nodes.get(i).getY(), nodes.get(i).getWidth(), nodes.get(i).getHeight());
				String im = outputLoc + "img-" + count + ".png";
				command += " "+im;
				ImagesHelper.cropImageAndSave(pathToPng, im, curjs.getX(), curjs.getY(), curjs.getWidth(), curjs.getHeight());
				info.put(im, curjs);
//				components = tree.getLeafNodes();
				count++;
				
			}
			
		}

		
        List<String> labelList = new ArrayList<String>();
        FileReader fileReader = new FileReader(labelFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
        bufferedReader.close();

		OCRImageParser ocrParser = new OCRImageParser(OCRParserLocation);
		String text = null;
		
		int index = 0;
		
			
			
            Runtime rt = Runtime.getRuntime();
            // System.out.println(command);
            Process proc = rt.exec(command);

            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            
            String s2 = null;
            while ((s2 = stdError.readLine()) != null) {
          //      System.out.println(s2);
            }     

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String s = null;
            
            List<String> labelNumbers= new ArrayList<String>();
            while ((s = stdInput.readLine()) != null) {
//                statements.add(getGUIEventVOFromRawEvent(s));
  //          	System.out.println(s);
            	labelList.add(s);
            }

      
            proc.waitFor();

            
        
			
		for (Entry<String, JSNode> item : info.entrySet()) {
			JSNode node = item.getValue();			//Check that the classification is indeed correct, somethings getting twisted somewhere
			
			int labelIndex = Integer.parseInt( item.getKey().substring(item.getKey().indexOf("-")+1,item.getKey().lastIndexOf(".")));
			
			String label= labelList.get(labelIndex);
			
			
			
			if (label.toLowerCase().contains("text")||label.toLowerCase().contains("button")) {
				try{
					text = ocrParser.parseImage(item.getKey(),true);
				}
				catch (NullPointerException e){
					System.out.println("WARNING: failed to parse image " + item.getKey() + ", continuing...");
				}
				text = (text == null ? "" : text);
				//                    int ndx = text.lastIndexOf(":");
				//                    text = text.substring(0, ndx);
			} else {
				text = "";
			}
			

			
			System.out.println("Text: " + text);
			System.out.println("Label: " + label);
			// Add data to bboxes and labels
			text = text.replace("&", "&amp;");
			text = text.replace("'", "&apos;");
			text = text.replace("’", "&apos;");
			text = text.replace("\"", "&quot;");
			text = text.replace("“", "&quot;");
			text = text.replace("”", "&quot;");
			text = text.replace("<", "&lt;");
			text = text.replace(">", "&gt;");
			text = text.replace("—", "-");
			text = text.replace("°", "Degrees");
			

			text = text.replace("©", "C");
			
			
			//command = "text = \"" + text + "\";";
			
			//eng.eval(command);

			UiTreeNode comp = new UiTreeNode(node.getX(), node.getY(), node.getWidth(), node.getHeight());
			comp.addAtrribute("class", "android.widget." + label);
			comp.addAtrribute("text", text);
			addBounds(node.getX(),node.getY(),node.getWidth(),node.getHeight(),comp);
			comp.setName("component");
			System.out.println("bounds: " + comp.getAttribute("bounds"));
			components.add(comp);

			System.out.println("Processing component: " + index);
			index++;
		}
		
		return components;
	}
	
	private static List<UiTreeNode> getNodesForKnnNONREMAUI(String path, String pngPath)
	{
		List<UiTreeNode> components = new ArrayList<UiTreeNode>();
		
		UIDumpParser loader = new UIDumpParser();
		//note that in this scenario, sketchfile is actually xml file
		//If the non-remaui is already in a tree, it should be simple to use a ground truth
		RootUINode tree = loader.parseXml(path);
		
		components = tree.getLeafNodes();
		
		
		return components;
	}

	/**
	 * @param containers 
	 * @return
	 * @TODO implement a parser which parses the exported sketch file into the
	 *       bounding boxes of the leaf nodes
	 */
	public static HashMap<String, JSNode> parseSketchFile(String imageFile, String sketchFile, List<JSNode> containers, String outputLocation) {
		MarketchJSParser parser = new MarketchJSParser(sketchFile);

		// initialize some settings for the marketchjsparser - taken from the
		// test suite.
		ConstantSettings settings = ConstantSettings.getInstance();
		int[] uiBoard = new int[] { 0, 0, 1200, 1920 };
		//        int[] uiBoard = new int[] { 0, 0, 120, 1920 };
		settings.setUIBoard(uiBoard); // default
		int[] dsBoard = new int[] { 0, 0, 1200, 1920 };
		//        int[] dsBoard = new int[] { 0, 0, 1440, 2372 };
		settings.setDSBoard(dsBoard);
		int[][] ignoredComps = new int[][] { { 0, 0, 1200, 72 }, { 0, 1776, 1200, 144 } };
		//        int[][] ignoredComps = new int[][] { { 0, 0, 1440, 100 }, { 0, 2372, 1440, 188 } };
		settings.setIgnoredCompDesign(ignoredComps);
		settings.setViolationThreshold(25);

		parser.runParser();
		parser.buildTree();
		parser.printOutTree();

		String imageName = new File(imageFile).getName();
		imageName = imageName.substring(0, imageName.lastIndexOf("."));

		HashMap<String, JSNode> map = new HashMap<>();
		ArrayList<JSNode> leafNodes = parser.getLeafNodes();
		ArrayList<JSNode> allNodes = new ArrayList<JSNode>(parser.getDSNodeSet());
		for (int i = 0; i < allNodes.size(); i++) {
			JSNode node = allNodes.get(i);
			// Validate whether it is a leaf node (component) or a container (Layout)
			if (leafNodes.contains(node)) {
				// Components
				String croppedImagePath = outputLocation + File.separator + imageName + "-" + i + ".png";
				try {
					// Crop image
					ImagesHelper.cropImageAndSave(imageFile, croppedImagePath, node.getX(), node.getY(),
							node.getWidth(), node.getHeight());
					node.setName("component");
					map.put(croppedImagePath, node);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else if(node.getPxHeight() *node.getPxWidth() > 0){
				// Containers (Layouts)
				node.setName("container");
				containers.add(node);
			}
		}

		return map;
	}

	public static void buildUiAutomatorDoc() {

	}

	private static void addNode(BasicTreeNode root, BasicTreeNode node) {
		boolean containedInChild = false;

		if (node instanceof JSNode) {
			for (BasicTreeNode child : root.getChildrenList()) {
				Rectangle r1 = new Rectangle(child.getX(), child.getY(), child.getWidth(), child.getHeight());
				Rectangle r2 = new Rectangle(node.getX(), node.getY(), node.getWidth(), node.getHeight());
				if (r1.contains(r2)) {
					addNode(child, node);
					containedInChild = true;
				}
			}
		}

		if (!containedInChild) {
			UiTreeNode uiNode = new UiTreeNode();
			String name = "";
			if (node instanceof JSNode) {
				name = ((JSNode) node).getName();
			} else if (node instanceof UiTreeNode) {
				name = ((UiTreeNode) node).getName();
			}
			switch (name) {
			case "container":
				uiNode.addAtrribute("class", "android.widget.RelativeLayout");
				uiNode.addAtrribute("text", "");
				uiNode.setName("container");
				uiNode.setType("android.widget.RelativeLayout");
				break;
			case "component":
				Object[] attributes = node.getAttributesArray();
				for (Object object : attributes) {
					AttributePair pair = (AttributePair) object;
					uiNode.addAtrribute(pair.key, pair.value);
					if (pair.key.equals("class")) {
						uiNode.setType(pair.value);
					}
				}
				uiNode.setName("component");
				break;
			default:
				break;
			}
			addBounds(node.getX(), node.getY(), node.getWidth(), node.getHeight(), uiNode);
			root.addNode(uiNode);
		}
	}

	/**
	 * @param node
	 * @param uiNode
	 */
	private static void addBounds(int x, int y, int width, int height, UiTreeNode uiNode) {
		uiNode.addAtrribute("bounds", "[" + x + "," + y + "][" + (width + x)
				+ "," + (height + y) + "]");
	}


	/**
	 * This method generates a list of JSNode objects representing the bounding boxes of leaf nodes components 
	 * that REMAUI was able to derive. Note this requires both OpenCV and tesseract to be installed.
	 * 
	 * @param imageFile: The full path the screenshot for an application to derive bounding CV-based bounding boxes from
	 * @param outputLocation: Location of output images
	 * @param remauiProjectDirectory: Full path to the root of the REMAUI project from required libraries
	 * @return
	 * @throws IOException 
	 */
	public static ArrayList<JSNode> getLeafNodesREMAUI(String imageFile, String outputLocation) throws IOException {
		ArrayList<JSNode> remauiLeafNodes = new ArrayList<JSNode>();
		String remauiProjectDirectory = "/scratch/mjcurcio.scratch/workspace/REMAUI";
		//crop out the top and bottom bars so that we do not pick up on those with the bounding box detection.
		trimmedIm = new File(imageFile).getParentFile().getAbsolutePath() + File.separator + "trimmed-im.png";
		ImagesHelper.cropImageAndSave(imageFile, trimmedIm, CONTENT_VIEW_DIMS[0], 
				CONTENT_VIEW_DIMS[1], CONTENT_VIEW_DIMS[2], CONTENT_VIEW_DIMS[3]);
//		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		// Instantiate new REMAUI object
		REMAUI remaui = new REMAUI();
		
		// Obtain the CV and OCR-based view hierarchy
		ViewHierarchy leafNodes = remaui.run(remauiProjectDirectory, trimmedIm, outputLocation);
		
		// Get a list of the identified nodes
		remauiLeafNodes = leafNodes.getList();
		
		return remauiLeafNodes;
	}
	



}

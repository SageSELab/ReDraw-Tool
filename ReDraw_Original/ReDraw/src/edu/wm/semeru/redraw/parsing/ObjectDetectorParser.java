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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;

/**
 * parses the data for an object detector CNN and makes it readable for MATLAB. =Not to be confused with ClassifierGroundTruth which
 * does the same thing but for a classifier CNN
 * 
 * Note from the author: this parser is a complete mess and horribly inefficient. I am aware of this however the complexity of the 
 * parser with respect to the number of classes is still linear so I am willing to accept it as long as it is functional. And it is, I promise.
 * @author mjcurcio
 *
 */
public class ObjectDetectorParser {

	public static HashMap<String, ArrayList<int[]>> cellArray = new HashMap<String, ArrayList<int[]>>();
	public static String curScreenShot = null;
	public static String curPNG = null;
	public static String appPackage = "";
	public static String outputFolder = "/Users/KevinMoran/Desktop/test-output";
	public static ArrayList<int[]> curList = null;
	//this is the number of inputs we need to give to the trainNetwork method we're using, these
	// metrics describe the bounding box of a textview
	public static final int NUMBER_OF_METRICS = 4;
	public static ArrayList<MLArray> mlList = new ArrayList<MLArray>();
	public static int count = 0;
	public static String curClass;
	public static String[] CLASSES = {"android.widget.TextView.png", "android.widget.ImageView.png", "android.widget.Button.png", 
			"android.widget.Switch.png", "android.widget.CheckedTextView.png", "android.widget.EditText.png", "android.widget.ImageButton.png",
			"android.widget.ProgressBar.png", "android.widget.RatingBar.png", "android.widget.ToggleButton.png", 
			"android.widget.CheckBox.png", "android.widget.SeekBar.png", "android.widget.RadioButton.png"};

	public static void main(String args[]) throws IOException{

		File inputFile = new File(args[0]);
		File outputFile = new File(args[1] + "/data-set.mat");

		ArrayList<Integer> componentTotals = new ArrayList<Integer>();
		
		for (String widget : CLASSES){
			System.out.println("Parsing " + widget + " component class...");
			int ndx = widget.lastIndexOf('.');
			widget = widget.substring(0, ndx);
			int compCtr = 0;
			parseData(inputFile, widget);

			//for debugging (printing the hashmap)
			//		for (String key : cellArray.keySet()){
			//			ArrayList<int[]>value = cellArray.get(key); 
			//			String str = "";
			//			if (value != null){
			//				for (int[] arr : value){
			//				   str += Arrays.toString(arr) + "\n";
			//				}
			//			}
			//			else{
			//				str = "null";
			//			}
			//			System.out.println(key + " " + str);
			//		}
			//		System.out.print(cellArray.toString());

			writeDotMatFile(widget);
			count++;
		}


		MatFileWriter writer = new MatFileWriter(outputFile, mlList);
		System.out.print("done!\nwrote to " + outputFile.getAbsolutePath() + "\n");
	}

	/**
	 * navigates the directory tree to find either png or xml files. if it is a png file we 
	 * add it to the array list, if it is an xml file we kick it to the xmlparser method
	 * @param pathToData
	 * @throws FileNotFoundException 
	 */
	private static void parseData(File pathToData, String widget) throws FileNotFoundException{

		//we will need file extension to filter by either png or xml files
		File parent = pathToData.getParentFile();
		String fileExtension = "";
		int index = pathToData.toString().lastIndexOf('.');
		if (index > 0){
			fileExtension = pathToData.toString().substring(index + 1);
		}

		File dir = pathToData;
		if (dir.isDirectory() && dir.getName() != "_MACOSX" && dir.getName().charAt(0) != '.'){
			String[] children = dir.list();
			int i;
			for(i=0; i < children.length; i++){
				File file = new File(dir.toString(), children[i]);
				if(!file.exists()){throw new FileNotFoundException();}
				parseData(new File(file.toString()), widget);
			}
		}
		else if (fileExtension.equals("xml") || fileExtension.equals("uix")){
			parseXmlFile(pathToData, widget);
			char lookingFor = pathToData.getAbsolutePath().charAt(pathToData.getAbsolutePath().length() - 5);
			for (File child : parent.listFiles()){
				int ndx = child.getAbsolutePath().lastIndexOf('.');
				String childXtension = child.getAbsolutePath().substring(ndx + 1);
				if (child.getAbsolutePath().charAt(child.getAbsolutePath().length() -5) == lookingFor &&
						childXtension.equalsIgnoreCase("png") && child.getName().charAt(0) != '.'){
					curScreenShot = child.getAbsolutePath();
				}
			}
			//add what we have to the hashmap and reset our current pointers

			cellArray.put(curScreenShot, curList);
			curScreenShot = null;
			curList = null;
		}

	}

	/**
	 * Called when we find an xml. Currently only looking for TextViews, this may change
	 * in the future
	 * @param file
	 */
	private static void parseXmlFile(File file, String widget){

		try{

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			curPNG = file.getAbsolutePath().replaceAll(".xml", ".png");
//			curPNG = curPNG.replaceAll("hierarchy_", "screenshot_");
			xmlTraveler(doc.getElementsByTagName("node").item(0), widget);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * iterates through the xml tree looking for Views. The code to obtain bound information for
	 * the view comes from J. Harless's work on the XML parser for the AndroidGUI Prototyping project
	 * @throws IOException 
	 */
	private static void xmlTraveler(Node node, String widget) throws IOException{
		//		NodeList childList = node.getChildNodes();
		//		Element element = (Element) node;
		//		int i;
		String target = node.getAttributes().getNamedItem("class").getNodeValue();
		if (node.hasChildNodes()){
			//			for (i = 0; i < childList.getLength(); i++){
			//				XmlTraveler(childList.item(i));
			//			}
			Node child = node.getFirstChild();
			while (child != null){
				if (child.getNodeType() == Node.ELEMENT_NODE){
					xmlTraveler(child, widget);
				}
				child = child.getNextSibling();
			}
		}

		else if (node.getAttributes().getNamedItem("class").getNodeValue().equals(widget)){
			String bounds = node.getAttributes().getNamedItem("bounds").getNodeValue();
			bounds = bounds.replaceAll("]", ",");
			bounds = bounds.substring(1);
			bounds = bounds.replace("[", "");
			List<String> list = new ArrayList<String>(Arrays.asList(bounds.split(",")));
			int x1, y1, x2, y2;
			x1 = Integer.parseInt(list.get(0));
			y1 = Integer.parseInt(list.get(1));
			x2 = Integer.parseInt(list.get(2));
			y2 = Integer.parseInt(list.get(3));
			int width = (x2 - x1);
			int height = (y2 - y1);

			// Here we check for the same cases as in the Classifier parser.  Basically ruling out any infeasible component coordinates.
			if(!(width == 0 || height == 0)  && !(x1 < 0 || y1 < 0 || width < 0 || height < 0) && !(x1 + width > 1200 || y1 + height > 1920) && (ClassifierGroundTruth.checkPixels(new File(curPNG)))){
				if (curList == null){
					curList = new ArrayList<int[]>();
				}
				curList.add(new int[]{x1, y1, width, height});

				// The code below is for debugging purposes. It outputs annotated images with the bonding boxes drawn on the full screenshots.
				// Note that you will most likely have to change the ordinalIndex of the forward slash when getting the app Package to match 
				// the number of forward slashes in your specific file path. You should set the outputFolder global variable to the path where
				// you would like the annotated images saved.
				
//				try {
//					System.out.println(curPNG);
//					appPackage = curPNG.substring(ordinalIndexOf(curPNG, "/", 5)+1, curPNG.lastIndexOf("/")-1);
//					System.out.println("x: " + x1 + " y: " + y1 + " width: " + width + " height: " + height);
//					if (!new File(outputFolder + File.separator + node.getAttributes().getNamedItem("class").toString().substring(7, node.getAttributes().getNamedItem("class").toString().length()-1) + File.separator).exists()){
//						new File(outputFolder + File.separator + node.getAttributes().getNamedItem("class").toString().substring(7, node.getAttributes().getNamedItem("class").toString().length()-1) + File.separator).mkdir();
//					}
//					ScreenshotModifier.augmentScreenShot(curPNG, outputFolder + File.separator + node.getAttributes().getNamedItem("class").toString().substring(7, node.getAttributes().getNamedItem("class").toString().length()-1) + File.separator + appPackage + "-" +  + Math.random() + ".png", x1, y1, width, height);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
				
			}// End if statement for checking for bad images and component bounds.
			
			//for debugging
			//			System.out.print((new Integer[]{x1, y1, width, height}).toString());

		}
	}

	public static int ordinalIndexOf(String str, String substr, int n) {
		int pos = str.indexOf(substr);
		while (--n > 0 && pos != -1)
			pos = str.indexOf(substr, pos + 1);
		return pos;
	}

	/**
	 * We use MathWorks' JMatIO library to easily write our the hashmap we parsed out into a .mat file,
	 * the data in which we will use to train our neural net
	 * @param map
	 * @throws IOException 
	 */
	private static void writeDotMatFile(String widget) throws IOException{
		Collection<String> images = cellArray.keySet();
		Collection<ArrayList<int[]>> bboxArrayLists = cellArray.values();
		int size = images.size();
		//construct our cell array of images
		MLCell textViewImages = new MLCell("TrainingImages", new int[]{size, 1});
		int i = 0;
		for (String im : images){
			MLChar mlarr = new MLChar("image" + Integer.toString(i), im);
			textViewImages.set(mlarr, i);
			i++;
		}
		//formatting b/c matlab doesn't like when there are periods in names of fields
		int ndx = widget.lastIndexOf('.');
		String name = widget.substring(ndx + 1);
		//		ndx = name.lastIndexOf('.');
		//		name = name.substring(ndx + 1);
		//construct our cell array of 2d arrays representing the bounding boxes
		MLCell textViewTrueValues = new MLCell(name, new int[]{size, 1});

		int j = 0;
		//need to examine each array list of arrays
		for (ArrayList<int[]> arrlist : bboxArrayLists){
			if (arrlist!=null && !arrlist.isEmpty()){
				int rowSpace = arrlist.size();
				int colSpace = NUMBER_OF_METRICS;
				double[][] twoDArray = new double[rowSpace][colSpace];
				//				int[] oneDArray = new int[rowSpace];
				int k;
				//take each array in the array list
				for (k = 0; k < arrlist.size(); k++){
					double[] doubleArray= new double[colSpace];
					int m;
					//convert entries in the array to a double so that we can use MLDouble
					for (m = 0; m < colSpace; m++){
						doubleArray[m] = (double) arrlist.get(k)[m];
					}
					twoDArray[k] = doubleArray;

				}
				textViewTrueValues.set(new MLDouble("bbox" + Integer.toString(j), twoDArray), j);

			}
			else{
				textViewTrueValues.set(new MLDouble("bbox" + Integer.toString(j), new int[]{0,0}), j);
			}
			j++;
		}

		//finally, instantiate a matfilewriter object and write to the .mat file specified

		if (count == 0){
			mlList.add(textViewImages);
		}
		mlList.add(textViewTrueValues);



	}

	/**
	 * small helper method allowing us to determine how many columns we need in our 2d array
	 * @param collection
	 */
	//	private static void longestArray(Collection<ArrayList<int[]>> collection){
	//		int max = 0;
	//		for (ArrayList<int[]> list : collection)
	//	}
}

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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * takes a toy app filled with examples of a single widget/component, generates the desired
 * number of new apps while semi-randomly changing size and fields which are relevant to that 
 * specific widget i.e. text, scale, checked/unchecked, etc.
 * @author mjcurcio
 *
 */
public class DistributionNormalizer {
	
	public static int MAX_HEIGHT = 320;
	public static int MIN_HEIGHT = 50;
	public static int MAX_WIDTH = 1180;
	public static int MIN_WIDTH = 50;
	
	public static int dictionaryRange;
	public static String component;
	public static ArrayList<String> words;
	
	public static ArrayList<String[]> usedValues;

	public static void main(String[] args){
		
		File inputFile = new File(args[0]);
		component = args[1];
		File outputDir = new File(args[2]);
		int numApps = Integer.parseInt(args[3]);
		String pathToWords = args[4];
		usedValues = new ArrayList<String[]>();
		words = new ArrayList<String>();
		
		NodeList widgets;
		buildWordList(new File(pathToWords));
		dictionaryRange = words.size();
		
		try {	
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			if (component.equals("ProgressBarHorizontal")){
				widgets = doc.getElementsByTagName("ProgressBar");
			}else{
				widgets = doc.getElementsByTagName(component);
			}
			editXML(widgets, numApps, doc, inputFile, outputDir);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Done!");
		System.out.println("wrote synthesized apps to " + outputDir.getAbsolutePath());
	}
	
	public static void editXML(NodeList list, int iterations, Document doc, File file, File dir){
		int i,j;
		for (i=0; i < iterations; i++){
			for (j=0; j < list.getLength(); j++){
				String newHeightStr = null;
				String newWidthStr = null;
				Node cur = list.item(j);
				String[] arr = new String[]{"0","0"};
				//check to make sure no duplicate values
				while (usedValues.indexOf(arr) != -1 || Arrays.equals(arr, new String[]{"0", "0"})){
					newHeightStr = Integer.toString((int)(Math.random() * (MAX_HEIGHT - MIN_HEIGHT) + MIN_HEIGHT)) + "px";
					newWidthStr = Integer.toString((int)(Math.random() * (MAX_WIDTH - MIN_WIDTH) + MIN_WIDTH)) + "px";
					arr = new String[]{newHeightStr, newWidthStr};
				}
				
				usedValues.add(arr);

				Element elem = (Element) cur;
				elem.setAttribute("android:layout_width", newWidthStr);
				elem.setAttribute("android:layout_height", newHeightStr);
				
				//choose randomly from the list of words, set it as the text
				int randNdx = (int)(Math.random() * dictionaryRange);
				String newText = words.get(randNdx);
				elem.setAttribute("android:text", newText);
				
				//Some widget-specific tuning of the xml attributes
				if (component.equals("RadioButton")){
					String newScaleStr = Double.toString( ((Math.random() * 2.5) + .5));
					elem.setAttribute("android:scaleX", newScaleStr);
					elem.setAttribute("android:scaleY", newScaleStr);
					elem.setAttribute("android:layout_width", "wrap_content");
					elem.setAttribute("android:layout_height", "wrap_content");
				}
				else if (component.equalsIgnoreCase("CheckBox")){
					String newScaleStr = Double.toString( ((Math.random() * 2.5) + .5));
					elem.setAttribute("android:scaleX", newScaleStr);
					elem.setAttribute("android:scaleY", newScaleStr);
					
					elem.setAttribute("android:layout_height", "wrap_content");
					
//					newWidthStr = Integer.toString((int)(Math.random() * (MAX_WIDTH / Math.max(1, (Math.random() * 2.5 + 0.5))))) + "px";
					elem.setAttribute("android:layout_width", "wrap_content");
					//sometimes this should not have any text, 30% of the time it will be empty, 30% will also start checked
					double rand = Math.random();
					if (rand < 0.7){
						elem.setAttribute("android:text", "");
					}
					double rand2 = Math.random();
					if (rand2 < 0.3){
						elem.setAttribute("android:checked", "true");
					}
				}
				else if (component.equalsIgnoreCase("ProgressBar")){
					elem.setAttribute("android:layout_width", newHeightStr);
				}
				else if (component.equalsIgnoreCase("ProgressBarHorizontal")){
					String progressRand = Integer.toString((int) (Math.random() * 100));
					String scaleYRand = Double.toString(Math.random() * 2.5 + 0.5);
					elem.setAttribute("android:layout_height", "wrap_content");
					elem.setAttribute("android:scaleY", scaleYRand);
					elem.setAttribute("android:progress", progressRand);
				}
				else if (component.equalsIgnoreCase("Switch")){
					String scaleRand = Double.toString(Math.random() * 1.5 + 0.5);
					double chanceRand = Math.random();
					int wordRand = (int) (Math.random() * dictionaryRange);
					elem.setAttribute("android:scaleX", scaleRand);
					elem.setAttribute("android:scaleY", scaleRand);
					if (chanceRand < 0.4){
						elem.setAttribute("android:text", "");
					}
					else{
						elem.setAttribute("android:text", words.get(wordRand));
					}
					elem.setAttribute("android:layout_width", "wrap_content");
					elem.setAttribute("android:layout_height", "wrap_content");
				}
				else if (component.equalsIgnoreCase("RatingBar")){
					String starsRand = Double.toString(Math.random() * 5);
					String scaleRand = Double.toString(Math.random() * 1.5 + 0.5);
					elem.setAttribute("android:layout_width", "wrap_content");
					elem.setAttribute("android:layout_height", "wrap_content");
					elem.setAttribute("android:scaleX", scaleRand);
					elem.setAttribute("android:scaleY", scaleRand);
					elem.setAttribute("android:rating", starsRand);
				}
				else if (component.equalsIgnoreCase("SeekBar")){
					String scaleRand = Double.toString(Math.random() * 2.5 + 0.5);
					String valueRand = Integer.toString((int)(Math.random() * 11));
					elem.setAttribute("android:layout_height", "wrap_content");
					elem.setAttribute("android:scaleY", scaleRand);
					elem.setAttribute("android:progress", valueRand);
				}
				else if (component.equalsIgnoreCase("Spinner")){
					String scaleRand = Double.toString(Math.random() * 3.5 + 0.5);
					elem.setAttribute("android:scaleX", scaleRand);
					elem.setAttribute("android:scaleY", scaleRand);
					elem.setAttribute("android:layout_width", "wrap_content");
					elem.setAttribute("android:layout_height", "wrap_content");
				}
				else if (component.equalsIgnoreCase("NumberPicker")){
					double scale = Math.random() + 0.5;
					String scaleRand = Double.toString(scale);
//					elem.setAttribute("android:scaleX", scaleRand);
//					elem.setAttribute("android:scaleY", scaleRand);
					double width = 170 * scale;
					double height = 446 * scale;
					String widthstr = Integer.toString((int) width);
					String heightstr = Integer.toString((int) height);
					elem.setAttribute("android:layout_width", widthstr + "px");
					elem.setAttribute("android:layout_height", heightstr + "px");
				}
				else if (component.equalsIgnoreCase("CheckedTextView")){
					double scaleRandDouble = Math.random() + 0.75;
					String scaleRandStr = Double.toString(scaleRandDouble);
					//we want this component to be a longer
					int min_width = MAX_WIDTH / 2;
					int newWidth = (int) (Math.max(min_width, MAX_WIDTH / (Math.max(1, scaleRandDouble)))) - 50 ;
					newWidthStr = Integer.toString(newWidth) + "px";
					elem.setAttribute("android:layout_width", newWidthStr);
					elem.setAttribute("android:layout_height", "wrap_content");
					elem.setAttribute("android:scaleX", scaleRandStr);
					elem.setAttribute("android:scaleY", scaleRandStr);
					//we'd like it to display short sentances, they will be nonsense
					int numWords = (int) (Math.random() * 10 + 6);
					String text = "";
					int l;
					for (l=0; l<numWords; l++){
						String curWord = words.get((int)(Math.random() * 40000));
						text += " " + curWord;
					}
					elem.setAttribute("android:text", text);
					
					double checkedRand = Math.random();
					if (checkedRand < 0.3){
						elem.setAttribute("android:checked", "true");
					}
					else{
						elem.setAttribute("android:checked", "false");
					}
					
//					double textSizeRand = Math.random() * (15 * ((Math.abs(1 - scaleRandDouble)/2)+1));
//					String textSizeStr = Double.toString(textSizeRand) + "dp";
//					elem.setAttribute("android:textSize", textSizeStr);
				}
				else if (component.equalsIgnoreCase("ToggleButton")){
					double clickedRand = Math.random();
					
					//we'd like our togglebuttons to be slightly smaller
					int newWidthInt = (int) (Integer.parseInt(newWidthStr.replaceAll("[A-Za-z]", "")) * 0.6);
					int newHeightInt = (int) (Integer.parseInt(newHeightStr.replaceAll("[A-Za-z]", "")) * 0.6);
					//but not too small!
					int height = Math.max(MIN_HEIGHT, newHeightInt);
					int width = Math.max(MIN_WIDTH, newWidthInt);
					
					elem.setAttribute("android:layout_width", Integer.toString(width) + "px");
					elem.setAttribute("android:layout_height", Integer.toString(height) + "px");
					//most will be unchecked, 30% will start checked
					if (clickedRand > 0.7){
						elem.setAttribute("android:checked", "true");
					}
					else{
						elem.setAttribute("android:checked", "false");
					}
				}
				cur = elem;
			}
			
			//write to the xml file
			try {
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(file);
				transformer.transform(source, result);
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//go up 6 directory levels to the root directory of the project, move the project to the output directory
			File rootDir = file.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
			try{
				
//				rootDir.renameTo(new File(rootDir.getAbsolutePath() + Integer.toString(i)));
				FileUtils.copyDirectoryToDirectory(rootDir, dir);
				File newName = new File(dir.getAbsolutePath() + "/" + rootDir.getName() + Integer.toString(i));
				File oldName = new File(dir.getAbsolutePath() + "/" + rootDir.getName());
				oldName.renameTo(newName);
				System.out.println("finished " + Integer.toString(i));
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	public static void buildWordList(File txtDoc){
		try {
			for (String line: Files.readAllLines(txtDoc.toPath())){
				words.add(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

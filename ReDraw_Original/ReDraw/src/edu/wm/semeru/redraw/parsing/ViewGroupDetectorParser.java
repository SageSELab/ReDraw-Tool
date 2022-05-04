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
 * get the names of all xml files and collect them up to write to a single variable in matlab. Please note that in the parser, only the main
 * method and the xmlParser method are used at this point, these are all that are necessary to write the file names. We are now doing the actual
 * parsing of the xml files in matlab itself.
 * @author mjcurcio
 *
 */
public class ViewGroupDetectorParser {
	
	public static ArrayList<MLArray> data;
	public static ArrayList<MLArray> finalList;
	public static int count;
	public static ArrayList<MLArray> writable;
	public static MLCell fileList;

	public static void main(String[] args) throws IOException{
		
		count = 0;
		finalList = new ArrayList<MLArray>();
		writable = new ArrayList<MLArray>();
		File inputDir = new File(args[0]);
		String outputDir = args[1];
		File output = new File(outputDir);
		data = new ArrayList<MLArray>();
		xmlParser(inputDir);
		fileList = new MLCell("fileList", new int[]{data.size(), 1});
		int i = 0;
		for (MLArray file : data){
			fileList.set(file, i);
			i++;
		}
		finalList.add(fileList);
		MatFileWriter writer = new MatFileWriter(outputDir + "/componentGrouperData.mat", finalList);
		System.out.print("done!\nwrote to " + output.getName() + "\n");
	}
	
	/**
	 * run the each xml file in the data directory, we want to record the view group type
	 * as the key and the contained bounding boxes as the values.
	 * @param file
	 * @throws FileNotFoundException 
	 */
	public static void xmlParser(File file) throws FileNotFoundException{
		
		String fileExtension = "";
		int index = file.toString().lastIndexOf('.');
		if (index > 0){
			fileExtension = file.toString().substring(index + 1);
		}
		
		//recursively descend until we find the bottom of the hierarchy
		if (file.isDirectory() && file.getName() != "_MACOSX" && file.getName().charAt(0) != '.'){
			File[] children = file.listFiles();
			for (File child : children){
				if(!child.exists()){throw new FileNotFoundException();}
				xmlParser(child);
			}
			
		}
		
		else if ((fileExtension.equalsIgnoreCase("xml") || fileExtension.equalsIgnoreCase("uix")) && file.getName().charAt(0) != '.'){
			try{
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				System.out.println("parsing " + file.toString() + "...");
				Document doc = dBuilder.parse(file);
				
				MLChar fileName = new MLChar("file", file.getAbsolutePath());
				data.add(fileName);
//				parseDocument(doc.getElementsByTagName("node").item(0));
//				
//				//add to the MLCell that we will write to the .mat file
//				String name = file.getParentFile().getName().replace('.','_');
//				String fileName = file.getName();
//				name = name.replace('-', '_');
//				int ndx = fileName.lastIndexOf('.');
//				int num = Integer.parseInt(fileName.substring(ndx - 1, ndx));
//				MLCell curList = new MLCell(name + "_" + Integer.toString(num), new int[]{data.size(), 2});
//				
//				int i = 0;
//				for (MLArray cur : data){
//					curList.set(cur, i,1);
//					MLChar curChar = new MLChar("curChar", cur.name);
//					curList.set(curChar, i,0);
//					i++;
//				}
//				finalList.add(curList);
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	
	public static void parseDocument(Node node){
		
		if (node.hasChildNodes()){
			Node child = node.getFirstChild();
			
			while (child != null){
				if (child.getNodeType() == Node.ELEMENT_NODE){
					parseDocument(child);
				}
				child = child.getNextSibling();
			}
		}
		
		//get the bounds of this view, we check to make sure that it is an element node again here to make sure we don't parse the fist node
//		if (!(node.getChildNodes().getLength() == 1 && 
//				node.getAttributes().getNamedItem("bounds").getNodeValue().equals(node.getFirstChild().getAttributes().getNamedItem("bounds").getNodeValue()))){
		if (node.getNodeType() == Node.ELEMENT_NODE){
			String bounds = node.getAttributes().getNamedItem("bounds").getNodeValue();
			bounds = bounds.replaceAll("]", ",");
			bounds = bounds.substring(1);
			bounds = bounds.replace("[", "");
			List<String> list = new ArrayList<String>(Arrays.asList(bounds.split(",")));
			int x1, y1;
			x1 = Integer.parseInt(list.get(0));
			y1 = Integer.parseInt(list.get(1));
			int x2 = Integer.parseInt(list.get(2));
			int y2 = Integer.parseInt(list.get(3));
		
			//get the type of view
			String fullname = node.getAttributes().getNamedItem("class").getNodeValue();
			int ndx = fullname.lastIndexOf('.');
			String name = fullname.substring(ndx + 1);
		
			//set the temp array
			MLDouble arr = new MLDouble(name, new int[]{4,1});
			arr.setReal((double) x1, 0);
			arr.setReal((double) y1, 1);
			arr.setReal((double) x2, 2);
			arr.setReal((double) y2, 3);
		
			data.add(arr);
		}
//		}
	}
}

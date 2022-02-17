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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * loop through and use Kevin's Redraw/../TrainingDataParser to get our training samples in 
 * PASCAL-VOC format
 * @author mjcurcio
 *
 */
public class DarkFlowHelper {
	//We define this guy because this is always where we will put our training data and I don't anticipate
	// needing to have more than one directory for now
	private static String DATASET_DIRECTORY;
	public static String png;
	public static int count;
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException{
		File file = new File(args[0]);
		DATASET_DIRECTORY = args[1];
		try {
			goGoDarkFlow(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//for debugging
//		System.out.println("donezo");
	}
	/**
	 * utilize recursion
	 * @param file
	 * @throws IOException 
	 */
	private static void goGoDarkFlow(File file) throws IOException{
		File parent = file.getParentFile();
		String extension = "";
		int ndx = file.toString().lastIndexOf('.');
		extension = file.toString().substring(ndx + 1);
		int i;
		String[] children = file.list();
		if (file.isDirectory() && !file.getName().equals(".DS_Store") && file.getName().charAt(0) != '.'){
			
			for (i=0; i < children.length; i++){
				File child = new File(file.toString(), children[i]);
				if (!child.exists()){throw new FileNotFoundException();}
				goGoDarkFlow(child);
			}
		}
		else{
			if (extension.equals("xml") && file.getName().charAt(0) != '.'){
//				System.out.println("xml");
				children = parent.list();
				char num = file.toString().charAt(file.getAbsolutePath().length() - 5);
				//get png file with matching number
				for (String cur : children){
//					String ss = cur.substring(cur.length() - 5);
//					String other = num + ".png";
					if (cur.substring(cur.length() - 5).equalsIgnoreCase(num + ".png") && cur.charAt(0) != '.' ){
//						System.out.println("match");
						File png = new File(parent.getAbsolutePath(), cur);
						pngToJpeg(png);
						TrainingDataParser.convertUIAutomatorXMLFile(file.getAbsolutePath(), 
								png.getAbsolutePath(), DATASET_DIRECTORY + "/JPEGImages", DATASET_DIRECTORY + "/Annotations/" + 
								Integer.toString(count + 1) + ".xml", Integer.toString(count + 1));
						count++;
					}
				}
			}
				
//			children = file.getParentFile().list();
			
//			if (extension.equalsIgnoreCase("png")){
////				png = file.getAbsolutePath();
////				pngToJpeg(file);
//				System.out.println("OOPS");
//			}
//			else{
//				if (file.getName().charAt(0) != '.'){
//					int len = file.getName().length();
//					char num = file.getName().charAt(len - 5);
//					for (String cur : children){
//						if (cur.charAt(cur.length() - 5) == num){
//							png = cur;
//							File p = new File(file.getParentFile().getAbsolutePath(), png);
//							pngToJpeg(p);
//						}
//					}
					
//					
//				}
//			}
		}
	}
	/**
	 * Converts our png images to jpeg and writes it the dataset folder
	 * @param file
	 * @throws IOException 
	 */
	private static void pngToJpeg(File file) throws IOException{
		try{
			BufferedImage bufferedImage = ImageIO.read(file);
			BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), 
					BufferedImage.TYPE_INT_RGB);
			newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, null);
			ImageIO.write(newBufferedImage, "jpg", new File(DATASET_DIRECTORY + "/JPEGImages/" 
					+ Integer.toString(count + 1) + ".jpg"));
			System.out.println(count);
		}
		catch (Exception e){
			System.out.println(file.getAbsolutePath());
			e.printStackTrace();
		}
		
		

	}

}

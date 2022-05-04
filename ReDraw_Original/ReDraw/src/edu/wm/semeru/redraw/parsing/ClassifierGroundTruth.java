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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLNumericArray;

/**
 * takes a directory of cropped-out android widgets and parses out the file names to establish
 * a ground truth vector for the classifier net
 * @author mjcurcio
 *
 */
public class ClassifierGroundTruth {
	
	public static ArrayList<Integer> truthVector = new ArrayList<>();
	public static ArrayList<Integer> groupVector = new ArrayList<>();
	public static ArrayList<String> viewTypes = new ArrayList<String>();
	public static String nameOfMatFile;
	public static int numberOfSlots;
	
	/**
	 * command line arguments:
	 * args[0] - path to directory with the cropped out components
	 * second - name of .mat file
	 * third - whether we are parsing for a gateway net
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		nameOfMatFile = args[1];
		File dataDir = new File(args[0]);
		boolean gateway = Boolean.parseBoolean(args[2]);
		File[] imList = dataDir.listFiles();
		//debugging
		int numberOfFiles = imList.length;
		numberOfSlots = dataDir.list().length; 
		for (File im : imList){
			//check to make sure not all pixels have same value, this would indicate that this
			// training example is a lemon
			if (!checkPixels(im)){
				im.delete();
			}
			//cut down the file name to only the underlying widget
			String name = im.getName();
			int ndx = name.lastIndexOf('-');
			String widget = name.substring(ndx + 1);
			//for debugging
//			constructViewList(dataDir, widget);
			
			switch (widget){
			case "android.widget.TextView.png":
				truthVector.add(0);
				break;
			case "android.widget.ImageView.png":
				truthVector.add(1);
				break;
			case "android.widget.Button.png":
				truthVector.add(2);
				break;
			case "android.widget.Switch.png":
				truthVector.add(8);
				break;
			case "android.widget.CheckedTextView.png":
				truthVector.add(11);
				break;
			case "android.widget.EditText.png":
				truthVector.add(9);
				break;
			case "android.widget.ImageButton.png":
				truthVector.add(10);
				break;
			case "android.widget.ProgressBarHorizontal.png":
				truthVector.add(12);
				break;
			case "android.widget.ProgressBarVertical.png":
				truthVector.add(14);
				break;
			case "android.widget.Image.png":
				truthVector.add(1);
				break;
			case "android.widget.RatingBar.png":
				truthVector.add(13);
				break;
			case "android.widget.ToggleButton.png":
				truthVector.add(16);
				break;
			case "android.widget.CheckBox.png":
				truthVector.add(19);
				break;
			case "android.widget.Spinner.png":
				truthVector.add(22);
				break;
			case "android.widget.SeekBar.png":
				truthVector.add(24);
				break;
			case "android.widget.NumberPicker.png":
				truthVector.add(26);
				break;
			case "android.widget.RadioButton.png":
				truthVector.add(27);
				break;  
			default:
				im.delete();
				System.out.println("WARNING: " + widget + " is not a supported class \nthe corresponding"
						+ " image has been removed from the dataset");
			}
			
			if(gateway){
				switch (widget){
				case "android.widget.TextView.png":
					groupVector.add(0);
					break;
				case "android.widget.ImageView.png":
					groupVector.add(0);
					break;
				case "android.widget.Button.png":
					groupVector.add(1);
					break;
				case "android.widget.Switch.png":
					groupVector.add(1);
					break;
				case "android.widget.CheckedTextView.png":
					groupVector.add(2);
					break;
				case "android.widget.EditText.png":
					groupVector.add(2);
					break;
				case "android.widget.ImageButton.png":
					groupVector.add(1);
					break;
				case "android.widget.ProgressBar.png":
					groupVector.add(3);
					break;
				case "android.widget.Image.png":
					groupVector.add(0);
					break;
				case "android.widget.RatingBar.png":
					groupVector.add(3);
					break;
				case "android.widget.ToggleButton.png":
					groupVector.add(1);
					break;
				case "android.widget.CheckBox.png":
					groupVector.add(1);
					break;
				case "android.widget.Spinner.png":
					groupVector.add(1);
					break;
				case "android.widget.SeekBar.png":
					groupVector.add(3);
					break;
				case "android.widget.NumberPicker.png":
					groupVector.add(4);
					break;
				case "android.widget.RadioButton.png":
					groupVector.add(1);
					break;  
				default:
					im.delete();
					System.out.println("WARNING: " + widget + " is not a supported class \nthe corresponding"
							+ " image has been removed from the dataset");
				}
			}
		}		
					
			
			
				
		
		dotMatWriter(dataDir);
//		for (String view : viewTypes){
//			System.out.println(view);
//		}
		
		//also for debugging
//		File f = new File(dataDir.getAbsolutePath() + "/views");
//		Path p = Paths.get(f.getAbsolutePath());
//		try {
//			Files.write(p, viewTypes, Charset.forName("UTF-8"));
//		} 
//		catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(truthVector.size());
	}
	/**
	 * method for debugging, outputs all types of lists found in the directory
	 * @param widget
	 */
	public static void constructViewList(File file, String widget){
		
		
		boolean flag = false;
		for (String cur : viewTypes){
			if (cur.equalsIgnoreCase(widget)){
				flag = true;
			}
		}
		if (!flag){
			viewTypes.add(widget);
		}

	}
	/**
	 * Writes our labels and images into a format that matlab can understand, so that we
	 * can use matlab to train the classifier
	 */
	public static void dotMatWriter(File datadir){
		
		//write the images into a cell array of character vectors
		
		MLCell trainingImages = new MLCell("trainingImages", new int[]{numberOfSlots, 1});
		int i = 0;
		for (File im : datadir.listFiles()){
			MLChar path = new MLChar("image" + Integer.toString(i), im.getAbsolutePath());
			trainingImages.set(path, i);
			i++;
		}
		//write the labels into a numeric array
		i = 0;
		MLDouble labels = new MLDouble("labels", new int[]{numberOfSlots, 1});
		try{
		for (int cur : truthVector){
			double d = (double) cur;
			labels.set(d, i);
			i++;
		}
		}catch(BufferOverflowException e){
			System.out.println("we got to label " + Integer.toString(i) + " before erroring out");
			System.out.println("truthVector is of length " + Integer.toString(truthVector.size()));
			System.out.println("groupVector is of length " + Integer.toString(groupVector.size()));
			e.printStackTrace();
		}
		
		int j = 0;
		MLDouble groups = new MLDouble("groups", new int[]{numberOfSlots,1});
		try{
		for (int cur : groupVector){
			double d = (double) cur;
			groups.set(d, j);
			j++;
		}
		}catch(BufferOverflowException e){
			System.out.println("we got to grouping " + Integer.toString(j) + " before erroring out");
			System.out.println("truthVector is of length " + Integer.toString(truthVector.size()));
			System.out.println("groupVector is of length " + Integer.toString(groupVector.size()));
			e.printStackTrace();
		}
		
		//put it all together and write the file
		ArrayList<MLArray> mList = new ArrayList<MLArray>();
		mList.add(trainingImages);
		mList.add(labels);
		mList.add(groups);
		File file = new File(datadir.getParentFile().getAbsolutePath() + "/" + nameOfMatFile  + ".mat");
		try {
			MatFileWriter writer = new MatFileWriter(file, mList);
			System.out.println("file writen");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * Some training images are useless for the purposes of training the net, such as
	 * cropped out images where the application was still loading at the time of image
	 * capture. This method is for weeding out such cases.
	 * @param img
	 * @throws IOException 
	 */
	public static Boolean checkPixels(File file) throws IOException{
		String path = file.getAbsolutePath();
		
		BufferedImage image = ImageIO.read(file);
		int x,y;
		//debugging
		if (image == null){
			System.out.println(path);
		}
		int firstPixel = image.getRGB(0, 0);
		for (y=0; y <image.getHeight(); y++){
			for (x=0; x < image.getWidth(); x++){
				if (image.getRGB(x, y) != firstPixel){
					return true;
				}
			}
		}
		return false;

	}

}

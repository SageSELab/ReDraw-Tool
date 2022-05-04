package edu.wm.semeru.redraw.pipeline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;
import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabExecutionException;
import com.mathworks.engine.MatlabSyntaxException;

import edu.semeru.android.core.helpers.device.EmulatorHelper;
//import edu.semeru.android.guichecker.helpers.AndroidUIHelper;
import edu.wm.cs.semeru.core.helpers.TerminalHelper;

/**
 * NOTE PLEASE READ: this is NOT a generalizable solution. There are a lot of assumptions made here about the naming
 * conventions of the files so please do not try to use this to compare hierarchies without looking through the code and
 * doing some refactoring.
 * @author mjcurcio
 *
 */

public class CompareHierarchies {
	
	public static String adbPath = "/scratch/mjcurcio.scratch/Programs/android/platform-tools";
	public static String outputDir = "/scratch/mjcurcio.scratch/SEMERUdata/HierarchyTestScreens/screenData/UIX-hierarchies"; //args[1];
	public static String apkLocation = "/scratch/mjcurcio.scratch/SEMERUdata/ReDraw-Output/build/outputs/apk/ReDraw-Output-debug.apk";
	public static String groundTruthXMLs = "/scratch/mjcurcio.scratch/SEMERUdata/HierarchyTestScreens/screenData/Ground-Truth-Hierarchies";
	
	public static String outputLocation = "/scratch/mjcurcio.scratch/SEMERUdata/PipelineScratch";
    public static String pathToAndroidSDK = "/scratch/mjcurcio.scratch/Programs/android";
    public static String pathToNet = "/scratch/mjcurcio.scratch/workspace/ReDraw/MATLAB-R-CNN/Android-Workspace/final-classifier-v3.mat";
    public static String pathToKnnData = "/scratch/mjcurcio.scratch/SEMERUdata/container-neighborhood.mat";
    public static String pathTesseract = "/scratch/mjcurcio.scratch/workspace/REMAUI/lib/tesseract";
    public static String nameMatlab = "ReDraw2";
    public static String useKnnAlgo = "true";
    public static String absolutePositioning = "true";
    public static String useREMAUI = "true";
    public static String sourceCodeLocation = "/scratch/mjcurcio.scratch/SEMERUdata/ReDraw-Output";	
    public static int count;
	public static void main(String args[]) throws InterruptedException, IOException, MatlabExecutionException, MatlabSyntaxException, ExecutionException{
		String execute;
		count = 0;
		File datadir = new File(args[0]);
		getXMLandScreenshots(datadir);
	}
	
	public static void getXMLandScreenshots(File datadir) throws MatlabExecutionException, MatlabSyntaxException, IOException, InterruptedException, ExecutionException{
		HashMap<String, String> lst = new HashMap<String, String>();
		File[] children = datadir.listFiles();	
		String image, xml;
		for(File cur: children){
			if (cur.isDirectory()){
				getXMLandScreenshots(cur);
			}
			else{
				int ndx = cur.getName().lastIndexOf('.');
				if (cur.getName().charAt(0) == '.' || cur.getName().equalsIgnoreCase("trimmed-im.png")){
					continue;
				}
				int num = Integer.parseInt(cur.getName().substring(0, ndx));
				if (cur.getName().substring(ndx).equalsIgnoreCase(".png")){
					image = cur.getAbsolutePath();
					xml = replaceFileExtension(cur.getAbsolutePath(), ".xml");
					boolean bool = generateApplication(image, xml, count);
					if (bool){
						String name = cur.getParentFile().getName() + "-" + num;
						FileUtils.copyDirectoryToDirectory(new File("/scratch/mjcurcio.scratch/SEMERUdata/ReDraw-Output"), new File("/scratch/mjcurcio.scratch/SEMERUdata/apps-for-hierarchies/CV/part2/" + name + "/"));
					}
//					File currentFile = new File("/scratch/mjcurcio.scratch/SEMERUdata/apps-for-hierarchies/ReDraw-Output");
//					currentFile.renameTo(new File(currentFile.getName() + count));
					count++;
				}
			}
		}
	}
	
	public static String replaceFileExtension(String file, String ex){
		int ndx = file.lastIndexOf('.');
		return file.substring(0,ndx) + ex;
	}
	
	public static boolean generateApplication(String image, String xml, int count) throws IOException, InterruptedException, MatlabExecutionException, MatlabSyntaxException, ExecutionException{
		String imageFile = image;
		String sketchFile = xml;
		boolean bool = true;
		String execute;
		Files.copy(new File(sketchFile), new File(groundTruthXMLs + File.separator +  count + ".xml"));
		String[] parameters = { imageFile, pathToKnnData, pathToNet, pathToAndroidSDK, outputLocation, 
				sourceCodeLocation, absolutePositioning, nameMatlab, sketchFile, pathTesseract, useREMAUI, useKnnAlgo };

		try {
			GeneratorWrapper.main(parameters);
			execute = TerminalHelper.executeCommand("cd " + sourceCodeLocation +";" + sourceCodeLocation +  "/gradlew build");
			System.out.println("executeCommand: " + execute);

		} catch (EngineException | IllegalArgumentException | IllegalStateException | InterruptedException | NullPointerException e) {
			System.out.println(xml + "failed to compile! continuing...");
			e.printStackTrace();
			bool = false;
		}
		

//		EmulatorHelper.unInstallAndInstallApp(pathToAndroidSDK, apkLocation, "cs435.guiproto.autogen", "5554", "5037");
//		AndroidUIHelper.captureUIDump(pathToAndroidSDK,  outputDir + File.separator + "ui-dump-" + count + ".xml");	
		//file utils to clear directory
//		FileUtils.cleanDirectory(new File(outputLocation));
		return bool;
	}

}

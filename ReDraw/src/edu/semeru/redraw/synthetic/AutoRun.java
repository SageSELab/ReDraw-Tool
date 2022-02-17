package edu.semeru.redraw.synthetic;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;

//TODO check junit and violation dependencies
//import org.junit.Assert;
//import org.junit.Ignore;
//import org.junit.Test;

//import edu.semeru.android.guichecker.AndroidGUIChecker;
import edu.wm.cs.semeru.redraw.ConstantSettings;
//import edu.semeru.android.guichecker.violation.ViolationType;


public class AutoRun {
	
	public static void main(String[] args) {
		ArrayList<String> dynamicComp = new ArrayList<String>(
				Arrays.asList("checkbox","checkedtextview","numpicker","radiobutton","ratingbar","seekbar","switch","image","progressbar","text"));
		String pathToGVTOutput = "C:"+File.separator+"Users"+File.separator+"Think"+File.separator+"Desktop"+File.separator+"GVT-output";
		String pathToOutputScreen = "C:"+File.separator+"Users"+File.separator+"Think"+File.separator+"Desktop"+File.separator+"output-screen-test";
		String pathToMockUp = "C:"+File.separator+"Users"+File.separator+"Think"+File.separator+"Desktop"+File.separator+"text_mockup";
		
		for (String com:dynamicComp) {
			if(com == "checkbox") {
				runGVT(com,pathToGVTOutput, pathToOutputScreen, pathToMockUp);
			}
			
		}
	}
	private static void runGVT(String com, String gvtOutput, String pathToOutputScreen, String pathToMockUp) {
		String mockupDS = pathToMockUp + File.separator + com + File.separator +"data.js";
		String mockupImagePath = pathToMockUp + File.separator + com + File.separator + "artboard.png";
		
		String[] pngs = { "png" };
		String[] xmls = { "xml" };
		
		File inputDir = new File(pathToOutputScreen + File.separator + com);
		Collection<File> screenshotCollection = FileUtils.listFiles(inputDir, pngs, true);
		List<File> screenshotList = new ArrayList<File>(screenshotCollection);
		Collection<File> uiautomatorCollection = FileUtils.listFiles(inputDir, xmls, true);
		List<File> uiautomatorList = new ArrayList<File>(uiautomatorCollection);
		
		for (int i = 0; i < screenshotList.size(); i++) {
			
			//if(i<10) {
			
			ConstantSettings settings = ConstantSettings.getInstance();
			int[] uiBoard = new int[] {0, 0, 1440, 2368};
			settings.setUIBoard(uiBoard);    
			int[] dsBoard = new int[] {0, 0, 1440, 2368};
			settings.setDSBoard(dsBoard);
			int[][] ignoredComps = new int[][]{{0,0,1440,100},{0,2372,1440,2560}};
			int[][] dynamicDim = new int[][] {};
			settings.setDynamicCompDesign(dynamicDim);
			settings.setIgnoredCompDesign(ignoredComps);
			
			settings.setViolationThreshold(15);
			settings.setImgDiffThreshold(20);
			
			String uiDumpFile = uiautomatorList.get(i).getAbsolutePath();
			String screenshot = screenshotList.get(i).getAbsolutePath();
			
			String outputFolder = gvtOutput + File.separator + com + File.separator + i + File.separator;
			File directory = new File(String.valueOf(outputFolder));
			if (! directory.exists()){
				directory.mkdirs();
			}
			
			/*
			if(screenshot.contains(com+"-s1")) {
				mockupDS = pathToMockUp + File.separator + com + File.separator + "s1" + File.separator +"data.js";
				mockupImagePath = pathToMockUp + File.separator + com + File.separator  + "s1" + File.separator+ "artboard.png";
				if(com == "image") {
					int[][] dynamicDim = new int[][] {{1197,536,1401,727}};
					settings.setDynamicCompDesign(dynamicDim);
				}
				if(com == "text") {
					int[][] dynamicDim = new int[][] {{55,880,940,1018}};
					settings.setDynamicCompDesign(dynamicDim);
				}

			}
			if(screenshot.contains(com+"-s2")) {
				mockupDS = pathToMockUp + File.separator + com + File.separator + "s2" + File.separator +"data.js";
				mockupImagePath = pathToMockUp + File.separator + com + File.separator  + "s2" + File.separator+ "artboard.png";
				if(com == "image") {
					int[][] dynamicDim = new int[][] {{0,474,724,930},{0,1348,717,1745}};
					settings.setDynamicCompDesign(dynamicDim);
				}
				if(com == "text") {
					int[][] dynamicDim = new int[][] {{81,1783,982,1889},{1260,2015,1427,2157}};
					settings.setDynamicCompDesign(dynamicDim);
				}
			}
			if(screenshot.contains(com+"-s3")) {
				mockupDS = pathToMockUp + File.separator + com + File.separator + "s3" + File.separator +"data.js";
				mockupImagePath = pathToMockUp + File.separator + com + File.separator  + "s3" + File.separator+ "artboard.png";
				int[][] dynamicDim = new int[][] {{9,337,1427,1702}};
				settings.setDynamicCompDesign(dynamicDim);
			}*/
			
			
			System.out.println("mockup folder: " + mockupDS);
			System.out.println("mockup image: "+ mockupImagePath);
			System.out.println("screen shot: " +screenshot);
			System.out.println("xml file: " + uiDumpFile);
			System.out.println("output folder: " + outputFolder);
			System.out.println();
/*			
			AndroidGUIChecker checker = new AndroidGUIChecker(mockupDS, mockupImagePath, uiDumpFile, screenshot, outputFolder);
			System.out.println("start to run analysis");
		    checker.RunChecker();
			checker.printOutViolations();
*/
			System.out.println("-- GUI Verfication Complete");
			
			
			//}
			
			
			
		

		}
		
	}

}

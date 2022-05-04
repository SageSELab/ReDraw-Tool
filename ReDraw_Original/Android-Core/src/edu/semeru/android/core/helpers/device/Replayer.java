/*******************************************************************************
 * Copyright (c) 2016, SEMERU
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
package edu.semeru.android.core.helpers.device;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;

import edu.semeru.android.core.model.CoverageValuesVO;
import edu.semeru.android.core.model.GUIEventVO;
import edu.wm.cs.semeru.core.helpers.LogHelper;

/**
 * 
 * This class generates the training data from participants scripts
 *
 * @author Mario Linares
 * @since Aug 12, 2014
 */
public class Replayer {

    public static void replaySteps(String apkPath, String packageName, String mainActivity, List<GUIEventVO> steps,
	    String sdkRoot, String outputFolder, String participant, String file) throws InterruptedException {

	// Install app
	StepByStepEngine.unInstallAndInstallApp(sdkRoot, apkPath, packageName);
	// Start app
	StepByStepEngine.startAPK(sdkRoot, packageName, mainActivity);
	int stepCount = 1;
//	int MAX_STEPS_BATCH = 10;
//	int stepsLength = steps.size();

//	HashSet<String> methods = new HashSet<String>();
//	HashSet<String> activities = new HashSet<String>();

//	methods.addAll(Utilities.getLogcatAndExtractAppMethods(sdkRoot, packageName));

//	activities.add(Utilities.getCurrentActivityImproved(sdkRoot, packageName));// rentActivity(sdkRoot));
	for (GUIEventVO step : steps) {
	    // --Cleaning log
	    if (stepCount == 1) {
		Utilities.clearLogcat(sdkRoot);
		Thread.sleep(1000);
	    }

	    // --Execute step
	    // StepByStepEngine.executeInputCommand(step, sdkRoot);
	    StepByStepEngine.executeEvent(step, sdkRoot, packageName, null);
	    Thread.sleep(4000);
//	    activities.add(Utilities.getCurrentActivityImproved(sdkRoot, packageName));// rentActivity(sdkRoot));

//	    if (stepCount == MAX_STEPS_BATCH || stepCount == stepsLength) {
//		stepCount = 1;
//		// --Get trace and get methods
//		methods.addAll(Utilities.getLogcatAndExtractAppMethods(sdkRoot, packageName));
//
//	    }
	    stepCount++;
	}
	// Stop app
	StepByStepEngine.stopAPK(sdkRoot, packageName);

//	try {
//	    CoverageValuesVO coverageValues = Utilities.getCoverageValues(methods, activities, apkPath,
//		    "/Users/charlyb07/Documents/workspace/semeru/Data-collector/libs4ast/",
//		    "/Users/charlyb07/Documents/workspace/semeru/APK-analyzer/output" + File.separator + packageName
//			    + File.separator + "src_main");
//
//	    Gson json = new Gson();
//	    LogHelper.getInstance(outputFolder + File.separator + participant + "_" + file + "_coverage.txt").addLine(
//		    json.toJson(coverageValues));
//	    System.out.println(coverageValues);
//	} catch (Exception e) {
//	    e.printStackTrace();
//	}
//	System.out.println("METHODS:");
//	for (String string : methods) {
//	    System.out.println(string);
//	}
//	System.out.println("------------");
//	System.out.println("ACTIVITIES:");
//	for (String string : activities) {
//	    System.out.println(string);
//	}
    }

    public static void replaySteps2(String apkPath, String packageName, String mainActivity, List<String> steps,
	    String sdkRoot, String outputFolder, String key) throws InterruptedException {

	// Install app
	// StepByStepEngine.unInstallAndInstallApp(sdkRoot, apkPath,
	// packageName);
	// Start app
	StepByStepEngine.startAPK(sdkRoot, packageName, mainActivity);
	int stepCount = 1;
	int MAX_STEPS_BATCH = 10;
	int stepsLength = steps.size();

	HashSet<String> methods = new HashSet<String>();
	HashSet<String> activities = new HashSet<String>();

	methods.addAll(Utilities.getLogcatAndExtractAppMethods(sdkRoot, packageName));

	activities.add(Utilities.getCurrentActivityImproved(sdkRoot, packageName));// rentActivity(sdkRoot));
	for (String step : steps) {
	    // --Cleaning log
	    if (stepCount == 1) {
		Utilities.clearLogcat(sdkRoot);
		Thread.sleep(1000);
	    }
	    // --Execute step
	    StepByStepEngine.executeInputCommand(step, sdkRoot);
	    // StepByStepEngine.executeEvent(step, sdkRoot);
	    Thread.sleep(1000);
	    activities.add(Utilities.getCurrentActivityImproved(sdkRoot, packageName));// rentActivity(sdkRoot));

	    if (stepCount == MAX_STEPS_BATCH || stepCount == stepsLength) {
		stepCount = 1;
		// --Get trace and get methods
		methods.addAll(Utilities.getLogcatAndExtractAppMethods(sdkRoot, packageName));

	    }
	    stepCount++;
	}
	// Stop app
	StepByStepEngine.stopAPK(sdkRoot, packageName);

	// TODO CHANGE THIS PATH!!
	try {
	    CoverageValuesVO coverageValues = Utilities.getCoverageValues(methods, activities, apkPath,
		    "/Users/charlyb07/Documents/workspace/semeru/Data-collector/libs4ast/",
		    "/Users/charlyb07/Documents/workspace/semeru/APK-analyzer/output" + File.separator + packageName
			    + File.separator + "src_main");

	    Gson json = new Gson();
	    LogHelper.getInstance(outputFolder + File.separator + key + "_coverage.txt").addLine(
		    json.toJson(coverageValues));
	    System.out.println(coverageValues);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	System.out.println("METHODS:");
	for (String string : methods) {
	    System.out.println(string);
	}
	System.out.println("------------");
	System.out.println("ACTIVITIES:");
	for (String string : activities) {
	    System.out.println(string);
	}
    }

    public static void main(String[] args) {
	// String apkPath =
	// "/Users/charlyb07/Documents/workspace/semeru/Data-collector/study/apps/instrumented/diabetesplus.apk";
	// String apkPath =
	// "/Users/charlyb07/Documents/workspace/semeru/Data-collector/study/apps/instrumented/wordweb.apk";
	String apkPath = "/Users/charlyb07/Documents/workspace/semeru/Data-collector/study/apps/instrumented/Keepscore.apk";
	// String packageName = "com.squaremed.diabetesplus.typ1";
	// String mainActivity =
	// "com.squaremed.diabetesplus.typ1.activities.LogEntryListActivity";
	String packageName = "com.nolanlawson.keepscore";
	String mainActivity = "com.nolanlawson.keepscore.MainActivity";
	List<String> steps = new ArrayList<String>();
	String sdkRoot = "/Users/charlyb07/Applications/android-sdk-macosx";
	String outputFolder = "output/steps_coverage/";
	String participant = "1";
	try {
	    File file = new File(
	    // "/Users/charlyb07/Documents/workspace/semeru/Data-collector/inputs/com.wordwebsoftware.android.wordweb_copy.txt");
	    // "/Users/charlyb07/Documents/workspace/semeru/Data-collector/study/output/ww_functional/shen_steps.txt");
		    "/Users/charlyb07/Documents/workspace/semeru/Data-collector/study/output/keepscore_functional/keepscore_functional_incomplete3.txt");
	    Scanner sc = new Scanner(file);

	    while (sc.hasNextLine()) {
		String command;
		String i = sc.nextLine();
		String action = i.split(", Action:")[1];
		boolean isSwipe = action.contains("SWIPE");
		boolean isLong = action.contains("LONG");

		String[] actionSplit = action.replace("(", "").replace(")", "").split(" ");

		String positionA = actionSplit[2];
		String positionB = "";
		if (isSwipe) {
		    positionB = positionA.split("-->")[1];
		    positionA = positionA.split("-->")[0];
		    String pX1 = positionA.split(",")[0];
		    String pY1 = positionA.split(",")[1];
		    String pX2 = positionB.split(",")[1];
		    String pY2 = positionB.split(",")[1];
		    command = "adb shell input touchscreen swipe " + pX1 + " " + pY1 + " " + pX2 + " " + pY2 + " 500";
		} else if (isLong) {
		    String pX1 = positionA.split(",")[0];
		    String pY1 = positionA.split(",")[1];
		    command = "adb shell input touchscreen swipe " + pX1 + " " + pY1 + " " + pX1 + " " + pY1 + " 2000";
		} else {
		    String pX1 = positionA.split(",")[0];
		    String pY1 = positionA.split(",")[1];
		    command = "adb shell input tap " + pX1 + " " + pY1;
		}
		// StepByStepEngine.executeInputCommand(command, sdkRoot);
		System.out.println(command);
		steps.add(command);
		// System.out.println(i);
	    }
	    sc.close();
	    replaySteps2(apkPath, packageName, mainActivity, steps, sdkRoot, outputFolder, participant + "-"
		    + packageName);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    // public static void main(String[] args) {
    //
    // HashSet<String> methods = new HashSet<String>();
    // HashSet<String> activities = new HashSet<String>();
    // // String apkPath =
    // //
    // "/Users/charlyb07/Documents/workspace/semeru/Data-collector/study/apps/tasks.apk";
    // // String apkPath =
    // //
    // "/Users/charlyb07/Documents/workspace/semeru/Data-collector/study/apps/Mileage.apk";
    // // String packageName = "org.dmfs.tasks";
    // // String packageName = "com.evancharlton.mileage";
    // String apkPath =
    // "/Users/charlyb07/Documents/workspace/semeru/Data-collector/study/apps/Keepscore.apk";
    // String packageName = "com.nolanlawson.keepscore";
    // try {
    // CoverageValuesVO coverageValues = Utilities.getCoverageValues(methods,
    // activities, apkPath,
    // "/Users/charlyb07/Documents/workspace/semeru/Data-collector/libs4ast/",
    // "/Users/charlyb07/Documents/workspace/semeru/APK-analyzer/output" +
    // File.separator + packageName
    // + File.separator + "src_main");
    // Gson json = new Gson();
    // System.out.println(json.toJson(coverageValues));
    // } catch (Exception e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
}

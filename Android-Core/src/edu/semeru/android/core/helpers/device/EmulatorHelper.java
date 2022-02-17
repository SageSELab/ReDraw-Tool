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
/**
 * Created by Kevin Moran on Jul 27, 2015
 */
package edu.semeru.android.core.helpers.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.semeru.android.core.helpers.device.StepByStepEngine;
import edu.semeru.android.core.helpers.ui.UiAutoConnector;
import edu.semeru.android.core.model.DynGuiComponentVO;
import edu.semeru.android.core.model.GUIEventVO;
import edu.semeru.android.core.model.WindowVO;
import edu.wm.cs.semeru.core.helpers.TerminalHelper;

/**
 * @author Carlos Bernal & Kevin Moran
 */
@Deprecated
public class EmulatorHelper {

	
	public static void main(String[] args) {
		
		//Main class for testing
		
		String androidSDKPath = "/Applications/AndroidSDK/sdk";
		String androidAVDPath = "/Users/kevinmoran/.android/avd";
		String apkPath = "/Users/kevinmoran/Google_Drive/SEMERU/FUSION.Project/apks_for_user_study/Apps_for_User_Study/oi_notepad.apk";
		String emuPort = "5554";
		String packageName = "org.openintents.notepad.noteslist.NotesList";
		String adbPort = "5037";
		String pathToGApps = "/Users/KevinMoran/Dropbox/Documents/My_Graduate_School_Work/SEMERU/TAMARIN/Google-Play-apks/";
		String pathToUIA = "/Users/KevinMoran/Desktop/uiautoautomator-modified/";
		
		//installApp(androidRoot, apkPath, packageName, emuPort, adbPort);
		
		loadModifiedUIAutomator(androidSDKPath, emuPort, adbPort, pathToUIA);
		
		//spinEmuScript(androidSDKPath, emuPort, adbPort, "test-n7", true);
		//setupGApps(androidSDKPath, emuPort, adbPort, pathToGApps);
		//unlockEmu(androidSDKPath, emuPort, adbPort);
		//createAVD(androidAVDPath, "test-n7", "Nexus7-19");
		
	}
	
    /***********************************************************************************************************
     * Method Name: startAPK
     * 
     * Description: This method starts a running application on a target emulator.
     * 
     * @param packageName: The package Name of the application to be stopped.
     * 
     * @param androidSDKPath: Path to the folder of the local install of the AndroidSDK.
     * 
     * @param emuPort: Port number of the emulator that you wish to start the .apk on.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     * @param mainActivity: The mainActivity of the app to be started.  This will be the activity started by this method.
     * 
     ***********************************************************************************************************/
	
	public static void startAPK(String androidSDKPAth, String packageName, String mainActivity, String emuPort, String adbPort) {
        try {
            System.out.println("-- Cleaning logcat before starting APK");
            String androidToolsPath = androidSDKPAth + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("-- Starting " + packageName + " on the device");
            TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort  +" shell logcat -c");
            TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort + " shell am start -n " + packageName + "/" + mainActivity);
            Thread.sleep(3000);
        } catch (Exception ex) {
            Logger.getLogger(EmulatorHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

	 /***********************************************************************************************************
	 * Method Name: executeInputCommand
	 * 
	 * Description: This method executes a given command in the adb shell of a target emulator.
	 * 
	 * @param command: The shell command to be executed.
	 * 
	 * @param androidSDKPath: Path to the folder of the local install of the AndroidSDK.
	 * 
	 * @param emuPort: Port number of the emulator that you wish to execute the command upon.
	 * 
	 * @param adbPort: The port of the adb server the target emulator is connected to.
	 * 
	 ***********************************************************************************************************/
	
	 public static void executeInputCommand(String command, String androidSdkPath, String emuPort, String adbPort) {
	        Runtime rt = Runtime.getRuntime();
	        String androidToolsPath = androidSdkPath + File.separator + "platform-tools" + File.separator;
	        String emuCommand = null;
	        System.out.println("--- Executing GUI event" + command);
	       
	        	emuCommand = command.substring(command.indexOf("adb")+3);
	        	
	        	emuCommand = androidToolsPath + "adb -P " + adbPort + " -s emulator-" + emuPort + emuCommand; 
	        	System.out.println(emuCommand);
	            //rt.exec(androidToolsPath + File.separator + emuCommand).waitFor();
	        	TerminalHelper.executeCommand(emuCommand);
	        
	    }
	

	 /***********************************************************************************************************
	 * Method Name: getCoverageFile
	 * 
	 * Description: This method pulls a specified emma coverage file (.ec) from a device to a specified location on
	 * the local machine.
	 * 
	 * @param emmaFilePath: The path to the emma File located on the specified device.
	 * 
	 * @param androidSDKPath: Path to the folder of the local install of the AndroidSDK.
	 * 
	 * @param emuPort: Port number of the emulator that you wish to collect the coverage file from.
	 * 
	 * @param adbPort: The port of the adb server the target emulator is connected to.
	 * 
	 * @param emmaExtractionPath: The path to the location where the emma coverage file will be extracted.
	 * 
	 ***********************************************************************************************************/
	 
	public static void getCoverageFile(String androidSDKPath, String emmaFilePath, String emmaExtractionPath, String emuPort, String adbPort)
	{
		 try {
	            System.out.println("-- Getting Coverage Files from device");
	            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
	            Runtime rt = Runtime.getRuntime();
	            rt.exec(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort  +" pull " + emmaFilePath).waitFor();
	        } catch (Exception ex) {
	            Logger.getLogger(EmulatorHelper.class.getName()).log(Level.SEVERE, null, ex);
	        }
	}
	
    /***********************************************************************************************************
     * Method Name: startInstrumentedAPK
     * 
     * Description: This method starts a running application that was instrumented with Emma on a target emulator.
     * 
     * @param packageName: The package Name of the application to be stopped.
     * 
     * @param androidSDKPath: Path to the folder of the local install of the AndroidSDK.
     * 
     * @param emuPort: Port number of the emulator that you wish to start the instrumented .apk on.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     * @param mainActivity: The mainActivity of the app to be started.  This will be the activity started by this method.
     * 
     * @param coverageFile: The name of the coverage File to be generated on the device.  This will be used later to 
     * pull the file off the device.
     * 
     ***********************************************************************************************************/
	
	public static void startInstumentedAPK(String androidSDKPath, String packageName, String mainActivity,
	            String coverageFile, String emuPort, String adbPort) {
	        try {
	            // System.out.println("-- Cleaning logcat before starting APK");
	            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
	            Runtime rt = Runtime.getRuntime();
	            System.out.println("-- Starting " + packageName + " on the device");
	            // rt.exec(androidToolsPath + File.separator +
	            // "adb shell logcat -c").waitFor();
	            rt.exec(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort  +" shell am instrument -e coverageFile " + coverageFile + " "
	                    + packageName + "/instrumentation.EmmaInstrumentation").waitFor();
	
	            Thread.sleep(1500);
	        } catch (Exception ex) {
	            Logger.getLogger(EmulatorHelper.class.getName()).log(Level.SEVERE, null, ex);
	        }
	
	    }
	
    /***********************************************************************************************************
     * Method Name: stopInstrumentedAPK
     * 
     * Description: This method stops a running application that was instrumented with Emma on a target emulator.
     * 
     * @param packageName: The package Name of the application to be stopped.
     * 
     * @param androidSDKPath: Path to the folder of the local install of the AndroidSDK.
     * 
     * @param emuPort: Port number of the emulator that you wish to stop the instrumented apk.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     ***********************************************************************************************************/
	
	public static void stopInstumentedAPK(String androidSDKPath, String packageName, String emuPort, String adbPort) {
        try {
            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("-- Stopping " + packageName + " on the device");
            rt.exec(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort  +" shell am broadcast -a com.instrumentation.STOP").waitFor();

            Thread.sleep(1000);
        } catch (Exception ex) {
            Logger.getLogger(EmulatorHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /***********************************************************************************************************
     * Method Name: stopAPK
     * 
     * Description: This method stops a running application on a target emulator.
     * 
     * @param packageName: The package Name of the application to be stopped.
     * 
     * @param androidSDKPath: Path to the folder of the local install of the AndroidSDK.
     * 
     * @param emuPort: Port number of the emulator that you wish to start logcat collection for.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     ***********************************************************************************************************/
	
    public static void stopAPK(String androidSDKPath, String packageName, String emuPort, String adbPort) {
        try {
            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("-- Stopping " + packageName + " on the device");
            rt.exec(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort  +" shell am force-stop " + packageName).waitFor();

            Thread.sleep(2000);
        } catch (Exception ex) {
            Logger.getLogger(EmulatorHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
	
    /***********************************************************************************************************
     * Method Name: startLogcatCollection
     * 
     * Description: This method stops the Process started to collect the logcat information from the emulator.
     * This method should be called ONLY after a logcat collection process from startLogcatCollection is started.
     * 
     * @param logFileName: This is the full path and file name of the logcat output to be saved.
     * 
     * @param androidSDKPath: Path to the folder of the local install of the AndroidSDK.
     * 
     * @param emuPort: Port number of the emulator that you wish to start logcat collection for.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     ***********************************************************************************************************/
    
    public static Process startLogcatCollection(String logFileName, String androidSdkPath, String emuPort, String adbPort) {
        Runtime rt = Runtime.getRuntime();
        String androidToolsPath = androidSdkPath + File.separator + "platform-tools";
        System.out.println("--- Starting Verbose Logcat Collection");
        Process logcat = null;
        try {
            logcat = rt.exec(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort  + " " + "logcat > " + logFileName);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return logcat;
    }
    
    /***********************************************************************************************************
     * Method Name: stopLogcatCollection
     * 
     * Description: This method stops the Process started to collect the logcat information from the emulator.
     * This method should be called ONLY after a logcat collection process from startLogcatCollection is started.
     * 
     * @param logcat: This is the Process object that was instantiated to start the logcat collection.
     * 
     ***********************************************************************************************************/
    
    public static void stopLogcatCollection(Process logcat) {
        logcat.destroy();
    }
    
    /***********************************************************************************************************
     * Method Name: installApp
     * 
     * Description: This method uninstalls a target application from the target emulator.
     * 
     * @param androidSDKPath: This is the path to the local Android SDK install on the host machine.
     * 
     * @param outputFolder: This is the full path to the folder where the screenshot files will be stored.
     * 
     * @param packageName: This is the package name of the app to be installed from the emulator.
     * 
     * @param emuPort: Port number of the emulator that you wish to install the app from.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     * @param apkPath: The path to the .apk file of the application to be installed.
     ************************************************************************************************************/
    
    public static void installApp(String androidRoot, String apkPath, String packageName, String emuPort, String adbPort) {
        try {
            String androidToolsPath = androidRoot + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            
           // rt.exec(androidToolsPath + File.separator + "adb install " + apkPath).waitFor();
            String output = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort  + " install " + apkPath);
           // System.out.println(output);

        } catch (Exception ex) {
            Logger.getLogger(EmulatorHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    /***********************************************************************************************************
     * Method Name: unInstallandInstallApp
     * 
     * Description: This method uninstalls and then reinstalls a target application from the target emulator.
     * 
     * @param androidSDKPath: This is the path to the local Android SDK install on the host machine.
     * 
     * @param outputFolder: This is the full path to the folder where the screenshot files will be stored.
     * 
     * @param packageName: This is the package name of the app to be uninstalled and reinstalled from the emulator.
     * 
     * @param emuPort: Port number of the emulator that you wish to uninstall and reinstall the app from.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     * @param apkPath: the path to the .apk file to be installed.
     ************************************************************************************************************/
    
    public static void unInstallAndInstallApp(String androidRoot, String apkPath, String packageName, String emuPort, String adbPort) {
        try {
            String androidToolsPath = androidRoot + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("--- Removing " + packageName + " from the device");
            // rt.exec(androidToolsPath + File.separator +
            // "adb shell pm uninstall " + packageName).waitFor();
            String executeCommand = TerminalHelper.executeCommand(androidToolsPath + File.separator
                    + "adb -P " +adbPort+ " -s emulator-" + emuPort  + " shell pm uninstall " + packageName);
            System.out.println("executeCommand: " + executeCommand);
            System.out.println("--- Installing " + packageName + " apk (" + apkPath + ")");
            // rt.exec(androidToolsPath + File.separator + "adb install " +
            // apkPath).waitFor();
            executeCommand = TerminalHelper
                    .executeCommand(androidToolsPath + File.separator + "adb -P "+ adbPort +" -s emulator-" + emuPort  + " install " + apkPath);
            System.out.println("executeCommand: " + executeCommand);

        } catch (Exception ex) {
            Logger.getLogger(EmulatorHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /***********************************************************************************************************
     * Method Name: getErrorsFromLogcat
     * 
     * Description: This method captures the errors from the lgocat for a specific application running on a 
     * target emulator.
     * 
     * @param outputFolder: This is the full path to the folder where the screenshot files will be stored.
     * 
     * @param packageName: This is the package name of the app for which you want to capture the errors.
     * 
     * @param emuPort: Port number of the emulator that you wish to get the errors from.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     * @param scriptsPAth: The path to the folder containing the logcat error helper script.
     ************************************************************************************************************/
    
    public static String getErrorsFromLogcat(String androidSDKPath, String packageName, String emuPort, String scriptsPath, String adbPort) {
        String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
        String command = scriptsPath + File.separator +"logcat_error_helper.sh " + packageName + " " + androidToolsPath + File.separator + "adb " + emuPort + " " + adbPort;
        String error = TerminalHelper.executeCommand(command);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String clear_logcat = androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator- " + emuPort + " logcat -c";
        String output = TerminalHelper.executeCommand(clear_logcat);
        if (error != null && !error.isEmpty()) {
            System.out.println("Logcat Errors: " + error);
            return (error);
        }
        System.out.println("No exceptions");
        return (null);

    }
    
    /***********************************************************************************************************
     * Method Name: unInstallApp
     * 
     * Description: This method uninstalls a target application from the target emulator.
     * 
     * @param androidSDKPath: This is the path to the local Android SDK install on the host machine.
     * 
     * @param outputFolder: This is the full path to the folder where the screenshot files will be stored.
     * 
     * @param packageName: This is the package name of the app to be uninstalled from the emulator.
     * 
     * @param emuPort: Port number of the emulator that you wish to uninstall the app from.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     ************************************************************************************************************/
    
    public static void unInstallApp(String androidSDKPath, String packageName, String emuPort, String adbPort) {
        try {
            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("--- Removing " + packageName + " from the device");
            // rt.exec(androidToolsPath + File.separator +
            // "adb shell pm uninstall " + packageName).waitFor();
            String executeCommand = TerminalHelper.executeCommand(androidToolsPath + File.separator
                    + "adb -P " + adbPort + " -s emulator-" + emuPort  + " shell pm uninstall " + packageName);
            System.out.println("executeCommand: " + executeCommand);
        } catch (Exception ex) {
            Logger.getLogger(EmulatorHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    /***********************************************************************************************************
     * Method Name: getAndPullScreenshot
     * 
     * Description: This method takes of screenshot of the current screen displayed on the emulator and saves it to file
     * on the local machine.
     * 
     * @param androidSDKPath: This is the path to the local Android SDK install on the host machine.
     * 
     * @param outputFolder: This is the full path to the folder where the screenshot files will be stored.
     * 
     * @param name: This is the filename of hte screenshot that will be saved in the output folder on the host machine.
     * 
     * @param emuPort: Port number of the emulator that you wish to capture the screen of.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     ************************************************************************************************************/
    
    public synchronized static void getAndPullScreenshot(String androidSDKPath, String outputFolder, String name, String emuPort, String adbPort) {
        try {
            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("--- Getting screenshot:" + name);
            String terminal = "";
            File testImage = null;
         // do-while to counteract the emulator behavior where the screenshot is sometimes not generated.
            // This will continually re-generate and pull the screenshot until it is not empty according to file size.
            do {
	           terminal = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-"+ emuPort +" shell /system/bin/screencap -p /sdcard/screen.png");
	           Logger.getLogger("adb-output").log(Level.INFO, terminal);
	           Thread.sleep(2000);
	            System.out.println("--- Pulling screenshot:" + name);
	            terminal = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-"+ emuPort +" pull /sdcard/screen.png " + outputFolder + File.separator
	                    + name);
	            Logger.getLogger("adb-output").log(Level.INFO, terminal);
	            testImage = new File(outputFolder + File.separator
	                    + name);
	        } while(testImage.length() == 0);
            
        } catch (Exception ex) {
            Logger.getLogger(EmulatorHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /***********************************************************************************************************
     * Method Name: createAVD
     * 
     * Description: The purpose of this method is to create an avd based off a pre-defined avd on the host machine.
     * The host machine should have several avds that represent physical devices already created through the 
     * android AVD manager, and these follow the naming scheme "DeviceName-AndroidPlatformNumber" e.g. Nexus7-19.
     * This method will clone the pre-existing under a new name and create a new instance of a bootable avd.
     * 
     * @param androidAVDPath: This is the path to the <.android/avd> folder on the host machine.
     * 
     * @param avdName: This is the desired name of the new avd being created.
     * 
     * @param deviceType: This is the name of the target pre-configured avd.
     ************************************************************************************************************/
    
    public static void createAVD(String androidAVDPath, String avdName, String deviceType){
    	 //String androidToolsPath = androidAVDPath;
    	
         System.out.println("---Creating new Emulator " + avdName + " by cloning " + deviceType);
         
        //Copy the AVD folder under the new desired name.
         
        String terminaloutput = TerminalHelper.executeCommand("cp -R " + androidAVDPath + File.separator + deviceType + ".avd " + androidAVDPath + File.separator + avdName + ".avd");
        
        //Create the new .ini file using the path and android API level specified by the user.
        
        String apiLevel = deviceType.substring(deviceType.indexOf('-')+1);
        
        PrintWriter pw;
		try {
			pw = new PrintWriter(new FileWriter(androidAVDPath + File.separator + avdName + ".ini"));
			
			pw.println("avd.ini.encoding=UTF-8");
			pw.println("path= " + androidAVDPath + File.separator + avdName + ".avd");
			pw.println("path.rel=avd/" + avdName + ".avd");
			pw.println("target=android" + apiLevel);
	    	pw.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Hardware Paramters: ");
		
		try (BufferedReader br = new BufferedReader(new FileReader(androidAVDPath + File.separator + avdName + ".avd" + File.separator + "config.ini"))) {
		    String line;
		    PrintWriter pw1;
		    pw1 = new PrintWriter(new FileWriter(androidAVDPath + File.separator + avdName + ".avd" + File.separator + "config.ini.new"));
		    while ((line = br.readLine()) != null) {
		   			
		       if(line.contains("sdcard.path=")){
		    	   pw1.println("sdcard.path=" + androidAVDPath + File.separator + avdName + ".avd" + File.separator + "sdcard.img");
		       }else{
		    	   pw1.println(line);
		   			System.out.println(line);
		       }
		    	}
		    	
		    pw1.close();
		    
		    } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		
		String terminalCommand2 = TerminalHelper.executeCommand("rm -f " + androidAVDPath + File.separator + avdName + ".avd" + File.separator + "config.ini");
		String terminalCommand3 = TerminalHelper.executeCommand("cp " + androidAVDPath + File.separator + avdName + ".avd" + File.separator + "config.ini.new " + androidAVDPath + File.separator + avdName + ".avd" + File.separator + "config.ini");
		String terminalCommand4 = TerminalHelper.executeCommand("rm -f " + androidAVDPath + File.separator + avdName + ".avd" + File.separator + "config.ini.new");
		
         //System.out.println(terminaloutput);
    	
    }
    
    /***********************************************************************************************************
     * Method Name: unlockEmus
     * 
     * Description: This method bypasses the unlock screen for an emulator on a specific adb server and emulator port.
     * 
     * @param androidSDKPath: Path to the local install of the Android SDK.
     * 
     * @param emuPort: Port number of the emulator that you wish to unlock.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     ***********************************************************************************************************/
    
    public static void unlockEmu(String androidSDKPath, String emuPort, String adbPort){
    
    	System.out.println("---Unlocking emulator-" + emuPort + " on adb server " + adbPort);
    	
    	String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
   	
        String terminaloutput = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort + " shell input keyevent 82");
        System.out.println(terminaloutput);
    
    }
    
    /***********************************************************************************************************
     * Method Name: spinEmu
     * 
     * Description: Starts an emulator of a given avd name on a specific avd server (specified by the port #) and emulator port.
     * 
     * @param androidSDKPath: Path to the local install of the Android SDK.
     * 
     * @param emuPort: Port number of the emulator that you wish to unlock.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     * @param avdName: The name of the android virtual device to started.
     * 
     * @param gpu: Specifies wether or not GPU acceleration should be enabled.  Takes a boolean argument true=on false=off. 
     ***********************************************************************************************************/
    
 public static void spinEmu(String androidSDKPath, String emuPort, String adbPort, String avdName, boolean gpu){
        
	 	System.out.println("---Starting emulator-" + emuPort + " on adb server " + adbPort);
	 	String bootcompleted = "0";
	 	String gpuAccel = "";
	 	
	 	//if statement to set the state of the gpu acceleration.
	 	
	 	if (gpu){
	 		gpuAccel = "on";
	 	}else{
	 		gpuAccel = "off";
	 	}
	 	
    	String androidToolsPath = androidSDKPath + File.separator + "tools";
    	String androidPlatformToolsPath = androidSDKPath + File.separator + "platform-tools";
    	
        System.out.println("ANDROID_ADB_SERVER_PORT=" + adbPort + " " + androidToolsPath + File.separator + "emulator -avd " + avdName + " -no-audio -port " + emuPort + " &");
        String terminaloutput = TerminalHelper.executeCommand("ANDROID_ADB_SERVER_PORT=" + adbPort + " " + androidToolsPath + File.separator + "emulator -avd " + avdName + " -no-audio -port " + emuPort + " -gpu " + gpuAccel + " &");
        System.out.println(terminaloutput);
        
        do{
       	 
        	//System.out.println(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort + " shell getprop sys.boot_completed");
     	   String terminalCommand1 = TerminalHelper.executeCommand(androidPlatformToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort + " shell getprop sys.boot_completed");
            bootcompleted = terminalCommand1;
           // System.out.println(bootcompleted);
            System.out.println("Waiting for emulator " + avdName + " to start on ADB:EMU Ports " + emuPort + ":" + adbPort + "...");
            
            try {
 			Thread.sleep(2000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	 
      }while(!bootcompleted.equals("1"));
    
    }
 /***********************************************************************************************************
  * Method Name: spinEmuScript
  * 
  * Description: Starts an emulator of a given avd name on a specific avd server (specified by the port #) and emulator port. 
  * (This is for server use)
  * 
  * @param androidSDKPath: Path to the local install of the Android SDK.
  * 
  * @param emuPort: Port number of the emulator that you wish to unlock.
  * 
  * @param adbPort: The port of the adb server the target emulator is connected to.
  * 
  * @param avdName: The name of the android virtual device to started.
  * 
  * @param gpu: Specifies wether or not GPU acceleration should be enabled.  Takes a boolean argument true=on false=off. 
  ***********************************************************************************************************/
 
 public static void spinEmuScript(String androidSDKPath, String emuPort, String adbPort, String avdName, boolean gpu){
     
	 	System.out.println("---Starting emulator-" + emuPort + " on adb server " + adbPort);
	 	String bootcompleted = "0";
	 	String gpuAccel = "";
	 	
	 	//if statement to set the state of the gpu acceleration.
	 	
	 	if (gpu){
	 		gpuAccel = "on";
	 	}else{
	 		gpuAccel = "off";
	 	}
	 	
 	String androidToolsPath = androidSDKPath + File.separator + "tools";
 	String androidPlatformToolsPath = androidSDKPath + File.separator + "platform-tools";
	
     System.out.println("ANDROID_ADB_SERVER_PORT=" + adbPort + " " + androidToolsPath + File.separator + "emulator -avd " + avdName + " -no-audio -port " + emuPort + " &");
     String terminaloutput = TerminalHelper.executeCommand("scripts/spin-emulator.sh " + androidToolsPath + " " + emuPort + " " + adbPort + " " + avdName + " " + gpuAccel );
     System.out.println(terminaloutput);
     
     do{
       	 
     //	System.out.println(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort + " shell getprop sys.boot_completed");
  	   String terminalCommand1 = TerminalHelper.executeCommand(androidPlatformToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort + " shell getprop sys.boot_completed");
         bootcompleted = terminalCommand1;
       //  System.out.println(bootcompleted);
         System.out.println("Waiting for emulator " + avdName + " to start on ADB:EMU Ports " + emuPort + ":" + adbPort + "...");
         
         try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  	 
   }while(!bootcompleted.equals("1"));
     
     System.out.println("---Emulator Boot has completed!");
 
 }
    
 	/***********************************************************************************************************
 	 * Method Name: SetupGApps
 	 * 
 	 * Description: This method installs and sets up the default Google Applications, e.g. Google Play Store, on an
 	 * emulator.  
 	 * 
 	 * @param androidSDKPath: Path to the local install of the Android SDK.
 	 * 
 	 * @param emuPort: The port of the emulator on which you wish to install the Google apps.
 	 * 
 	 * @param adbPort: The port of the adb server the emulator is attached to.
 	 * 
 	 * @param pathToGApps: The path the Folder containing the .apk files of the Google Apps.
 	 ***********************************************************************************************************/
 
	public static void setupGApps(String androidSDKPath, String emuPort, String adbPort, String pathToGApps) {
		
		String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
		
        String terminalCommand1 = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort + " remount");
        System.out.println(terminalCommand1);
        String terminalCommand2 = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort + " push " + pathToGApps + " /system/priv-app/");
        System.out.println(terminalCommand2);
        String terminalCommand3 = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort + " shell stop && " + androidToolsPath + File.separator + "adb  -P " + adbPort + " -s emulator-" + emuPort + " shell start");
        System.out.println(terminalCommand3);
 }
	
	/***********************************************************************************************************
 	 * Method Name: SetupGApps
 	 * 
 	 * Description: This method installs and sets up the default Google Applications, e.g. Google Play Store, on an
 	 * emulator.  
 	 * 
 	 * @param androidSDKPath: Path to the local install of the Android SDK.
 	 * 
 	 * @param emuPort: The port of the emulator on which you wish to install the Google apps.
 	 * 
 	 * @param adbPort: The port of the adb server the emulator is attached to.
 	 * 
 	 * @param pathToGApps: The path the Folder containing the .apk files of the Google Apps.
 	 ***********************************************************************************************************/
 
	public static void killEmu(String androidSDKPath, String emuPort, String adbPort) {
		
		System.out.println("---Killing Emulator registered to Port: " + emuPort + " on ADB Server " + adbPort + "---");
		
		String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
		
        String terminalCommand1 = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort + " emu kill");
       
 }
	
	public static void startAppByPackage(String androidSDKPath, String emuPort, String adbPort, String appPackageName) {
		
		String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
		
        String terminalCommand1 = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort + " shell monkey -p " + appPackageName + " -c android.intent.category.LAUNCHER 1");
		
	}
 
	
	public static void loadModifiedUIAutomator(String androidSDKPath, String emuPort, String adbPort, String pathToUIA) {
		
		String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
		
		
		//Remount the file system so we can modify the /system directory
		String terminalCommand1 = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort + " remount");
		System.out.println(terminalCommand1);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Push the modified binary, jar, and odex files to the system
		String terminalCommand2 = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort + " push " + pathToUIA + "uiautomator /system/bin/");
		System.out.println(terminalCommand2);
		String terminalCommand3 = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort + " push " + pathToUIA + "uiautomator.jar /system/framework/");
		System.out.println(terminalCommand3);
		String terminalCommand4 = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s emulator-" + emuPort + " push " + pathToUIA + "uiautomator.odex /system/framework/");
		System.out.println(terminalCommand4);
	}
}

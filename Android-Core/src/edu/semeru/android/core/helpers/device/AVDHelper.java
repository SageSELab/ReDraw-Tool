/**
 * Created by Kevin Moran on Dec 18, 2015
 */
package edu.semeru.android.core.helpers.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.semeru.android.core.helpers.device.StepByStepEngine;
import edu.semeru.android.core.helpers.ui.UiAutoConnector;
import edu.semeru.android.core.model.DynGuiComponentVO;
import edu.semeru.android.core.model.GUIEventVO;
import edu.wm.cs.semeru.core.helpers.TerminalHelper;

/**
 * @author KevinMoran
 *
 */
@Deprecated
public class AVDHelper {

public static void main(String[] args) {
		
		//Main class for testing
		
		String androidSDKPath = "/Applications/AndroidSDK/sdk";
		String androidAVDPath = "/Users/kevinmoran/.android/avd";
		String apkPath = "/Users/kevinmoran/Google_Drive/SEMERU/FUSION.Project/apks_for_user_study/Apps_for_User_Study/oi_notepad.apk";
		String avdAddress = "192.168.56.101";
		String avdName = "android-4.4.2";
		String packageName = "org.openintents.notepad.noteslist.NotesList";
		String adbPort = "5037";
		String virtualBoxPath = "/Applications/VirtualBox.app/Contents/MacOS/";
		String pathToGApps = "/Users/KevinMoran/Dropbox/Documents/My_Graduate_School_Work/SEMERU/TAMARIN/Google-Play-apks/";
		String pathToUIA = "/Users/KevinMoran/Desktop/uiautoautomator-modified/";
		String pathToData = "/sdcard/gnucash";
		
		//installApp(androidRoot, apkPath, packageName, avdAddress, adbPort);
		
		//removeAppData(androidSDKPath, avdAddress, adbPort, pathToData);
		//startAVD(androidSDKPath, virtualBoxPath, avdAddress, adbPort, avdName, false);
		//killAVD(virtualBoxPath, avdName, adbPort);
		//enableVirtualKeyboardNexus7(androidSDKPath, avdAddress, adbPort);
		//spinEmuScript(androidSDKPath, avdAddress, adbPort, "test-n7", true);
		//setupGApps(androidSDKPath, avdAddress, adbPort, pathToGApps);
		//unlockEmu(androidSDKPath, avdAddress, adbPort);
		//createAVD(androidAVDPath, "test-n7", "Nexus7-19");
		checkForCrash(androidSDKPath, 1200, 1870, avdAddress, adbPort, "/Users/KevinMoran/Desktop/ui_dump");
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
     * @param avdAddress: Port number of the emulator that you wish to start the .apk on.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     * @param mainActivity: The mainActivity of the app to be started.  This will be the activity started by this method.
     * 
     ***********************************************************************************************************/
	
	public static void startAPK(String androidSDKPAth, String packageName, String mainActivity, String avdAddress, String adbPort) {
        try {
            System.out.println("-- Cleaning logcat before starting APK");
            String androidToolsPath = androidSDKPAth + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("-- Starting " + packageName + " on the device");
            TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress  +":5555 shell logcat -c");
            TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress + ":5555 shell am start -n " + packageName + "/" + mainActivity);
            Thread.sleep(25000);
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
	 * @param avdAddress: Port number of the emulator that you wish to execute the command upon.
	 * 
	 * @param adbPort: The port of the adb server the target emulator is connected to.
	 * 
	 ***********************************************************************************************************/
	
	 public static void executeInputCommand(String command, String androidSdkPath, String avdAddress, String adbPort, boolean waitForTrans) {
	        String androidToolsPath = androidSdkPath + File.separator + "platform-tools" + File.separator;
	        String emuCommand = null;
	        String appTransitionState = "";
	        
	        System.out.println("--- Executing GUI event" + command);
	       
	        emuCommand = command.substring(command.indexOf("adb")+3);
	        	
	        emuCommand = androidToolsPath + "adb -P " + adbPort + " -s " + avdAddress + ":5555 " + emuCommand; 
	        System.out.println(emuCommand);
	        TerminalHelper.executeCommand(emuCommand);
	        	
	        if(waitForTrans == true){
	        
	        	do{
	        	System.out.println("-App State not Idle, waiting...");
	        	appTransitionState = getAppTransitionState(androidSdkPath, avdAddress, adbPort);
	        	try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// Catch Thread Interrupted Exception
					e.printStackTrace();
				}
	        	
	        	}while(!appTransitionState.equals("APP_STATE_IDLE"));
	        	
	        	System.out.println("-App State Idle - Ready to Continue");
	        	
	        	try {
					Thread.sleep(9000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }// End if statement to check App Idle State
	        	
	        
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
	 * @param avdAddress: Port number of the emulator that you wish to collect the coverage file from.
	 * 
	 * @param adbPort: The port of the adb server the target emulator is connected to.
	 * 
	 * @param emmaExtractionPath: The path to the location where the emma coverage file will be extracted.
	 * 
	 ***********************************************************************************************************/
	 
	public static void getCoverageFile(String androidSDKPath, String emmaFilePath, String emmaExtractionPath, String avdAddress, String adbPort)
	{
		 try {
	            System.out.println("-- Getting Coverage Files from device");
	            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
	            Runtime rt = Runtime.getRuntime();
	            rt.exec(androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress  +":5555 pull " + emmaFilePath + " " + emmaExtractionPath).waitFor();
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
     * @param avdAddress: Port number of the emulator that you wish to start the instrumented .apk on.
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
	            String coverageFile, String avdAddress, String adbPort) {
	        try {
	            // System.out.println("-- Cleaning logcat before starting APK");
	            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
	            Runtime rt = Runtime.getRuntime();
	            System.out.println("-- Starting " + packageName + " on the device");
	            // rt.exec(androidToolsPath + File.separator +
	            // "adb shell logcat -c").waitFor();
	            System.out.println(androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress  +":5555 shell am instrument -e coverageFile " + coverageFile + " "
	                    + packageName + "/instrumentation.EmmaInstrumentation");
	            rt.exec(androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress  +":5555 shell am instrument -e coverageFile " + coverageFile + " "
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
     * @param avdAddress: Port number of the emulator that you wish to stop the instrumented apk.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     ***********************************************************************************************************/
	
	public static void stopInstumentedAPK(String androidSDKPath, String packageName, String avdAddress, String adbPort) {
        try {
            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("-- Stopping " + packageName + " on the device");
            rt.exec(androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress  +":5555 shell am broadcast -a com.instrumentation.STOP").waitFor();

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
     * @param avdAddress: Port number of the emulator that you wish to start logcat collection for.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     ***********************************************************************************************************/
	
    public static void stopAPK(String androidSDKPath, String packageName, String avdAddress, String adbPort) {
        try {
            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("-- Stopping " + packageName + " on the device");
            rt.exec(androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress  +":5555 shell am force-stop " + packageName).waitFor();

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
     * @param avdAddress: Port number of the emulator that you wish to start logcat collection for.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     ***********************************************************************************************************/
    
    public static Process startLogcatCollection(String logFileName, String androidSdkPath, String avdAddress, String adbPort) {
        Runtime rt = Runtime.getRuntime();
        String androidToolsPath = androidSdkPath + File.separator + "platform-tools";
        System.out.println("--- Starting Verbose Logcat Collection");
        Process logcat = null;
        try {
            logcat = rt.exec(androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress  + ":5555 " + "logcat > " + logFileName);
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
     * @param avdAddress: Port number of the emulator that you wish to install the app from.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     * @param apkPath: The path to the .apk file of the application to be installed.
     ************************************************************************************************************/
    
    public static void installApp(String androidRoot, String apkPath, String packageName, String avdAddress, String adbPort) {
        try {
            String androidToolsPath = androidRoot + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            
           // rt.exec(androidToolsPath + File.separator + "adb install " + apkPath).waitFor();
            String output = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress  + ":5555 install " + apkPath);
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
     * @param avdAddress: Port number of the emulator that you wish to uninstall and reinstall the app from.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     * @param apkPath: the path to the .apk file to be installed.
     ************************************************************************************************************/
    
    public static void unInstallAndInstallApp(String androidRoot, String apkPath, String packageName, String avdAddress, String adbPort) {
        try {
            String androidToolsPath = androidRoot + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("--- Removing " + packageName + " from the device");
            // rt.exec(androidToolsPath + File.separator +
            // "adb shell pm uninstall " + packageName).waitFor();
            String executeCommand = TerminalHelper.executeCommand(androidToolsPath + File.separator
                    + "adb -P " +adbPort+ " -s " + avdAddress  + ":5555 shell pm uninstall " + packageName);
            System.out.println("executeCommand: " + executeCommand);
            System.out.println("--- Installing " + packageName + " apk (" + apkPath + ")");
            // rt.exec(androidToolsPath + File.separator + "adb install " +
            // apkPath).waitFor();
            executeCommand = TerminalHelper
                    .executeCommand(androidToolsPath + File.separator + "adb -P "+ adbPort +" -s " + avdAddress  + ":5555 install " + apkPath);
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
     * @param avdAddress: Port number of the emulator that you wish to get the errors from.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     * @param scriptsPAth: The path to the folder containing the logcat error helper script.
     ************************************************************************************************************/
    
    public static String getErrorsFromLogcat(String androidSDKPath, String packageName, String avdAddress, String scriptsPath, String adbPort) {
        String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
        String command = scriptsPath + File.separator +"logcat_error_helper-avd.sh " + packageName + " " + androidToolsPath + File.separator + "adb " + avdAddress + " " + adbPort;
        String error = TerminalHelper.executeCommand(command);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String clear_logcat = androidToolsPath + File.separator + "adb -P " + adbPort + " -s  " + avdAddress + ":5555 logcat -c";
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
     * @param avdAddress: Port number of the emulator that you wish to uninstall the app from.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     ************************************************************************************************************/
    
    public static void unInstallApp(String androidSDKPath, String packageName, String avdAddress, String adbPort) {
        try {
            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("--- Removing " + packageName + " from the device");
            // rt.exec(androidToolsPath + File.separator +
            // "adb shell pm uninstall " + packageName).waitFor();
            String executeCommand = TerminalHelper.executeCommand(androidToolsPath + File.separator
                    + "adb -P " + adbPort + " -s " + avdAddress  + ":5555 shell pm uninstall " + packageName);
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
     * @param avdAddress: Port number of the emulator that you wish to capture the screen of.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     ************************************************************************************************************/
    
    public static void getAndPullScreenshot(String androidSDKPath, String outputFolder, String name, String avdAddress, String adbPort) {
        try {
            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("--- Getting screenshot:" + name);
            String terminal = "";
            File testImage = null;
         // do-while to counteract the emulator behavior where the screenshot is sometimes not generated.
            // This will continually re-generate and pull the screenshot until it is not empty according to file size.
            do {
	           terminal = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s "+ avdAddress +":5555 shell /system/bin/screencap -p /sdcard/screen.png");
	           Logger.getLogger("adb-output").log(Level.INFO, terminal);
	           Thread.sleep(2000);
	            System.out.println("--- Pulling screenshot:" + name);
	            terminal = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s "+ avdAddress +":5555 pull /sdcard/screen.png " + outputFolder + File.separator
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
    
    public static void createAVD(String androidAVDPath, String avdName, String deviceType, boolean headless){
    	 //String androidToolsPath = androidAVDPath;
    	
         System.out.println("---Creating new Emulator " + avdName + " by cloning " + deviceType);
         
        //Clone the target device to the new name.
         
        String terminaloutput = TerminalHelper.executeCommand("VBoxManage clonevm \"" + deviceType + "\" --name " + avdName + " --register");
        
    }
    
    /***********************************************************************************************************
     * Method Name: unlockAVD
     * 
     * Description: This method bypasses the unlock screen for an emulator on a specific adb server and emulator port.
     * 
     * @param androidSDKPath: Path to the local install of the Android SDK.
     * 
     * @param avdAddress: Port number of the emulator that you wish to unlock.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     ***********************************************************************************************************/
    
    public static void unlockAVD(String androidSDKPath, String avdAddress, String adbPort){
    
    	System.out.println("---Unlocking " + avdAddress + ":5555 on adb server " + adbPort);
    	
    	String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
   	
        String terminaloutput = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress + ":5555 shell input keyevent 82");
        System.out.println(terminaloutput);
    
    }
    
    /***********************************************************************************************************
     * Method Name: startAVD
     * 
     * Description: Starts an emulator of a given avd name on a specific avd server (specified by the port #) and emulator port.
     * 
     * @param androidSDKPath: Path to the local install of the Android SDK.
     * 
     * @param avdAddress: Port number of the emulator that you wish to unlock.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     * @param avdName: The name of the android virtual device to started.
     * 
     * @param gpu: Specifies wether or not GPU acceleration should be enabled.  Takes a boolean argument true=on false=off. 
     ***********************************************************************************************************/
    
 public static void startAVD(String androidSDKPath, String virtualBoxPath, String avdAddress, String adbPort, String avdName, boolean headless){
        
	 	System.out.println("---Starting " + avdAddress + ":5555 on adb server " + adbPort);
	 	String bootcompleted = "0";
	 	String gui = "";
	 	
	 	//if statement to set whether the machine should be headless or not.
	 	
	 	if (headless){
	 		gui = "--type headless";
	 	}else{
	 		gui = "";
	 	}
	 	
    	String androidToolsPath = androidSDKPath + File.separator + "tools";
    	String androidPlatformToolsPath = androidSDKPath + File.separator + "platform-tools";
    	
        String terminaloutput = TerminalHelper.executeCommand(virtualBoxPath + File.separator +"VBoxManage startvm \"" + avdName + "\" " + gui);
        System.out.println(terminaloutput);
        
        //Wait for the Virtual Machine to boot.
	 	
	 	 try {
	 			Thread.sleep(45000);
	 		} catch (InterruptedException e) {
	 			// TODO Auto-generated catch block
	 			e.printStackTrace();
	 		}
        
       // Connect the virtual machine to the specified adb port.
	 	 
	 	String terminaloutput1 = TerminalHelper.executeCommand(androidPlatformToolsPath + File.separator +  "adb -P " + adbPort + " connect " + avdAddress + ":5555 ");
        System.out.println(terminaloutput1);
	 	 
    
    }
	
	/***********************************************************************************************************
 	 * Method Name: killAVD
 	 * 
 	 * Description: This method stops a currently running emulator via an adb command.
 	 * 
 	 * @param androidSDKPath: Path to the local install of the Android SDK.
 	 * 
 	 * @param avdAddress: The port of the emulator on which you wish to install the Google apps.
 	 * 
 	 * @param adbPort: The port of the adb server the emulator is attached to.
 	 * 
 	 ***********************************************************************************************************/
 
	public static void killAVD(String virtualBoxPath, String avdName, String adbPort) {
		
		System.out.println("---Killing Emulator called: " + avdName + " on ADB Server " + adbPort + "---");
		
        String terminalCommand1 = TerminalHelper.executeCommand(virtualBoxPath + "VBoxManage controlvm \"" + avdName + "\" poweroff");
        System.out.println(terminalCommand1);
       
 }
	
	/***********************************************************************************************************
 	 * Method Name: startAppByPackage
 	 * 
 	 * Description: This method starts an application by using only it's package, useful for when the main 
 	 * activity cannot be identified through apkTool or through aapt dump.
 	 * 
 	 * @param androidSDKPath: Path to the local install of the Android SDK.
 	 * 
 	 * @param avdAddress: The port of the emulator on which you wish to install the Google apps.
 	 * 
 	 * @param adbPort: The port of the adb server the emulator is attached to.
 	 * 
 	 * @param appPackageName: This is the name of the app's package that you wish to start
 	 * 
 	 ***********************************************************************************************************/
	
	public static void startAppByPackage(String androidSDKPath, String avdAddress, String adbPort, String appPackageName) {
		
		String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
		
        String terminalCommand1 = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress + ":5555 shell monkey -p " + appPackageName + " -c android.intent.category.LAUNCHER 1");
		
	}
	
	/***********************************************************************************************************
 	 * Method Name: enableVirtualKeyboard
 	 * 
 	 * Description: This method starts an application by using only it's package, useful for when the main 
 	 * activity cannot be identified through apkTool or through aapt dump.
 	 * 
 	 * @param androidSDKPath: Path to the local install of the Android SDK.
 	 * 
 	 * @param avdAddress: The port of the emulator on which you wish to install the Google apps.
 	 * 
 	 * @param adbPort: The port of the adb server the emulator is attached to.
 	 * 
 	 * @param appPackageName: This is the name of the app's package that you wish to start
 	 * 
 	 ***********************************************************************************************************/
	
	public static void enableVirtualKeyboardNexus7(String androidSDKPath, String avdAddress, String adbPort) {
		
		String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
		
		System.out.println("--- Enabling Virtual Keyboard...");
		
		String commands[] = {"input tap 609 1864",
							"input touchscreen swipe 917 0 917 917 1131",
							"input tap 1008 142",
							"input tap 537 1573",
							"input tap 554 657",
							"input tap 1048 865",
							"input tap 609 1864"};
		
		for (int i = 0; i <= 6; i++ ){
		
			System.out.println("Executing command: " + commands[i]);
			
        String terminalCommand = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress + ":5555 shell " + commands[i]);
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
				
		}
	}
	
	/***********************************************************************************************************
 	 * Method Name: getAppTransitionState
 	 * 
 	 * Description: This method gets the current animation transition state 
 	 * 
 	 * @param androidSDKPath: Path to the local install of the Android SDK.
 	 * 
 	 * @param avdAddress: The port of the emulator on which you wish to install the Google apps.
 	 * 
 	 * @param adbPort: The port of the adb server the emulator is attached to.
 	 * 
 	 * @param appPackageName: This is the name of the app's package that you wish to start
 	 * 
 	 ***********************************************************************************************************/
	
	public static String getAppTransitionState(String androidSDKPath, String avdAddress, String adbPort) {
		
		String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
		
			System.out.println("-Getting current Window Transition State...");
			//System.out.println(androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress + ":5555 shell dumpsys window -a | grep 'mAppTransitionState'");
        String terminalCommand = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress + ":5555 shell dumpsys window -a | grep 'mAppTransitionState'");
        
        terminalCommand = terminalCommand.substring(terminalCommand.indexOf("mAppTransitionState=")+20, terminalCommand.length());
        
        //System.out.println(terminalCommand);
        
        return terminalCommand;
		
				
	}
	
public static void removeAppData(String androidSDKPath, String avdAddress, String adbPort, String pathToData) {
		
		String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
		
		System.out.println("Delteing Stale Application Data at: " + pathToData);
		System.out.println(androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress + ":5555 shell rm -rf '"+ pathToData +"'");
		String terminalCommand = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress + ":5555 shell rm -rf '"+ pathToData +"'");
        
        
        System.out.println(terminalCommand);
        
	}
	
    public static String getCurrentActivityImprovedAVD(String androidSDKPath, String appPackage, String emuPort, String adbPort) {

        String emulatorConnect = "";

        String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
        // System.out.println(activity);

        if (emuPort != null && !emuPort.isEmpty()) {
            emulatorConnect = "adb -P " + adbPort + " -s " + emuPort + ":5555 ";
        }

        String command = androidToolsPath + File.separator + emulatorConnect
                + " shell dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp'";
        String test = TerminalHelper.executeCommand(command).trim();
        // System.out.println(test);
        String[] result = test.split("\n");
        String activity1 = null;
        String activity2 = null;
        try {
            activity2 = result[1].trim().split(" ")[4];
        } catch (Exception e) {
            return "com.android.launcher2.Launcher";
        }
        String packageName = activity2.substring(0, activity2.indexOf("/"));
        activity2 = packageName + activity2.replace(packageName + "/", "").replace("..", ".");

        // If current window is null then take app focused activity, this
        // happens when asking for the current window during a transition
        if (result[0].contains("mCurrentFocus=null")) {
            return activity2;
        }

        activity1 = result[0].trim().split(" ")[2].replace("}", "");

        // does it contain PopupWindow?
        if (activity1.startsWith("PopupWindow")) {
            return activity2;
        } else if (activity1.startsWith("AtchDlg:")) {
            // does it contain AtchDlg?
            activity1 = activity1.replace("AtchDlg:", "");
        }

        activity1 = packageName + activity1.replace(packageName, "").replace("/", "").replace("..", ".");

        if (!activity1.equals(activity2)) {
            return activity1;
        } else {
            return activity2;
        }
    }
    
    public static void pullFilesFromDeviceFolder(String androidSDKPath, String devicePath, String targetPath, String avdAddress, String adbPort) {
        String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
        String command = androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress + ":5555 pull " + devicePath + " " + targetPath;
        System.out.println(command);
        System.out.println(TerminalHelper.executeCommand(command));
    }
    
    public static void createDeviceFolder(String androidSDKPath, String devicePath, String avdAddress, String adbPort) {
        String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
        String command = androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress + ":5555 shell mkdir -p " + devicePath;
        System.out.println(command);
        System.out.println(TerminalHelper.executeCommand(command));
    }
    
    public static void connectAVDtoABD(String androidSDKPath, String avdAddress, String adbPort) {
        String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
        String command = androidToolsPath + File.separator + "adb -P " + adbPort + " connect " + avdAddress + ":5555";
        System.out.println(TerminalHelper.executeCommand(command));
    }
 
    public static boolean checkForCrash(String androidSDKPath, int widthScreen, int heightScreen, String avdAddress, String adbPort, String uiDumpName) {
        // Check for Crash
    	boolean crash = false;
        System.out.println("Checking for Crash...");
        ArrayList<DynGuiComponentVO> nodes = UiAutoConnector.getScreenInfoAvdOld(androidSDKPath, widthScreen,
                heightScreen, true, false, false, avdAddress, adbPort, uiDumpName);
        for (DynGuiComponentVO dynGuiComponent : nodes) {
        	//System.out.println(dynGuiComponent.getText());
            if (dynGuiComponent.getText() != null && dynGuiComponent.getText().contains("has stopped.")) {
                System.out.println("CRASH");
                crash = true;
            }
        }
        return crash;
    }
    
    public static void setAVDPortNumber(String virtualBoxPath, String avdName, String adbPort, String consolePort){
    	
    	String adbPortCommand = virtualBoxPath + File.separator + "VBoxManage modifyvm \"" + avdName + "\" --natpf1 " +"\"adb1,tcp,," + adbPort + ",,5555\"";
    	TerminalHelper.executeCommand(adbPortCommand);
    	String consolePortCommand = virtualBoxPath + File.separator + "VBoxManage modifyvm \"" + avdName + "\" --natpf1 " +"\"adb1,tcp,," + adbPort + ",,5554\"";
    	TerminalHelper.executeCommand(consolePortCommand);
    	
    }
    
public static void generateCoverageFiles(String androidSDKPath, String avdPort, String adbPort){
		String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
    	String coverageCommand = androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdPort + ":5555 shell am broadcast -a edu.semeru.android.emma.COLLECT_COVERAGE";
    	String coverageOutput = TerminalHelper.executeCommand(coverageCommand);
    	System.out.println(coverageOutput);
    	
    }
    
}

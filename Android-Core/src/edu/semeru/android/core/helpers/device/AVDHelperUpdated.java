/**
 * Created by Kevin Moran on Feb 3, 2016
 */
package edu.semeru.android.core.helpers.device;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.android.uiautomator.tree.BasicTreeNode;
import com.android.uiautomator.tree.UiHierarchyXmlLoader;

import edu.semeru.android.core.helpers.device.StepByStepEngine;
import edu.semeru.android.core.helpers.ui.UiAutoConnector;
import edu.semeru.android.core.model.DynGuiComponentVO;
import edu.semeru.android.core.model.GUIEventVO;
import edu.semeru.android.core.model.WindowVO;
import edu.wm.cs.semeru.core.helpers.TerminalHelper;



/**
 * @author KevinMoran
 *
 */
public class AVDHelperUpdated {

	public final static int CLICK = 0;
    public final static int LONG_CLICK = 1;
    public final static int SWIPE = 2;
    public final static int SWIPE_UP = 20;
    public final static int SWIPE_RIGHT = 21;
    public final static int SWIPE_DOWN = 22;
    public final static int SWIPE_LEFT = 23;
    @Deprecated
    public final static int CLICK_TYPE = 3;
    public final static int BACK = 4;
    public final static int TYPE = 5;
    public final static int OPEN_APP = 6;
    public final static int ROTATION = 7;
    public final static int GPS = 8;
    public final static int NETWORK = 9;
    public final static int CRASH = 100;
    private static final int KEYEVENT = 19;

    // check ->
    // http://stackoverflow.com/questions/7789826/adb-shell-input-events?answertab=votes#tab-top
    private final static String KEYCODE_MENU = "1";
    private final static String KEYCODE_HOME = "3";
    public final static String KEYCODE_BACK = "4";
	
	 // for contextual menu and bottom menu
    private static final String MENU_BOTTOM_AND_CONTEXTUAL = "AtchDlg";
    private static final String POPUP = "PopupWindow";
	
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
		
		//startAndConnectAVD(androidSDKPath, virtualBoxPath, 55551, , avdName, false);
		//removeAppData(androidSDKPath, avdAddress, adbPort, pathToData);
		//startAVD(androidSDKPath, virtualBoxPath, avdAddress, adbPort, avdName, false);
		//killAVD(virtualBoxPath, avdName, adbPort);
		//enableVirtualKeyboardNexus7(androidSDKPath, avdAddress, adbPort);
		//spinEmuScript(androidSDKPath, avdAddress, adbPort, "test-n7", true);
		//setupGApps(androidSDKPath, avdAddress, adbPort, pathToGApps);
		//unlockEmu(androidSDKPath, avdAddress, adbPort);
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
     * @param avdPort: Port number of the emulator that you wish to start the .apk on.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     * @param mainActivity: The mainActivity of the app to be started.  This will be the activity started by this method.
     * 
     ***********************************************************************************************************/
	
	public static void startAPK(String androidSDKPAth, String packageName, String mainActivity, String avdPort, String adbPort) {
        try {
            System.out.println("-- Cleaning logcat before starting APK");
            String androidToolsPath = androidSDKPAth + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("-- Starting " + packageName + " on the device");
            TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + avdPort  +" shell logcat -c");
            TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + avdPort + " shell am start -n " + packageName + "/" + mainActivity);
            Thread.sleep(500);
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
	 * @param avdPort: Port number of the emulator that you wish to execute the command upon.
	 * 
	 * @param adbPort: The port of the adb server the target emulator is connected to.
	 * 
	 ***********************************************************************************************************/
	
	 public static void executeInputCommand(String command, String androidSdkPath, String avdPort, String adbPort, boolean waitForTrans) {
	        String androidToolsPath = androidSdkPath + File.separator + "platform-tools" + File.separator;
	        String emuCommand = null;
	        String appTransitionState = "";
	        
	        System.out.println("--- Executing GUI event" + command);
	       
	        emuCommand = command.substring(command.indexOf("adb")+3);
	        	
	        emuCommand = androidToolsPath + "adb -P " + adbPort + " -s localhost:" + avdPort + " " + emuCommand; 
	        System.out.println(emuCommand);
	        TerminalHelper.executeCommand(emuCommand);
	        	
	        if(waitForTrans == true){
	        
	        	do{
	        	System.out.println("-App State not Idle, waiting...");
	        	appTransitionState = getAppTransitionState(androidSdkPath, avdPort, adbPort);
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
	 * @param avdPort: Port number of the emulator that you wish to collect the coverage file from.
	 * 
	 * @param adbPort: The port of the adb server the target emulator is connected to.
	 * 
	 * @param emmaExtractionPath: The path to the location where the emma coverage file will be extracted.
	 * 
	 ***********************************************************************************************************/
	 
	public static void getCoverageFile(String androidSDKPath, String emmaFilePath, String emmaExtractionPath, String avdPort, String adbPort)
	{
		 try {
	            System.out.println("-- Getting Coverage Files from device");
	            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
	            Runtime rt = Runtime.getRuntime();
	            rt.exec(androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + avdPort  +" pull " + emmaFilePath + " " + emmaExtractionPath).waitFor();
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
     * @param avdPort: Port number of the emulator that you wish to start the instrumented .apk on.
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
	            String coverageFile, String avdPort, String adbPort) {
	        try {
	            // System.out.println("-- Cleaning logcat before starting APK");
	            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
	            Runtime rt = Runtime.getRuntime();
	            System.out.println("-- Starting " + packageName + " on the device");
	            // rt.exec(androidToolsPath + File.separator +
	            // "adb shell logcat -c").waitFor();
	            System.out.println(androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + avdPort  +" shell am instrument -e coverageFile " + coverageFile + " "
	                    + packageName + "/instrumentation.EmmaInstrumentation");
	            rt.exec(androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + avdPort  +" shell am instrument -e coverageFile " + coverageFile + " "
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
     * @param avdPort: Port number of the emulator that you wish to stop the instrumented apk.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     ***********************************************************************************************************/
	
	public static void stopInstumentedAPK(String androidSDKPath, String packageName, String avdPort, String adbPort) {
        try {
            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("-- Stopping " + packageName + " on the device");
            rt.exec(androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + avdPort  +" shell am broadcast -a com.instrumentation.STOP").waitFor();

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
     * @param avdPort: Port number of the emulator that you wish to start logcat collection for.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     ***********************************************************************************************************/
	
    public static void stopAPK(String androidSDKPath, String packageName, String avdPort, String adbPort) {
        try {
            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("-- Stopping " + packageName + " on the device");
            rt.exec(androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + avdPort  +" shell am force-stop " + packageName).waitFor();

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
     * @param avdPort: Port number of the emulator that you wish to start logcat collection for.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     ***********************************************************************************************************/
    
    public static Process startLogcatCollection(String logFileName, String androidSdkPath, String avdPort, String adbPort) {
        Runtime rt = Runtime.getRuntime();
        String androidToolsPath = androidSdkPath + File.separator + "platform-tools";
        System.out.println("--- Starting Verbose Logcat Collection");
        Process logcat = null;
        try {
            logcat = rt.exec(androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + avdPort  + " " + "logcat > " + logFileName);
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
     * @param avdPort: Port number of the emulator that you wish to install the app from.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     * @param apkPath: The path to the .apk file of the application to be installed.
     ************************************************************************************************************/
    
    public static void installApp(String androidRoot, String apkPath, String packageName, String avdPort, String adbPort) {
        try {
            String androidToolsPath = androidRoot + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("--- Installing " + packageName + " apk (" + apkPath + ") on emulator " + avdPort);
           // rt.exec(androidToolsPath + File.separator + "adb install " + apkPath).waitFor();
            String output = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + avdPort  + " install " + apkPath);
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
     * @param avdPort: Port number of the emulator that you wish to uninstall and reinstall the app from.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     * @param apkPath: the path to the .apk file to be installed.
     ************************************************************************************************************/
    
    public static void unInstallAndInstallApp(String androidRoot, String apkPath, String packageName, String avdPort, String adbPort) {
        try {
            String androidToolsPath = androidRoot + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("--- Removing " + packageName + " from the device");
            // rt.exec(androidToolsPath + File.separator +
            // "adb shell pm uninstall " + packageName).waitFor();
            String executeCommand = TerminalHelper.executeCommand(androidToolsPath + File.separator
                    + "adb -P " +adbPort+ " -s localhost:" + avdPort  + " shell pm uninstall " + packageName);
            System.out.println("executeCommand: " + executeCommand);
            System.out.println("--- Installing " + packageName + " apk (" + apkPath + ")");
            // rt.exec(androidToolsPath + File.separator + "adb install " +
            // apkPath).waitFor();
            executeCommand = TerminalHelper
                    .executeCommand(androidToolsPath + File.separator + "adb -P "+ adbPort +" -s localhost:" + avdPort  + " install " + apkPath);
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
     * @param avdPort: Port number of the emulator that you wish to get the errors from.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     * @param scriptsPAth: The path to the folder containing the logcat error helper script.
     ************************************************************************************************************/
    
    public static String getErrorsFromLogcat(String androidSDKPath, String packageName, String avdPort, String scriptsPath, String adbPort) {
        String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
        String command = scriptsPath + File.separator +"logcat_error_helper-avd.sh " + packageName + " " + androidToolsPath + File.separator + "adb " + avdPort + " " + adbPort;
        String error = TerminalHelper.executeCommand(command);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String clear_logcat = androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + avdPort + " logcat -c";
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
     * @param avdPort: Port number of the emulator that you wish to uninstall the app from.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     ************************************************************************************************************/
    
    public static void unInstallApp(String androidSDKPath, String packageName, String avdPort, String adbPort) {
        try {
            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("--- Removing " + packageName + " from the device");
            // rt.exec(androidToolsPath + File.separator +
            // "adb shell pm uninstall " + packageName).waitFor();
            String executeCommand = TerminalHelper.executeCommand(androidToolsPath + File.separator
                    + "adb -P " + adbPort + " -s localhost:" + avdPort  + " shell pm uninstall " + packageName);
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
     * @param avdPort: Port number of the emulator that you wish to capture the screen of.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     ************************************************************************************************************/
    
    public synchronized static void getAndPullScreenshot(String androidSDKPath, String outputFolder, String name, String avdPort, String adbPort) {
        try {
            String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
            Runtime rt = Runtime.getRuntime();
            System.out.println("--- Getting screenshot:" + name);
            String terminal = "";
            File testImage = null;
         // do-while to counteract the emulator behavior where the screenshot is sometimes not generated.
            // This will continually re-generate and pull the screenshot until it is not empty according to file size.
            do {
	           terminal = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:"+ avdPort +" shell /system/bin/screencap -p /sdcard/screen.png");
	           Logger.getLogger("adb-output").log(Level.INFO, terminal);
	           Thread.sleep(2000);
	            System.out.println("--- Pulling screenshot:" + name);
	            terminal = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:"+ avdPort +" pull /sdcard/screen.png " + outputFolder + File.separator
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
         
        //Clone the target device to the new name.
         
        String terminaloutput = TerminalHelper.executeCommand("VBoxManage clonevm \"" + deviceType + "\" --name " + avdName + " --register");
        
    }
    
    /***********************************************************************************************************
     * Method Name: startAVD
     * 
     * Description: Starts an emulator of a given avd name on a specific avd server (specified by the port #) and emulator port.
     * 
     * @param androidSDKPath: Path to the local install of the Android SDK.
     * 
     * @param avdPort: Port number of the emulator that you wish to start and connect to.
     * 
     * @param adbPort: The port of the adb server the target emulator is connected to.
     * 
     * @param avdName: The name of the android virtual device to started.
     * 
     * @param gpu: Specifies wether or not GPU acceleration should be enabled.  Takes a boolean argument true=on false=off. 
     ***********************************************************************************************************/
    
 public static void startAndConnectAVD(String androidSDKPath, String virtualBoxPath, String avdPort, String adbPort, String avdName, boolean headless){
        
	 	System.out.println("---Starting " + avdPort + " on adb server " + adbPort);
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
	 			Thread.sleep(90000);
	 		} catch (InterruptedException e) {
	 			// TODO Auto-generated catch block
	 			e.printStackTrace();
	 		}
        
       // Connect the virtual machine to the specified adb port.
	 	 
	 	String terminaloutput1 = TerminalHelper.executeCommand(androidPlatformToolsPath + File.separator +  "adb -P " + adbPort + " connect localhost:" + avdPort + " ");
        System.out.println(terminaloutput1);
	 	 
    
    }

	
	/***********************************************************************************************************
 	 * Method Name: killAVD
 	 * 
 	 * Description: This method stops a currently running emulator via an adb command.
 	 * 
 	 * @param androidSDKPath: Path to the local install of the Android SDK.
 	 * 
 	 * @param avdName: The name of the avd you wish to kill.
 	 * 
 	 * @param adbPort: The port of the adb server the emulator is attached to.
 	 * 
 	 ***********************************************************************************************************/
 
	public static void killAVD(String virtualBoxPath, String avdName) {
		
		System.out.println("---Killing Emulator called: " + avdName);
		
        String terminalCommand1 = TerminalHelper.executeCommand(virtualBoxPath + "VBoxManage controlvm \"" + avdName + "\" poweroff");
        System.out.println(terminalCommand1);
       
 }
	
	/***********************************************************************************************************
 	 * Method Name: pauseAVD
 	 * 
 	 * Description: This method pauses a currently running emulator via an adb command.
 	 * 
 	 * @param androidSDKPath: Path to the local install of the Android SDK.
 	 * 
 	 * @param avdName: The name of the avd you wish to kill.
 	 * 
 	 * @param adbPort: The port of the adb server the emulator is attached to.
 	 * 
 	 ***********************************************************************************************************/
 
	public static void pauseAVD(String virtualBoxPath, String avdName) {
		
		System.out.println("---Killing Emulator called: " + avdName);
		
        String terminalCommand1 = TerminalHelper.executeCommand(virtualBoxPath + "VBoxManage controlvm \"" + avdName + "\" savestate");
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
 	 * @param avdPort: The port of the emulator on which you wish to start the app on.
 	 * 
 	 * @param adbPort: The port of the adb server the emulator is attached to.
 	 * 
 	 * @param appPackageName: This is the name of the app's package that you wish to start
 	 * 
 	 ***********************************************************************************************************/
	
	public static void startAppByPackage(String androidSDKPath, String avdPort, String adbPort, String appPackageName) {
		
		String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
		
        String terminalCommand1 = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + avdPort + " shell monkey -p " + appPackageName + " -c android.intent.category.LAUNCHER 1");
		
	}
	
	/***********************************************************************************************************
 	 * Method Name: enableVirtualKeyboardNexus7
 	 * 
 	 * Description: This method starts an application by using only it's package, useful for when the main 
 	 * activity cannot be identified through apkTool or through aapt dump.
 	 * 
 	 * @param androidSDKPath: Path to the local install of the Android SDK.
 	 * 
 	 * @param avdPort: The port of the emulator on which you wish to enable the virtual keyboard.
 	 * 
 	 * @param adbPort: The port of the adb server the emulator is attached to.
 	 * 
 	 * @param appPackageName: This is the name of the app's package that you wish to start
 	 * 
 	 ***********************************************************************************************************/
	
	public static void enableVirtualKeyboardNexus7(String androidSDKPath, String avdPort, String adbPort) {
		
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
			
        String terminalCommand = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + avdPort + " shell " + commands[i]);
		
		try {
			Thread.sleep(15000);
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
 	 * @param avdPort: The port of the emulator on which you wish to get the current transition state.
 	 * 
 	 * @param adbPort: The port of the adb server the emulator is attached to.
 	 * 
 	 * @param appPackageName: This is the name of the app's package that you wish to start
 	 * 
 	 ***********************************************************************************************************/
	
	public static String getAppTransitionState(String androidSDKPath, String avdPort, String adbPort) {
		
		String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
		
			System.out.println("-Getting current Window Transition State...");
			//System.out.println(androidToolsPath + File.separator + "adb -P " + adbPort + " -s " + avdAddress + " shell dumpsys window -a | grep 'mAppTransitionState'");
        String terminalCommand = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + avdPort + " shell dumpsys window -a | grep 'mAppTransitionState'");
        
        terminalCommand = terminalCommand.substring(terminalCommand.indexOf("mAppTransitionState=")+20, terminalCommand.length());
        
        //System.out.println(terminalCommand);
        
        return terminalCommand;
		
				
	}
	
	/***********************************************************************************************************
 	 * Method Name: removeAppData
 	 * 
 	 * Description: This removes target external application data (e.g. saved on sdcard) on an Android device.
 	 * 
 	 * @param androidSDKPath: Path to the local install of the Android SDK.
 	 * 
 	 * @param avdPort: The port of the emulator on which you wish to remove data.
 	 * 
 	 * @param adbPort: The port of the adb server the emulator is attached to.
 	 * 
 	 * @param pathToData: This is the path of the data that you wish to delete on the device
 	 * 
 	 ***********************************************************************************************************/
	
public static void removeAppData(String androidSDKPath, String avdPort, String adbPort, String pathToData) {
		
		String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
		
		System.out.println("Delteing Stale Application Data at: " + pathToData);
		System.out.println(androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + avdPort + " shell rm -rf '"+ pathToData +"'");
		String terminalCommand = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + avdPort + " shell rm -rf '"+ pathToData +"'");
        
        
        System.out.println(terminalCommand);
        
	}
	
/***********************************************************************************************************
	 * Method Name: getCurrentActivityAVD
	 * 
	 * Description: This method returns the current focused activity for an Android device.
	 * 
	 * @param androidSDKPath: Path to the local install of the Android SDK.
	 * 
	 * @param avdPort: The port of the emulator on which you wish to get the current activity.
	 * 
	 * @param adbPort: The port of the adb server the emulator is attached to.
	 * 
	 ***********************************************************************************************************/

    public static String getCurrentActivityAVD(String androidSDKPath, String avdPort, String adbPort) {

        String emulatorConnect = "";

        String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
        // System.out.println(activity);

        if (avdPort != null && !avdPort.isEmpty()) {
            emulatorConnect = "adb -P " + adbPort + " -s loclahost:" + avdPort + " ";
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
    
    /***********************************************************************************************************
	 * Method Name: pullFilesFromDeviceFolder
	 * 
	 * Description: This method pulls all the files from a folder on a target device.
	 * 
	 * @param androidSDKPath: Path to the local install of the Android SDK.
	 * 
	 * @param avdPort: The port of the emulator on which you wish to pull files from.
	 * 
	 * @param adbPort: The port of the adb server the emulator is attached to.
	 * 
	 * @param devicePath: The path to folder on the device from which you want to pull the contents.
	 * 
	 * @param targetPath: The path to location you would like the files extracted on the home machine.
	 * 
	 ***********************************************************************************************************/
    
    public static void pullFilesFromDeviceFolder(String androidSDKPath, String devicePath, String targetPath, String avdPort, String adbPort) {
        String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
        String command = androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + avdPort + " pull " + devicePath + " " + targetPath;
        System.out.println(command);
        System.out.println(TerminalHelper.executeCommand(command));
    }
    
    /***********************************************************************************************************
	 * Method Name: createDeviceFolder
	 * 
	 * Description: This method creates a Folder with a specified path on the target device.
	 * 
	 * @param androidSDKPath: Path to the local install of the Android SDK.
	 * 
	 * @param avdPort: The port of the emulator on which you wish to create a folder on.
	 * 
	 * @param adbPort: The port of the adb server the emulator is attached to.
	 * 
	 * @param devicePath: The path to folder on the device that you want to create.
	 * 
	 ***********************************************************************************************************/
    
    public static void createDeviceFolder(String androidSDKPath, String devicePath, String avdPort, String adbPort) {
        String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
        String command = androidToolsPath + File.separator + "adb -P " + adbPort + " -s loclahost:" + avdPort + " shell mkdir -p " + devicePath;
        System.out.println(command);
        System.out.println(TerminalHelper.executeCommand(command));
    }
    
    /***********************************************************************************************************
	 * Method Name: connectAVDtoADB
	 * 
	 * Description: This method connects a currently running android device to a running adb server at a specified port.
	 * 
	 * @param androidSDKPath: Path to the local install of the Android SDK.
	 * 
	 * @param avdPort: The port of the emulator that you wish to connect to adb.
	 * 
	 * @param adbPort: The port of the adb server the emulator is attached to.
	 * 
	 ***********************************************************************************************************/
    
    public static void connectAVDtoABD(String androidSDKPath, String avdPort, String adbPort) {
        String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
        String command = androidToolsPath + File.separator + "adb -P " + adbPort + " connect localhost:" + avdPort + "";
        System.out.println(TerminalHelper.executeCommand(command));
    }
 
    
    /***********************************************************************************************************
	 * Method Name: checkForCrash
	 * 
	 * Description: This method checks to see whether the currently running application on a target device has crashed.
	 * 
	 * @param androidSDKPath: Path to the local install of the Android SDK.
	 * 
	 * @param avdPort: The port of the emulator on which you wish to check for a crash.
	 * 
	 * @param adbPort: The port of the adb server the emulator is attached to.
	 * 
	 * @param appPackage: The package of the currently running application that you wish to check the crash status.
	 * 
	 ***********************************************************************************************************/
    
    public static boolean checkForCrash(String appPackage, String mainActivity, String androidSDKPath, int widthScreen, int heightScreen, String avdAddress, String adbPort, String uiDumpName) {
        // Check for Crash
    	boolean crash = false;
        System.out.println("Checking for Crash...");
        ArrayList<DynGuiComponentVO> nodes = getScreenInfoAVD(androidSDKPath, widthScreen,
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
    
    /***********************************************************************************************************
	 * Method Name: setAVDPortNumber
	 * 
	 * Description: This method checks to see whether the currently running application on a target device has crashed.
	 * 
	 * @param virtualBoxPath: Path to the VBoxManage executable
	 * 
	 * @param avdName: The name of the AVD for which you would like to set the port numbers
	 * 
	 * @param adbPort: The desired port number to connect to adb for this avd on the host machine
	 * 
	 * @param consolePort: The desired number of the console port of the avd on the host machine.
	 * 
	 ***********************************************************************************************************/
    
    public static void setAVDPortNumber(String virtualBoxPath, String avdName, String adbPort, String consolePort){
    	
    	String adbPortCommand = virtualBoxPath + File.separator + "VBoxManage modifyvm \"" + avdName + "\" --natpf1 " +"\"adb1,tcp,," + adbPort + ",,5555\"";
    	TerminalHelper.executeCommand(adbPortCommand);
    	String consolePortCommand = virtualBoxPath + File.separator + "VBoxManage modifyvm \"" + avdName + "\" --natpf1 " +"\"adb1,tcp,," + adbPort + ",,5554\"";
    	TerminalHelper.executeCommand(consolePortCommand);
    	
    }
	
    /***********************************************************************************************************
 	 * Method Name: getScreenInfoAVD
 	 * 
 	 * Description: This method gets the current list of GUI components displayed on the screen.  It returns an ArrayList
 	 * of type DynGuiComponent.
 	 * 
 	 * @param androidSDKPath: Path to the local install of the Android SDK.
 	 * 
 	 * @param widthScreen: The width of the target device screen in pixels. 
 	 * 
 	 * @param heightScreen: The height of the target device screen in pixels.
 	 * 
 	 * @param all:  
 	 * 
 	 * @param cache: 
 	 * 
 	 * @param isLists:
 	 *  
 	 * @param avdPort: The port of the emulator on which you wish to get the screen info.
	 * 
	 * @param adbPort: The port of the adb server the emulator is attached to.
	 * 
	 * @param name: the name of the temporary ui-dump file.
 	 * 
 	 ***********************************************************************************************************/
    
    private static ArrayList<DynGuiComponentVO> getScreenInfoAVD(String androidSDKPath, int widthScreen, int heightScreen,
            boolean all, boolean cache, boolean isLists, String avdPort, String adbPort, String name) {
        ArrayList<DynGuiComponentVO> list = new ArrayList<DynGuiComponentVO>();

        BasicTreeNode tree = getTreeFromXmlAVD(androidSDKPath, cache, avdPort, adbPort, name);

        String currentActivity = getCurrentActivityImproved(androidSDKPath, null, avdPort, adbPort);
        // int indexOf = currentActivity.indexOf("/");
        // String appPackage = currentActivity.substring(0, indexOf);
        // currentActivity = appPackage + "." +
        // currentActivity.replace(appPackage, "").replace("/", "");
        visitNodes(currentActivity, tree, list, widthScreen, heightScreen, all, null, isLists, 0);
        boolean isOffset = false;
        boolean isCalendarWindow = false;
        int offset = 0;
        for (DynGuiComponentVO vo : list) {
            if (vo.getOffset() != 0) {
                offset = vo.getOffset();
                isOffset = true;
                // break;
            }
            if (vo.isCalendarWindow()) {
                isCalendarWindow = true;
            }
        }
        // // Remove hidden component on date picker
        // if (index != -1) {
        // list.remove(index);
        // }
        if (isOffset || isCalendarWindow) {
            for (DynGuiComponentVO vo : list) {
                vo.setPositionY(vo.getPositionY() + offset);
                vo.setCalendarWindow(isCalendarWindow);
            }
        }

        return list;
    }
    
    public static String getCurrentActivityImproved(String androidSDKPath, String appPackage, String emuPort, String adbPort) {

        String emulatorConnect = "";

        String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
        // System.out.println(activity);

        if (emuPort != null && !emuPort.isEmpty()) {
            emulatorConnect = " -P " + adbPort + " -s localhost:" + emuPort;
        }

        String command = androidToolsPath + File.separator + "adb" +emulatorConnect
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

        if (activity1.contains("/")) {
            packageName = activity1.substring(0, activity1.indexOf("/"));
        }
        
        activity1 = packageName + activity1.replace(packageName, "").replace("/", "").replace("..", ".");

        if (!activity1.equals(activity2)) {
            return activity1;
        } else {
            return activity2;
        }
    }
    
    private synchronized static BasicTreeNode getTreeFromXmlAVD(String androidSDKPath, boolean cache, String avdPort, String adbPort, String name) {
        if (!cache) {
        	File testDump = null;
        	 String adb = androidSDKPath + File.separator + "platform-tools" + File.separator + "adb";
             String terminal = TerminalHelper.executeCommand(adb + " -P " + adbPort + " -s localhost:" + avdPort + " shell mkdir /sdcard/uimonkeyautomator");
             int i = 1;
        		System.out.println("---Grabbing ui-dump " + i + " time(s).");
	            terminal = TerminalHelper.executeCommand(adb + " -P " + adbPort + " -s localhost:" + avdPort + " shell /system/bin/uiautomator dump /sdcard/uimonkeyautomator/ui_dump.xml");
	            terminal = TerminalHelper.executeCommand(adb + " -P " + adbPort + " -s localhost:" + avdPort + " pull /sdcard/uimonkeyautomator/ui_dump.xml " + name + ".xml");
	        	testDump = new File(name);
	        	i++;
        }
        // System.out.println(terminal);
        UiHierarchyXmlLoader loader = new UiHierarchyXmlLoader();
        String xmlPath = name + ".xml";
        BasicTreeNode tree = loader.parseXml(xmlPath); 
        return tree;
    }
    
    public static void generateCoverageFiles(String androidSDKPath, String avdPort, String adbPort){
		String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
    	String coverageCommand = androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + avdPort + " shell am broadcast -a edu.semeru.android.emma.COLLECT_COVERAGE";
    	String coverageOutput = TerminalHelper.executeCommand(coverageCommand);
    	System.out.println(coverageOutput);
    	
    }
    
    public static String getAppVersionAdb(String androidSDKPath, String appPackage, String avdPort, String adbPort) {
        String result = "";
        try {
            String androidToolsPath = androidSDKPath + File.separator + "platform-tools" + File.separator;
            String command = androidToolsPath + "adb -P " + adbPort + " -s localhost:" + avdPort
                    + " shell dumpsys package " + appPackage + " | grep versionName";
            String version = TerminalHelper.executeCommand(command);
            return version.substring(version.indexOf("=") + 1, version.length()).trim();
        } catch (Exception ex) {
            Logger.getLogger(AVDHelperUpdated.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public static WindowVO detectTypeofWindow(String androidSDKPath, int widthScreen, int heightScreen, String emuPort, String adbPort) {
        WindowVO vo = new WindowVO();
        String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
        // System.out.println(activity);
        String command = androidToolsPath + File.separator
                + "adb -P " + adbPort + " -s localhost:" + emuPort+" shell dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp' | awk '{print $3 }'";

        String result = TerminalHelper.executeCommand(command).split("\n")[0];
        result = result.substring(0, result.length() - 1);
        String activity = getCurrentActivityImproved(androidSDKPath, "", emuPort, adbPort);
        String window = "";
        if (result.contains(MENU_BOTTOM_AND_CONTEXTUAL)) {
            // It's contextual menu or it's in the bottom
            window += "MENU:";
        } else if (result.contains(POPUP)) {
            // It's option menu in the top, popup windows from spinners and
            // other android components
            window += "POPUP:";
        }

        boolean isAlert = false;
        // It's a normal activity or an AlertDialog
        DynGuiComponentVO root = getScreenInfoHierarchyAVD(androidSDKPath, new StringBuilder(), widthScreen, heightScreen, false,
                        emuPort, adbPort, emuPort + "ui_dump").getChildren().get(0);

        DynGuiComponentVO title = UiAutoConnector.getComponentByIdAndType("id/title", "TextView", root);
        if (title != null && title.getParent() != null && title.getParent().getName().endsWith("LinearLayout")
                && title.getParent().getIdXml().isEmpty()) {
            isAlert = true;
        } else {
            title = UiAutoConnector.getComponentByIdAndType("id/alertTitle", "TextView", root);
            if (title != null) {
                isAlert = true;
            }
        }

        if (isAlert) {
            window += "ALERT:";
        } else {
            title = UiAutoConnector.getComponentByIdAndType("id/action_bar_title", "TextView", root);
            if (title == null) {
                title = UiAutoConnector.getComponentByIdAndType("id/title", "TextView", root);
            }
        }

        String fragment = getCurrentFragment(androidSDKPath, activity, emuPort, adbPort);
        if (!fragment.isEmpty()) {
            window += "FRAGMENT:" + fragment;
        } else {
            window += "ACTIVITY:" + activity;
        }
        vo.setWindow(window);
        if (!window.contains("MENU:") && title != null) {
            vo.setTitle(title.getText());
        }
        return vo;
    }
    
    public static String getCurrentFragment(String androidSDKPath, String currentActivity, String emuPort,
            String adbPort) {
        String result = "";
        String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
        // System.out.println(activity);
        String command = androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + emuPort
                + " shell dumpsys activity " + currentActivity;
        int max = -1;

        String[] executeCommand = TerminalHelper.executeCommand(command).split("\n");

        for (String line : executeCommand) {
            if (line.startsWith("      #") && !line.contains("null") && line.contains("{")) {
                String temp = line.trim();
                String key = temp.substring(temp.indexOf("#") + 1, temp.indexOf(":"));
                String fragment = temp.substring(temp.indexOf(":"), temp.indexOf("{")).trim();
                int value = Integer.valueOf(key);
                if (max < value) {
                    max = value;
                    // result = key + "#" + fragment;
                    result = fragment.replace(": ", "").trim();
                }
            }
        }
        return result;
    }
    
    public static int getOrientation(String androidSDKPath, String emuPort, String adbPort) {
        String line = null;
        String adb = androidSDKPath + File.separator + "platform-tools" + File.separator + "adb";
        try {
            String command = adb + " -P " + adbPort + " -s localhost:" + emuPort
                    + " shell dumpsys input | grep 'SurfaceOrientation' |  awk '{ print $2 }'";
            
            line = TerminalHelper.executeCommand(command);
            if (line == null || (line != null && line.isEmpty())) {
                return 0;
            }

        } catch (Exception ex) {
            Logger.getLogger(AVDHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Integer.parseInt(line);
    }
    
    public static String getScreenSize(String androidSDKPath, String emuPort, String adbPort) {
        String result = "";
        try {
            String androidToolsPath = androidSDKPath + File.separator + "platform-tools" + File.separator;
            String command = androidToolsPath + "adb -P " + adbPort + " -s localhost:" + emuPort
                    + " shell dumpsys window | grep \"mUnrestrictedScreen\"|  awk '{ print $2 }'";
            return TerminalHelper.executeCommand(command);
        } catch (Exception ex) {
            Logger.getLogger(AVDHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public static void restartADBServer(String androidSDKPath, String adbPort) {
        String result = "";
        try {
            String androidToolsPath = androidSDKPath + File.separator + "platform-tools" + File.separator;
            String command = androidToolsPath + "adb -P kill-server";
            TerminalHelper.executeCommand(command);
            Thread.sleep(3000);
            command = androidToolsPath + "adb -P " + adbPort + " devices";
            TerminalHelper.executeCommand(command);
            Thread.sleep(3000);
           
        } catch (Exception ex) {
            Logger.getLogger(AVDHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
    
    public static DynGuiComponentVO getScreenInfoHierarchyAVD(String androidSDKPath, StringBuilder builder, int widthScreen, int heightScreen,
            boolean cache, String emuPort, String adbPort, String name) {
        DynGuiComponentVO parent = null;

        BasicTreeNode tree = getTreeFromXmlAVD(androidSDKPath, cache, emuPort, adbPort, name);

        String currentActivity = getCurrentActivityImproved(androidSDKPath, null, emuPort, adbPort);
        // int indexOf = currentActivity.indexOf("/");
        // String appPackage = currentActivity.substring(0, indexOf);
        // currentActivity = appPackage + "." +
        // currentActivity.replace(appPackage, "").replace("/", "");
        return visitNodes(currentActivity, tree, widthScreen, heightScreen, parent, builder);

    }
    
    public static void cleanUiAutomator(String androidSDKPath, String emuPort, String adbPort) {
        String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
        
        String terminalCommand = TerminalHelper.executeCommand(androidToolsPath + File.separator + "adb -P " + adbPort
                + " -s localhost:" + emuPort + " shell rm /sdcard/uimonkeyautomator/ui_dump.xml");
        
    }
    
    public static String doType(String androidRoot, String text, String packageName, String emuPort, String adbPort) {

        String androidToolsPath = androidRoot + File.separator + "platform-tools";
        String command = androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + emuPort
                + " shell input text '" + text + "'";
        System.out.println("--- Executing GUI event " + command);
        TerminalHelper.executeCommand(command);
        return command;

    }
    
    public static String doKey(String androidRoot, String keyEvent, String packageName, String emuPort, String adbPort) {
        String command = "adb -P " + adbPort + " -s localhost:" + emuPort + " shell input keyevent '" + keyEvent + "'";

        String androidToolsPath = androidRoot + File.separator + "platform-tools";
        TerminalHelper.executeCommand(androidToolsPath + File.separator + command);
        System.out.println("--- Executing GUI event " + command);
        return command;
    }
    
    public static String executeEvent(GUIEventVO vo, String androidRoot, String packageName, String executionType,
            boolean hideKeyboard, String emuPort, String adbPort) {
        String command = null;
        switch (vo.getEventTypeId()) {
        case AVDHelperUpdated.CLICK:
            command = "adb -P " + adbPort + " -s localhost:" + emuPort + " shell input tap " + vo.getRealFinalX() + " "
                    + vo.getRealFinalY();
            break;
        case AVDHelperUpdated.LONG_CLICK:
            command = "adb -P " + adbPort + " -s localhost:" + emuPort + " shell input touchscreen swipe  "
                    + vo.getRealInitialX() + " " + vo.getRealInitialY() + " " + vo.getRealInitialX() + " "
                    + vo.getRealInitialY() + " 2000";
            break;

        case AVDHelperUpdated.SWIPE:
            command = "adb -P " + adbPort + " -s localhost:" + emuPort + " shell input touchscreen swipe  "
                    + vo.getRealInitialX() + " " + vo.getRealInitialY() + " " + vo.getRealFinalX() + " "
                    + vo.getRealFinalY() + " " + (int) (vo.getDuration() * 1000);
            break;

        case AVDHelperUpdated.CLICK_TYPE:
            command = "adb -P " + adbPort + " -s localhost:" + emuPort + " shell input tap " + vo.getRealFinalX() + " "
                    + vo.getRealFinalY();
            break;

        case AVDHelperUpdated.TYPE:
            int inputType = InputHelper.checkInputType(androidRoot,adbPort + " -s localhost:" + emuPort);
            String input = InputHelper.generateInput(inputType, executionType);
            vo.getHvInfoComponent().setText(input);
            vo.setText(input);
            command = "adb -P " + adbPort + " -s localhost:" + emuPort + " shell input text " + input;
            break;

        case AVDHelperUpdated.BACK:
            command = "adb -P " + adbPort + " -s localhost:" + emuPort + " shell input keyevent "
                    + AVDHelperUpdated.KEYCODE_BACK;
            break;

        }
        if (command != null) {
            try {
                String androidToolsPath = androidRoot + File.separator + "platform-tools";
                Runtime rt = Runtime.getRuntime();
                System.out.println("--- Executing GUI event " + command);
                rt.exec(androidToolsPath + File.separator + command).waitFor();
                if (vo != null && vo.getEventTypeId() == AVDHelperUpdated.CLICK_TYPE) {
                    if (isKeyboardActive(androidRoot, emuPort, adbPort) && hideKeyboard) {
                        disposeKeyboard(androidRoot, emuPort, adbPort);
                    }
                    // System.out.println("Generating Input ;)");
                    int inputType = InputHelper.checkInputType(androidRoot,adbPort + " -s localhost:" + emuPort);
                    String input = InputHelper.generateInput(inputType, executionType);
                    String back = "67 67 67 67 67 67 67 67 67 67 67 67 67 67 67 67 67 67 67 67 67 67"
                            + " 67 67 67 67 67 67 67 67 67 67 67 67 67 67 67 67 67 67 67 67 67 67 67 67";
                    command = command + "\n";
                    command += doKey(androidRoot, back, packageName, emuPort, adbPort) + "\n";
                    command += doType(androidRoot, input, packageName, emuPort, adbPort) + "\n";
                    return command;
                }

            } catch (Exception ex) {
                Logger.getLogger(AVDHelperUpdated.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String appTransitionState = null;
        do {
            System.out.println("-App State not Idle, waiting...");
            appTransitionState = getAppTransitionState(androidRoot, emuPort, adbPort);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Catch Thread Interrupted Exception
                e.printStackTrace();
            }

        } while (!appTransitionState.equals("APP_STATE_IDLE"));
        return command;
    }
    
    public static void disposeKeyboard(String androidSDKPath, String emuPort, String adbPort) {
        String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
        String command = androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + emuPort
                + " shell input keyevent 111";
        TerminalHelper.executeCommand(command);
    }
    
    public static boolean isKeyboardActive(String androidSDKPath, String emuPort, String adbPort) {
        String androidToolsPath = androidSDKPath + File.separator + "platform-tools";
        String command = androidToolsPath + File.separator + "adb -P " + adbPort + " -s localhost:" + emuPort
                + " shell dumpsys input_method|grep mInputShown| awk '{print $4 }'";
        String line = TerminalHelper.executeCommand(command);
        if (line != null && !line.isEmpty() && line.contains("true")) {
            return true;
        }
        return false;
    }
    
    public static void visitNodes(String currentActivity, BasicTreeNode node, ArrayList<DynGuiComponentVO> list,
            int widthScreen, int heightScreen, boolean all, DynGuiComponentVO parent, boolean includeLists, int offset) {
        DynGuiComponentVO vo = Utilities.getHVComponentFromBasicTreeNode(node, widthScreen, heightScreen,
                currentActivity, parent);
        if (vo.getName().contains("android.widget.DatePicker") && vo.getIdXml().contains("id/datePicker")) {
            vo.setCalendarWindow(true);
        }
        if (parent != null && parent.isCalendarWindow()) {
            vo.setCalendarWindow(true);
        }
        // Fixing bug of UIAutomator temporally for Nexus 7
        if ((vo.getName().contains("android.widget.DatePicker") && vo.getIdXml().contains("id/datePicker"))
                || (vo.getName().contains("android.widget.TimePicker") && vo.getIdXml().contains("id/timePicker"))) {
            if ((vo.getPositionX() == 75 && vo.getPositionY() == 225)
                    || (vo.getPositionX() == 152 && vo.getPositionY() == 323)) {
                // Android 5.x
                offset = 347;
            } else if ((vo.getPositionX() == 52 && vo.getPositionY() == 322)
                    || (vo.getPositionX() == 52 && vo.getPositionY() == 390)) {
                // Android 4.x
                offset = 346;
            } else if ((vo.getPositionX() == 52 && vo.getPositionY() == 366)
                    || (vo.getPositionX() == 52 && vo.getPositionY() == 434)) {
                // Android 4.x
                offset = 302;
            } else if ((vo.getPositionX() == 68 && vo.getPositionY() == 308)
                    || (vo.getPositionX() == 68 && vo.getPositionY() == 376)) {
                // Android 2.x
                offset = 302;
            }
        }
        vo.setOffset(offset);
        if ((vo.isClickable() || vo.isLongClickable() || vo.isCheckable()
        // seek bar is not clickable nor longclickable nor checkeable
                || (vo.getName().equals("android.widget.FrameLayout") || all) || vo.getName().equals(
                "android.widget.SeekBar"))
                // discards the list view but includes children
                && vo.getPositionY() < heightScreen
                && (!vo.getName().equals("android.widget.ListView") || includeLists)) {
            list.add(vo);
        }
        // System.out.println("------------");
        for (BasicTreeNode child : node.getChildren()) {
            visitNodes(currentActivity, child, list, widthScreen, heightScreen, all, vo, includeLists, offset);
        }
    }
    
    public static DynGuiComponentVO visitNodes(String currentActivity, BasicTreeNode node, int widthScreen,
            int heightScreen, DynGuiComponentVO parent, StringBuilder builder) {
        DynGuiComponentVO vo = Utilities.getHVComponentFromBasicTreeNode(node, widthScreen, heightScreen,
                currentActivity, parent);
        // System.out.println("------------");
        builder.append("<" + vo.getName() + "_" + vo.getIdXml().replace("id/", "") + ">");
        for (BasicTreeNode child : node.getChildren()) {
            vo.setParent(parent);
            DynGuiComponentVO component = visitNodes(currentActivity, child, widthScreen, heightScreen, vo, builder);
            vo.addChild(component);
        }
        builder.append("</" + vo.getName() + "_" + vo.getIdXml().replace("id/", "") + ">");
        return vo;
    }
    
}

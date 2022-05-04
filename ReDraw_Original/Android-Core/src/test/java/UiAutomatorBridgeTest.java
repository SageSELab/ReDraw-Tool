package test.java;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.android.uiautomator.tree.UiHierarchyXmlLoader;

import edu.semeru.android.core.helpers.device.DeviceHelper;
import edu.semeru.android.core.helpers.ui.UiAutoConnector.TypeDeviceEnum;
import edu.wm.cs.semeru.core.helpers.TerminalHelper;
import edu.semeru.android.core.helpers.ui.UiAutomatorBridge;

public class UiAutomatorBridgeTest {
	
	private static String sdkPath = "/Users/Rich/android_sdk_symlink";
	private static String hierarchyPath = "/Users/Rich/Desktop/ui_dump.xml";
	private static String screenPath = "/Users/Rich/Desktop/screen.png";
	private static String adbPort = "5037";
	private static String adb = sdkPath + File.separator + "platform-tools" + File.separator + "adb";
	private static UiHierarchyXmlLoader parser;
	private static String[] devices;
	private static UiAutomatorBridge[] bridges;
	
	@BeforeClass
	public static void setup() {
		devices = TerminalHelper.executeCommand(adb + " devices | tail -n +2 | sed '$d'").split("device");
		bridges = new UiAutomatorBridge[devices.length];
		for (int i = 0; i < devices.length; i++) {
			devices[i] = devices[i].trim();
		}
		for (int i = 0; i < devices.length; i++) {
			String[] port = devices[i].split(":");
			if (port.length < 2) {
				port = devices[i].split("-");
			}
			DeviceHelper d = new DeviceHelper(TypeDeviceEnum.AVD, sdkPath, port[1], adbPort);
			d.setDeviceCommand(adbPort + " -s " + devices[i]);
			bridges[i] = new UiAutomatorBridge(d);
		}
		for (int j = 0; j < bridges.length; j++) {
			if (!bridges[j].startUiAutomatorServer()) {
				System.err.println("Could not start UiAutomatorServer for " + devices[j] + "!!!");
				//bridges[j] = null;
			}
		}
		parser = new UiHierarchyXmlLoader();
	}
	
	@Test
	public void testScreenHierarchyCapabilities() {
		for (int i = 0; i < bridges.length; i++) {
			System.out.println("Testing hierarchy capabilities of server at " + devices[i]);
			if (bridges[i] == null) {
				fail();
			}
			String hierarchy = bridges[i].getScreenHierarchyString();
			assertNotNull("Could not pull hierarchy string for " + devices[i] + "!!!", hierarchy);
			assertNotNull("Hierarchy string could not be parsed for " + devices[i] + "!!!", parser.parseXml(new ByteArrayInputStream(hierarchy.getBytes())));
			
			bridges[i].saveScreenHierarchyToLocalFile(hierarchyPath);
			File file = new File(hierarchyPath);
			assertTrue("Could not pull hierarchy file for " + devices[i] + "!!!", file.exists());
			assertNotNull("Hierarchy file could not be parsed for " + devices[i] + "!!!", parser.parseXml(hierarchyPath));
			if (file.exists()) {
				file.delete();
			}		
		}
	}
	
	@Test
	public void testScreenshotCapabilities() {
		for (int i = 0; i < bridges.length; i++) {
			System.out.println("Testing screenshot capabilities of server at " + devices[i]);
			if (bridges[i] == null) {
				fail();
			}
			String screen = bridges[i].getScreenshotString();
			assertNotNull("Could not pull screenshot string for " + devices[i] + "!!!", screen);
			
			bridges[i].saveScreenshotToLocalFile(screenPath);
			File file = new File(screenPath);
			assertTrue("Could not pull screenshot for " + devices[i] + "!!!", file.exists());
			if (file.exists()) {
				file.delete();
			}	
		}
	}
	
	@AfterClass
	public static void tearDown() {
		for (UiAutomatorBridge bridge : bridges) {
			if (bridge != null) {
				bridge.stopUiAutomatorServer();
			}
		}
	}

}

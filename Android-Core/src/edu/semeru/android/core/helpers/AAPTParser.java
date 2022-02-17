package edu.semeru.android.core.helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.wm.cs.semeru.core.helpers.TerminalHelper;

/**
 * Parses aapt output into meaningful Java object
 *
 * @author Richard Bonett
 * @since Feb 14, 2017
 */
public class AAPTParser {

	public static ApkInfo parse(String input) {
		String[] patterns = {"(.*package: name=')([^']*)('.*)", "(.*versionName=')([^']*)(.*)",
				"(.*launchable-activity: name=')([^']*)('.*)", "(.*application: label=')([^']*)('.*)",
				"(.*sdkVersion:')([^']*)('.*)", "(.*targetSdkVersion:')([^']*)('.*)"};
		String[] results = new String[patterns.length];

		for (int i = 0; i < patterns.length; i++) {
			Matcher m = Pattern.compile(patterns[i]).matcher(input);
			if (m.find()) {
				results[i] = m.group(2);
			}
		}
		return new ApkInfo(results);
	}
	
	public static String[] parseToStringArray(String input) {
		String[] patterns = {"(.*package: name=')([^']*)('.*)", "(.*versionName=')([^']*)(.*)",
				"(.*launchable-activity: name=')([^']*)('.*)", "(.*application: label=')([^']*)('.*)",
				"(.*sdkVersion:')([^']*)('.*)", "(.*targetSdkVersion:')([^']*)('.*)"};
		String[] results = new String[patterns.length];

		for (int i = 0; i < patterns.length; i++) {
			Matcher m = Pattern.compile(patterns[i]).matcher(input);
			if (m.find()) {
				results[i] = m.group(2);
			}
		}
		return results;
	}

	public static ArrayList<String> findAPKs(String apkFolder){

		System.out.println("Finding apks in the specified directory");

		ArrayList<String> apkPaths = new ArrayList<String>();

		apkPaths = listFiles(apkFolder, ".apk");
		
//		File[] apkFilePaths = dir.listFiles(new FilenameFilter() { 
//			public boolean accept(File dir, String filename)
//			{ return filename.endsWith(".apk"); }
//		} );
//
//		for(int i = 0; i < apkFilePaths.length; i++){
//			apkPaths.add(apkFilePaths[i].toString());
//		}
		
		return apkPaths;
		
	}

	public static ArrayList<String> listFiles( String path, String extension ) {

        File root = new File( path );
        ArrayList<String> apkPaths = new ArrayList<String>();
        File[] list = root.listFiles();

        if (list == null) return apkPaths;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
            apkPaths.addAll(listFiles(f.getAbsolutePath(), extension));
            }
            else {
                if(f.getAbsolutePath().endsWith(extension)){
                	apkPaths.add(f.getAbsolutePath());
                }
            }
        }
        return apkPaths;
    }
	
	public static ApkInfo analyzeApk(String aaptPath, String apkPath) {
		System.out.println("Analyzing apk: " + apkPath);
		String command = aaptPath + " dump badging " + apkPath;
		String results = TerminalHelper.executeCommand(command);
		return parse(results);
	}
	
	public static String[] analyzeApktoString(String aaptPath, String apkPath) {
		System.out.println("Analyzing apk: " + apkPath);
		String command = aaptPath + " dump badging " + apkPath;
		String results = TerminalHelper.executeCommand(command);
		return parseToStringArray(results);
	}


	public static void saveApkInfo(ArrayList<ApkInfo> info, String output) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();;
		String json = gson.toJson(info);
		try {
			File out = new File(output);
			BufferedWriter writer = new BufferedWriter(new FileWriter(out));
			writer.write(json);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			System.err.println("ERROR: Could not write to file: " + e.getMessage());
		}
	}


	public static class ApkInfo {
		private String packageName;
		private String versionName;
		private String mainActivity;
		private String appName;
		private String sdkVersion;
		private String targetSdkVersion;
		private String apkDir;
		private String apkName;

		public ApkInfo(String[] params) {
			setPackageName(params[0]);
			setVersionName(params[1]);
			setMainActivity(params[2]);
			setAppName(params[3]);
			setSdkVersion(params[4]);
			setTargetSdkVersion(params[5]);    
		}

		public void setPackageName(String s) {
			packageName = s;
		}

		public void setVersionName(String s) {
			versionName = s;
		}

		public void setMainActivity(String s) {
			mainActivity = s;
		}

		public void setAppName(String s) {
			appName = s;
		}

		public void setSdkVersion(String s) {
			sdkVersion = s;
		}

		public void setTargetSdkVersion(String s) {
			targetSdkVersion = s;
		}

		public void setApkDir(String s) {
			apkDir = s;
		}

		public void setApkName(String s) {
			apkName = s;
		}

		public String getPackageName() {
			return packageName;
		}

		public String getVersionName() {
			return versionName;
		}

		public String getMainActivity() {
			return mainActivity;
		}

		public String getAppName() {
			return appName;
		}

		public String getSdkVersion() {
			return sdkVersion;
		}

		public String getTargetSdkVersion() {
			return targetSdkVersion;
		}

		public String getApkDir() {
			return apkDir;
		}

		public String getApkName() {
			return apkName;
		}

		@Override
		public String toString() {
			return "package: " + packageName + "\nversion: " + versionName + "\nmain activity: " + 
					mainActivity + "\napp name: " + appName + "\nSDK Version: " + sdkVersion + 
					"\ntarget SDK version: " + targetSdkVersion;
		}

		public boolean equals(ApkInfo other) {
			return toString().equals(other.toString());
		}
	}


	public static void main(String[] args) {
		String aaptPath = args[0];
		String path = args[1]; // single apk file or directory of apk files
		String output = args[2];
		AAPTParser parser = new AAPTParser();
		if (path.endsWith(".apk")) {
			ArrayList<ApkInfo> wrapper = new ArrayList<ApkInfo>(1);
			ApkInfo parsed = parser.analyzeApk(aaptPath, path);
			parsed.setApkDir(path.substring(0, path.lastIndexOf(File.separator)));
			parsed.setApkName(path.substring(path.lastIndexOf(File.separator) + 1));
			wrapper.add(parsed);
			parser.saveApkInfo(wrapper, output);
		}
		else {
			File[] files = new File(path).listFiles();
			if (files == null) {
				return;
			}
			ArrayList<ApkInfo> info = new ArrayList<ApkInfo>(files.length);
			for (int i = 0; i < files.length; i++) {
				if (files[i].getAbsolutePath().endsWith(".apk")) {
					ApkInfo parsed = parser.analyzeApk(aaptPath, files[i].getAbsolutePath());
					parsed.setApkDir(path);
					parsed.setApkName(files[i].getName());
					info.add(parsed);
				}
			}
			parser.saveApkInfo(info, output);
		}
	}
}


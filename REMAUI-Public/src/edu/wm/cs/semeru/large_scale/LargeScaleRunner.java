package edu.wm.cs.semeru.large_scale;

import java.io.File;

import org.opencv.core.Core;

import edu.wm.cs.semeru.redraw.REMAUI;

/**
 * The LargeScaleRunner runs ReDraw on every screenshot in a directory
 *
 * @author Richard Bonett
 */
public class LargeScaleRunner {
	private static String inputFolder;
	private static String outputFolder;
	private static REMAUI worker;

	/**
	 * Returns true if the file type denoted by file extension is supported by this program
	 * @param f File to check
	 * @return true if supported, false otherwise
	 */
	public static boolean isSupported(File f) {
		return f.getName().endsWith(".png");
	}
	
	/**
	 * Helper function to return a filename without the extension
	 * @param f File
	 * @return String filename without characters after last '.'
	 */
	public static String fileNameWithoutExtension(File f) {
		String name = f.getName();
		return name.substring(0, name.lastIndexOf('.'));
	}
	
	/**
	 * Helper function to return the extension of a file
	 * @param f File
	 * @return String characters after last '.'
	 */
	public static String getExtension(File f) {
		String name = f.getName();
		return name.substring(name.lastIndexOf('.', name.length()));
	}
	
	private static void runRecursive(File dir) {
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				runRecursive(f);
			}
			else if (f.isFile() && isSupported(f)) {
				String filename = outputFolder + File.separator + fileNameWithoutExtension(f);
				new File(filename).mkdirs();
				worker.run(".", f.getPath(), filename);
			}
		}
	}
	
	/**
	 * Runs ReDraw component analysis on every image file in a directory
	 * Arguments:
	 *  -- 0: path to input folder containing screenshots
	 *  -- 1: path to output folder where data will be written
	 *  -- 2: true if directories in input folder should be recursively parsed
	 * @param args 
	 */
	public static void main(String args[]) {
		inputFolder = args[0];
		outputFolder = args[1];
		boolean recursive = args.length < 3 ? false : Boolean.valueOf(args[2]);

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		worker = new REMAUI();
		
		for (File f : new File(inputFolder).listFiles()) {
			if (recursive && f.isDirectory()) {
				runRecursive(f);
			}
			else if (f.isFile() && isSupported(f)) {
				String filename = outputFolder + File.separator + fileNameWithoutExtension(f);
				new File(filename).mkdirs();
				worker.run(".", f.getPath(), filename);
			}
		}
	}
}

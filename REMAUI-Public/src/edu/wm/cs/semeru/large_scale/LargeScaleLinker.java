package edu.wm.cs.semeru.large_scale;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.opencv.core.Core;

//TODO there are two BasicTreeNodes, Not good. One from GVT and one from Android Core
import com.android.uiautomator.tree.UiHierarchyXmlLoader;
import com.android.uiautomator.tree.BasicTreeNode;

import edu.semeru.android.core.entity.model.fusion.DynGuiComponent;
import edu.semeru.android.core.helpers.device.StepByStepEngine;
//import edu.wm.cs.semeru.redraw.Utilities;
import edu.semeru.android.core.helpers.device.Utilities;
import edu.semeru.android.core.model.DynGuiComponentVO;
import edu.wm.cs.semeru.redraw.REMAUI;

/**
 * Runs ReDraw component analysis on every image file in a directory, then links
 * the produced components with meta-data based on a corresponding ui_dump.xml file
 * 
 * Requires that each screenshot file has a corresponding .xml file within the same 
 * directory and having the same name. Will skip files that do not match this layout.
 * 
 * .xml files must match uiautomator's style, NOT the original ReDraw app assembly style
 *
 * LargeScaleLinker can be run after LargeScaleRunner, but can also be run on its own. 
 * By default, if data folders produced by LargeScaleRunner are found, these will be 
 * used, but if they are not present a ReDraw instance will be used to generate them.
 * 
 * @author Richard Bonett
 */
public class LargeScaleLinker {
	private static String inputRoot;
	private static String outputRoot;
	private static String imageRoot;
	private static REMAUI worker;
	private static EntityManager em;
	
	private static double SCORE_THRESHOLD = 0.5; // minimum percent overlap required: 0.0 >= x <= 1.0
	
	/**
	 * Driver for main
	 * @param dir
	 * @param force
	 */
	private static void linkRecursive(File dir, boolean force) {
		if (!dir.exists()) {
			return;
		}
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				linkRecursive(f, force);
			}
			else if (f.isFile() && LargeScaleRunner.isSupported(f)) {
				try {
					File uiDump = new File(f.getAbsolutePath().replace(LargeScaleRunner.getExtension(f), ".xml"));
					if (!uiDump.exists()) {
						System.err.println(String.format("Cannot link %s, missing %s!", f.getName(), uiDump.getName()));
						continue;
					}
					
					File dataFolder = new File(outputRoot + File.separator + LargeScaleRunner.fileNameWithoutExtension(f));
					if (force || !dataFolder.exists()) {
						dataFolder.mkdirs();
						worker.run(".", f.getAbsolutePath(), dataFolder.getAbsolutePath());
					}
					else {
						System.out.println(String.format("Skipping parsing %s as data folder present!", f.getName()));
					}
					
					System.out.println(String.format("Linking %s!", f.getName()));
					if (!linkFiles(f, uiDump, dataFolder)) {
						System.err.println(String.format("Error linking %s!", f.getName()));
					}
				} catch (Exception e) {
					// Don't stop because one link mysteriously fails
					System.err.println(String.format("Unexpected error linking %s", f));
					e.printStackTrace();
					continue;
				}
			}
		}
	}
	
	private static void traverseTree(BasicTreeNode root, ArrayList<DynGuiComponent> ls, Dimension dim) {
		if (root == null) {
			return;
		}
		for (BasicTreeNode child : root.getChildrenList()) {
			traverseTree(child, ls, dim);
		}
		DynGuiComponentVO vo = Utilities.getHVComponentFromBasicTreeNode(root,
				(int) dim.getWidth(), (int) dim.getHeight(), "", null);
		ls.add(StepByStepEngine.getEntityFromVO(vo));
		root.clearAllChildren();
	}
	
	private static ArrayList<DynGuiComponent> getNodeList(File xmlDump, Dimension dim) {
		UiHierarchyXmlLoader loader = new UiHierarchyXmlLoader();
		BasicTreeNode root = loader.parseXml(xmlDump.getAbsolutePath());
		ArrayList<DynGuiComponent> ls = new ArrayList<DynGuiComponent>();
		traverseTree(root, ls, dim);
		return ls;
	}
	
	private static int max(int a, int b) {
		return a > b ? a : b;
	}
	
	private static int min(int a, int b) {
		return a < b ? a : b;
	}
	
	private static DynGuiComponent findClosestMatch(DynGuiComponent in, ArrayList<DynGuiComponent> options) {
		double score = 0;
		DynGuiComponent match = null;
		for (DynGuiComponent node : options) {
			double intersection = max(0, min(in.getPositionX() + in.getWidth(), node.getPositionX() + node.getWidth()) - max(in.getPositionX(), node.getPositionX()));
			intersection *= max(0, min(in.getPositionY() + in.getHeight(), node.getPositionY() + node.getHeight()) - max(in.getPositionY(), node.getPositionY()));
			double areaIn = in.getWidth() * in.getHeight();
			double areaNode = node.getWidth() * node.getHeight();
			double overlap = intersection / (areaIn + areaNode - intersection);
			if (overlap > score) {
				match = node;
				score = overlap;
			}
		}
		if (score > SCORE_THRESHOLD) {
			return match;
		}
		return null;
	}
	
	private static boolean linkFiles(File screenshot, File uiDump, File dataFolder) {
		File reDrawLayout = new File(dataFolder.getAbsolutePath() + File.separator + "ui_dump.xml");
		File imageFolder = new File(dataFolder.getAbsolutePath() + File.separator + "drawable");
		if (!(reDrawLayout.exists() && imageFolder.exists())) {
			return false;
		}
		Dimension dim = getImageDimension(screenshot);
		ArrayList<DynGuiComponent> truth = getNodeList(uiDump, dim);
		ArrayList<DynGuiComponent> guess = getNodeList(reDrawLayout, dim);

		String persistFolder = imageRoot + File.separator + dataFolder.getName();
		
		for (DynGuiComponent node : guess) {
			if (node.getContentDescription() == null) {
				// if no content description, it has no image and should be skipped
				continue;
			}
			DynGuiComponent match = findClosestMatch(node, truth);
			if (match == null) {
				// match is null when there is no 'truth' within the threshold for the guess
				match = new DynGuiComponent();
				//match.setName(DynGuiComponent.INVALID);
			}

			//TODO: Need to find a way to get currentActivity here
			
			match.setPositionX(node.getPositionX());
			match.setPositionY(node.getPositionY());
			match.setWidth(node.getWidth());
			match.setHeight(node.getHeight());

			// Copy image file to persist folder
			String nodeImage, imgFile, copy;
			try {
				nodeImage = node.getContentDescription();
				imgFile = imageFolder.getAbsolutePath() + File.separator + nodeImage + ".png";
				copy = persistFolder + File.separator + nodeImage + ".png";
			} catch (Exception e) {
				// Fails if root window
				imgFile = screenshot.getAbsolutePath();
				copy = persistFolder + File.separator + screenshot.getName();
			}
			
			new File(copy).getParentFile().mkdirs();
			try {
				Files.copy(Paths.get(imgFile), Paths.get(copy));
			} catch (IOException e) {
				System.err.println(String.format("Could not copy image to %s!", copy));
				continue;
			}
			
			// Persist component to database
			match.setGuiScreenshot(copy);
			em.getTransaction().begin();
            em.persist(match);
            em.getTransaction().commit();
			}
		return true;
	}
	
	/**
	 * Gets image dimensions for given file 
	 * @param imgFile image file
	 * @return dimensions of image
	 */
	public static Dimension getImageDimension(File imgFile) {
		String suffix = LargeScaleRunner.getExtension(imgFile).replace(".", "");
		Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
		while(iter.hasNext()) {
			ImageReader reader = iter.next();
			try {
				ImageInputStream stream = new FileImageInputStream(imgFile);
				reader.setInput(stream);
				int width = reader.getWidth(reader.getMinIndex());
				int height = reader.getHeight(reader.getMinIndex());
				return new Dimension(width, height);
			} catch (IOException e) {
				System.err.println("Error reading: " + imgFile.getAbsolutePath());
				System.err.println(e.getMessage());
			} finally {
				reader.dispose();
			}
		}
		return null;
	}
	
	/**
	 * Arguments:
	 *  -- 0: path to root input folder containing screenshots + ui_dump.xml files
	 *  -- 1: path to output folder where ReDraw data will be written
	 *  -- 2: path to output folder specifically for persisted component images
	 *  -- 3: boolean force: if true, will always re-parse screenshots even if data folder is present
	 * @param args 
	 */
	public static void main(String args[]) {
		inputRoot = args[0];
		outputRoot = args[1];
		imageRoot = args[2];
		boolean force = args.length < 4 ? false : Boolean.valueOf(args[3]);
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		worker = new REMAUI();
		em = Persistence.createEntityManagerFactory("REDRAW_NOISY").createEntityManager();
		
		new File(outputRoot).mkdirs();
		new File(imageRoot).mkdirs();
		linkRecursive(new File(inputRoot), force);
	}
}

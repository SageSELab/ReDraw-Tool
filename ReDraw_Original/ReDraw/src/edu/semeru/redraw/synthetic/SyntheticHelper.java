/*******************************************************************************
 * Copyright (c) 2016, SEMERU
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package edu.semeru.redraw.synthetic;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.redraw.uiautomator.tree.BasicTreeNode;
import com.redraw.uiautomator.tree.RootUINode;

import edu.wm.semeru.redraw.helpers.CmdProcessBuilder;
import edu.wm.cs.semeru.redraw.helpers.ImagesHelper;
import edu.semeru.redraw.model.Triplet;
import edu.semeru.redraw.synthetic.SyntheticViolation.SyntheticBuilder;
import edu.wm.semeru.redraw.pipeline.UIDumpParser;

import com.redraw.uiautomator.tree.BasicTreeNode;
import com.redraw.uiautomator.tree.UiTreeNode;
import imagingbook.pub.color.statistics.ColorHistogram;

/**
 * {Insert class description here}
 *
 * @author Carlos Bernal
 * @since Nov 30, 2016
 */
public class SyntheticHelper {

	public static final String LOCATION = "LOCATION";
	public static final String NUMBER_COMPONENTS = "MISSING";
	public static final String SIZE = "SIZE";
	public static final String TEXT_FONT = "TEXT_FONT";
	public static final String TEXT_CONTENT = "TEXT_CONTENT";
	public static final String TEXT_COLOR = "TEXT_COLOR";
	public static final String IMAGE = "IMAGE";
	public static final String IMAGE_COLOR = "IMAGE_COLOR";
	public static final String COMPONENT_COLOR = "COMPONENT_COLOR";
	public static final int ALPHABET_SIZE = 26;
	public static final float IMAGE_PERCENTAGE = 0.4f;
	private static final Map<FontType, Font> FONTS;
	static {
		Map<FontType, Font> fonts = new HashMap<>();
		fonts.put(FontType.ARIAL, Font.decode("Arial"));
		fonts.put(FontType.COMIC, Font.decode("ComicSansMS"));
		fonts.put(FontType.COURIER, Font.decode("Courier"));
		Font roboto = null;
		try {
			roboto = Font
					.createFont(Font.TRUETYPE_FONT,
							new File("resources" + File.separator + "fonts" + File.separator + "Roboto-Regular.ttf"))
					.deriveFont(12);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		fonts.put(FontType.ROBOTO, roboto);
		fonts.put(FontType.TIMES, Font.decode("Times-Roman"));
		FONTS = Collections.unmodifiableMap(fonts);
	}

	private static final String HEADER_CSV = "name_file, idxml, component_type, original_x, original_y, original_width, original_height, original_text, original_color, new_x, new_y, new_width, new_height, new_text, new_font, new_color, injection_type\n";

	private static boolean compareViolations(SyntheticViolation violationOne, ArrayList<SyntheticViolation> violationsToCheck) {

		boolean violationsEqual = false;

		for(SyntheticViolation violationTwo: violationsToCheck) {

			String hashOne = violationOne.getOrgXMLFile() + "-" + violationOne.getComponentType() + "-" + violationOne.getOriginalX() + "-" + violationOne.getOriginalY() + "-" + violationOne.getOriginalWidth() + "-" + violationOne.getOriginalWidth();

			String hashTwo = violationTwo.getOrgXMLFile() + "-" +violationTwo.getComponentType() + "-" + violationTwo.getOriginalX() + "-" + violationTwo.getOriginalY() + "-" + violationTwo.getOriginalWidth() + "-" + violationTwo.getOriginalWidth();

			if(hashOne.equals(hashTwo)) {
				violationsEqual = true;
			}

		}
		return violationsEqual;

	}

	public static boolean perturbateWindow(String type, int numberComponents, String original, String xmlName,
			String originalScreenshot, String outputScreenshot, String outputCsv) throws Exception {

		boolean violationFound = true;
		UIDumpParser loader = new UIDumpParser();
		RootUINode tree = loader.parseXml(original);
		List<SyntheticViolation> violations = getViolations(type, numberComponents, original, originalScreenshot);

		SyntheticViolation [] arrayViolations = new SyntheticViolation[violations.size()];
		violations.toArray(arrayViolations);
		// Print the new xml based on the violations
		printNewXml(xmlName, loader, tree, arrayViolations);

		if (violations.isEmpty()) {
			violationFound = false;
		}

		// Generate the image
		generateScreenshots(violations, originalScreenshot, outputScreenshot);

		// Print rules
		printCsvs(outputCsv, violations);

		return violationFound;

	}

	public static void seedViolation(SyntheticViolation violation, String originalXMLFile, String xmlName,
			String originalScreenshot, String outputScreenshot, String outputCsv) throws Exception {

		UIDumpParser loader = new UIDumpParser();
		RootUINode tree = loader.parseXml(originalXMLFile);
		// Print the new xml based on the violations
		printNewXml(xmlName, loader, tree, violation);


		// Generate the image
		generateScreenshot(violation, originalScreenshot, outputScreenshot);

		// Print rules
		printCsv(outputCsv, violation);


	}

	public static SyntheticViolation getViolation(String type,
			String originalXmlFile, String originalSSFile) throws Exception {

		SyntheticViolation violation = null;
		List<SyntheticViolation> violations = getViolations(type, 1, originalXmlFile, originalSSFile);

		if(!violations.isEmpty()) {
			violation = violations.get(0);
		}

		return violation;

	}
	
	public static SyntheticViolation getDynViolation(String type,
			String originalXmlFile, String originalSSFile) throws Exception {

		SyntheticViolation violation = null;
		List<SyntheticViolation> violations = getDynViolations(type, 1, originalXmlFile, originalSSFile);

		if(!violations.isEmpty()) {
			violation = violations.get(0);
		}

		return violation;

	}

	private static List<SyntheticViolation> getDynViolations(String type, int numberComponents, String originalXmlFile,
			String originalSSFile) throws Exception {
		UIDumpParser loader = new UIDumpParser();
		RootUINode tree = loader.parseXml(originalXmlFile);

		List<UiTreeNode> leafnodes = tree.getLeafNodes();
		List<UiTreeNode> nodes = new ArrayList<UiTreeNode>();
		System.out.println("in getViolations============" + originalSSFile);
		for(UiTreeNode n : leafnodes) {
			//System.out.println(n.getType());
			if(originalSSFile.contains("checkbox")&&n.getType().equals("CheckBox")) {
				//System.out.println("yeahhh");
				nodes.add(n);
			}
			if(originalSSFile.contains("checkedtextview")&&n.getType().equals("CheckedTextView")) {
				//System.out.println("yeahhh");
				nodes.add(n);
			}
			if(originalSSFile.contains("radiobutton")&&n.getType().equals("RadioButton")) {
				//System.out.println("yeahhh");
				nodes.add(n);
			}
			if(originalSSFile.contains("ratingbar")&&n.getType().equals("RatingBar")) {
				//System.out.println("yeahhh");
				nodes.add(n);
			}
			if(originalSSFile.contains("seekbar")&&n.getType().equals("SeekBar")) {
				//System.out.println("yeahhh");
				nodes.add(n);
			}
			if(originalSSFile.contains("switch")&&n.getType().equals("Switch")) {
				//System.out.println("yeahhh");
				nodes.add(n);
			}
			if(originalSSFile.contains("progressbar")&&n.getType().equals("ProgressBar")) {
				//System.out.println("yeahhh");
				nodes.add(n);
			}
			if(originalSSFile.contains("text")) {
				if(originalSSFile.contains("s1") && n.getType().equals("TextView")) {

					if(n.getName().equals("IWUzhdIPVOHMdvdpKSO")||n.getName().equals("TUHBKAtUhCFgQAnTAaX")) {
						System.out.println("pandora add node");
						nodes.add(n);}

				}
				if(originalSSFile.contains("s2") && n.getType().equals("TextView")) {
					
					if(n.getName().equals("oQOS")||n.getName().equals("ypNHu")) {
						System.out.println("dropbox add node");
						nodes.add(n);}
				}

				
			}
			
			if(originalSSFile.contains("image")&&n.getType().equals("ImageView")) {
				if(originalSSFile.contains("s3") && n.getX() == 80 && n.getY() == 384 && n.getWidth() == 1280 && n.getHeight() == 1280) {
					nodes.add(n);
				}
				if(originalSSFile.contains("s1") && n.getX() == 1232 && n.getY() == 559 && n.getWidth() == 144 && n.getHeight() == 144) {
					nodes.add(n);
				}
				if(originalSSFile.contains("s2") && n.getX() == 4 && n.getY() == 472 && n.getWidth() == 712 && n.getHeight() == 462) {
					nodes.add(n);
				}
				if(originalSSFile.contains("s2") && n.getX() == 4 && n.getY() == 1352 && n.getWidth() == 712 && n.getHeight() == 462) {
					nodes.add(n);
				}
				

			}
			
		}
		// Discard generic views
		filterLeafs(nodes);
		List<SyntheticViolation> violations = getViolationsGeneric(nodes, numberComponents, type, originalXmlFile, originalSSFile);

		return violations;
	}

	public static List<SyntheticViolation> getViolations(String type, int numberComponents,
			String originalXmlFile, String originalSSFile) throws Exception {

		UIDumpParser loader = new UIDumpParser();
		RootUINode tree = loader.parseXml(originalXmlFile);

		List<UiTreeNode> nodes = tree.getLeafNodes();
		// Discard generic views
		filterLeafs(nodes);
		List<SyntheticViolation> violations = getViolationsGeneric(nodes, numberComponents, type, originalXmlFile, originalSSFile);

		return violations;
	}

	private static void generateScreenshot(SyntheticViolation violation, String originalScreenshot,
			String outputScreenshot) {

		ArrayList<SyntheticViolation> violations = new ArrayList<SyntheticViolation>();

		violations.add(violation);
		generateScreenshots(violations, originalScreenshot, outputScreenshot);

	}


	/**
	 * @param violations
	 * @param originalScreenshot
	 * @param outputScreenshot
	 */
	private static void generateScreenshots(List<SyntheticViolation> violations, String originalScreenshot,
			String outputScreenshot) {

		try {
			// Get original image
			BufferedImage original = ImageIO.read(new File(originalScreenshot));
			Graphics2D gOriginal = (Graphics2D) original.getGraphics();
			ImageIO.write(original, "png", new File("original.png"));
			BufferedImage mask = copyImage(original);
			// Font initialization
			// Font customFont = null;
			// try {
			// customFont = Font
			// .createFont(Font.TRUETYPE_FONT,
			// new File(
			// "resources" + File.separator + "fonts" + File.separator +
			// "Roboto-Regular.ttf"))
			// .deriveFont(12);
			// gOriginal.setFont(customFont);
			// } catch (FontFormatException e1) {
			// e1.printStackTrace();
			// }

			for (SyntheticViolation violation : violations) {
				try {
					// Crop the region of the original component
					BufferedImage cropImage = ImagesHelper.cropImage(originalScreenshot, violation.getOriginalX(),
							violation.getOriginalY(), violation.getOriginalWidth(), violation.getOriginalHeight());
					// Generate second image
					Graphics g = mask.getGraphics();
					g.setColor(Color.WHITE);
					g.fillRect(violation.getOriginalX(), violation.getOriginalY(), violation.getOriginalWidth(),
							violation.getOriginalHeight());
					Color top = null;
					Color bottom = null;
					Color left = null;
					Color right = null;
					Font customfont = null;
					ColorHistogram histogram = null;
					float size = 0;
					Color topColor = null;
					Random ranSeed = new Random();

					// Resolve accordingly
					switch (violation.getInjectionType()) {
					case LOCATION:
						fillOriginalSpace(original, gOriginal, violation, top, bottom, left, right);
						gOriginal.drawImage(cropImage, Integer.parseInt(violation.getNewX()),
								Integer.parseInt(violation.getNewY()), null);
						break;
					case NUMBER_COMPONENTS:
						fillOriginalSpace(original, gOriginal, violation, top, bottom, left, right);

						break;
					case SIZE:
						fillOriginalSpace(original, gOriginal, violation, top, bottom, left, right);
						gOriginal.drawImage(cropImage, Integer.parseInt(violation.getNewX()),
								Integer.parseInt(violation.getNewY()), Integer.parseInt(violation.getNewWidth()),
								Integer.parseInt(violation.getNewHeight()), null);
						break;
					case TEXT_FONT:
						fillOriginalSpace(original, gOriginal, violation, top, bottom, left, right);
						customfont = getFont(FontType.valueOf(violation.getNewFont()));
						// Update size
						// Reference:https://websemantics.uk/articles/font-size-conversion/
						size = 12 * violation.getOriginalHeight() / 16f;
						gOriginal.setFont(customfont.deriveFont(size));
						// Get Color of the text
						histogram = ImagesHelper.getOutsideHistrogram(cropImage, 0, 0, cropImage.getWidth(),
								cropImage.getHeight(), null);
						// Take top color
						topColor = new Color(histogram.getColor(histogram.getNumberOfColors() == 1 ? 0 : 1));

						// Update violation
						violation.setOriginalColor(ImagesHelper.argb2Hex(topColor));

						gOriginal.setColor(topColor);
						gOriginal.drawString(violation.getOriginalText(), violation.getOriginalX(),
								violation.getOriginalY() + violation.getOriginalHeight());

						break;
					case TEXT_CONTENT:
						fillOriginalSpace(original, gOriginal, violation, top, bottom, left, right);
						customfont = getFont(FontType.valueOf(violation.getNewFont()));
						// Update size
						// Reference:https://websemantics.uk/articles/font-size-conversion/
						size = 12 * violation.getOriginalHeight() / 16f;
						gOriginal.setFont(customfont.deriveFont(size));
						// Get Color of the text
						histogram = ImagesHelper.getOutsideHistrogram(cropImage, 0, 0, cropImage.getWidth(),
								cropImage.getHeight(), null);
						// Take top color
						topColor = new Color(histogram.getColor(histogram.getNumberOfColors() == 1 ? 0 : 1));
						
						// Update violation
						violation.setOriginalColor(ImagesHelper.argb2Hex(topColor));

						//gOriginal.setColor(topColor);
						gOriginal.setColor(Color.WHITE);
						gOriginal.drawString(violation.getNewText(), violation.getOriginalX(),
								violation.getOriginalY() + violation.getOriginalHeight());

						break;
					case TEXT_COLOR:
						fillOriginalSpace(original, gOriginal, violation, top, bottom, left, right);
						customfont = getFont(FontType.valueOf(violation.getNewFont()));
						// Update size
						// Reference:https://websemantics.uk/articles/font-size-conversion/
						size = 12 * violation.getOriginalHeight() / 16f;
						gOriginal.setFont(customfont.deriveFont(size));
						// Get Color of the text
						histogram = ImagesHelper.getOutsideHistrogram(cropImage, 0, 0, cropImage.getWidth(),
								cropImage.getHeight(), null);
						// Take top color
						topColor = new Color(histogram.getColor(histogram.getNumberOfColors() == 1 ? 0 : 1));
						Color hueColor = ImagesHelper.hexStringToARGB(violation.getNewColor());

						// Update violation
						violation.setOriginalColor(ImagesHelper.argb2Hex(topColor));

						gOriginal.setColor(hueColor);
						gOriginal.drawString(violation.getOriginalText(), violation.getOriginalX(),
								violation.getOriginalY() + violation.getOriginalHeight());

						break;
					case IMAGE:
						// Read the pixels and update the color base on the
						// information already generated
						for (Triplet<Integer, Integer, Color> pixel : violation.getPixels()) {
							original.setRGB(pixel.first, pixel.second, (pixel.third).getRGB());
						}
						break;
					case IMAGE_COLOR:
					case COMPONENT_COLOR:
						Rectangle boundary = new Rectangle(violation.getOriginalX(), violation.getOriginalY(),
								violation.getOriginalWidth(), violation.getOriginalHeight());
						// Shift hue component 30º
						ImagesHelper.changeHue(original, boundary, (float)(Math.PI/2 + ranSeed.nextFloat()*Math.PI));

						break;

					default:
						break;
					}

				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			gOriginal.dispose();
			ImageIO.write(mask, "png", new File(outputScreenshot.replace(".png", "-mask.png")));
			ImageIO.write(original, "png", new File(outputScreenshot));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * @param xmlName
	 * @param loader
	 * @param tree
	 * @param violations 
	 */
	private static void printNewXml(String xmlName, UIDumpParser loader, RootUINode tree, SyntheticViolation... violations) {
		StringBuilder builderXml = new StringBuilder();
		builderXml.append("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>");
		loader.buildXml(xmlName, tree, builderXml, violations);
		// Print new XML file
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(xmlName));
			out.write(builderXml.toString().replace("&", "&amp;"));
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void printCsv(String ruleFile, SyntheticViolation violation) {

		ArrayList<SyntheticViolation> violations = new ArrayList<SyntheticViolation>();

		violations.add(violation);

		printCsvs(ruleFile, violations);

	}

	/**
	 * @param output
	 * @param violations
	 */
	private static void printCsvs(String ruleFile, List<SyntheticViolation> violations) {
		// Print rules file
		try {
			boolean append = new File(ruleFile).exists();
			BufferedWriter out = new BufferedWriter(new FileWriter(ruleFile, append));
			if (!append)
				out.write(HEADER_CSV);
			for (SyntheticViolation violation : violations) {
				out.write(violation.toString() + "\n");
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param original
	 * @param gOriginal
	 * @param violation
	 * @param top
	 * @param bottom
	 * @param left
	 * @param right
	 */
	private static void fillOriginalSpace(BufferedImage original, Graphics gOriginal, SyntheticViolation violation,
			Color top, Color bottom, Color left, Color right) {

		// Shift in pixels
		int SHIFT = 20;

		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

		minX = Math.max(violation.getOriginalX() - SHIFT, 0);
		minY = Math.max(violation.getOriginalY() - SHIFT, 0);
		maxX = Math.min(violation.getOriginalX() + violation.getOriginalWidth() + SHIFT, original.getWidth());
		maxY = Math.min(violation.getOriginalY() + violation.getOriginalHeight() + SHIFT, original.getHeight());

		Rectangle component = new Rectangle(violation.getOriginalX(), violation.getOriginalY(),
				violation.getOriginalWidth(), violation.getOriginalHeight());

		ColorHistogram histogram = ImagesHelper.getOutsideHistrogram(original, minX, minY, maxX, maxY, component);
		// Assign the top color
		Color topColor = new Color(histogram.getColor(0));
		gOriginal.setColor(topColor);
		gOriginal.fillRect(violation.getOriginalX(), violation.getOriginalY(), violation.getOriginalWidth(),
				violation.getOriginalHeight());
	}

	public static BufferedImage copyImage(BufferedImage source) {
		BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		Graphics g = b.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, source.getWidth(), source.getHeight());
		g.dispose();
		return b;
	}

	private static List<SyntheticViolation> getViolationsGeneric(List<UiTreeNode> nodes, int numberComponents,
			String type, String originalXml, String originalSS) throws Exception {
		List<SyntheticViolation> violations = new ArrayList<>();
		int count = 0;
		Random seed = new Random();
		SyntheticViolation violation = null;
		int oldCount = 0;
		Collections.shuffle(nodes);
		System.out.println(type);
		while (count != numberComponents) {
			for (UiTreeNode node : nodes) {
				switch (type) {
				case LOCATION:
					violation = validateLocation(node, seed);
					break;
				case NUMBER_COMPONENTS:
					violation = validateNumberComponents(node, seed);
					break;
				case SIZE:
					violation = validateSize(node, seed);
					break;
				case TEXT_FONT:
					// TextView
					violation = validateTextFont(node, seed);
					break;
				case TEXT_CONTENT:
					// TextView
					violation = validateTextContent(node, seed);
					break;
				case TEXT_COLOR:
					// TextView
					violation = validateTextColor(node, seed);
					break;
				case IMAGE:
					// ImageView
					violation = validateImage(node, seed);
					break;
				case IMAGE_COLOR:
					// ImageView
					System.out.println("in case image color");
					violation = validateImageColor(node, seed);
					break;
				case COMPONENT_COLOR:
					// What components should we filter here?
					violation = validateComponentColor(node, seed);
					break;

				default:
					throw new Exception();
				}
				if (violation != null && !violations.contains(violation)) {
					count++;
					violation.setOrgXMLFile(originalXml);
					violation.setOrgSSFile(originalSS);
					violation.setNode(node);
					violations.add(violation);
					System.out.println(violation);
				}
				if (count >= numberComponents) {
					break;
				}
			}
			if (oldCount == count) {
				break;
			}
			oldCount = count;
		}
		return violations;
	}

	/**
	 * Removes generic Views
	 * 
	 * @param nodes
	 */
	private static void filterLeafs(List<UiTreeNode> nodes) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < nodes.size(); i++) {
			UiTreeNode uiTreeNode = nodes.get(i);
			String attribute = uiTreeNode.getAttribute("class");
			if (attribute.endsWith(".View")) {
				ids.add(i);
			}
		}

		for (int i = ids.size() - 1; i >= 0; i--) {
			nodes.remove(ids.get(i).intValue());
		}
	}

	/**
	 * @param node
	 * @param seed
	 * @return
	 */
	private static SyntheticViolation validateComponentColor(UiTreeNode node, Random seed) {
		System.out.println("in component color");
		boolean applyViolation = seed.nextBoolean();
		// Don't apply the transformations to images and generic views (usually
		// those views are empty containers)
		if (applyViolation && !node.getType().contains("ImageView") && !node.getType().equals("View")) {
			SyntheticViolation violation = new SyntheticBuilder(node.getId(), node.getType(), node.getX(), node.getY(),
					node.getWidth(), node.getHeight()).newX(node.getX()).newY(node.getY()).newWidth(node.getWidth())
					.newHeight(node.getHeight()).injectionType(COMPONENT_COLOR).build();
			return violation;
		}
		return null;
	}

	/**
	 * @param node
	 * @param seed
	 * @return
	 */
	private static SyntheticViolation validateImageColor(UiTreeNode node, Random seed) {
		boolean applyViolation = seed.nextBoolean();
		// Is it an ImageView component?
		if (applyViolation && node.getType().contains("ImageView")) {
			SyntheticViolation violation = new SyntheticBuilder(node.getId(), node.getType(), node.getX(), node.getY(),
					node.getWidth(), node.getHeight()).newX(node.getX()).newY(node.getY()).newWidth(node.getWidth())
					.newHeight(node.getHeight()).injectionType(IMAGE_COLOR).build();
			return violation;
		}
		return null;
	}

	/**
	 * @param node
	 * @param seed
	 * @return
	 */
	private static SyntheticViolation validateImage(UiTreeNode node, Random seed) {
		boolean applyViolation = seed.nextBoolean();
		// Is it an ImageView component?
		if (applyViolation && node.getType().contains("ImageView")) {

			SyntheticViolation violation = new SyntheticBuilder(node.getId(), node.getType(), node.getX(), node.getY(),
					node.getWidth(), node.getHeight()).newX(node.getX()).newY(node.getY()).newWidth(node.getWidth())
					.newHeight(node.getHeight()).injectionType(IMAGE).build();

			// Generate all the pixels
			int modifications = (int) (node.getWidth() * node.getHeight() * IMAGE_PERCENTAGE);
			List<Triplet<Integer, Integer, Color>> pixels = violation.getPixels();

			for (int i = 0; i < modifications; i++) {
				// Random location
				int rX = seed.nextInt(node.getWidth()) + node.getX();
				int rY = seed.nextInt(node.getHeight()) + node.getY();
				// Random color
				int red = seed.nextInt(256);
				int green = seed.nextInt(256);
				int blue = seed.nextInt(256);

				pixels.add(new Triplet<Integer, Integer, Color>(rX, rY, new Color(red, green, blue)));
			}
			return violation;
		}
		return null;
	}

	/**
	 * @param node
	 * @param seed
	 * @return
	 */
	private static SyntheticViolation validateTextFont(UiTreeNode node, Random seed) {
		boolean applyViolation = seed.nextBoolean();
		// Is it a TextView component?
		if (applyViolation && node.getType().contains("TextView")) {
			int fontIndex = seed.nextInt(5);
			System.out.println("in validate text font");
			SyntheticViolation violation = new SyntheticBuilder(node.getId(), node.getType(), node.getX(), node.getY(),
					node.getWidth(), node.getHeight(), node.getName()).newX(node.getX()).newY(node.getY())
					.newWidth(node.getWidth()).newHeight(node.getHeight()).injectionType(TEXT_FONT)
					.newFont(FontType.values()[fontIndex]).build();
			return violation;
		}

		return null;
	}

	/**
	 * @param node
	 * @param seed
	 * @return
	 */
	private static SyntheticViolation validateTextContent(UiTreeNode node, Random seed) {
		boolean applyViolation = seed.nextBoolean();
		// Is it a TextView component?
		if (applyViolation && node.getType().contains("TextView")) {
			String newText = "";
			String originalText = node.getName();
			System.out.println("original" + originalText);
			boolean lowerCase = seed.nextBoolean();
			int replace = seed.nextInt(ALPHABET_SIZE);
			for (int i = 0; i < originalText.length(); i++) {
				// Don't replace the character if it is an space
				if (originalText.charAt(i) != ' ') {
					// Generate random text
					if (lowerCase) {
						// 97-122 (a-z)
						replace += 97;
					} else {
						// 65-90 (A-Z)
						replace += 65;
					}
				} else {
					replace = (int) ' ';
				}
				newText += ((char) replace);
				lowerCase = seed.nextBoolean();
				replace = seed.nextInt(ALPHABET_SIZE);
			}

			int fontIndex = seed.nextInt(5);
			System.out.println("newText" + newText);;
			SyntheticViolation violation = new SyntheticBuilder(node.getId(), node.getType(), node.getX(), node.getY(),
					node.getWidth(), node.getHeight(), node.getName()).newX(node.getX()).newY(node.getY())
					.newWidth(node.getWidth()).newHeight(node.getHeight()).newText(newText)
					.injectionType(TEXT_CONTENT).newFont(FontType.values()[fontIndex]).build();
			return violation;
		}

		return null;
	}

	/**
	 * @param node
	 * @param seed
	 * @return
	 */
	private static SyntheticViolation validateTextColor(UiTreeNode node, Random seed) {
		boolean applyViolation = seed.nextBoolean();
		// Is it a TextView component?
		if (applyViolation && node.getType().contains("TextView")) {

			int fontIndex = seed.nextInt(5);

			int r = seed.nextInt(256);
			int g = seed.nextInt(256);
			int b = seed.nextInt(256);

			String hex = ImagesHelper.argb2Hex(new Color(r, g, b, 255));

			SyntheticViolation violation = new SyntheticBuilder(node.getId(), node.getType(), node.getX(), node.getY(),
					node.getWidth(), node.getHeight(), node.getName()).newX(node.getX()).newY(node.getY())
					.newWidth(node.getWidth()).newHeight(node.getHeight()).injectionType(TEXT_COLOR)
					.newFont(FontType.values()[fontIndex]).newColor(hex).build();
			return violation;
		}

		return null;
	}

	/**
	 * @param node
	 * @param seed
	 * @return
	 */
	private static SyntheticViolation validateLocation(UiTreeNode node, Random seed) {
		boolean repeat = true;
		boolean valid;
		Random seed2 = new Random();
		// System.out.println(node);
		UiTreeNode parent = (UiTreeNode) node.getParent();
		int minX = parent.getX(), minY = parent.getY();
		// Sometimes it is impossible to have a different location, limit added
		// to avoid infinite loop
		int limit = 50;
		// 1/4 of the width of the screen
		int MAX_DISTANCE = 200;
		do {
			valid = true;

			int xRandom = (parent.getWidth() != node.getWidth())
					? seed.nextInt(Math.abs(parent.getWidth() - node.getWidth())-4) + 5 : 0;
					int yRandom = (parent.getHeight() != node.getHeight())
							? seed.nextInt(Math.abs(parent.getHeight() - node.getHeight())-4) + 5 : 0;

							int x = xRandom + minX;
							int y = yRandom + minY;
							
							// Check all nodes don't overlap
							
							for (BasicTreeNode child : parent.getChildren()) {
								if (!node.equals(child)) {
									valid &= !overlaps(new Rectangle(child.getX(), child.getY(), child.getWidth(), child.getHeight()),
											new Rectangle(x, y, node.getWidth(), node.getHeight()));
								}
							}
							//MAX_DISTANCE= 60;
							int distance = (int) Math
									.sqrt(((x - node.getX()) * (x - node.getX())) + ((y - node.getY()) * (y - node.getY())));
							System.out.println("distance"+distance);
							// Change the node if valid
							if (valid && distance <= MAX_DISTANCE) {
								repeat = false;

								SyntheticViolation violation = new SyntheticBuilder(node.getId(), node.getType(), node.getX(),
										node.getY(), node.getWidth(), node.getHeight()).newX(x).newY(y).newWidth(node.getWidth())
										.newHeight(node.getHeight()).injectionType(LOCATION).build();

								return violation;
							}
							limit--;
		} while (repeat && parent.getChildCount() != 1 && limit >= 0);
		return null;
	}

	/**
	 * @param node
	 * @param seed
	 * @return
	 */
	private static SyntheticViolation validateNumberComponents(UiTreeNode node, Random seed) {
		boolean remove = seed.nextBoolean();
		// Remove only if there is no children
		if (remove && node.getChildCount() == 0 && validType(node.getType())) {
			SyntheticViolation violation = new SyntheticBuilder(node.getId(), node.getType(), node.getX(), node.getY(),
					node.getWidth(), node.getHeight()).injectionType(NUMBER_COMPONENTS).build();
			return violation;
		}
		return null;
	}

	/**
	 * @param node
	 * @param seed
	 * @return
	 */
	private static SyntheticViolation validateSize(UiTreeNode node, Random seed) {
		boolean repeat = true;
		boolean valid;
		// System.out.println(node);
		UiTreeNode parent = (UiTreeNode) node.getParent();
		// 20%
		double FIXED_RESIZE = 0.1;
		// Sometimes it is impossible to have a different location, limit added
		// to avoid infinite loop
		int limit = 50;
		do {
			valid = true;
			int one = (seed.nextBoolean() ? -1 : 1);
			int widthRandom = (int) (node.getWidth() * (1 + (one * FIXED_RESIZE)));
			int heightRandom = (int) (node.getHeight() * (1 + (one * FIXED_RESIZE)));
			// Check all nodes don't overlap
			for (BasicTreeNode child : parent.getChildren()) {
				if (!node.equals(child)) {
					valid &= !overlaps(new Rectangle(child.getX(), child.getY(), child.getWidth(), child.getHeight()),
							new Rectangle(node.getX(), node.getY(), widthRandom, heightRandom));
				}
			}
			// Change the node if valid
			if (valid) {
				repeat = false;
				SyntheticViolation violation = new SyntheticBuilder(node.getId(), node.getType(), node.getX(),
						node.getY(), node.getWidth(), node.getHeight()).newX(node.getX()).newY(node.getY())
						.newWidth(widthRandom).newHeight(heightRandom).injectionType(SIZE).build();
				return violation;
			}
			limit--;
		} while (repeat && parent.getChildCount() != 1 && limit >= 0);
		return null;
	}

	/**
	 * @param component
	 * @return
	 */
	private static boolean validType(String component) {
		// Initial list of invalid types (include other types if needed)
		String[] invalidTypes = { "view", "linearlayout" };
		boolean invalid = false;
		for (String type : invalidTypes) {
			invalid |= component.equalsIgnoreCase(type);
		}
		return !invalid;
	}

	private static boolean overlaps(Rectangle rOriginal, Rectangle rNew) {
		return rOriginal.intersects(rNew);
		/*
		Rectangle inter = rOriginal.intersection(rNew);
		System.out.println(inter.getSize());
		double area = Math.abs(inter.getWidth() * inter.getHeight());
		System.out.println(area);
		if(area < 200000.0) {
			System.out.println("return not overlap");
			return false;
		}
		return true;*/
	}

	public static void main(String[] args) {
		// String originalXml =
		// "Subjects/Full-Examples/Legacy-Files/Huawei-AppStore/App-Implementation-Files/HiApp-Top-UI-Dump.xml";
		// String originalImage =
		// "Subjects/Full-Examples/Legacy-Files/Huawei-AppStore/App-Implementation-Files/HiApp-Top-SS.png";
		// String originalImage =
		// "Subjects/Full-Examples/Legacy-Files/Huawei-AppStore/App-Implementation-Files/HiApp-Top-SS.png";
//		        String originalXml = "Subjects/Full-Examples/Current-Files/Implementation/Huawei-App-Store/HiApp-Me/window_dump.xml";
//		        String originalImage = "Subjects/Full-Examples/Current-Files/Implementation/Huawei-App-Store/HiApp-Me/screenshot.png";
//		        String outputXml = "output";
//		        String outputImage = "output";
//		        try {
		            //
		            // SyntheticHelper.perturbateWindow(SyntheticHelper.NUMBER_COMPONENTS,
		            // 3, originalXml, outputXml,
		            // SyntheticHelper.perturbateWindow(SyntheticHelper.LOCATION, 3,
		            // originalXml, outputXml, originalImage,
//		            SyntheticHelper.perturbateWindow(SyntheticHelper.NUMBER_COMPONENTS, 5, originalXml, outputXml + "1.xml",
//		                    originalImage, outputImage + "1.png", outputImage + "-rules.csv");
//		            SyntheticHelper.perturbateWindow(SyntheticHelper.LOCATION, 5, originalXml, outputXml + "2.xml",
//		                    originalImage, outputImage + "2.png", outputImage + "-rules.csv");
//		            SyntheticHelper.perturbateWindow(SyntheticHelper.SIZE, 5, originalXml, outputXml + "3.xml", originalImage,
//		                    outputImage + "3.png", outputImage + "-rules.csv");
//		            SyntheticHelper.perturbateWindow(SyntheticHelper.TEXT_CONTENT, 5, originalXml, outputXml + "4.xml",
//		                    originalImage, outputImage + "4.png", outputImage + "-rules.csv");
//		            SyntheticHelper.perturbateWindow(SyntheticHelper.TEXT_FONT, 5, originalXml, outputXml + "5.xml",
//		                    originalImage, outputImage + "5.png", outputImage + "-rules.csv");
//		            SyntheticHelper.perturbateWindow(SyntheticHelper.TEXT_COLOR, 5, originalXml, outputXml + "6.xml",
//		                    originalImage, outputImage + "6.png", outputImage + "-rules.csv");
//		            SyntheticHelper.perturbateWindow(SyntheticHelper.IMAGE, 5, originalXml, outputXml + "7.xml", originalImage,
//		                    outputImage + "7.png", outputImage + "-rules.csv");
//		            SyntheticHelper.perturbateWindow(SyntheticHelper.IMAGE_COLOR, 5, originalXml, outputXml + "8.xml",
//		                    originalImage, outputImage + "8.png", outputImage + "-rules.csv");
//		        } catch (Exception e) {
//		            e.printStackTrace();
//		        }

		// String image1 =
		// "Subjects/Testing/Image-Similarity-Tests/Node1DS.jpg";
		// String image2 =
		// "Subjects/Testing/Image-Similarity-Tests/Node1UI.jpg";
		//
		// boolean areHistogramsClose =
		// ImagesHelper.areHistogramsClose(image1,image2 , 0.20);
		
		/*
		String pathToInitialExamples =
				"/Users/KevinMoran/Dropbox/Documents/My_Graduate_School_Work/SEMERU/Huawei_Collaboration/GVT.project/GVT-Empirical-Study/Study-Subjects-Final/Empirical-Study-Implementation-Screens-Only";
		String initialScreensDirectory =
				"/Users/KevinMoran/Desktop/test-GVT/";
		String outputDirectory = "/Users/KevinMoran/Desktop/Empirical-Study-Screens-Second-Round/";
		String pathToCSVFile =
				"/Users/KevinMoran/Desktop/test-GVT/Violations.csv";
		String studyOutputDirectory =
				"/Users/KevinMoran/Desktop/GVT-Subjects-Final";
		getRandomScreensforUserStudy("/Users/KevinMoran/Dropbox/Documents/My_Graduate_School_Work/SEMERU/Huawei_Collaboration/GVT.project/GVT-Empirical-Study/Study-Subjects-Final/Screenshots_for_User_Study/", 10);*/
		//
		//generateStudyData(pathToInitialExamples, initialScreensDirectory, outputDirectory, 100);
		//copyPerturbedFiles(pathToCSVFile, outputDirectory,studyOutputDirectory);
		 // for(int i = 0; i < 100; i++) {
		 // System.out.println("Step: " + i);
		 // try {
		 // SyntheticHelper.perturbateWindow(SyntheticHelper.TEXT_FONT, 1,
		 // initialScreensDirectory + File.separator + "ui-dump--35.xml",
		 // outputDirectory + File.separator + "temp.xml",
		 // initialScreensDirectory + File.separator + "original-35.png",
		 // outputDirectory + File.separator + "temp.png", outputDirectory +
		 // File.separator + "Violations.csv");
		 // } catch (Exception e) {
		 // // TODO Auto-generated catch block
		 // e.printStackTrace();
		 // }
		 // }
		String pathToOriginScreen = "C:/Users/Think/Desktop/origin-screen";
		String pathToOutputFolder = "C:"+File.separator+"Users"+File.separator+"Think"+File.separator+"Desktop"+File.separator+"output-screen";
		generateDynamicComponentStudyData(pathToOriginScreen,pathToOutputFolder,3);

	}

	public static void copyPerturbedFiles(String pathToCSVFile, String pathToPerturbedScreens, String outputDirectory) {

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		ArrayList<String> fileNames = new ArrayList<String>();

		try {

			br = new BufferedReader(new FileReader(pathToCSVFile));
			int ctr = 0;
			while ((line = br.readLine()) != null) {
				if (ctr == 0) {
					ctr++;
					continue;
				}
				// use comma as separator
				String[] violation = line.split(cvsSplitBy);
				System.out.println(violation[0]);
				System.out.println(violation[0]);

				if (!fileNames.contains(violation[0])) {
					fileNames.add(violation[0]);
				}

				ctr++;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Found " + fileNames.size() + " unique files");
		for (String currFileName : fileNames) {

			System.out.println("Copying Screen and XML #: " + currFileName);

			String ssName = currFileName.substring(currFileName.lastIndexOf("/") + 1, currFileName.length()-4)
					.replaceAll("ui-dump-", "original");
			String ssFile = currFileName.replaceAll("ui-dump-", "original");
			ssFile = ssFile.substring(0,ssFile.length()-4);
			System.out.println(ssName);

			String[] copyCommandSS = { "cp", ssFile + ".png", outputDirectory + File.separator + ssName + ".png" };
			try {
				System.out.println(ssFile);
				System.out.println(CmdProcessBuilder.executeCommand(copyCommandSS));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String[] copyCommandXML = { "cp", currFileName, outputDirectory + File.separator
					+ currFileName.substring(currFileName.lastIndexOf("/") + 1, currFileName.length()-4) + ".xml" };
			try {
				System.out.println(CmdProcessBuilder.executeCommand(copyCommandXML));

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	public static ArrayList<ImplementationScreen> generateDynamicInitialScreenPool(String pathToInitialExamples,String com,
			String outputDirectory, int numOfScreensToGenerate) {

		ArrayList<ImplementationScreen> tempScreenPool = new ArrayList<ImplementationScreen>();
		ArrayList<ImplementationScreen> initialScreenPool = new ArrayList<ImplementationScreen>();

		ImplementationScreen currScreen = null;
		ImplementationScreen finalScreen = null;

		String[] ssExts = { "png" }; // File Extensions to search for
		String[] xmlExts = { "xml" };

		File inputDir = new File(pathToInitialExamples);

		// Use Apache FileUtils to find all screenshots
		Collection<File> screenshotCollection = FileUtils.listFiles(inputDir, ssExts, true);
		List<File> screenshotList = new ArrayList<File>(screenshotCollection);
		Collection<File> uiautomatorCollection = FileUtils.listFiles(inputDir, xmlExts, true);
		List<File> uiautomatorList = new ArrayList<File>(uiautomatorCollection);

		for (int i = 0; i < screenshotList.size(); i++) {

			currScreen = new ImplementationScreen();
			currScreen.setPathToScreenShot(screenshotList.get(i).getAbsolutePath());
			System.out.println(screenshotList.get(i).getAbsolutePath());
			currScreen.setPathtoUiAutomatorFile(uiautomatorList.get(i).getAbsolutePath());
			System.out.println(uiautomatorList.get(i).getAbsolutePath());
			System.out.println();
			tempScreenPool.add(currScreen);

		}

		Random ranSeed = new Random();
		int randomIndex;
		String screenFileName;
		String xmlFileName;

		System.out.println("Generating new screens...");
		System.out.println();

		for (int j = 0; j < numOfScreensToGenerate; j++) {

			System.out.println("Generating screen " + j);

			randomIndex = ranSeed.nextInt(screenshotList.size());

			currScreen = tempScreenPool.get(randomIndex);
			screenFileName = currScreen.getPathToScreenShot().substring(
					currScreen.getPathToScreenShot().lastIndexOf(com+File.separator) + com.length()+1,
					currScreen.getPathToScreenShot().lastIndexOf("."));
			
			System.out.println(screenFileName);
			String[] copyCommand1 = { "copy", currScreen.getPathToScreenShot(),
					outputDirectory+File.separator+screenFileName+"-"+j+".png"};
			
			for(int i=0; i < copyCommand1.length; i++){
				System.out.print(copyCommand1[i]);
				
			}
			
			try {
				System.out.println("output for copy png");
				System.out.println(CmdProcessBuilder.executeCommand(copyCommand1));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			xmlFileName = currScreen.getPathtoUiAutomatorFile().substring(
					currScreen.getPathtoUiAutomatorFile().lastIndexOf(com+File.separator) + com.length()+1,
					currScreen.getPathToScreenShot().lastIndexOf("."));
			//xmlFileName = xmlFileName.replaceAll("/", "-");
			System.out.println(xmlFileName);
			String[] copyCommand2 = { "copy", currScreen.getPathtoUiAutomatorFile(),
					outputDirectory + File.separator+xmlFileName + "-" + j + ".xml" };
			for(int i=0; i < copyCommand1.length; i++){
				System.out.print(copyCommand2[i]);
				
			}

			try {
				System.out.println("output for copy xml");
				System.out.println(CmdProcessBuilder.executeCommand(copyCommand2));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			currScreen = new ImplementationScreen();
			currScreen.setPathToScreenShot(outputDirectory + File.separator+screenFileName + "-" + j + ".png");
			currScreen.setPathtoUiAutomatorFile(outputDirectory + File.separator+xmlFileName + "-" + j + ".xml");
			initialScreenPool.add(currScreen);

		}

		return initialScreenPool;

	}
	

	public static ArrayList<ImplementationScreen> generateInitialScreenPool(String pathToInitialExamples,
			String outputDirectory, int numOfScreensToGenerate) {

		ArrayList<ImplementationScreen> tempScreenPool = new ArrayList<ImplementationScreen>();
		ArrayList<ImplementationScreen> initialScreenPool = new ArrayList<ImplementationScreen>();

		ImplementationScreen currScreen = null;
		ImplementationScreen finalScreen = null;

		String[] ssExts = { "png" }; // File Extensions to search for
		String[] xmlExts = { "xml" };

		File inputDir = new File(pathToInitialExamples);

		// Use Apache FileUtils to find all screenshots
		Collection<File> screenshotCollection = FileUtils.listFiles(inputDir, ssExts, true);
		List<File> screenshotList = new ArrayList<File>(screenshotCollection);
		Collection<File> uiautomatorCollection = FileUtils.listFiles(inputDir, xmlExts, true);
		List<File> uiautomatorList = new ArrayList<File>(uiautomatorCollection);

		for (int i = 0; i < screenshotList.size(); i++) {

			currScreen = new ImplementationScreen();
			currScreen.setPathToScreenShot(screenshotList.get(i).getAbsolutePath());
			System.out.println(screenshotList.get(i).getAbsolutePath());
			currScreen.setPathtoUiAutomatorFile(uiautomatorList.get(i).getAbsolutePath());
			System.out.println(uiautomatorList.get(i).getAbsolutePath());
			System.out.println();
			tempScreenPool.add(currScreen);

		}

		Random ranSeed = new Random();
		int randomIndex;
		String screenFileName;
		String xmlFileName;

		System.out.println("Generating new screens...");
		System.out.println();

		for (int j = 0; j < numOfScreensToGenerate; j++) {

			System.out.println("Generating screen " + j);

			randomIndex = ranSeed.nextInt(screenshotList.size());

			currScreen = tempScreenPool.get(randomIndex);

			screenFileName = currScreen.getPathToScreenShot().substring(
					currScreen.getPathToScreenShot().lastIndexOf("Only/") + 5,
					currScreen.getPathToScreenShot().lastIndexOf("."));
			screenFileName = screenFileName.replaceAll("/", "-");
			System.out.println(screenFileName);
			String[] copyCommand1 = { "cp", currScreen.getPathToScreenShot(),
					outputDirectory + screenFileName + "-" + j + ".png" };
			try {
				System.out.println(CmdProcessBuilder.executeCommand(copyCommand1));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			xmlFileName = currScreen.getPathtoUiAutomatorFile().substring(
					currScreen.getPathtoUiAutomatorFile().lastIndexOf("Only/") + 5,
					currScreen.getPathToScreenShot().lastIndexOf("."));
			xmlFileName = xmlFileName.replaceAll("/", "-");
			String[] copyCommand2 = { "cp", currScreen.getPathtoUiAutomatorFile(),
					outputDirectory + xmlFileName + "-" + j + ".xml" };

			try {
				System.out.println(CmdProcessBuilder.executeCommand(copyCommand2));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			currScreen = new ImplementationScreen();
			currScreen.setPathToScreenShot(outputDirectory + screenFileName + "-" + j + ".png");
			currScreen.setPathtoUiAutomatorFile(outputDirectory + xmlFileName + "-" + j + ".xml");
			initialScreenPool.add(currScreen);

		}

		return initialScreenPool;

	}
	public static void generateDynamicComponentStudyData(String pathToInput,String outputDic, int numOfScreensToGenerate) {
		ArrayList<String> dynamicComp = new ArrayList<String>(
				Arrays.asList("checkbox","checkedtextview","numpicker","radiobutton","ratingbar","seekbar","switch","image","progressbar","text"));

		ArrayList<String> violationsToSeed = new ArrayList<String>();
		int locationViolations = numOfScreensToGenerate*4-1;
		int sizeViolations = numOfScreensToGenerate;
		int imageColorViolations = numOfScreensToGenerate;
		int WrongText = numOfScreensToGenerate;
		int fontStyle = numOfScreensToGenerate;
		int fontColor = numOfScreensToGenerate;
		String pathToOutput = "";
		/*
		for (int i = 0; i < locationViolations; i++) {
			violationsToSeed.add(SyntheticHelper.LOCATION);
		}*/
		
		for (int i = 0; i < sizeViolations; i++) {
			violationsToSeed.add(SyntheticHelper.SIZE);
		}
		/*
		for (int i = 0; i < fontStyle; i++) {
			violationsToSeed.add(SyntheticHelper.TEXT_FONT);
		}
		for (int i = 0; i < fontColor; i++) {
			violationsToSeed.add(SyntheticHelper.TEXT_COLOR);
		}
		
		/*
		for (int i = 0; i < imageColorViolations; i++) {
			violationsToSeed.add(SyntheticHelper.COMPONENT_COLOR);
		}
		
		for (int i = 0; i < imageColorViolations; i++) {
			violationsToSeed.add(SyntheticHelper.IMAGE_COLOR);
		}*/
		
		
		
		for(String com : dynamicComp) {
			if(com == "seekbar") {
				System.out.print("in com folder  "+com);
				System.out.println(" folder path"+pathToInput+File.separator+com);
				pathToOutput = outputDic + File.separator + com;
				ArrayList<ImplementationScreen> screens = generateDynamicInitialScreenPool(pathToInput+File.separator+com, com,pathToOutput,
						20);
			
				Random ranSeed = new Random();
				int randomIndex;
				ImplementationScreen currScreen;
				ArrayList<ImplementationScreen> screensUsed = new ArrayList<ImplementationScreen>();
				ArrayList<SyntheticViolation> synViolationsToSeed = new ArrayList<SyntheticViolation>();
				Collections.shuffle(violationsToSeed);
				
				for (String currViolationType : violationsToSeed) {
					System.out.println(currViolationType);
					randomIndex = ranSeed.nextInt(screens.size());
					currScreen = screens.get(randomIndex);
					
					boolean violationFound = true;
					boolean screenOkay;
					screenOkay = checkScreenUsage(screensUsed, currScreen);
					
					if(screenOkay) {
						System.out.println("Attempting to find Violation for screen: " + currScreen.getPathToScreenShot() + " with Violation Type: "
								+ currViolationType);
						
						try {
							SyntheticViolation currViolation = getDynViolation(currViolationType, currScreen.getPathtoUiAutomatorFile(), currScreen.getPathToScreenShot());
							System.out.println("currViolation: "+currViolation);
							if(!(currViolation == null) ) {
								violationFound = true;
								screensUsed.add(currScreen);
								synViolationsToSeed.add(currViolation);
							}else {
								violationFound = false;
								System.out.println("failed to add");
							}
							screensUsed.add(currScreen);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					else {
						System.out.println("Screen Already has three violations, looking for another one...");
						violationFound = false;
					}

					if (!violationFound) {

						System.out.println("Violation not found for current screen, searching for screen with violation...");
						int ctr = 0;
						while (!violationFound && ctr < 100) {
							randomIndex = ranSeed.nextInt(screens.size());
							currScreen = screens.get(randomIndex);

							screenOkay = checkScreenUsage(screensUsed, currScreen);

							if (!screenOkay) {
								ctr++;
								continue;
							}

							try {
								System.out.println("==============ctr===============" + ctr );
								SyntheticViolation currViolation = getDynViolation(currViolationType, currScreen.getPathtoUiAutomatorFile(), currScreen.getPathToScreenShot());
								if(!(currViolation == null) ) {
									violationFound = true;
									screensUsed.add(currScreen);
									synViolationsToSeed.add(currViolation);
								}else {
									violationFound = false;
								}


							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
						ctr++;
					}
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}

				
				
				int j = 0;
				for(SyntheticViolation violationToSeed: synViolationsToSeed) {
					
					try {
						System.out.println("Seeding Violation: " + violationToSeed.getInjectionType() + " to " + violationToSeed.getOrgXMLFile());
						String name = violationToSeed.getOrgSSFile().substring(
								violationToSeed.getOrgSSFile().lastIndexOf(com)+com.length()+1, violationToSeed.getOrgSSFile().lastIndexOf("-"));
						//seedViolation(violationToSeed, violationToSeed.getOrgXMLFile(),  pathToOutput + File.separator + com +"-temp" + j +".xml", violationToSeed.getOrgSSFile(), pathToOutput + File.separator +  com +"-temp" + j + ".png", pathToOutput + File.separator + "Violations.csv");
						seedViolation(violationToSeed, violationToSeed.getOrgXMLFile(),  pathToOutput + File.separator + com+ "-"+ name +"-temp" + j +".xml", violationToSeed.getOrgSSFile(), pathToOutput + File.separator +  com+ "-"+ name +"-temp" + j + ".png", pathToOutput + File.separator + "Violations.csv");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
			
					String name = violationToSeed.getOrgSSFile().substring(
							violationToSeed.getOrgSSFile().lastIndexOf(com)+com.length()+1, violationToSeed.getOrgSSFile().lastIndexOf("-"));
							
							/*screenFileName = currScreen.getPathToScreenShot().substring(
									currScreen.getPathToScreenShot().lastIndexOf(com+File.separator) + com.length()+1,
									currScreen.getPathToScreenShot().lastIndexOf("."));*/
					/*
					String[] mvScreenCommand = { "mv", pathToOutput +  com+ File.separator + com + "-temp" + j +".xml",
							violationToSeed.getOrgXMLFile() };
					String[] mvXMLCommand = { "mv", pathToOutput +  com+ File.separator + com +"-temp" + j + ".png",
							violationToSeed.getOrgSSFile() };*/
					
					String[] mvScreenCommand = { "mv", pathToOutput +  com+ File.separator + com+ "-"+ name + "-temp" + j +".xml",
							violationToSeed.getOrgXMLFile() };
					String[] mvXMLCommand = { "mv", pathToOutput +  com+ File.separator + com+ "-"+ name +"-temp" + j + ".png",
							violationToSeed.getOrgSSFile() };
					try {
						System.out.println(CmdProcessBuilder.executeCommand(mvScreenCommand));
						System.out.println(CmdProcessBuilder.executeCommand(mvXMLCommand));
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					j++;
				}
				
				
			}

			
		}
		
	}

	public static void generateStudyData(String pathToInitialExamples, String initialScreensDirectory,
			String outputDirectory, int numOfScreensToGenerate) {

		// Create the initial set of violations to seed

		ArrayList<String> violationsToSeed = new ArrayList<String>();

		int locationViolations = 68;
		int sizeViolations = 18;
		int imageColorViolation = 22;
		int fontStyle = 18;
		int fontColor = 18;
		int missingComponent = 26;
		int imageViolation = 16;
		int WrongText = 14;

//		int locationViolations = 0;
//		int sizeViolations = 0;
//		int imageColorViolation = 4;
//		int fontStyle = 0;
//		int fontColor = 0;
//		int missingComponent = 0;
//		int imageViolation = 0;
//		int WrongText = 0;
		
		for (int i = 0; i < locationViolations; i++) {
			violationsToSeed.add(SyntheticHelper.LOCATION);
		}
		for (int i = 0; i < sizeViolations; i++) {
			violationsToSeed.add(SyntheticHelper.SIZE);
		}
		for (int i = 0; i < fontStyle; i++) {
			violationsToSeed.add(SyntheticHelper.TEXT_FONT);
		}
		for (int i = 0; i < missingComponent; i++) {
			violationsToSeed.add(SyntheticHelper.NUMBER_COMPONENTS);
		}
		for (int i = 0; i < imageViolation; i++) {
			violationsToSeed.add(SyntheticHelper.IMAGE);
		}
		for (int i = 0; i < imageColorViolation; i++) {
			violationsToSeed.add(SyntheticHelper.IMAGE_COLOR);
		}
		for (int i = 0; i < fontColor; i++) {
			violationsToSeed.add(SyntheticHelper.TEXT_COLOR);
		}
		for (int i = 0; i < WrongText; i++) {
			violationsToSeed.add(SyntheticHelper.TEXT_CONTENT);
		}

		// Generate the initial Pool of screens

		ArrayList<ImplementationScreen> screens = generateInitialScreenPool(pathToInitialExamples, outputDirectory,
				numOfScreensToGenerate);

		// Seed Violations Randomly across screen pool

		Random ranSeed = new Random();
		int randomIndex;
		ImplementationScreen currScreen;
		ArrayList<ImplementationScreen> screensUsed = new ArrayList<ImplementationScreen>();
		ArrayList<SyntheticViolation> synViolationsToSeed = new ArrayList<SyntheticViolation>();
		Collections.shuffle(violationsToSeed);
		for (String currViolationType : violationsToSeed) {

			boolean violationFound = true;
			boolean screenOkay;

			randomIndex = ranSeed.nextInt(screens.size());
			currScreen = screens.get(randomIndex);

			screenOkay = checkScreenUsage(screensUsed, currScreen);

			if (screenOkay) {

				System.out.println("Attempting to find Violation for screen: " + currScreen.getPathToScreenShot() + " with Violation Type: "
						+ currViolationType);

				try {
					SyntheticViolation currViolation = getViolation(currViolationType, currScreen.getPathtoUiAutomatorFile(), currScreen.getPathToScreenShot());
					//                    violationFound = SyntheticHelper.perturbateWindow(currViolationType, 1,
					//                            currScreen.getPathtoUiAutomatorFile(), outputDirectory + File.separator + "temp.xml",
					//                            currScreen.getPathToScreenShot(), outputDirectory + File.separator + "temp.png",
					//                            outputDirectory + File.separator + "Violations.csv");
					if(!(currViolation == null) && !compareViolations(currViolation, synViolationsToSeed)) {
						violationFound = true;
						screensUsed.add(currScreen);
						synViolationsToSeed.add(currViolation);
					}else {
						violationFound = false;
					}
					screensUsed.add(currScreen);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				System.out.println("Screen Already has three violations, looking for another one...");
				violationFound = false;
			}

			if (!violationFound) {

				System.out.println("Violation not found for current screen, searching for screen with violation...");
				int ctr = 0;
				while (!violationFound && ctr < 100) {
					randomIndex = ranSeed.nextInt(screens.size());
					currScreen = screens.get(randomIndex);

					screenOkay = checkScreenUsage(screensUsed, currScreen);

					if (!screenOkay) {
						ctr++;
						continue;
					}

					try {

						SyntheticViolation currViolation = getViolation(currViolationType, currScreen.getPathtoUiAutomatorFile(), currScreen.getPathToScreenShot());
						//                        violationFound = SyntheticHelper.perturbateWindow(currViolationType, 1,
						//                                currScreen.getPathtoUiAutomatorFile(), outputDirectory + File.separator + "temp.xml",
						//                                currScreen.getPathToScreenShot(), outputDirectory + File.separator + "temp.png",
						//                                outputDirectory + File.separator + "Violations.csv");

						if(!(currViolation == null) && !compareViolations(currViolation, synViolationsToSeed)) {
							violationFound = true;
							screensUsed.add(currScreen);
							synViolationsToSeed.add(currViolation);
						}else {
							violationFound = false;
						}


					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				ctr++;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		System.out.println("Preparing to Inject Violations...");

		for(SyntheticViolation violationToSeed: synViolationsToSeed) {

			try {
				System.out.println("Seeding Violation: " + violationToSeed.getInjectionType() + " to " + violationToSeed.getOrgXMLFile());
				seedViolation(violationToSeed, violationToSeed.getOrgXMLFile(),  outputDirectory + File.separator + "temp.xml", violationToSeed.getOrgSSFile(), outputDirectory + File.separator + "temp.png", outputDirectory + File.separator + "Violations.csv");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			String[] mvScreenCommand = { "mv", outputDirectory + File.separator + "temp.xml",
					violationToSeed.getOrgXMLFile() };
			String[] mvXMLCommand = { "mv", outputDirectory + File.separator + "temp.png",
					violationToSeed.getOrgSSFile() };

			try {
				System.out.println(CmdProcessBuilder.executeCommand(mvScreenCommand));
				System.out.println(CmdProcessBuilder.executeCommand(mvXMLCommand));
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

	public static boolean checkScreenUsage(ArrayList<ImplementationScreen> screensToCheck,
			ImplementationScreen screenToCheck) {
		boolean okToUse;

		int ctr = 0;

		for (ImplementationScreen currScreen : screensToCheck) {
			if (currScreen.equals(screenToCheck)) {
				ctr++;
			}
		}

		if (ctr < 5) {
			okToUse = true;
		} else {
			okToUse = false;
		}

		return okToUse;
	}

	private static Font getFont(FontType font) {
		return FONTS.get(font);
	}
	
	public static void getRandomScreensforUserStudy(String inputDirectory, int numOfScreens) {
		

		String[] ssExts = { "png" }; // File Extensions to search for

		File inputDir = new File(inputDirectory);

		// Use Apache FileUtils to find all screenshots
		Collection<File> screenshotCollection = FileUtils.listFiles(inputDir, ssExts, true);
		List<File> screenshotList = new ArrayList<File>(screenshotCollection);

		Collections.shuffle(screenshotList);
		
		for (int i = 0; i < numOfScreens; i++) {
			System.out.println(screenshotList.get(i).getAbsolutePath());
		}
		
		
	}
	
}

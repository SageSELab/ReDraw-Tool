package edu.wm.cs.semeru.redraw;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.opencv.core.Rect;

import edu.wm.cs.semeru.redraw.JSNode;
import edu.wm.cs.semeru.redraw.helpers.ImagesHelper;
import edu.wm.cs.semeru.core.helpers.ScreenshotModifier;

/**
 * A representation of an Android view hierarchy.
 * 
 * @author William T. Hollingsworth
 * @author Steve Walker
 */
public class ViewHierarchy extends ViewNode {
	public static final String HIERARCHY_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>\n" +
			"<hierarchy>\n";
	public static final String HIERARCHY_FOOTER = "</hierarchy>";
	public static final String LAYOUT_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<RelativeLayout\n    " +
			"xmlns:android=\"http://schemas.android.com/apk/res/android\"\n    " +
			"xmlns:tools=\"http://schemas.android.com/tools\"\n    " +
			"android:layout_width=\"match_parent\"\n    " +
			"android:layout_height=\"match_parent\"\n    " +
			"android:paddingLeft=\"0dp\"\n    " +
			"android:paddingRight=\"0dp\"\n    " +
			"android:paddingTop=\"0dp\"\n    " +
			"android:paddingBottom=\"0dp\"\n    " +
			"tools:context=\".MainActivity\"\n";
	public static final String LAYOUT_FOOTER = "</RelativeLayout>\n";
	public static final String INDENTATION_STRING = "    ";

	/**
	 * Constructor; creates an empty view hierarchy.
	 *
	 * @param width     The width of the entire hierarchy
	 * @param height    The height of the entire hierarchy
	 */
	public ViewHierarchy(int width, int height) {
		super(null, ViewType.LAYOUT, new Rect(0, 0, width, height), null);
	}

	/**
	 * Exports to file in the format of a uiautomator hierarchy dump
	 */
	public void exportToFileAsUiDump(String fileName) {
		try {
			File file = new File(fileName);
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter writer = new BufferedWriter(fileWriter);
			writer.write(HIERARCHY_HEADER);

			// DFS traversal
			for (int x = 0; x < getNumChildren(); x++) {
				writeNodeUiDump(writer, childAtIndex(x), 1, x);
			}

			writer.write(HIERARCHY_FOOTER);
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<JSNode> getList() {
		ArrayList<JSNode> nodes = new ArrayList<JSNode>();
		try {
		for (int x = 0; x < getNumChildren(); x++) {
			traverseNodes(childAtIndex(x),nodes, 1, x);
		}
		
		}catch(IOException e) {
			e.printStackTrace();
		}
		return nodes;
	}
	
	/**
	 * Retreives the child at the given index without performing any bounds
	 * checking on the argument.
	 *
	 * @param fileName  The name of the file this file should be exported to.
	 */
	public void exportToFile(String fileName, String pathToImage, String outputPath) {
		try {
			Random rand = new Random();
			File file = new File(fileName);
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter writer = new BufferedWriter(fileWriter);
			Color[] colors  = ImagesHelper.quantizeImageAndGetColors(pathToImage, 1);
			int background = colors[0].getRGB();
			String backHex = ImagesHelper.argb2Hex(ImagesHelper.intToArgb(background));
			writer.write(LAYOUT_HEADER + "android:background=\"" + backHex + "\">\n\n");

			// DFS traversal
			for (int x = 0; x < getNumChildren(); x++) {
				writeNode(writer, childAtIndex(x), 1, pathToImage, outputPath);
			}

			writer.write(LAYOUT_FOOTER);
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Traverses the tree and saves the nodes to an ArrayList
	 */
	private void traverseNodes(ViewNode node, ArrayList<JSNode> nodes, int depth, int index) throws IOException {
		JSNode currNode = new JSNode(node.bounds.x, node.bounds.y, node.bounds.width, node.bounds.height);
		if(node.getViewType() != ViewType.LAYOUT) {
		nodes.add(currNode);
		}
			// write each child
			for (int x = 0; x < node.getNumChildren(); x++) {
				traverseNodes(node.childAtIndex(x), nodes, depth + 1, x);
			}

	}
	
	/**
	 * Writes the current node to the view hierarchy as a uiautomator dump
	 */
	private void writeNodeUiDump(BufferedWriter writer, ViewNode node, int depth, int index) throws IOException {
		writer.write("\n");
		writeIndentation(writer, depth);
		writer.write("<node index=\"" + index + "\" class=\"" + node.viewType.viewName() + "\"\n");

		if (ViewType.IMAGE.equals(node.viewType)) {
			writeIndentation(writer, depth + 1);
			writer.write(" content-desc=\""+ node.data + "\"\n");
		}

		writeIndentation(writer, depth + 1);
		writer.write(" bounds=\"[" + node.bounds.x + "," + node.bounds.y + "][" 
				+ (node.bounds.x + node.bounds.width) + "," + (node.bounds.y + node.bounds.height) + "]\"");

		if (node.getNumChildren() == 0) {
			writer.write("/>\n");
		}
		else {
			writer.write(">\n");

			// write each child
			for (int x = 0; x < node.getNumChildren(); x++) {
				writeNodeUiDump(writer, node.childAtIndex(x), depth + 1, x);
			}

			// write footer
			writeIndentation(writer, depth);
			writer.write("</node>\n");
		}
	}

	/**
	 * Writes the current node to the view hierarchy.
	 *
	 * @param writer  The writer that the node will be written to.
	 * @param node    The node that is being written.
	 * @param depth   The depth of the node in the hierarchy. Used for indentation.
	 */
	private void writeNode(BufferedWriter writer, ViewNode node, int depth, String pathToImage, String outputPath) throws IOException {
		
		Random rand = new Random();
		
		File croppedPath = new File(outputPath + File.separator + "CroppedIms");
		croppedPath.mkdirs();
		
		// write current node header
		writer.write("\n");
		writeIndentation(writer, depth);
		writer.write("<" + node.viewType.viewName() + "\n");

		if (node.viewType.viewProperty() != null) {
			writeIndentation(writer, depth + 1);
			writer.write(node.viewType.viewProperty() + node.data + "\"\n");
		}

		if (node.bounds != null) {
			String backHex = null;
			if(node.getViewType().equals(ViewType.TEXT)) {
				String output = outputPath + File.separator + "CroppedIms" + File.separator + rand.nextInt() + ".png";
				ScreenshotModifier.cropScreenshot(pathToImage, output, node.bounds.x, node.bounds.y, node.bounds.height, node.bounds.width);
				Color[] colors  = ImagesHelper.quantizeImageAndGetColors(output, 2);
				if(colors[1] != null) {
				int background = colors[1].getRGB();
					backHex = ImagesHelper.argb2Hex(ImagesHelper.intToArgb(background));
				}else {
					backHex = "#ffffff";
				}
				writeIndentation(writer, depth + 1);
				writer.write("android:textColor=\"" + backHex + "\"\n");
				writeIndentation(writer, depth + 1);
				int textHeight = node.bounds.height - 5;
				writer.write("android:textSize=\"" + textHeight + "px\"");
			}
			if(node.getViewType().equals(ViewType.LAYOUT)) {
				String output = outputPath + File.separator + "CroppedIms" + File.separator + rand.nextInt() + ".png";
				ScreenshotModifier.cropScreenshot(pathToImage, output, node.bounds.x, node.bounds.y, node.bounds.height, node.bounds.width);
				Color[] colors  = ImagesHelper.quantizeImageAndGetColors(output, 1);
				int background = colors[0].getRGB();
				backHex = ImagesHelper.argb2Hex(ImagesHelper.intToArgb(background));
				writeIndentation(writer, depth + 1);
				writer.write("android:background=\"" + backHex + "\"\n");
				
			}
			writeIndentation(writer, depth + 1);
			writer.write("android:layout_marginLeft=\"" + (node.bounds.x - node.parent.bounds.x) + "px\"\n");
			writeIndentation(writer, depth + 1);
			writer.write("android:layout_marginTop=\"" + (node.bounds.y - node.parent.bounds.y) + "px\"\n");
			writeIndentation(writer, depth + 1);
			writer.write("android:layout_width=\"" + node.bounds.width + "px\"\n");
			writeIndentation(writer, depth + 1);
			writer.write("android:layout_height=\"" + node.bounds.height + "px\"");
		}

		if (node.getNumChildren() == 0) {
			writer.write("/>\n");
		}
		else {
			writer.write(">\n");

			// write each child
			for (int x = 0; x < node.getNumChildren(); x++) {
				writeNode(writer, node.childAtIndex(x), depth + 1, pathToImage, outputPath);
			}

			// write footer
			writeIndentation(writer, depth);
			writer.write("</" + node.viewType.viewName() + ">\n");
		}
	}


	/**
	 * Writes the indentation spacing for the specified depth.
	 *
	 * @param writer  The writer that the identation will be written to.
	 * @param depth   The depth of indentation to be written.
	 */
	private void writeIndentation(BufferedWriter writer, int depth) throws IOException {
		for (int x = 0; x < depth; x++) {
			writer.write(INDENTATION_STRING);
		}
	}

	/**
	 * Eliminates nested images. Goes from the bottom up. Only affects image nodes that have children.
	 * If all children are image nodes, the children are removed and all that is left is the larger parent.
	 * If there are any non-image nodes (text or lists), then the image view is changed to a layout node.
	 */
	public void imageReduction() {
		for(ViewNode node : children) {
			imageReduction(node);
		}
	}

	/**
	 * Peforms the image reduction as described above. Peforms a post-order traversal.
	 *
	 * @param node The node the reduction will be performed on.
	 */
	private void imageReduction(ViewNode node) {
		if (node.viewType.equals(ViewType.IMAGE)) {
			boolean allImages = true;

			for (ViewNode child : node.children) {
				imageReduction(child);
				if (child.viewType.equals(ViewType.IMAGE)) {
					allImages = false;
				}
			}

			if (node.children.size() != 0 && allImages) {
				node.children.clear();
			}
			else if (!allImages) {
				node.viewType = ViewType.LAYOUT;
			}
		}
	}

}

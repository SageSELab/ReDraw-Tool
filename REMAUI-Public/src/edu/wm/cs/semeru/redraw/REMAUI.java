package edu.wm.cs.semeru.redraw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.Exception;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import edu.semeru.android.core.helpers.device.DeviceHelper;
import edu.semeru.android.core.helpers.device.StepByStepEngine;
import edu.semeru.android.core.helpers.ui.UiAutoConnector.TypeDeviceEnum;
import edu.wm.cs.semeru.redraw.JSNode;
import edu.wm.cs.semeru.core.helpers.TerminalHelper;
import edu.wm.cs.semeru.redraw.classifiers.KMeansClassifier;
import edu.wm.cs.semeru.redraw.helpers.ImagesHelper;
import edu.wm.cs.semeru.redraw.ocr.OCRLine;
import edu.wm.cs.semeru.redraw.ocr.OCRParser;
import edu.wm.cs.semeru.redraw.ocr.OCRTextBlock;
import edu.wm.cs.semeru.redraw.ocr.OCRTextBlockGenerator;
import edu.wm.cs.semeru.redraw.ocr.OCRWord;
import sun.misc.IOUtils;

/**
 * The main class.
 *
 * @author Steven Wallker
 * @author William T. Hollingsworth
 */
public class REMAUI {
	
	private int MIN_AREA = 100;

    /**
     * The main method.
     *
     * @param inputImagePath The app image for which to extract a view hierarchy.
     * @param outputRoot A directory in which to dump all output images.
     */
    public ViewHierarchy run(String projectRootDirectory, String inputImagePath, String outputRoot) {

        File outputDirectory = new File(outputRoot);
        outputDirectory.mkdirs();

        System.out.println("Starting OpenCV");
        
        if (!(new File(inputImagePath).exists())) {
        	System.out.println("Image file does not exist!");
        	return null;
        }

        // get vision boxes from OpenCV
        CVContours contours = cvProcess(inputImagePath, outputRoot);
        // TODO: pass this as a parameter to cvProcess
        Mat inputImg = Highgui.imread(inputImagePath);
        ViewHierarchy vh = createViewHierarchy(contours, inputImg.width(), inputImg.height());

        System.out.println("OpenCV completed. Starting OCR");
        
        // run tesseract on the input image
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        boolean mac = System.getProperty("os.name").toLowerCase().contains("mac");
        String tessDir = projectRootDirectory + File.separator + "lib" + File.separator + (windows ? "tesseract-win" : mac ? "tesseract-mac" : "tesseract");
        String tesseract = tessDir + File.separator + "tesseract";
        String outputPath = outputRoot + File.separator + "out";
        String tessConfig = tessDir + File.separator + "tessdata" + File.separator + "configs" + File.separator + "hocr";
        //String[] envp = {"TESSDATA_PREFIX=" + tessDir, "LD_LIBRARY_PATH="+ tessDir};
        
        Runtime rt = Runtime.getRuntime();
        //tesseract = "/usr/local/Cellar/tesseract/3.05.01/bin/tesseract";
        try {
        	String[] cmd = new String[] {tesseract, inputImagePath, outputPath, "--tessdata-dir", tessDir, tessConfig};
        	ProcessBuilder builder = new ProcessBuilder(cmd);
            Process proc = builder.start();
            String terminal = readInputStream(proc.getInputStream());
            proc.waitFor();
            System.out.println(terminal);
        }
        catch (Exception e) {
            // log if there was a failure and bail
            Logger logger = Logger.getLogger(REMAUI.class.getName());
            logger.log(Level.SEVERE, null, e);
            return null;
        }

        System.out.println("OCR completed. Starting OCR parsing");


        // create a parser to extract information from the OCR output
        OCRParser parser = new OCRParser(outputRoot + File.separator + "out.hocr");
        List<OCRLine> lines = parser.getLines();
        

        System.out.println("OCR parsing completed. Starting heuristics");


        List<Rect> originalLinesBoundingBoxes = new ArrayList<Rect>();

        List<OCRWord> words = new ArrayList<OCRWord>();
        for (OCRLine line: lines) {
            words.addAll(line.getContainedWords());
            originalLinesBoundingBoxes.add(line.getBoundingBox());
        }

        List<OCRWord> filtered = new ArrayList<OCRWord>();

        List<Rect> wordBoundingBoxes = new ArrayList<Rect>();
        List<Rect> lineBoundingBoxes = new ArrayList<Rect>();
        List<Rect> filteredBoundingBoxes = new ArrayList<Rect>();
        List<Rect> blockBoundingBoxes = new ArrayList<Rect>();


        // filter out false positive words and get collections of bounding boxes
        for (OCRWord word : words) {
        	wordBoundingBoxes.add(word.getBoundingBox());
        	filtered.add(word);
        	
        	if (boundsCheck(word, inputImg.width(), inputImg.height()) && ocrCheck(word)) {
        		System.out.println(word.getText() + " added!");
        	    filtered.add(word);
        	}
        	else {
        	    word.setValid(false);
        	}
        	
        }

        // merge the OpenCV and OCR results
        MergeOperation mergeop = new MergeOperation(filtered, contours);
        mergeop.setImageDimensions(inputImg.width(), inputImg.height());
        mergeop.getFilteredOCRWords();

        // TODO: seem to have empty lines for some reason, obviously better fixed somewhere else
        for (int i = lines.size()-1; i >= 0; i--) {
            lines.get(i).removeInvalidWords();
            if (lines.get(i).getContainedWords().size() == 0) {
                lines.remove(i);
            }
        }

        // draw the bounding boxes for the final OCR results
        for (OCRWord word: filtered) {
            filteredBoundingBoxes.add(word.getBoundingBox());
        }

        // generate text blocks for the extracted text content
        OCRTextBlockGenerator generator = new OCRTextBlockGenerator();
        List<OCRTextBlock> blocks = new ArrayList<OCRTextBlock>();
        for (OCRLine line : lines) {
            generator.reset(line);
            blocks.addAll(generator.getTextBlocks());
            lineBoundingBoxes.add(line.getBoundingBox());
        }

        for (OCRTextBlock block: blocks) {
            vh.addTextBlock(block);
            blockBoundingBoxes.add(block.getBoundingBox());
        }

        //drawBoundingBoxesToFile(wordBoundingBoxes, outputRoot + File.separator + "wordBoundingBoxes.png", inputImg.width(), inputImg.height(), false);
        //drawBoundingBoxesToFile(filteredBoundingBoxes, outputRoot + File.separator + "filteredBoundingBoxes.png", inputImg.width(), inputImg.height(), false);
        //drawBoundingBoxesToFile(lineBoundingBoxes, outputRoot + File.separator + "lineBoundingBoxes.png", inputImg.width(), inputImg.height(), false);
        //drawBoundingBoxesToFile(blockBoundingBoxes, outputRoot + File.separator + "blockBoundingBoxes.png", inputImg.width(), inputImg.height(), false);
        //drawBoundingBoxesToFile(originalLinesBoundingBoxes, outputRoot + File.separator + "originalLineBoundingBoxes.png", inputImg.width(), inputImg.height(), false);

        vh.imageReduction();
        //vh.identifyAndInsertLists();
        
        vh.exportImages(outputRoot, inputImg);

        //vh.exportToFile(outputRoot + File.separator + "layout_basic.xml");
       // vh.exportToFileAsUiDump(outputRoot + File.separator + "ui_dump.xml");
        
       // vh.identifyComponentTypes(new KMeansClassifier(outputRoot + "/drawable"));
        vh.exportToFile(outputRoot + File.separator + "activity_main.xml", inputImagePath, outputRoot);
       
        System.out.println("Creating Skeleton App...");
        boolean compile = createSkeletonApp(projectRootDirectory + File.separator + "App-Skeleton", outputRoot);
        
        if(compile) {
        	captureSSandUIDump(outputRoot);
        }
        
        
        ArrayList<JSNode> jsNodes = vh.getList();
        
        for(JSNode test : jsNodes) {
        	System.out.println("x: " + test.getPxX() + " y: " + test.getPxY() +  " width: " + test.getPxWidth() + " height: " + test.getPxHeight());
        }
        
        System.out.println("Process complete!");
        
        //Return the viewHierarchy for use in other applications
        return vh;
    }
    
    private boolean createSkeletonApp(String pathToSkeletonApp, String outputRoot){
    	
    	boolean compiled = false;
    	
    	String exportCommand = "cp -R " + pathToSkeletonApp + " " + outputRoot + File.separator;
    System.out.println(exportCommand);
    	System.out.println(TerminalHelper.executeCommand(exportCommand));	
    
    	String copyXML = "cp " + outputRoot + File.separator + "activity_main.xml" + " " + outputRoot + File.separator + "App-Skeleton" + File.separator + 
    			"app" + File.separator + "src" + File.separator + "main" + File.separator + "res" + File.separator + "layout";
    	System.out.println(copyXML);
    System.out.println(TerminalHelper.executeCommand(copyXML));
    
	String copyImages = "cp -R " + outputRoot + File.separator + "drawable" + " " + outputRoot + File.separator + "App-Skeleton" + File.separator + 
			"app" + File.separator + "src" + File.separator + "main" + File.separator + "res" + File.separator;
	System.out.println(copyImages);
	System.out.println(TerminalHelper.executeCommand(copyImages));
	
	String cleanApp = "cd " + outputRoot + "App-Skeleton; /usr/local/bin/gradle clean";
	System.out.println(cleanApp);
	System.out.println(TerminalHelper.executeCommand(cleanApp));
	
	
	String compileApp = "cd " + outputRoot + "App-Skeleton; /usr/local/bin/gradle assembleDebug";
	System.out.println(compileApp);
	String compileAppOutput = TerminalHelper.executeCommand(compileApp);
	System.out.println(compileAppOutput);
	
	if (compileAppOutput.contains("BUILD SUCCESSFUL")) {
		compiled = true;
	}
	
	return compiled;
	
    }
    
    private void captureSSandUIDump(String outputRoot){
    	
    	DeviceHelper deviceHelper = new DeviceHelper(TypeDeviceEnum.DEVICE, "/Applications/AndroidSDK/sdk", "09103097", "5037");
    	deviceHelper.unInstallAndInstallApp(outputRoot + "App-Skeleton/app/build/outputs/apk/app-debug.apk" , "edu.wm.semeru.remaui_app");
    	
    	deviceHelper.startAPK("edu.wm.semeru.remaui_app", "edu.wm.semeru.remaui_app.MainActivity");
    	
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	deviceHelper.getAndPullScreenshot(outputRoot, "screen.png");
    	deviceHelper.getAndPullUIDump(outputRoot, "ui-dump.xml");
    	
    }
    
    
    /**
     * Helper function to fully read an InputStream
     */
    private String readInputStream(InputStream in) {
    	InputStreamReader is = new InputStreamReader(in);
        StringBuilder b = new StringBuilder();
        char buf[] = new char[2048];
        try {
	        while (is.read(buf) > 0) {
	        	b.append(buf);
	        }
        } catch (Exception e) {
        	return null;
        }
        return b.toString();
    }
    
    /**
     * Construct a view hierarchy.
     *
     * @param cvc           The contours and contour hierarchy from OpenCV.
     * @param screenWidth   The width of the input image.
     * @param screenHeight  The height of the input image.
     * @return              A view hierarchy for the contours.
     */
    private ViewHierarchy createViewHierarchy(CVContours cvc, int screenWidth, int screenHeight) {
        // construction parameters
        ViewHierarchy vh = new ViewHierarchy(screenWidth, screenHeight);
        List<MatOfPoint> contours = cvc.getContours();
        Mat hierarchy = cvc.getHierarchy();

        // add the top-level nodes
        insertNode(vh, contours, hierarchy, 0);

        return vh;
    }

    /**
     * Insert a node into the view hierarchy.
     *
     * @param node      The parent of the node to add.
     * @param contours  The contours detected by OpenCV.
     * @param hierarchy The contour hierarchy computed by OpenCV.
     * @param index     The index of the current node in the contours list.
     */
    private void insertNode(ViewNode parent, List<MatOfPoint> contours, Mat hierarchy, int index) {
        // add a new node
        Rect bounds = Imgproc.boundingRect(contours.get(index));
        if (bounds.height * bounds.width < MIN_AREA) {
        	return;
        }
    	String data = "img_" + bounds.x + "_" + bounds.y + "_" + bounds.width + "_" + bounds.height;
    	parent.addChild(ViewType.IMAGE, bounds, data);
        ViewNode current = parent.childAtIndex(parent.getNumChildren()-1);

        int firstChildIndex = (int)hierarchy.get(0, index)[2];
        int nextSiblingIndex = (int)hierarchy.get(0, index)[0];

        // future siblings will be added in recursive calls
        if (nextSiblingIndex != -1) {
            insertNode(parent, contours, hierarchy, nextSiblingIndex);
        }
        // other children will be added in recursive calls
        if (firstChildIndex != -1) {
            insertNode(current, contours, hierarchy, firstChildIndex);
        }
    }


    /**
     * Process the image using OpenCV to obtain vision boxes.
     *
     * @param inputImagePath The app image for which to extract a view hierarchy.
     * @param outputRoot A directory in which to dump all output images.
     * @return The contours as detected by OpenCV and the hierarchy of those contours.
     */
    public CVContours cvProcess(String inputImagePath, String outputRoot) {
        double lowThreshold = 30.0;

        Mat input = Highgui.imread(inputImagePath);
        Mat temp1 = new Mat(input.rows(), input.cols(), input.type());
        Mat temp2 = new Mat(input.rows(), input.cols(), input.type());
        Size kernel = new Size(3, 3);
        List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
        Mat hierarchy = new Mat();

        // convert to grayscale
        //Imgproc.cvtColor(input, temp1, Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(input, temp1, Imgproc.COLOR_BGR2GRAY);

        Highgui.imwrite(outputRoot + File.separator + "grayscale.png", temp1);

        // blur to reduce noise
        Imgproc.blur(temp1, temp2, kernel);

        Imgproc.Canny(temp2, temp1, lowThreshold, lowThreshold * 3); // Canny's recommendation is to set high threshold 3 * low threshold
        Highgui.imwrite(outputRoot + File.separator + "canny.png", temp1);

        // dilate
        Imgproc.dilate(temp1, temp2, new Mat());
        Imgproc.dilate(temp2, temp1, new Mat());
        Imgproc.dilate(temp1, temp2, new Mat());
        Imgproc.dilate(temp2, temp1, new Mat());

        Highgui.imwrite(outputRoot + File.separator + "dilate.png", temp1);

        // get contours
        Imgproc.findContours(temp1, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

        //Mat contourOutput = Mat.zeros(input.rows(), input.cols(), temp2.type());
        Mat contourOutput = Mat.zeros(input.rows(), input.cols(), temp1.type());
        sumContours(contours, contourOutput);

        Highgui.imwrite(outputRoot + File.separator + "contours.png", contourOutput);

        List<Rect> contourBoundingBoxes = getContourBoundingBoxes(contours);

        drawBoundingBoxesToFile(contourBoundingBoxes, outputRoot + File.separator + "contourBoundingBoxes.png", input.width(), input.height(), true);
        return new CVContours(contours, hierarchy);
    }

    /**
     * Creates an output image showing the contours.
     *
     * @param contours The detected contours.
     * @param output The matrix describing the output image.
     */
    public void sumContours(List<MatOfPoint> contours, Mat output) {
        for (int x = 0; x < contours.size(); x++) {
            Scalar color = new Scalar(255, 255, 255);
            Imgproc.drawContours(output, contours, x, color);
        }
    }

    /**
     * Gets the bounding boxes for all detected contours.
     *
     * @param contours A list of the contours
     * @return A a list of the bounding boxes for the given contours.
     */
    public List<Rect> getContourBoundingBoxes(List<MatOfPoint> contours) {
        List<Rect> boundingBoxes = new ArrayList<>();

        for (MatOfPoint contour : contours) {
            boundingBoxes.add(Imgproc.boundingRect(contour));
        }

        return boundingBoxes;
    }

    /**
     * Save the image with the drawn bounding boxes to a file.
     *
     * @param boundingBoxes The bounding boxes to draw.
     * @param filePath The full path of the file to save to.
     * @param width The width of the image to draw.
     * @param height The height of the image to draw.
     * @param fill A boolean describing whether to fill the boxes or simply draw outlines.
     */
    public void drawBoundingBoxesToFile(List<Rect> boundingBoxes, String filePath, int width, int height, boolean fill) {
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = outputImage.createGraphics();

        g2.setPaint(Color.white);
        g2.fillRect(0, 0, width, height);

        for (Rect boundingBox : boundingBoxes) {
            g2.setPaint(new Color((float) Math.random() / 2.0f, (float) Math.random() / 2.0f, (float) Math.random() / 2.0f));
            if (fill) {
            	g2.fillRect(boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
            }
            else {
            	g2.drawRect(boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
            }
        }

        try {
            ImageIO.write(outputImage, "PNG", new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Eliminates false positive OCR results. See the first three items in Table I in
     * Nguyen and Csallner.
     *
     * @param word A word detected by the OCR engine.
     * @param width The width of the app image.
     * @param height The height of the app image.
     * @return
     */
    public boolean boundsCheck(OCRWord word, int width, int height) {
        Rect boundingBox = word.getBoundingBox();

        return boundingBox.x >= 0 && boundingBox.y >= 0
                && boundingBox.width > 0 && boundingBox.height > 0
                && boundingBox.x + boundingBox.width <= width && boundingBox.y + boundingBox.height <= height
                && (float) boundingBox.width / (float) boundingBox.height > 0.05f && (float) boundingBox.height / (float) boundingBox.width > 0.05f;
    }

    /**
     * Eliminates false positive oCR results.  See the last three items in Table I in
     * Nguyen and Csallner.
     *
     * @param word A word detected by the OCR engine.
     * @return
     */
    public boolean ocrCheck(OCRWord word) {
        // the bounds of the word as actually detected
        float w = word.getBoundingBox().width;
        float h = word.getBoundingBox().height;

        // the estimated bounds when the word is drawn using desktop Java
        Rect estimatedBounds = word.getDrawnTextBounds();
        float e_w = estimatedBounds.width;
        float e_h = estimatedBounds.height;

        // the confidence that the word was correctly detected
        float c = word.getConfidence();

        // the bounding box area as actually detected, and as estimated by drawing
        float a = w * h;
        float e = e_w * e_h;

        // the drawn text
        String text = word.getText();

        System.out.println(text + " conf: " + c);
        
        boolean checkFour = !(c <= 0.75f);
        boolean checkFive = !(c <= 0.7f && (Math.abs(e_h/e_w - h/w) / Math.max(e_h/e_w, h/w) > 0.5f || Math.abs(a-e) / Math.max(a, e) > 0.8f));
        boolean checkSix = !(text.matches("[\\p{Cntrl}\\s]*") || text.matches("[^\\x00-\\x7f]*"));

        // if any of the individual heuristics is false, the detection of this as a word was a false positive
        return checkFour && checkFive && checkSix;
    }

    public static void main(String[] args) {
        REMAUI reach = new REMAUI();
        Scanner scanner = new Scanner(System.in);
        
//        System.out.println("Enter the full path for the project root directory:");
//        String projectRootDirectory = scanner.next();
//
//        System.out.println("Enter the relative path for the input image:");
//        String inputImageRelativePath = scanner.next();
//        String inputImagePath = projectRootDirectory + File.separator + inputImageRelativePath;
//
//        System.out.println("Enter the relative path for the output images:");
//        String outputRootRelative = scanner.next();
//        String outputRoot = projectRootDirectory + File.separator + outputRootRelative;

        String projectRootDirectory = "/Users/KevinMoran/Dropbox/Documents/My_Graduate_School_Work/SEMERU/git_src_code/gitlab-code/REMAUI/";
        String inputImagePath = "/Users/KevinMoran/Desktop/NewScreens/";
//        String inputImagePath = "/Users/KevinMoran/Dropbox/Documents/My_Graduate_School_Work/SEMERU/git_src_code/gitlab-code/REMAUI/ReDraw-Study-Screens/com.yelp.android/yelp-1200x1920-cropped.png";
//        String outputRoot = "/Users/KevinMoran/Desktop/REMAUI-Output/Yelp/";
        String outputRoot = "/Users/KevinMoran/Desktop/REMAUI-Output/Empirical-Study-Apps/";
//        String projectRootDirectory = "/Users/Rich/Desktop/REMAUI";
//        String inputImageRoot = projectRootDirectory + File.separator + "ReDraw-Study-Screens";
//        String outputRoot = "/Users/Rich/Desktop/REMAUI-Output";

        String[] exts = {"png"}; //File Extensions to search for

		File inputDir = new File(inputImagePath);

		//Use Apache FileUtils to find all screenshots
		Collection<File> screenshotCollection = FileUtils.listFiles(inputDir, exts, true);
        
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String outputApp = null;
        
        for(File currFile : screenshotCollection) {
        
        	outputApp = outputRoot + currFile.getAbsolutePath().substring(currFile.getAbsolutePath().indexOf("NewScreens")+11, currFile.getAbsolutePath().length());
        	outputApp = outputApp.replaceAll(".png", "");
        	outputApp = outputApp + File.separator;
        	System.out.println(outputApp);
        	new File("outputApp").mkdirs();
//        	try {
//				ImagesHelper.cropImageAndSave(currFile.getAbsolutePath(), currFile.getAbsolutePath(), 0, 50, 1200, 1774);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
        	
        reach.run(projectRootDirectory, currFile.getAbsolutePath(), outputApp);
        
        }
        /*KMeansClassifier k = new KMeansClassifier("");
        k.train("REACH");
        k.testOnTrainingData(); */
//        File inputRoot = new File(inputImagePath);
//        for (File dir : inputRoot.listFiles()) {
//        	if (!dir.isDirectory()) {
//        		continue;
//        	}
//        	for (File img : dir.listFiles()) {
//	        	String outputPath = outputRoot + File.separator + dir.getName() + File.separator + img.getName();
//	        	reach.run(projectRootDirectory, img.getAbsolutePath(), outputPath);
//	        	reach.cvProcess(img.getAbsolutePath(), outputPath);
//        	}
//        }
    }
}

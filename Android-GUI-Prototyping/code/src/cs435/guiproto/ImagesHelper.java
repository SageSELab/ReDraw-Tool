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
package cs435.guiproto;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import boofcv.alg.color.ColorHsv;
import boofcv.alg.descriptor.DescriptorDistance;
import boofcv.alg.descriptor.UtilFeature;
import boofcv.alg.feature.color.GHistogramFeatureOps;
import boofcv.alg.feature.color.Histogram_F64;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.TupleDesc_F64;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.Planar;
import cs435.extra.KMeans;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import imagingbook.pub.color.quantize.ColorQuantizer;
import imagingbook.pub.color.quantize.MedianCutQuantizer;
import imagingbook.pub.color.quantize.MedianCutQuantizer.Parameters;
import imagingbook.pub.color.statistics.ColorHistogram;

/**
 * 
 * A series of utility functions for working with and analyzing images.
 *
 * @author Mario Linares, Carlos Bernal, & Kevin Moran
 * @since Sep 22, 2015
 */
public class ImagesHelper {

	private static final int[] RGB_MASKS = {0xFF0000, 0xFF00, 0xFF};
	private static final ColorModel RGB_OPAQUE =
			new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);
	
	/**
	 * Name: cropImageAndSave
	 * 
	 * Description: This method crops an input image and saves the output image.
	 * This is good for taking individual screenshots of GUI-components.
	 * 
	 * @author Kevin Moran
	 * @param sourceImagePath:
	 *            Path to the inital image to be cropped.
	 * @param croppedImagePath:
	 *            Path of the output image.
	 * @param x:
	 *            x coordinate of the cropping rectangle.
	 * @param y:
	 *            y coordinate of the cropping rectangle.
	 * @param width:
	 *            width of the cropping rectangle.
	 * @param height:
	 *            height of the cropping rectangle.
	 * @throws IOException
	 */
	public static void cropImageAndSave(String sourceImagePath, String croppedImagePath, int x, int y, int width,
			int height) throws IOException {
		cropImageAndSave(sourceImagePath, croppedImagePath, x, y, width, height, "png");
	}

	/**
	 * Name: cropImageAndSave
	 * 
	 * Description: This method crops an input image and saves the output image.
	 * This is good for taking individual screenshots of GUI-components.
	 * 
	 * @author Kevin Moran
	 * @param sourceImagePath:
	 *            Path to the inital image to be cropped.
	 * @param croppedImagePath:
	 *            Path of the output image.
	 * @param x:
	 *            x coordinate of the cropping rectangle.
	 * @param y:
	 *            y coordinate of the cropping rectangle.
	 * @param width:
	 *            width of the cropping rectangle.
	 * @param height:
	 *            height of the cropping rectangle.
	 * @throws IOException
	 */
	public static void cropImageAndSave(String sourceImagePath, String croppedImagePath, int x, int y, int width,
			int height, String type) throws IOException {
		if (width != 0 && height != 0) {
			BufferedImage source = ImageIO.read(new File(sourceImagePath));
			
			int rX = width < 0 ? x + width : x;
			int rY = height < 0 ? x + height : y;
			int rWidth = rX + Math.abs(width) >= source.getWidth()
					? source.getWidth() - Math.max(x, 0)
					: Math.abs(width);
			int rHeight = rY + Math.abs(height) >= source.getHeight()
					? source.getHeight() - Math.max(y, 0)
					: Math.abs(height);
			//try/catch to debug
			try{
				BufferedImage cropped = source.getSubimage(x, y, rWidth, rHeight);
				ImageIO.write(cropped, type, new File(croppedImagePath));
			}catch(RasterFormatException e){
				System.out.println("x: " + Integer.toString(x) + " width: " + Integer.toString(rWidth));
			}
			
		}
	}
	
	public static Color getPrimaryColor(String image) throws IOException {
		return getPrimaryColor(image, new ArrayList<>());
	}
	
	public static Color getPrimaryColorCropped(String image, int x, int y, int w, int h) throws IOException {
		return getTwoColorsCropped(image, x, y, w, h)[0];
	}
	
	public static Color getPrimaryColor(String image, List<Rectangle> masks) throws IOException {
		return getTwoColors(image, masks)[0];
	}
	
	public static Color[] getTwoColors(String image) throws IOException {
		return getTwoColors(image, new ArrayList<>());
	}
	
	public static Color[] getTwoColorsCropped(String image, int x, int y, int w, int h) throws IOException {
		Path tempFile = Files.createTempFile("cropped", ".png");
		cropImageAndSave(image, tempFile.toString(), x, y, w, h);
		
		return getTwoColors(tempFile.toString(), new ArrayList<>());
	}
	
	/**
	 * Get both the primary color (in index 0) and the color farthest from it (in index 1).
	 * @param image
	 * @param masks
	 * @return
	 * @throws IOException
	 */
	public static Color[] getTwoColors(String image, List<Rectangle> masks) throws IOException {
		// 6 clusters is a bit large, but usually accurate
		List<Color> colors = getMajorColors(image, 6, masks);
		Color[] out = new Color[2];
		out[0] = colors.get(0);
		
		// Find color farthest from the primary
		double maxDist = Double.NEGATIVE_INFINITY;
		int maxInd = -1;
		for (int i=1; i<colors.size(); i++) {
			double dist = colorDistance(colors.get(i), out[0]);
			if (dist > maxDist) {
				maxDist = dist;
				maxInd = i;
			}
		}
		out[1] = colors.get(maxInd);
		
		return out;
	}
	
	/**
	 * Get a representative sample of the image's colors.
	 * This version lets you specify multiple "masks"; pixels inside a mask
	 * will not be factored into color calculation.
	 * 
	 * @param image
	 * @param clusters
	 * @param masks
	 * @return
	 * @throws IOException 
	 */
	private static List<Color> getMajorColors(String image, int clusters, List<Rectangle> masks) throws IOException {
	if (clusters <= 0) {
		throw new IllegalArgumentException("Number of colors cannot be negative");
	}
    	// Load an image
    	ImagePlus imagePlus = IJ.openImage(image);
    	if (imagePlus == null) {
    		throw new IOException("Couldn't find image" + image);
    	}
    	
	ImageProcessor ip = imagePlus.getChannelProcessor();
	ColorProcessor cp = (ColorProcessor) ip.convertToRGB();
    	int[] raw_pixels = (int[]) cp.getPixels();
    	
    	// Mask out the unused pixels
    	int pixels[];
    	if (masks.size() == 0) {
    		pixels = raw_pixels;
    	} else {
    		// Most obvious and slow implementation.
    		// It doesn't seem to take that long regardless, at least on my computer...
    		List<Integer> pixels_ls = new ArrayList<>();
    		int width = imagePlus.getWidth();
    		int height = imagePlus.getHeight();
    		for (int y=0; y<height; y++) {
    			for (int x=0; x<width; x++) {
    				boolean isMasked = false;
    				for (Rectangle mask : masks) {
    					if (mask.contains(x, y)) {
    						isMasked = true;
    						break;
    					}
    				}
    				if (!isMasked)
    					pixels_ls.add(raw_pixels[y*width + x]);
    			}
    		}
    		
    		// If we somehow masked out every pixel, just
        	// use the whole image and hope for the best
        	if (pixels_ls.size() == 0) {
        		pixels = raw_pixels;
        	} else {
        		// Can't convert a List<Integer> to int[] easily? Weird.
        		pixels = new int[pixels_ls.size()];
        		for (int i=0; i<pixels_ls.size(); i++) {
        			pixels[i] = pixels_ls.get(i);
        		}
        		pixels_ls.clear();
        	}
    	}
    	
		return KMeans.getColorsThroughKMeans(pixels, clusters);
	}
	
	/**
	 * Get the N most common colors in an image.
	 * @param image
	 * @param colors
	 * @return
	 * @throws IOException
	 */
	public static Color[] quantizeImageAndGetColors(String image, int colors) throws IOException {
		Color[] colorsArray = new Color[colors];
		boolean sortByFrequency = true;

		// Get colors
		ImagePlus imagePlus = IJ.openImage(image);
		ImageProcessor ip = imagePlus.getChannelProcessor();
		ColorProcessor cp = (ColorProcessor) ip.convertToRGB();
		
		Parameters parameters = new Parameters();
		parameters.maxColors = colors;
		ColorQuantizer quantizer = new MedianCutQuantizer((int[]) cp.getPixels(), parameters);
		int[] rgbPixels = quantizer.quantize((int[]) cp.getPixels());
		
		// Get histogram sorted by the frequency
		ColorHistogram histogram = new ColorHistogram(rgbPixels, sortByFrequency);
		for (int i = 0; i < colors; i++) {
			int rgb = histogram.getColor(i);
			// int cnt = histogram.getCount(i);
			colorsArray[i] = intToRgb(rgb);
		}

		return colorsArray;
	}

	
	/**
	 * Name: augmentScreenShot
	 * 
	 * Description: This method augments an existing screenshot and draws a
	 * rectangle around a specific component on a screen. This can be used to
	 * highlight GUI-components on an Android screen.
	 * 
	 * @author Kevin Moran
	 * @param imagePath:
	 *            Path to the image to be modified.
	 * @param outputPath:
	 *            Path where the augmented image will be saved.
	 * @param x:
	 *            x-coordinate of the rectangle.
	 * @param y:
	 *            y-coordinate of the rectangle.
	 * @param width:
	 *            width of the rectangle.
	 * @param height:
	 *            height of the rectangle.
	 * @throws IOException
	 */
	public static void augmentScreenShot(String imagePath, String outputPath, int x, int y, int width, int height)
			throws IOException {
		augmentScreenShot( imagePath,  outputPath,  x,  y,  width,  height, "png");
	}

	/**
	 * Name: augmentScreenShot
	 * 
	 * Description: This method augments an existing screenshot and draws a
	 * rectangle around a specific component on a screen. This can be used to
	 * highlight GUI-components on an Android screen.
	 * 
	 * @author Kevin Moran
	 */
	public static void augmentScreenShot(String imagePath, String outputPath, int x, int y, int width, int height, String type)
			throws IOException {
		BufferedImage img = ImageIO.read(new File(imagePath));

		Graphics2D g = img.createGraphics();

		float dash1[] = { 5.0f };
		BasicStroke dashed = new BasicStroke(10.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);

		g.setStroke(dashed);
		g.setColor(Color.RED);
		g.drawRoundRect(x, y, width, height, 1, 1);
		g.dispose();

		ImageIO.write(img, type, new File(outputPath));
	}

	public static void augmentScreenShotMult(String imagePath, String outputPath, int x1, int y1, int width1,
			int height1, int x2, int y2, int width2, int height2) throws IOException {
		BufferedImage img = ImageIO.read(new File(imagePath));

		Graphics2D g = img.createGraphics();

		float dash1[] = { 5.0f };
		BasicStroke dashed = new BasicStroke(10.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);

		g.setStroke(dashed);
		g.setColor(Color.RED);
		g.drawRoundRect(x1, y1, width1, height1, 1, 1);
		g.dispose();

		Graphics2D gr = img.createGraphics();

		BasicStroke dashed2 = new BasicStroke(10.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);

		gr.setStroke(dashed2);
		gr.setColor(Color.RED);
		gr.drawRoundRect(x2, y2, width2, height2, 1, 1);
		gr.dispose();

		ImageIO.write(img, "png", new File(outputPath));

	}
	
	/**
	 * This method will compute the similarity between 2 images based on the
	 * color histogram with a default threshold of 10%
	 * 
	 * @param design
	 *            path to the design image
	 * @param implem
	 *            path to the implementation image
	 * @return true if all the colors are close enough
	 */
	public static boolean areHistogramsClose(String design, String implem) {
		return areHistogramsClose(design, implem, 0.10);
	}

	/**
	 * This method will compute the similarity between 2 images based on the
	 * color histogram with a given threshold
	 * 
	 * @param design
	 *            path to the design image
	 * @param implem
	 *            path to the implementation image
	 * @param threshold
	 *            the smaller the value the more accurate it will be, for
	 *            example 10% -> 0.10d
	 * @return true if all the colors are close enough
	 */
	public static boolean areHistogramsClose(String design, String implem, double threshold) {
		boolean result = true;
		int colors = 3;
		try {
			Color[] designHistogram = quantizeImageAndGetColors(design, colors);
			Color[] implemHistogram = quantizeImageAndGetColors(implem, colors);
			for (int i = 0; i < colors; i++) {
				result &= isRgbClose(designHistogram[i], implemHistogram[i], threshold);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 
	 * @param rgbDesign
	 * @param rgbImplem
	 * @param threshold
	 * @return
	 */
	public static boolean isRgbClose(Color rgbDesign, Color rgbImplem, double threshold) {
		// This is a constant calculated manually
		double maxDifference = 764.8339663572415;
		// TODO: test other distances
		double colorDistance = colorDistance(rgbDesign, rgbImplem);
		//System.out.println(colorDistance);
		if (colorDistance / maxDifference <= threshold) {
			return true;
		}
		return false;
	}

	/**
	 * Euclidean distance "improved" between 2 colors based on Wikipedia
	 * https://en.wikipedia.org/wiki/Color_difference
	 * 
	 * @param c1
	 * @param c2
	 * @return
	 */
	private static double colorDistance(Color c1, Color c2) {
		double rmean = (c1.getRed() + c2.getRed()) / 2;
		int deltaR = c1.getRed() - c2.getRed();
		int deltaG = c1.getGreen() - c2.getGreen();
		int deltaB = c1.getBlue() - c2.getBlue();
		double weightR = 2 + rmean / 256;
		double weightG = 4.0;
		double weightB = 2 + (255 - rmean) / 256;
		return Math.sqrt(weightR * deltaR * deltaR + weightG * deltaG * deltaG + weightB * deltaB * deltaB);
	}

	public static Color intToRgb(int rgb) {
		int red = ((rgb >> 16) & 0xFF);
		int grn = ((rgb >> 8) & 0xFF);
		int blu = (rgb & 0xFF);
		return new Color(red, grn, blu);
	}

	public static int rgbToInt(int red, int grn, int blu) {
		return ((red & 0xff) << 16) | ((grn & 0xff) << 8) | blu & 0xff;
	}

	/**
	 * Transfer from PNG to JPG file
	 * @author USBOLI
	 * @param pngFile
	 * @param jpgFile
	 */
	public static void TransferPNGToJPG(String pngFile, String jpgFile){
		try {
			Image img = Toolkit.getDefaultToolkit().createImage(pngFile);

			PixelGrabber pg = new PixelGrabber(img, 0, 0, -1, -1, true);
			try {
				pg.grabPixels();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int width = pg.getWidth(), height = pg.getHeight();

			DataBuffer buffer = new DataBufferInt((int[]) pg.getPixels(), pg.getWidth() * pg.getHeight());
			WritableRaster raster = Raster.createPackedRaster(buffer, width, height, width, RGB_MASKS, null);
			BufferedImage bi = new BufferedImage(RGB_OPAQUE, raster, false, null);

			String to = jpgFile;
			ImageIO.write(bi, "jpg", new File(to));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * @author Kevin Moran
	 * 
	 * Description: This method takes as input two images and outputs the euclidean distance
	 * between their color histograms as a measure of image similarity. In this case a value 
	 * of 0 indicates near perfect similarity between the images and a value of 1 represents
	 * very different images.  This is should basically be used as a check against false positives 
	 * output from the PID analysis.  See TestImageComparison for example output values.
	 * 
	 * @param pathToMockUpImage
	 * @param pathToImplementationImage
	 * @return
	 */
	public static double calculateImageDistance(String pathToMockUpImage, String pathToImplementationImage){
		double imageCompError = 0;
		File mockup = new File(pathToMockUpImage);
		File implementation = new File(pathToImplementationImage);

		//Create a list of images to process.
		List<File> images = new ArrayList<File>();
		images.add(mockup);
		images.add(implementation);

		//Compute the Color Histograms
		List<double[]> points = coupledHueSat(images);

		TupleDesc_F64 image1 = new TupleDesc_F64(30);
		TupleDesc_F64 image2 = new TupleDesc_F64(30);

		image1.set(points.get(0));
		image2.set(points.get(1));

		imageCompError = DescriptorDistance.euclidean(image1, image2);

		return imageCompError;

	}

	/**
	 * @author Peter Abeles & Kevin Moran
	 * 
	 * Description: This method takes as input a list of images and computes the color histogram
	 * of each image using the RGB color space.  This are output as an array of double-based histograms.
	 * 
	 * @param images: A list of Image files
	 * @return A list of color histograms represented as an array of double values
	 */
	public static List<double[]> coupledRGB( List<File> images ) {
		List<double[]> points = new ArrayList<>();

		Planar<GrayF32> rgb = new Planar<>(GrayF32.class,1,1,3);

		for( File f : images ) {
			BufferedImage buffered = UtilImageIO.loadImage(f.getPath());
			if( buffered == null ) throw new RuntimeException("Can't load image!");

			rgb.reshape(buffered.getWidth(), buffered.getHeight());
			buffered = ConvertBufferedImage.stripAlphaChannel(buffered);
			ConvertBufferedImage.convertFrom(buffered, rgb, true);

			// The number of bins is an important parameter.  Try adjusting it
			Histogram_F64 histogram = new Histogram_F64(10,10,10);
			histogram.setRange(0, 0, 255);
			histogram.setRange(1, 0, 255);
			histogram.setRange(2, 0, 255);

			GHistogramFeatureOps.histogram(rgb,histogram);

			UtilFeature.normalizeL2(histogram); // normalize so that image size doesn't matter

			points.add(histogram.value);
		}

		return points;
	}

	/**
	 * @author Peter Abeles & Kevin Moran
	 * 
	 * Description: This method takes as input a list of images and computes the color histogram
	 * of each image using the coupled Hue and saturation of the images.  
	 * This are output as an array of double-based histograms.
	 * 
	 * @param images: A list of Image files
	 * @return A list of color histograms represented as an array of double values
	 */
	public static List<double[]> coupledHueSat( List<File> images  ) {
		List<double[]> points = new ArrayList<>();

		Planar<GrayF32> rgb = new Planar<>(GrayF32.class,1,1,3);
		Planar<GrayF32> hsv = new Planar<>(GrayF32.class,1,1,3);

		for( File f : images ) {
			BufferedImage buffered = UtilImageIO.loadImage(f.getPath());
			if( buffered == null ) throw new RuntimeException("Can't load image!");

			rgb.reshape(buffered.getWidth(), buffered.getHeight());
			hsv.reshape(buffered.getWidth(), buffered.getHeight());

			buffered = ConvertBufferedImage.stripAlphaChannel(buffered);
			ConvertBufferedImage.convertFrom(buffered, rgb, true);
			ColorHsv.rgbToHsv_F32(rgb, hsv);

			Planar<GrayF32> hs = hsv.partialSpectrum(0,1);

			// The number of bins is an important parameter.  Try adjusting it
			Histogram_F64 histogram = new Histogram_F64(12,12);
			histogram.setRange(0, 0, 2.0*Math.PI); // range of hue is from 0 to 2PI
			histogram.setRange(1, 0, 1.0);         // range of saturation is from 0 to 1

			// Compute the histogram
			GHistogramFeatureOps.histogram(hs,histogram);

			UtilFeature.normalizeL2(histogram); // normalize so that image size doesn't matter

			points.add(histogram.value);
		}

		return points;
	}

	static String c2hex(Color c) {
		return String.format("#%02X%02X%02X",
				c.getRed(),
				c.getGreen(), 
				c.getBlue()
		);
	}


}

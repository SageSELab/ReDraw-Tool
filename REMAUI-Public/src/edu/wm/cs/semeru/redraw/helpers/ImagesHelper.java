package edu.wm.cs.semeru.redraw.helpers;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.alg.color.ColorHsv;
import boofcv.alg.descriptor.DescriptorDistance;
import boofcv.alg.descriptor.UtilFeature;
import boofcv.alg.feature.color.GHistogramFeatureOps;
import boofcv.alg.feature.color.Histogram_F64;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.feature.TupleDesc_F64;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import imagingbook.pub.color.quantize.ColorQuantizer;
import imagingbook.pub.color.quantize.MedianCutQuantizer;
import imagingbook.pub.color.quantize.MedianCutQuantizer.Parameters;
import imagingbook.pub.color.statistics.ColorHistogram;
import georegression.struct.point.Point2D_I16;

/**
 * 
 * {Insert class description here}
 *
 * @author Mario Linares, Carlos Bernal, & Kevin Moran
 * @since Sep 22, 2015
 */
public class ImagesHelper {

	private static final int[] RGB_MASKS = { 0xFF0000, 0xFF00, 0xFF };
	private static final ColorModel RGB_OPAQUE = new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);

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

	public static void cropImageAndSave(String sourceImagePath, String croppedImagePath, int x, int y, int width,
			int height, String type) throws IOException {
		if (width != 0 && height != 0) {
			BufferedImage cropped = cropImage(sourceImagePath, x, y, width, height);
			ImageIO.write(cropped, type, new File(croppedImagePath));
		}
	}

	/**
	 * @param sourceImagePath
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage cropImage(String sourceImagePath, int x, int y, int width, int height)
			throws IOException {
		BufferedImage source = ImageIO.read(new File(sourceImagePath));
		int rX = width < 0 ? x + width : x;
		int rY = height < 0 ? x + height : y;
		int rWidth = rX + Math.abs(width) >= source.getWidth() ? source.getWidth() - Math.max(x, 0) : Math.abs(width);
		int rHeight = rY + Math.abs(height) >= source.getHeight() ? source.getHeight() - Math.max(y, 0)
				: Math.abs(height);

		BufferedImage cropped = source.getSubimage(x, y, rWidth, rHeight);
		return cropped;
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
		augmentScreenShot(imagePath, outputPath, x, y, width, height, "png");

	}

	public static void augmentScreenShot(String imagePath, String outputPath, int x, int y, int width, int height,
			String type) throws IOException {
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
	 * This method reduces the number of colors of an image and returns the
	 * histogram
	 * 
	 * @param image
	 * @param colors
	 * @return
	 * @throws IOException
	 */
	public static Color[] quantizeImageAndGetColors(String image, int colors) throws IOException {
		// Load image from path
		ImagePlus imagePlus = IJ.openImage(image);
		return quantizeImageAndGetColors(colors, imagePlus);
	}

	/**
	 * This method reduces the number of colors of an image and returns the
	 * histogram
	 * 
	 * @param image
	 * @param colors
	 * @return
	 * @throws IOException
	 */
	public static Color[] quantizeImageAndGetColors(BufferedImage image, int colors) throws IOException {
		// Load image from buffered image
		ImagePlus imagePlus = new ImagePlus("", image);
		return quantizeImageAndGetColors(colors, imagePlus);
	}

	/**
	 * @param colors
	 * @param colorsArray
	 * @param sortByFrequency
	 * @param imagePlus
	 * @return
	 */
	private static Color[] quantizeImageAndGetColors(int colors, ImagePlus imagePlus) {

		Color[] colorsArray = new Color[colors];
		boolean sortByFrequency = true;
		// Perform image quantization
		ImageProcessor ip = imagePlus.getChannelProcessor();
		ColorProcessor cp = (ColorProcessor) ip.convertToRGB();
		Parameters parameters = new Parameters();
		parameters.maxColors = colors;
		ColorQuantizer quantizer = new MedianCutQuantizer((int[]) cp.getPixels(), parameters);
		int[] rgbPixels = quantizer.quantize((int[]) cp.getPixels());

		// Get histogram sorted by the frequency
		ColorHistogram histogram = new ColorHistogram(rgbPixels, sortByFrequency);
		int cnt = histogram.getNumberOfColors();
		for (int i = 0; i < cnt; i++) {
			int rgb = histogram.getColor(i);
			// int cnt = histogram.getCount(i);
			colorsArray[i] = intToRgb(rgb);
		}

		return colorsArray;
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
		return areHistogramsClose(design, implem, 0.05);
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
	public static boolean areHistogramsCloseBW(String design, String implem, String outputFolder) {
		BufferedImage designBI = null;
		BufferedImage implemBI = null;
		try {
			designBI = ImageIO.read(new File(design));
			implemBI = ImageIO.read(new File(implem));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		  BufferedImage blackAndWhiteImg1 = new BufferedImage(
			        designBI.getWidth(), designBI.getHeight(),
			        BufferedImage.TYPE_BYTE_BINARY);
		  
		  BufferedImage blackAndWhiteImg2 = new BufferedImage(
			        implemBI.getWidth(), implemBI.getHeight(),
			        BufferedImage.TYPE_BYTE_BINARY);
		
		  Graphics2D g2d = blackAndWhiteImg1.createGraphics();
		  g2d.drawImage(designBI, 0, 0, null);
		  
		  Graphics2D g2d2 = blackAndWhiteImg2.createGraphics();
		  g2d2.drawImage(implemBI, 0, 0, null);
		  
		  String designName = design.substring(design.lastIndexOf("/")+1, design.lastIndexOf(".")-1);
		  String implemName = implem.substring(implem.lastIndexOf("/")+1, implem.lastIndexOf(".")-1);
		  
		  try {
			ImageIO.write(blackAndWhiteImg1, "png", new File(outputFolder + File.separator + "implement" + File.separator + designName + "-bw.png"));
			  ImageIO.write(blackAndWhiteImg2, "png", new File(outputFolder + File.separator + "design" + File.separator + implemName + "-bw.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return areHistogramsClose(outputFolder + File.separator + "implement" + File.separator + designName + "-bw.png", outputFolder + File.separator + "design" + File.separator + implemName + "-bw.png", 0.00);
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

			// System.out.println("Length:" + implemHistogram.length);
			//
			// for (int k = 0; k <colors; k++) {
			// System.out.println("impl Hist:" + implemHistogram[k].getRGB());
			// System.out.println("design Hist:" + designHistogram[k].getRGB());
			// }

			System.out.println(colors);
			for (int i = 0; i < colors; i++) {
				if (designHistogram[i] != null && implemHistogram[i] != null) {
					result &= isRgbClose(designHistogram[i], implemHistogram[i], threshold);
				}
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
	private static boolean isRgbClose(Color rgbDesign, Color rgbImplem, double threshold) {
		// This is a constant calculated manually
		double maxDifference = 764.8339663572415;
		// TODO: test other distances
		double colorDistance = colorDistance(rgbDesign, rgbImplem);
		 System.out.println("Histogram Difference: " + (colorDistance / maxDifference) + " Threshold " + threshold);
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
	public static double colorDistance(Color c1, Color c2) {
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

	/**
	 * 
	 * @param rgb
	 * @param alpha
	 *            [0-255]
	 * @return
	 */
	public static Color intToArgb(int rgb, int alpha) {
		int red = ((rgb >> 16) & 0xFF);
		int grn = ((rgb >> 8) & 0xFF);
		int blu = (rgb & 0xFF);
		return new Color(red, grn, blu, alpha);
	}

	public static Color intToArgb(int argb) {
		int a = (argb >> 24) & 0xFF;
		int r = (argb >> 16) & 0xFF;
		int g = (argb >> 8) & 0xFF;
		int b = (argb >> 0) & 0xFF;
		return new Color(r, g, b, a);
	}

	public static int rgbToInt(int red, int grn, int blu, int alpha) {
		return ((alpha & 0xff) << 24) | ((red & 0xff) << 16) | ((grn & 0xff) << 8) | blu & 0xff;
	}

	public static int argbToInt(Color color) {
		int red = color.getRed();
		int grn = color.getGreen();
		int blu = color.getBlue();
		return rgbToInt(red, grn, blu, color.getAlpha());
	}

	public static int rgbToInt(Color color) {
		int red = color.getRed();
		int grn = color.getGreen();
		int blu = color.getBlue();
		return rgbToInt(red, grn, blu, 255);
	}

	/**
	 * Blend two colors with taking into account a ratio.
	 * 
	 * @param c1
	 *            First color to blend.
	 * @param c2
	 *            Second color to blend.
	 * @param ratio
	 *            Blend ratio. 0.5 will give even blend, 1.0 will return color1,
	 *            0.0 will return color2 and so on.
	 * @return Blended color.
	 */
	public static Color blend(Color c1, Color c2, double ratio) {
		float r = (float) ratio;
		float ir = (float) 1.0 - r;

		if (c1 == null) {
			return c2;
		} else if (c2 == null) {
			return c1;
		}

		Color color = new Color((int) (c1.getRed() * r + c2.getRed() * ir),
				(int) (c1.getGreen() * r + c2.getGreen() * ir), (int) (c1.getBlue() * r + c2.getBlue() * ir),
				(int) (c1.getAlpha() * r + c2.getAlpha() * ir));

		return color;
	}

	/**
	 * Blend two colors with a default contribution of 50% for each color.
	 * 
	 * @param c1
	 *            First color to blend.
	 * @param c2
	 *            Second color to blend.
	 * @return Blended color.
	 */
	public static Color blend(Color color1, Color color2) {
		return blend(color1, color2, 0.5);
	}

	/**
	 * Transfer from PNG to JPG file
	 * 
	 * @author USBOLI
	 * @param pngFile
	 * @param jpgFile
	 */
	public static void TransferPNGToJPG(String pngFile, String jpgFile) {
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
	 *         Description: This method takes as input two images and outputs
	 *         the euclidean distance between their color histograms as a
	 *         measure of image similarity. In this case a value of 0 indicates
	 *         near perfect similarity between the images and a value of 1
	 *         represents very different images. This is should basically be
	 *         used as a check against false positives output from the PID
	 *         analysis. See TestImageComparison for example output values.
	 * 
	 * @param pathToMockUpImage
	 * @param pathToImplementationImage
	 * @return
	 */
	public static double calculateImageDistance(String pathToMockUpImage, String pathToImplementationImage) {
		double imageCompError = 0;
		File mockup = new File(pathToMockUpImage);
		File implementation = new File(pathToImplementationImage);

		// Create a list of images to process.
		List<File> images = new ArrayList<File>();
		images.add(mockup);
		images.add(implementation);

		// Compute the Color Histograms
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
	 *         Description: This method takes as input a list of images and
	 *         computes the color histogram of each image using the RGB color
	 *         space. This are output as an array of double-based histograms.
	 * 
	 * @param images:
	 *            A list of Image files
	 * @return A list of color histograms represented as an array of double
	 *         values
	 */
	public static List<double[]> coupledRGB(List<File> images) {
		List<double[]> points = new ArrayList<>();

		Planar<GrayF32> rgb = new Planar<>(GrayF32.class, 1, 1, 3);

		for (File f : images) {
			BufferedImage buffered = UtilImageIO.loadImage(f.getPath());
			if (buffered == null)
				throw new RuntimeException("Can't load image!");

			rgb.reshape(buffered.getWidth(), buffered.getHeight());
			buffered = ConvertBufferedImage.stripAlphaChannel(buffered);
			ConvertBufferedImage.convertFrom(buffered, rgb, true);

			// The number of bins is an important parameter. Try adjusting it
			Histogram_F64 histogram = new Histogram_F64(10, 10, 10);
			histogram.setRange(0, 0, 255);
			histogram.setRange(1, 0, 255);
			histogram.setRange(2, 0, 255);

			GHistogramFeatureOps.histogram(rgb, histogram);

			UtilFeature.normalizeL2(histogram); // normalize so that image size
			// doesn't matter

			points.add(histogram.value);
		}

		return points;
	}

	/**
	 * @author Peter Abeles & Kevin Moran
	 * 
	 *         Description: This method takes as input a list of images and
	 *         computes the color histogram of each image using the coupled Hue
	 *         and saturation of the images. This are output as an array of
	 *         double-based histograms.
	 * 
	 * @param images:
	 *            A list of Image files
	 * @return A list of color histograms represented as an array of double
	 *         values
	 */
	public static List<double[]> coupledHueSat(List<File> images) {
		List<double[]> points = new ArrayList<>();

		Planar<GrayF32> rgb = new Planar<>(GrayF32.class, 1, 1, 3);
		Planar<GrayF32> hsv = new Planar<>(GrayF32.class, 1, 1, 3);

		for (File f : images) {
			BufferedImage buffered = UtilImageIO.loadImage(f.getPath());
			if (buffered == null)
				throw new RuntimeException("Can't load image!");

			rgb.reshape(buffered.getWidth(), buffered.getHeight());
			hsv.reshape(buffered.getWidth(), buffered.getHeight());

			buffered = ConvertBufferedImage.stripAlphaChannel(buffered);
			ConvertBufferedImage.convertFrom(buffered, rgb, true);
			ColorHsv.rgbToHsv_F32(rgb, hsv);

			Planar<GrayF32> hs = hsv.partialSpectrum(0, 1);

			// The number of bins is an important parameter. Try adjusting it
			Histogram_F64 histogram = new Histogram_F64(12, 12);
			histogram.setRange(0, 0, 2.0 * Math.PI); // range of hue is from 0
			// to 2PI
			histogram.setRange(1, 0, 1.0); // range of saturation is from 0 to 1

			// Compute the histogram
			GHistogramFeatureOps.histogram(hs, histogram);

			UtilFeature.normalizeL2(histogram); // normalize so that image size
			// doesn't matter

			points.add(histogram.value);
		}

		return points;
	}

	/**
	 * Returns histogram of an image removing a delimited area
	 * 
	 * @param original
	 *            the entire image
	 * @param minX
	 *            minimum x value to start the extraction
	 * @param minY
	 *            minimum y value to start the extraction
	 * @param maxX
	 *            maximum x value to stop the extraction
	 * @param maxY
	 *            maximum y value to stop the extraction
	 * @param removedArea
	 *            area to remove inside the boundaries of the minimum and max
	 *            values, use null if no area is removed
	 * @return
	 */
	public static ColorHistogram getOutsideHistrogram(BufferedImage original, int minX, int minY, int maxX, int maxY,
			Rectangle removedArea) {
		// Colors that are outside the component
		List<Integer> validColors = new ArrayList<Integer>();

		// Filter colors that belong to the component
		for (int j = minX; j < maxX; j++) {
			for (int k = minY; k < maxY; k++) {
				if (removedArea == null || !(removedArea.contains(j, k))) {
					//					if (original.getRGB(j, k) == -1) {
					// Special case
					//						validColors.add(ImagesHelper.argbToInt(new Color(255, 255, 255, 254)));
					//					} else {
					validColors.add(original.getRGB(j, k));
					//					}
				}
			}
		}

		// Compute histogram
		int[] pixelsOrig = validColors.stream().mapToInt(i -> i).toArray();
		return new ColorHistogram(pixelsOrig, true);
	}

	/**
	 * This method shifts the hue of an image using HSB color representation
	 * 
	 * @param original
	 * @param area
	 * @param degrees2Shift
	 */
	public static void changeHue(BufferedImage original, Rectangle area, float degrees2Shift) {
		
		for (int i = area.x; i < area.x + area.width; i++) {
			for (int j = area.y; j < area.y + area.height; j++) {
				int argb = original.getRGB(i, j);
				Color color = ImagesHelper.intToArgb(argb);
				Color tempColor = shiftHue(color, degrees2Shift);
				// Transforming the color into the integer representation
				original.setRGB(i, j, ImagesHelper.argbToInt(tempColor));
			}
		}
	}

	/**
	 * This method shifts the hue of a color using HSB color representation and
	 * it will consider the alpha channel of the original color
	 * 
	 * @param original
	 * @param degrees2Shift
	 *            e.g. Math.PI
	 * @return
	 */
	public static Color shiftHue(Color original, float degrees2Shift) {
		float[] hsb = new float[3];
		hsb[0] = 0;
		hsb[1] = 0;
		hsb[2] = 0;

		Color.RGBtoHSB(original.getRed(), original.getGreen(), original.getBlue(), hsb); 
		
		if(hsb[2]== 1.0) {
			hsb[2] = (float) (hsb[2] - 0.6);
		}
		
		hsb[0] = hsb[0] + degrees2Shift;
		int hsBtoRGB = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
		// This doesn't have the alpha channel
		Color tempColor = new Color(hsBtoRGB);
		// Using original alpha channel from the color
		return new Color(tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), original.getAlpha());
		
		/*only change gray*/
		/*
		if(hsb[1]!= 0.0) {
			hsb[0] = hsb[0] + degrees2Shift;
			int hsBtoRGB = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
			// This doesn't have the alpha channel
			Color tempColor = new Color(hsBtoRGB);
			// Using original alpha channel from the color
			return new Color(tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), original.getAlpha());
		}
		else {
			if(hsb[2]>0.05 && hsb[2]<0.95) {
				
				//System.out.println("l in hsl"+hsb[2]);
				//hsb[2] = (float) (hsb[2] - 0.02);
				hsb[1] = (float) (hsb[1] + 0.5);
				hsb[0] = hsb[0] + 4*degrees2Shift;
				int hsBtoRGB = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
				// This doesn't have the alpha channel
				Color tempColor = new Color(hsBtoRGB);
				// Using original alpha channel from the color
				return new Color(tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), original.getAlpha());
			}
			else {
				int hsBtoRGB = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
				// This doesn't have the alpha channel
				Color tempColor = new Color(hsBtoRGB);
				// Using original alpha channel from the color
				return new Color(tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), original.getAlpha());
				
			}

		}*/
		
		//don't change grayish
		/*
		if(hsb[1]>0.2 && hsb[2]>0.2 && hsb[2]<0.8) {
			hsb[0] = hsb[0] + degrees2Shift;
			
		}
		int hsBtoRGB = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
		// This doesn't have the alpha channel
		Color tempColor = new Color(hsBtoRGB);
		// Using original alpha channel from the color
		return new Color(tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), original.getAlpha());*/

	}

	/**
	 * This method generates an image with different colors
	 * 
	 * @param inputImage
	 *            path to input image, (example.jpg)
	 * @param outputImage
	 *            path to output image, (example-color.jpg will result in
	 *            example-color<number>.jpg)
	 * @param imageType
	 *            [png, jpg, gif]
	 * @param numberOfImages
	 *            number of perturbed copies
	 */
	public static void changeImageColor(String inputImage, String outputImage, int numberOfImages) {
		try {

			float constant = (float) (Math.PI / (numberOfImages + 1f));
			for (int i = 1; i <= numberOfImages; i++) {
				BufferedImage read = ImageIO.read(new File(inputImage));
				String output = outputImage.substring(0, outputImage.lastIndexOf("."));
				String type = outputImage.substring(outputImage.lastIndexOf(".") + 1, outputImage.length());
				changeHue(read, new Rectangle(0, 0, read.getWidth(), read.getHeight()), constant * i);
				ImageIO.write(read, type, new File(output + i + "." + type));
				System.out.println();
				System.out.println();
				System.out.println();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String argb2Hex(Color c) {
		return String.format("#%02X%02X%02X%02X", c.getAlpha(), c.getRed(), c.getGreen(), c.getBlue());
	}

	public static String rgb2Hex(Color c) {
		return String.format("#%02X%02X%02X", c.getAlpha(), c.getRed(), c.getGreen(), c.getBlue());
	}

	public static Color hexStringToARGB(String hexARGB) throws IllegalArgumentException {

		if (!hexARGB.startsWith("#") || !(hexARGB.length() == 7 || hexARGB.length() == 9)) {

			throw new IllegalArgumentException("Hex color string is incorrect!");
		}

		int[] argb = new int[4];

		if (hexARGB.length() == 9) {
			argb[0] = Integer.valueOf(hexARGB.substring(1, 3), 16); // alpha
			argb[1] = Integer.valueOf(hexARGB.substring(3, 5), 16); // red
			argb[2] = Integer.valueOf(hexARGB.substring(5, 7), 16); // green
			argb[3] = Integer.valueOf(hexARGB.substring(7), 16); // blue
		} else {
			hexStringToARGB("#FF" + hexARGB.substring(1));
		}

		return new Color(argb[1], argb[2], argb[3], argb[0]);
	}

	public static boolean compareImagesSURF(String pathToImageOne, String pathToImageTwo) {
		boolean match = true;

		BufferedImage imgOne = null;
		BufferedImage imgTwo = null;

		try {
			imgOne = ImageIO.read(new File(pathToImageOne));
			imgTwo = ImageIO.read(new File(pathToImageTwo));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		GrayF32 tempGreyOne = null;
		GrayF32 tempGreyTwo = null;
		
		GrayF32 grayOne = ConvertBufferedImage.convertFrom(imgOne, tempGreyOne);
		GrayF32 grayTwo = ConvertBufferedImage.convertFrom(imgTwo, tempGreyTwo);
		
		

		DetectDescribePoint<GrayF32,BrightFeature> surfOne = FactoryDetectDescribe.
				surfStable(new ConfigFastHessian(0, 2, 200, 2, 9, 4, 4), null, null,GrayF32.class);

		DetectDescribePoint<GrayF32,BrightFeature> surfTwo = FactoryDetectDescribe.
				surfStable(new ConfigFastHessian(0, 2, 200, 2, 9, 4, 4), null, null,GrayF32.class);

		surfOne.detect(grayOne);
		surfTwo.detect(grayTwo);
		
		System.out.println("Found Features for Image One: "+surfOne.getNumberOfFeatures());
		for(int i = 0; i < surfOne.getDescription(0).size(); i++) {
		System.out.println("First descriptor's first value for Image One: "+surfOne.getDescription(0).value[i]);
		}
		System.out.println();
		System.out.println("Found Features for Image Two: "+surfTwo.getNumberOfFeatures());
		for(int i = 0; i < surfTwo.getDescription(0).size(); i++) {
		System.out.println("First descriptor's first value for Image Two: "+surfTwo.getDescription(0).value[0]);
		}

		return match;
	}

	public static void main(String arg[]) throws IOException {
		// String file = "/Users/KevinMoran/Desktop/HiWallet-1-Nexus5.png";
		// String out = "/Users/KevinMoran/Desktop/HiWallet-1-Nexus5-2.png";

		// augmentScreenShotMult(file, out, 50, 100, 50, 50, 400, 200, 50, 50);

		// String image1 =
		// "Subjects/Testing/Image-Similarity-Tests/Node1DS.jpg";
		// String image2 =
		// "Subjects/Testing/Image-Similarity-Tests/Node1UI.jpg";
		//
		// boolean areHistogramsClose = ImagesHelper.areHistogramsClose(image1,
		// image2, 0.20);
		
		BufferedImage org1 = ImageIO.read(new File("/Users/KevinMoran/Desktop/GVT-output/implement/423031029.jpg"));
		BufferedImage org2 = ImageIO.read(new File("/Users/KevinMoran/Desktop/GVT-output/design/1844169442.jpg"));
		
		  BufferedImage blackAndWhiteImg1 = new BufferedImage(
			        org1.getWidth(), org1.getHeight(),
			        BufferedImage.TYPE_BYTE_BINARY);
		  
		  BufferedImage blackAndWhiteImg2 = new BufferedImage(
			        org2.getWidth(), org2.getHeight(),
			        BufferedImage.TYPE_BYTE_BINARY);
		
		  Graphics2D g2d = blackAndWhiteImg1.createGraphics();
		  g2d.drawImage(org1, 0, 0, null);
		  
		  Graphics2D g2d2 = blackAndWhiteImg2.createGraphics();
		  g2d2.drawImage(org2, 0, 0, null);
		  
		  ImageIO.write(blackAndWhiteImg1, "png", new File("/Users/KevinMoran/Desktop/test-1.png"));
		  ImageIO.write(blackAndWhiteImg2, "png", new File("/Users/KevinMoran/Desktop/test-2.png"));
		  
		  
		//compareImagesSURF(inputOne, inputTwo);
		//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.out.println(areHistogramsClose("/Users/KevinMoran/Desktop/test-1.png", "/Users/KevinMoran/Desktop/test-2.png", 0.05));
		//System.out.println(compareImagesORB("/Users/KevinMoran/Desktop/test-1.png", "/Users/KevinMoran/Desktop/test-2.png"));
	}



}
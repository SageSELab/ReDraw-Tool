package cs435.extra;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import boofcv.struct.image.GrayF32;
import boofcv.struct.image.Planar;
import boofcv.abst.distort.FDistort;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import cs435.guiproto.*;

/**
 * Try to get a working background image from an activity (i.e. one with no buttons or text).
 * 
 * The essential algorithm here is pretty simple:
 *   - Take the original screenshot.
 *   - Some components, like buttons, are completely opaque. If they aren't, we "paint over"
 *     where the original components were on the screenshot using colors from nearby pixels.
 *     The effect is not very good at replicating details, but that's hardly relevant when
 *     most apps use solid colors or gradients for their backgrounds.
 *     
 * Exactly how we "paint over" these spaces is up in the air right now.
 * @author bdpowell
 */
public class BackgroundGetter {
	
	//we define the dims for a nexus 7 (2013)
	//TODO implement functionality to adapt to different screensizes
	public static int[] CONTENT_VIEW_DIMENSIONS = {0, 72, 1200, 1704 };
	
	public static void main(String[] args) throws SAXException, IOException {
		final String screenPath = "acceptance-tests/com.morphoss.acal/screen.png";
		final String xmlPath = "acceptance-tests/com.morphoss.acal/ui-dump.xml";
		final String className = "TestActivity";
		
		preview(Paths.get(screenPath), "Original");
		
		// Using the yellow-and-blue one, since that's the ugliest
		// and has the most complex colors
		ActivityHolder activity = XMLParser.parseActivityFromFile(
				Paths.get(xmlPath),
				Paths.get(screenPath),
				className
		);
		
		Path temp = Files.createTempFile("background", ".png");
		generateBackground(activity, temp);
		preview(temp, "Filled");
	}
	
	public static void generateBackground(ActivityHolder activity, Path out) throws IOException {
		String screenPath = activity.getScreenshotPath().toString();
		
		// Load the screenshot, convert it into an RGB F32 planar
		Planar<GrayF32> image = new Planar<>(GrayF32.class, 1, 1, 4);
		BufferedImage file = UtilImageIO.loadImage(screenPath);
		
		image.reshape(file.getWidth(), file.getHeight());
		image = ConvertBufferedImage.convertFromMulti(file, null, true, GrayF32.class).partialSpectrum(0,1,2);
		
		// Replace opaque components with the background color
		List<Rectangle> masks = new ArrayList<Rectangle>();
		for (View child : activity.getViewList()) {
			if (!(child instanceof ViewGroup)) {
				int x, y, w, h;
				x = child.getX();
				y = child.getY();
				w = child.getWidth();
				h = child.getHeight();
				masks.add(new Rectangle(x, y, w, h));
			}
		}
		
		// Cropping out the status/progress bar will throw the alignment of the background off.
		// Instead, we'll just paint them over.
//		View contentView = activity.getViewList().get(2);
		Rectangle content = new Rectangle(
				CONTENT_VIEW_DIMENSIONS[0],
				CONTENT_VIEW_DIMENSIONS[1],
				CONTENT_VIEW_DIMENSIONS[2],
				CONTENT_VIEW_DIMENSIONS[3]
		);
		// Top bar
		masks.add(new Rectangle(
				0, 0,
				content.width,
				content.y
		));
		// Bottom bar
		int top = content.y+content.height;
		int bottom = 99999;
		masks.add(new Rectangle(
				0,
				top,
				content.width,
				bottom
		));
		
		
		Color background = ImagesHelper.getPrimaryColor(screenPath, masks);
		
		//this is a bandaid we are coloring the whole background the same color
		//TODO: get rid of this, implement some sort of gradient fill or make the selective cropping viable
		masks.add(content);

		Planar<GrayF32> filledImage = image.clone();
		for (Rectangle rect : masks) {
			BackgroundGetter.drawRect(filledImage,
					Constants.unscale(rect.x, true),
					Constants.unscale(rect.y, false),
					Constants.unscale(rect.width, true),
					Constants.unscale(rect.height, false),
					background);
		}
		
		UtilImageIO.saveImage(filledImage, out.toString());
	}
	
	private static void drawRect(Planar<GrayF32> source, int x1, int y1, int width, int height, Color color) {
		x1 = Math.max(0, x1);
		y1 = Math.max(0, y1);
		int x2 = Math.min(source.getWidth()-1, x1 + width);
		int y2 = Math.min(source.getHeight()-1, y1 + height);
		
		for (int y = y1; y < y2; y++) {
			for (int x = x1; x < x2; x++) {
				for (int band=0; band < source.getNumBands(); band++) {
					float c = 255.0F;
					switch (band) {
					case 0:
						c = (float) color.getRed();
						break;
					case 1:
						c = (float) color.getGreen();
						break;
					case 2:
						c = (float) color.getBlue();
						break;
					}
					source.getBand(band).set(x, y, c);
				}
			}
		}
	}
	
	private static void preview(Path source, String title) {
		// Load the screenshot, convert it into an RGB F32 planar
		Planar<GrayF32> image = new Planar<>(GrayF32.class, 1, 1, 4);
		BufferedImage file = UtilImageIO.loadImage(source.toString());
		
		image.reshape(file.getWidth(), file.getHeight());
		image = ConvertBufferedImage.convertFromMulti(file, null, true, GrayF32.class).partialSpectrum(0,1,2);
		
		Planar<GrayF32> out = new Planar<>(GrayF32.class, image.getWidth() / 4, image.getHeight() / 4, 3);
		new FDistort(image, out).scaleExt().apply();
		ShowImages.showWindow(out, title);
	}
	
}

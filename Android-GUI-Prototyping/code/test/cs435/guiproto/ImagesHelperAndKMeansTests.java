package cs435.guiproto;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import ij.plugin.Colors;

/**
 * Tests for ImagesHelper functions.
 * @author bdpowell
 *
 */
public class ImagesHelperAndKMeansTests {
	
	private static final Path images = Paths.get("resources/test-images");
	
	/**
	 * Colors corresponding to the images.
	 */
	private static final Color yellow = new Color(239, 160, 33);
	private static final Color blue = new Color(0, 18, 94);
	private static final Color red = new Color(255, 0, 0);
	
	/**
	 * Get the most frequent color in an image.
	 */
	@Test
	public void testGetPrimaryColor() {
		try {
			Color color;
			color = ImagesHelper.getPrimaryColor(images.resolve("red.jpg").toString());
			Helpers.assertColorsClose("Solid red doesn't match", color, red);
			
			color = ImagesHelper.getPrimaryColor(images.resolve("cropped.jpg").toString());
			Helpers.assertColorsClose("Button yellow doesn't match", color, yellow);
		} catch (IOException e) {
			fail("Couldn't find button: " + e.getMessage());
		}
	}
	
	/**
	 * Test getMajorColor()'s masking feature by removing the red pixels from a blue image.
	 * @throws IOException 
	 */
	@Test
	public void testGetMajorColorsMasked() throws IOException {
		final List<Rectangle> mask = new ArrayList<>();
		mask.add(new Rectangle(3, 7, 176, 170));
		Color color;
		
		color = ImagesHelper.getPrimaryColor(images.resolve("masked.png").toString(), mask);
		Helpers.assertColorsClose("Masked red doesn't match", color, red);
	}
	
	/**
	 * Get both a button's background color and text color.
	 */
	@Test
	public void testGetPrimaryAndSecondaryColors() {
		try {
			Color[] colors = ImagesHelper.getTwoColors(images.resolve("cropped.jpg").toString());
			System.out.println(colors);
			Helpers.assertColorsClose("Button's yellow background doesn't match", colors[0], yellow);
			Helpers.assertColorsClose("Button's blue text doesn't match", colors[1], blue);
		} catch (IOException e) {
			fail("Couldn't find button: " + e.getMessage());
		}
	}
	
}

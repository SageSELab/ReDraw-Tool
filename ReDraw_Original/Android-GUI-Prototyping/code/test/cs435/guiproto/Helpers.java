package cs435.guiproto;

import static org.junit.Assert.fail;

import java.awt.Color;

/**
 * Static methods used across tests
 * @author bdpowell
 */
public class Helpers {
	
	// Threshold for color closeness. Higher values allow for more discrepancy between actual and
	// expected color
	private static final double tolerance = 0.1;
	
	/**
	 * Convert #FF0000 into Color(255, 0, 0), for example
	 * 
	 * @param in Should have the #
	 * @return
	 */
	public static Color colorFromHex(String in) {
		return new Color(Integer.parseInt(in.substring(1), 16));
	}
	
	/**
	 * Fail if two colors aren't that close to one another.
	 * @param message
	 * @param actual
	 * @param expected
	 */
	public static void assertColorsClose(String message, Color actual, Color expected) {
		if (!ImagesHelper.isRgbClose(actual, expected, tolerance)) {
			message += ": expected " + expected + ", but got " + actual;
			fail(message);
		}
	}
}

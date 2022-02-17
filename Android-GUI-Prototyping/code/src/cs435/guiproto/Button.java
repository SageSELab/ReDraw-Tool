package cs435.guiproto;

import org.w3c.dom.Element;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import org.w3c.dom.Document;
import cs435.extra.KMeans;

/**
 * A pressable button. It tries to guess its color and text size from the activity's screenshot.
 * @author bdpowell
 */
public class Button extends ComponentWithColor {

	public Button() {
		super("Button", "android:background", "android:textColor");
	}
	
}

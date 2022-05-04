package cs435.guiproto;

import java.awt.Color;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A component whose text and background color will be estimated from the screenshot.
 * 
 * @author bdpowell
 */
public class ComponentWithColor extends Component {
	
	/**
	 * This XML attribute will have the most frequent color assigned to it.
	 * Use this for background color, etc.
	 */
	protected String primaryAttribute;
	
	/**
	 * This XML attribute will have a less frequent (but still prominent) color assigned to it.
	 * Use this for things like text color.
	 */
	protected String secondaryAttribute;
	
	public ComponentWithColor(String name, String primeAttr, String secondAttr) {
		super(name);
		primaryAttribute = primeAttr;
		secondaryAttribute = secondAttr;
	}
	
	/**
	 * Get the component's primary and secondary colors.
	 * You can merge this style fragment with other fragments of the same class to
	 * consolidate them.
	 */
	@Override
	public void initStyleFragment() {
		Color primary;
		Color secondary;
		try {
			String screen = activity.getScreenshotPath().toString();
			int ux, uy, uw, uh;
			ux = Constants.unscale(x,      true);
			uy = Constants.unscale(y,      false);
			uw = Constants.unscale(width,  true);
			uh = Constants.unscale(height, false);
			assert(uw > 0 );
			assert(uh > 0);
			assert(ux >= 0);
			assert(uy >= 0);
			Color[] cs = ImagesHelper.getTwoColorsCropped(screen, ux, uy, uw, uh);
			primary = cs[0];
			secondary = cs[1];
		} catch (IOException e) {
			System.out.println("Couldn't get colors from screen: defaulting to bright red+green");
			primary = Color.RED;
			secondary = Color.GREEN;
			e.printStackTrace();
		}
		
		fragment = new StyleFragment();
		if (primaryAttribute != null)
			fragment.addColorAttribute(primaryAttribute, primary);
		
		if (secondaryAttribute != null)
			fragment.addColorAttribute(secondaryAttribute, secondary);
	}
	
}

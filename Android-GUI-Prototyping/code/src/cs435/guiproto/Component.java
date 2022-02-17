package cs435.guiproto;

import java.util.HashMap;

/**
 * Abstract base class for a component - a View that doesn't have any children.
 */

public class Component extends View {

	
	public Component(String name) {
		super(name);
		marginMap = new HashMap<Constants.margin, Integer>();
	}
	
}

package cs435.guiproto;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * ViewGroup for unsupported components. We're currently using a
 * @author bdpowell
 *
 */
public class DummyViewGroup extends RelativeLayout {
	
	private String originalName;
	
	public DummyViewGroup(String originalName) {
		super();
		this.originalName = originalName;
	}
	
	/**
	 * Get the name of the class that this should be (for debugging purpsoes).
	 * @return
	 */
	public String getOriginalName() {
		return originalName;
	}
}

package cs435.guiproto;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A component whose text color should be estimated from the screenshot.
 * @author bdpowell
 */
public class TextView extends ComponentWithColor {
	
	public TextView() {
		super("TextView", null, "android:textColor");
	}

	@Override
	public Element getLayoutElement(Document doc) {
		Element out = super.getLayoutElement(doc);
		// TODO Auto-generated method stub
//		out.setAttribute("android:autoSizeText", "1");
		return out;
	}
	
}

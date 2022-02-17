package cs435.guiproto;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Simple class extending Relative Layout, the only difference is how we write
 * the class to the xml. For this class, we want to match parent horizontally
 * @author Michael
 *
 */
public class ScrollView extends ViewGroup {
	
	public ScrollView(){
		super("ScrollView");
	}
	
	@Override
	public Element getLayoutElement(Document doc){
		Element out = super.getLayoutElement(doc);
		out.setAttribute("android:layout_height", "wrap_content");
		out.setAttribute("android:layout_width", "match_parent");
		return out;
	}

}

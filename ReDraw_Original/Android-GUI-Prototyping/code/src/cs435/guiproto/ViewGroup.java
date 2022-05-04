package cs435.guiproto;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Superclass for any View with children.
 * @author bdpowell
 *
 */
public class ViewGroup extends View {
	
	public List<View> children;
	private String name;
	
	public ViewGroup(String name) {
		super(name);
		this.name = name;
		children = new ArrayList<>();
		marginMap = new HashMap<Constants.margin, Integer>();
	}
	
	/**
	 * Get this view's children as a list.
	 * @return
	 */
	public List<View> getChildren() {
		return children;
	}
	
	/**
	 * Add a child to the list.
	 * @param view
	 */
	public void addToChildren(View view){
		children.add(view);
	}
	
	@Override
	public void setActivity(ActivityHolder a) {
		super.setActivity(a);
		for (View child : children)
			child.setActivity(a);
	}
	
	/**
	 * Add attributes to each child based on their positions.
	 * This will be called only after all of the children have been added.
	 */
	public void pack() {
	}
	
	@Override
	public void generateResources(Path root) throws IOException {
		for (View child : children) {
			child.generateResources(root);
		}
	}
	
	/**
	 * Return the XML element that this node generates.
	 * 
	 * This generates elements for the children of the layout as well as the layout itself.
	 */
	@Override
	public Element getLayoutElement(Document doc) {
		Element out = super.getLayoutElement(doc);
		for (View child : children) {
			// For reference: an XML node is an attribute, element, text, etc.
			// So an element can be upcasted to a node
			out.appendChild((Node) child.getLayoutElement(doc));
		}
		return out;
	}
	
	@Override
	public Element getLayoutElementAbsolute(Document doc, float marginLeft, float marginTop){
		Element out = super.getLayoutElementAbsolute(doc, marginLeft, marginTop);
		if (name.equalsIgnoreCase("FrameLayout")){
			for (View child : children){
				out.appendChild((Node) child.getLayoutElementAbsolute(doc, marginLeft, marginTop));
			}
		}
		return out;
	}
	/**
	 * useful for linear layouts when we need to re-order children
	 * @param children
	 */
	public void setChildren(ArrayList<View> children){
		this.children = children;
	}
}
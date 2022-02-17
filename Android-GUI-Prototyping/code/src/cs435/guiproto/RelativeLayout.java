package cs435.guiproto;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Set;

import cs435.guiproto.Constants.margin;

/**
 * A RelativeLayout stores the positions of components relative
 * to others.
 * 
 * The packing algorithms used don't work very well with overlapping
 * components, but should do fine otherwise. It's the most flexible
 * layout, which is why we tend to use it as a fallback.
 * 
 * @author Michael Curcio
 */
public class RelativeLayout extends ViewGroup {
	
	//these positional values should be visualized as vertical/horizontal lines on the screen,
	// we will use these lines to figure out the appropriate alignment attributes for children.
	private List<Integer> verticalLines = new ArrayList<Integer>();
	private List<Integer> horizontalLines = new ArrayList<Integer>();
	private List<RelativeDecorator> newList;
	
	public RelativeLayout(){
		super("RelativeLayout");
		children = new ArrayList<View>();
	}
	
	@Override
	public Element getLayoutElementAbsolute(Document doc, float marginLeft, float marginTop){
		Element out = super.getLayoutElementAbsolute(doc, marginLeft, marginTop);
		int top,left;
		int x = this.getX();
		int y = this.getY();
		
		for (View child : children){
			top = child.getY() - y;
			left = child.getX() - x;
			out.appendChild((Node) child.getLayoutElementAbsolute(doc, left, top));
		}
		return out;
	}
	
	public List<RelativeDecorator> getNewList(){
		return newList;
	}
	
	public void setNewList(List<RelativeDecorator> lst){
		newList = lst;
	}
	
	public void pack(){
		super.pack();
		int i;
		RelativeDecorator anchor;
		newList = new ArrayList<RelativeDecorator>();
		
		//convert children into the proper type for a relative layout using the decorator pattern to extend
		// functionality, store it in a new list because we need to convert back later.
		for (View cur : children){
			newList.add(new RelativeDecorator(cur));
		}
		int num = newList.size();
		
		List<RelativeDecorator> compList = newList;
		
		anchor = findAnchor(compList);
		determineAnchorMargins(anchor);
		children.set(0, anchor);
		compList.remove(anchor);
		
		for(i=1; i < num; i++){
			RelativeDecorator cur = getClosestComponent(anchor, compList);
			determineMargins(cur, anchor);
			compList.remove(cur);
			anchor = cur;
			children.set(i, cur);
		}
		
		// Put newList back into children
//		for (i=0; i<newList.size(); i++) {
//			children.set(i, newList.get(i));
//		}
	}
	
	private void determineMargins(RelativeDecorator newComp, RelativeDecorator anchor){
		determineHorizontalMargins(newComp, anchor);
		determineVerticalMargins(newComp, anchor);
	}
	
	private void determineHorizontalMargins(RelativeDecorator newComp, RelativeDecorator anchor){
		ArrayList<Integer> distances = new ArrayList<Integer>();
		ArrayList<Constants.RelAttributes> atts = new ArrayList<Constants.RelAttributes>();
		Constants.RelAttributes attribute;
		int curmin;
		int ndx;

		int anchorLeft = anchor.view.getX();
		int anchorRight = anchorLeft + anchor.view.getWidth();
		
		int newLeft = newComp.view.getX();
		int newRight = newLeft + newComp.view.getWidth();
		//1. align start
		distances.add(Math.abs(anchorLeft - newLeft));
		atts.add(Constants.RelAttributes.ALIGN_START);
		//2. align end
		distances.add(Math.abs(anchorRight - newRight));
		atts.add(Constants.RelAttributes.ALIGN_END);
		//3. to start of
		distances.add(Math.abs(newRight - anchorLeft));
		atts.add(Constants.RelAttributes.START_OF);
		//4. to end of
		distances.add(Math.abs(newLeft - anchorRight));
		atts.add(Constants.RelAttributes.END_OF);
		
		curmin = distances.get(0);
		ndx = 0;
		int count = 0;
		for (Integer i: distances){
			if (i < curmin){
				curmin = i;
				ndx = count;
			}
			count++;
		}
		attribute = atts.get(ndx);
		
		if (attribute.equals(Constants.RelAttributes.ALIGN_START) || attribute.equals(Constants.RelAttributes.END_OF)){
			newComp.getMap().put(attribute, anchor.view.getAndroidId());
			newComp.getMargins().put(margin.START, curmin);
		}
		else{
			newComp.getMap().put(attribute, anchor.view.getAndroidId());
			newComp.getMargins().put(margin.END, curmin);
		}
		
	}
	
	private void determineVerticalMargins(RelativeDecorator newComp, RelativeDecorator anchor){
		ArrayList<Integer> distances = new ArrayList<Integer>();
		ArrayList<Constants.RelAttributes> atts = new ArrayList<Constants.RelAttributes>();
		Constants.RelAttributes attribute;
		int curmin;
		int ndx;

		int anchorTop = anchor.view.getY();
		int anchorBottom = anchorTop + anchor.view.getHeight();
		
		int newTop = newComp.view.getY();
		int newBottom = newTop + newComp.view.getHeight();
		//1. align start
		distances.add(Math.abs(anchorTop - newTop));
		atts.add(Constants.RelAttributes.ALIGN_TOP);
		//2. align end
		distances.add(Math.abs(anchorBottom - newBottom));
		atts.add(Constants.RelAttributes.ALIGN_BOTTOM);
		//3. to start of
		distances.add(Math.abs(newBottom - anchorTop));
		atts.add(Constants.RelAttributes.ABOVE);
		//4. to end of
		distances.add(Math.abs(newTop - anchorBottom));
		atts.add(Constants.RelAttributes.BELOW);
		
		curmin = distances.get(0);
		ndx = 0;
		int count = 0;
		for (Integer i: distances){
			if (i < curmin){
				curmin = i;
				ndx = count;
			}
			count++;
		}
		attribute = atts.get(ndx);
		
		if (attribute.equals(Constants.RelAttributes.ALIGN_TOP) || attribute.equals(Constants.RelAttributes.BELOW)){
			newComp.getMap().put(attribute, anchor.view.getAndroidId());
			newComp.getMargins().put(margin.TOP, curmin);
		}
		else{
			newComp.getMap().put(attribute, anchor.view.getAndroidId());
			newComp.getMargins().put(margin.BOTTOM, curmin);
		}
	}
	
	private RelativeDecorator getClosestComponent(RelativeDecorator comp, List<RelativeDecorator> lst){
		Point anchorPoint = new Point((int) comp.view.getCenterX(), (int) comp.view.getCenterY());
		
		RelativeDecorator out = smallestDistance(anchorPoint, lst);
		return out;
	}
	
	private void determineAnchorMargins(RelativeDecorator a){
		int marginLeft = a.view.getX() - this.getX();
		int marginTop = a.view.getY() - this.getY();
		
		a.getMap().put(Constants.RelAttributes.ALIGN_PARENT_START, "true");
		a.getMap().put(Constants.RelAttributes.ALIGN_PARENT_TOP, "true");
		
		a.getMargins().put(Constants.margin.START, marginLeft);
		a.getMargins().put(Constants.margin.TOP, marginTop);
	}
	
	private RelativeDecorator findAnchor(List<RelativeDecorator> lst){

		RelativeDecorator out = null;

		Point anchor = new Point(this.getX(), this.getY());
		out = smallestDistance(anchor, lst);
		return out;
	}
	
	private RelativeDecorator smallestDistance(Point pt, List<RelativeDecorator> lst){
		int i;
		double curmin = 0;
		double distance;
		RelativeDecorator out = null;
		
		for (i=0; i<lst.size(); i++){
			RelativeDecorator curComp = lst.get(i);
			Point curPoint = new Point(curComp.view.getX(), curComp.view.getY());
			distance = Point2D.distance(curPoint.getX(), curPoint.getY(), pt.getX(), pt.getY());
			if (i==0 || distance < curmin){
				out = curComp;
				curmin = distance;
			}
		}

		assert out!=null;
		return out;
	}
	
	private RelativeDecorator findDecoratorById(String id){
		
		for (RelativeDecorator dec : newList){
			if (dec.view.getAndroidId().equalsIgnoreCase(id)){
				return dec;
			}
		}
		//if we get here, something went very wrong
		System.out.println("FATAL: no decorator found matching the given ID " + id + ", exiting...");
		System.exit(0);
		return null;
		
		
	}
	

}

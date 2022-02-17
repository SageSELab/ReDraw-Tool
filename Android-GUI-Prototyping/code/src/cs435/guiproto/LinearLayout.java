
package cs435.guiproto;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class LinearLayout extends ViewGroup {


	enum Orientation {
			Horizontal,
			Vertical;
		
		@Override
		public String toString() {
			return (this == Horizontal ? "horizontal" : "vertical");
		}
	}
	
	/**
	 * this is racist
	 * @author Michael
	 */
	Orientation orient;
	//if true, the total weight will be 1
	boolean needWeight;
	
	int totalweight;
	
	
	public LinearLayout() {
		super("LinearLayout");
	}
	
	@Override
	public void pack() {
		super.pack();
		
		determineOrientation();
		boolean vert = orient==Orientation.Vertical;
		reOrderChildren(vert);
		if (orient == Orientation.Vertical){
			setPadding(true);
		}
		else{
			setPadding(false);
		}
		
		setWeights(orient);
	}
	
	@Override
	public Element getLayoutElement(Document doc) {
		Element out = super.getLayoutElement(doc);
//		out.setAttribute("android:padding", "2dp" );
		out.setAttribute("android:orientation", orient.toString());
		if (needWeight) {
			out.setAttribute("android:weightSum", Integer.toString(totalweight));
			for (View view : this.getChildren()){
				if (orient == Orientation.Vertical){
					view.getElement().setAttribute("android:layout_weight",
							Integer.toString(view.getHeight()));
			 	}
				else{
					view.getElement().setAttribute("android:layout_weight",
							Integer.toString(view.getWidth()));
				}
			}
		}
		for (View view : this.getChildren()){
			if (orient == Orientation.Vertical){
				if (needWeight){
				view.getElement().setAttribute("android:layout_height", 
						"0dp");
				}
				view.getElement().setAttribute("android:layout_width", 
						"match_parent");
			}
			else{
				if (needWeight){
				view.getElement().setAttribute("android:layout_width", 
						"0dp");
				}
				view.getElement().setAttribute("android:layout_height", 
						"match_parent");
			}
		}
		return out;
	}
	
	public Element getLayoutElementAbsolute(Document doc, float leftMargin, float topMargin){
		Element out = super.getLayoutElementAbsolute(doc, leftMargin, topMargin);
		Constants.margin key;
		boolean orientBool = orient==Orientation.Vertical;
		if (orientBool){
			out.setAttribute("android:orientation", "vertical");
		}
		else{
			out.setAttribute("android:orientation", "horizontal");
		}
		
		for (View child : this.children){
			out.appendChild((Node) child.getLayoutElementAbsolute(doc,  child.marginMap.get(Constants.margin.START), 
					child.marginMap.get(Constants.margin.TOP)));
		}
		return out;
	}
	
	/**
	 * takes the children and reorders them based on increasing x or y values
	 */
	private void reOrderChildren(boolean vert){
		ArrayList<View> newList = new ArrayList<View>();
		List<View> oldList = this.getChildren();
		int stat;
		int curmin = 0;
		int ndx = 0;
		int count = 0;
		
		while (oldList.size() != 0){
			count = 0;
			for (View cur : oldList){
				if (vert){stat=cur.getY();}else{stat=cur.getX();}
				if (count==0 || stat < curmin){
					curmin = stat;
					ndx = count;
				}
				count++;
			}
			newList.add(oldList.get(ndx));
			oldList.remove(ndx);
		}
		this.setChildren(newList);
	}
	
	private void determineOrientation(){
		// Test if layout is horizontal. If it isn't, it's vertical.
		
				// Find tallest element
				View tallest = children.get(0);
				for (int i=1; i<children.size(); i++) {
					if (children.get(i).getHeight() > tallest.getHeight()) {
						tallest = children.get(i);
					}
				}
				// Check if every element is "within" the tallest one vertically
				boolean isHorizontal = true;
				int ty1, ty2;
				ty1 = tallest.getY();
				ty2 = ty1 + tallest.getHeight();
				// Layout is horizontal iff every child is between its top and bottom
				for (View child : children) {
					int y1 = child.getY();
					int y2 = y1 + child.getHeight();
					if (y1 < ty1 || y2 > ty2) {
						isHorizontal = false;
						break;
					}
				}
				
				if (isHorizontal) {
					orient = Orientation.Horizontal;
				} else {
					orient = Orientation.Vertical;
				}
	}
	
	private void setWeights(Orientation orient){
		int total = 0;
		if (this.getChildren().size() == 1){
			this.needWeight = false;
			return;
		}
		else{
			this.needWeight = true;
			for (View child : this.getChildren()){
//				child.isLinear();
				if (orient == Orientation.Vertical){
					total += child.getHeight();
				}
				else{
				total += child.getWidth();
				}
			}
			totalweight = total;
//			for (View child : this.getChildren()){
//				float percent = ((float) (child.getHeight() * child.getWidth())) / totalArea;
//				child.setWeight(percent);
//			}
		}
	}
	/**
	 * method to set the margin for each individual component of the linear layout. 
	 * note that we need to do this for three directions for all of them, and the fourth only for the last one,
	 * granted that we size the children utilizing the weight attribute.
	 * @author M. Curcio
	 */
	private void setPadding(boolean isVertical){
		int paddingVert;
		int paddingHor;
		View lastNode = null;
		if (isVertical){
			int curPosition = this.getY();
			for (View curNode : this.getChildren()){
				paddingVert = curNode.getY() - curPosition;
				curNode.marginMap.put(Constants.margin.TOP, paddingVert);
				paddingHor = curNode.getX() - this.getX();
				curNode.marginMap.put(Constants.margin.START, paddingHor);
				curNode.marginMap.put(Constants.margin.END, (this.getX() + this.getWidth()) - 
						(curNode.getX() + curNode.getWidth()));
				curPosition = curNode.getY() + curNode.getHeight();
				lastNode = curNode;
			}
			//for the last view, need to know how far from the bottom
			int bottomMargin = (this.getY() + this.getHeight()) - (lastNode.getY() + lastNode.getHeight());
			lastNode.marginMap.put(Constants.margin.BOTTOM, bottomMargin);
		}
		else{
			int curPosition = this.getX();
			for (View curNode : this.getChildren()){
				paddingHor = curNode.getX() - curPosition;
//				if (
				curNode.marginMap.put(Constants.margin.START, paddingHor);
//				}
				paddingVert = curNode.getY() - this.getY();
				curNode.marginMap.put(Constants.margin.TOP, paddingVert);
				curNode.marginMap.put(Constants.margin.BOTTOM, (this.getY() + this.getHeight()) - 
						(curNode.getY() + curNode.getHeight()));
				curPosition = curNode.getX() + curNode.getWidth();
				lastNode = curNode;
			}
			//the last element needs to know how far it is from the end, unless there's only one
//			if (this.getChildren().size() != 0){
//			if ((this.getX() + this.getWidth()) - (lastNode.getX() + lastNode.getWidth()) > 0){
				lastNode.marginMap.put(Constants.margin.END, (this.getX() + this.getWidth()) - 
						(lastNode.getX() + lastNode.getWidth()));
//			}
//			}
		}
		
	}
	
}


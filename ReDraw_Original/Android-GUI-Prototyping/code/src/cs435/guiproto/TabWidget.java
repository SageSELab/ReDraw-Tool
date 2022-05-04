package cs435.guiproto;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class extends the linear layout. The purpose of this class is that we need a
 * linear layout which only supports imageviews. TabWidgets seem to have extremely strange
 * characteristics with respect to the input xml, making it nearly impossible to align images
 * correctly in the usual way. As a workaround, we implement TabWidgets as a linear layout of 
 * imageViews
 * @author Michael
 *
 */
public class TabWidget extends LinearLayout{
	
	public TabWidget(){
		super();
//		convertToImage();
		
	}
	/**
	 * method converts the children into imageviews so that we do not have to deal with
	 * strange xml input from tabWidgets. Should, ideally, result in the halting of the recursion
	 * down the view tree when we go to write.
	 */
	@Override
	public void pack(){
		ArrayList<View> newList = new ArrayList<View>();
		for (View view : this.getChildren()){
			//casting down works because we already have the necessary fields
			View out = new ComponentWithImage("ImageView");
			int height = view.getHeight();
			int width = view.getWidth();
			int x = view.getX();
			int y = view.getY();
//			out.setValues((int) Constants.scale(x, true), (int) Constants.scale(y,  false), (int) Constants.scale(width, true), 
//					(int) Constants.scale(height, false));
			out.setValues(x, y, width, height);
			newList.add(out);
		}
		this.children = newList;
		super.pack();
	}

}

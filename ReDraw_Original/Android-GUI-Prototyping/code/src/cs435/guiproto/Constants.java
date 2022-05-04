package cs435.guiproto;
/**
 * Class for holding constants such as enum types and public final static variables
 * 
 * @author Michael Curcio
 *
 */
public class Constants {
	
	/**
	 * assuming the acal acceptance test
	 * @TODO set this based on the given xml file
	 */
	public static double dpi = 323; // Nexus 5
	public static float inputscreenwidth = 1200;
	public static float inputscreenheight = 1920;
	public static float outputscreenwidth;
	public static float outputscreenheight;
	
	public static float xscalar;
	public static float yscalar;
	
	public static void setGUIconstants(double dpinum, float isw,float ish, float osw, float osh){
		dpi  = dpinum;
		inputscreenwidth = isw;
		inputscreenheight = ish;
		outputscreenwidth =osw;
		outputscreenheight = osh;
		setscalar();
	}
	/**
	 * for now we are focusing on one screen - 1200x1920. 
	 * 
	 * TODO implement scaling mechanism for multiple screen sizes, DPIs, and input/output layout schemes
	 */
	public static void setscalar(){
		//Material Theme: 24 = statusBar, 64 = actionBar, 48 = AndroidNavBar
		//float[] screenDimensions = {1080, 1920};
		float[] screenDimensions = {outputscreenwidth,outputscreenheight};
		float[] appDims = {screenDimensions[0], (float) (0)};
		float[] result = {appDims[0] / inputscreenwidth, appDims[1] / inputscreenheight};
		xscalar = 1;
		yscalar = 1;
	}
	public void setdpi(double dpinum){
		dpi = dpinum;
	}
	//only 4 directions that margins can be in: up, down, right, left
	public enum margin{
		TOP("marginTop"),
		BOTTOM("marginBottom"),
		START("marginStart"),
		END("marginEnd");
		
		private final String attrName;
		
		private margin(final String attrName) {
			this.attrName = attrName;
		}
		
		@Override
		public String toString() {
			return "android:layout_" + attrName;
		}
		
	}
	//possible alignment settings for children of a relative layout
	public enum RelAttributes {
		ABOVE("above"),
		ALIGN_BASELINE("alignBaseline"),
		ALIGN_BOTTOM("alignBottom"),
		ALIGN_END("alignEnd"),
		ALIGN_LEFT("alignLeft"),
		ALIGN_PARENT_BOTTOM("alignParentBottom"),
		ALIGN_PARENT_END("alignParentEnd"),
		
		ALIGN_PARENT_LEFT("alignParentLeft"),
		ALIGN_PARENT_RIGHT("alignParentRight"),
		ALIGN_PARENT_START("alignParentStart"),
		ALIGN_PARENT_TOP("alignParentTop"),
		ALIGN_RIGHT("alignRight"),
		ALIGN_START("alignStart"),
		ALIGN_TOP("alignTop"),
		BELOW("below"),
		
		CENTER_HORIZONTAL("centerHorizontal"),
		CENTER_IN_PARENT("centerInParent"),
		CENTER_VERTICAL("centerVertical"),
		END_OF("toEndOf"),
		LEFT_OF("toLeftOf"),
		RIGHT_OF("toRightOf"),
		START_OF("toStartOf");
		
		private final String attrName;
		
		private RelAttributes(final String attrName) {
			this.attrName = attrName;
		}
		
		@Override
		public String toString() {
			return "android:layout_" + attrName;
		}
		
	}
	/**
	 * takes the given device and converts pixel values to dp so that we can 
	 * correctly place things regardless of device
	 * Note: currently we're just assuming that we're using a Nexus 5
	 * @param px
	 * @return
	 * 
	 * @TODO implement parser that maps device to dpi
	 */
	public static float toDP(int px){
		return (float) ((float) px / (dpi/160));
	}
	/**
	 * scales the given image to the screen size, again we are assuming it is a nexus 5, and
	 * assuming we are using the material theme
	 * @param x
	 * @return
	 * 
	 * @TODO implement a parser for the given device
	 * @TODO implement the function s.t. it depends on the theme, ie the theme is not hardcoded in
	 */
	public static float scale(int x, boolean hor){
		assert(xscalar != 0.0 && yscalar != 0.0);
		return x * (hor ? xscalar : yscalar);
	}
	
	/**
	 * unscale, we'll need this when we go to try to match colors using Ben's work
	 * @param d
	 * @param hor
	 * @return
	 */
	public static int unscale(double d, boolean hor){
		assert(xscalar != 0.0 && yscalar != 0.0);
		return (int) (d / (hor ? xscalar : yscalar));
	}
}

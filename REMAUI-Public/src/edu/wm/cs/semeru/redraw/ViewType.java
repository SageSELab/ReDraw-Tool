package edu.wm.cs.semeru.redraw;


/**
 * A class storing view information
 */
public class ViewType {
	
	/* Some predefined types for legacy */
    public static ViewType TEXT = new ViewType("TextView", "android:text=\"");
    public static ViewType IMAGE = new ViewType("ImageView", "android:src=\"@drawable/");
    public static ViewType LIST = new ViewType("ListView", null);
    public static ViewType LAYOUT = new ViewType("RelativeLayout", null);
    public static ViewType NONE = new ViewType("NONE", null);
	
    private final String viewName;
    private final String viewProperty;

    /**
     * Constuctor.
     *
     * @param viewName      The type of view to use.
     * @param viewProperty  The view property.
     */
    public ViewType(String viewName, String viewProperty) {
        this.viewName = viewName;
        this.viewProperty = viewProperty;
    }

    /**
     * Test for equality
     * @param other
     * @return
     */
    public boolean equals(ViewType other) {
    	if (other == null) {
    		return false;
    	}
    	return equivalent(this.viewName, other.viewName()) && equivalent(this.viewProperty, other.viewProperty());
    }
    
    
    /**
     * Returns true if both Strings are null, or equal.
     * @param first
     * @param second
     * @return
     */
    public boolean equivalent(String first, String second) {
    	if (first == null && second == null) {
    		return true;
    	}
    	return first != null && first.equals(second);
    }
    
    /**
     * Retrieves the name of the view.
     *
     * @return  The name of the view.
     */
    public String viewName() {
        return viewName;
    }

    /**
     * Retrieves property of this view; typically the text in a text view or the
     * filename of the image.
     *
     * @return  The property of the view.
     */
    public String viewProperty() {
        return viewProperty;
    }
    
    public String toString() {
    	return "{Name: " + viewName + ", Property: " + viewProperty + "}";
    }
}

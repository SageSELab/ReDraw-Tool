package cs435.guiproto;

/**
 * Builders for components and layouts.
 * Give it the name of the view you're trying to make, and it'll give you the right class in return!
 * @author bdpowell, jrharless, nciko
 */
public class ViewBuilder {

	/**
	 * Make a new ViewGroup.
	 * @param type Name of the ViewGroup, e.g. "LinearLayout"
	 * @return A Layout, or null if unsupported.
	 */
	public static ViewGroup buildViewGroup(String type) {
		switch(type){
		case "ScrollView":
			return new ScrollView();
		case "LinearLayout":
		case "ListView":
		case "View":
		case "TableLayout":
		case "TableRow":
			return new LinearLayout();
		case "TabWidget":
			return new TabWidget();
		case "RelativeLayout":
			return new RelativeLayout();
		case "FrameLayout":
		case "TabHost":
		
			return new ViewGroup("FrameLayout");
		default:
			System.out.println("Unsupported layout type: " + type);
			return new DummyViewGroup(type);
		}
	}

	/**
	 * Make a new component.
	 * @param componentName Name of the node, e.g. "ImageView", "Button"
	 * @return A Component object. DummyComponent will be returned for unsupported types.
	 */
	public static View buildComponent(String componentName) {
		switch (componentName) {
		case "ImageButton":
			return new ComponentWithImage("ImageButton");
		case "ImageView":
			return new ComponentWithImage("ImageView");
		case "Button":
			return new ComponentWithColor("Button", "android:background", "android:textColor");
		case "TextView":
			return new TextView();
		case "Switch":
			return new Component("Switch");
		case "CheckBox":
			return new Component("CheckBox");
		case "EditText":
			return new Component("EditText");
			//return new ComponentWithColor("EditText", null, "android:textColor");
		default:
			System.out.println("Unsupported component type " + componentName);
			return new DummyComponent(componentName);
		}
	}

}

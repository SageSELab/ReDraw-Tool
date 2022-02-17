package cs435.guiproto;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A map of attributes, like color and shape, that are attached to a certain class of View.
 * 
 * Two maps from the same class of View can be merged according to certain rules.
 * A StyleFragment can also generate <style /> element that can be inserted into 
 * styles.xml
 * 
 * By merging and consolidating all the StyleFragments from disparate elements,
 * a style derived from all of them can be generated.
 * 
 * @author bdpowell
 *
 */
public class StyleFragment {
	
	private static int nextId = 0;
	private int id;
	
	private Map<String, String> stringAttrs;
	private Map<String, Color> colorAttrs;
	
	/**
	 * Create a StyleFragment for a view.
	 * 
	 * @param styleClass Class of the view this is paired to.
	 */
	public StyleFragment() {
		id = nextId++;
		stringAttrs = new HashMap<>();
		colorAttrs = new HashMap<>();
	}
	
	/**
	 * Add a general-purpose attribute to the StyleFragment.
	 * 
	 * These attributes can't be merged. If two StyleFragments have the
	 * same attribute, then the mergee's attribute is simply discarded.
	 * 
	 * @param name  Attribute name, e.g. "android:autoSizeText"
	 * @param value 
	 * @throws IllegalArgumentException if the attribute already exists
	 */
	public void addStringAttribute(String name, String value) {
		if (stringAttrs.containsKey(name)) {
			throw new IllegalArgumentException("Duplicate insert: {\"" + name + "\" -> \"" + value + "\"} already exists");
		}
		stringAttrs.put(name, value);
	}
	
	/**
	 * Add a color attribute.
	 * 
	 * When merged, the colors will be averaged.
	 * They will be converted to hexadecimal when output to XML.
	 * 
	 * @param name  Attribute name, e.g. "android:backgroundTint"
	 * @param value Color
	 * @throws IllegalArgumentException if the attribute already exists
	 */
	public void addColorAttribute(String name, Color value) {
		if (colorAttrs.containsKey(name)) {
			throw new IllegalArgumentException("Duplicate insert: {\"" + name + "\" -> \"" + value + "\"} already exists");
		}
		colorAttrs.put(name, value);
	}
	
	/**
	 * Automagically merge two style fragments.
	 * 
	 * To check if these two fragments are mergeable in the first place,
	 * use distance().
	 * 
	 * Merging will try to amalgamate certain attributes, like color, and will
	 * leave the rest untouched.
	 * 
	 * @param other The mergee. Will not be altered or consumed.
	 * @throws IllegalArgumentException If the two StyleFragments are of a different class.
	 * @return A new merged fragment
	 */
	public StyleFragment mergeFragments(StyleFragment other) {
		assert(distance(other) < Double.POSITIVE_INFINITY);
		
		StyleFragment merged = new StyleFragment();
		// Both styles should have the same stringAttrs
		merged.stringAttrs.putAll(stringAttrs);
		// colorAttrs are averaged
		for (String attr : colorAttrs.keySet()) {
			Color c1 = colorAttrs.get(attr);
			Color c2 = other.colorAttrs.get(attr);
			Color m = new Color(
					(c1.getRed() + c2.getRed()) / 2,
					(c1.getGreen() + c2.getGreen()) / 2,
					(c1.getBlue() + c2.getBlue()) / 2
			);
			merged.colorAttrs.put(attr, m);
		}
		
		return merged;
	}
	
	/**
	 * Convert the style fragment into a <style /> element.
	 * 
	 * This can be further inserted into styles.xml
	 * 
	 * @return
	 */
	public Element getStyleXML(Document doc) {
		/*
		 * The result should be:
		 * <style name="DummyStyle" parent="android:style/Widget.Dummy">
         *   <item name="sname1">svalue1</item>
         *   <item name="sname2">svalue2</item>
         * 	 <item name="red">#ff0000</item>
    	 * </style> 
		 */
		Element style = doc.createElement("style");
		style.setAttribute("name", "Style" + id);
		style.setAttribute("parent", "AppTheme");
		
		for (Map.Entry<String, String> entry : stringAttrs.entrySet()) {
			Element item = doc.createElement("item");
			item.setAttribute("name", entry.getKey());
			item.setTextContent(entry.getValue());
			
			style.appendChild(item);
		}
		
		for (Map.Entry<String, Color> entry : colorAttrs.entrySet()) {
			Element item = doc.createElement("item");
			item.setAttribute("name", entry.getKey());
			item.setTextContent(ImagesHelper.c2hex(entry.getValue()));
			
			style.appendChild(item);
		}
		
		return style;
	}

	/**
	 * Return the "distance" between two fragments.
	 * 
	 * The lower the distance between two fragments, the more
	 * similar they are in terms of attributes. A distance of
	 * 0 means that the two are identical; a distance of positive
	 * infinity means that they cannot be merged.
	 * 
	 * @param fragment
	 * @return
	 */
	public double distance(StyleFragment fragment) {
		final double failure = Double.POSITIVE_INFINITY;
		// Rule 1. If the fragments don't have the same attributes, they won't merge.
		if (!stringAttrs.keySet().equals(fragment.stringAttrs.keySet())) {
			return failure;
		}
		if (!colorAttrs.keySet().equals(fragment.colorAttrs.keySet())) {
			return failure;
		}
		// Rule 2. If the two fragments have string attributes with different values,
		// they won't merge.
		for (String attr : stringAttrs.keySet()) {
			String s1 = stringAttrs.get(attr);
			String s2 = fragment.stringAttrs.get(attr);
			if (s1 != s2)
				return failure;
		}
		// Rule 3. The distance between two fragments is the sum of the distances of their
		// colors, all other things being equal.
		double dist = 0.0;
		for (String attr : colorAttrs.keySet()) {
			Color c1 = colorAttrs.get(attr);
			Color c2 = fragment.colorAttrs.get(attr);
			double xd = (c2.getRed() - c1.getRed());
			double yd = (c2.getGreen() - c1.getGreen());
			double zd = (c2.getBlue() - c1.getBlue());
			dist += Math.sqrt(xd*xd + yd*yd + zd*zd);
		}
		return dist;
	}
	
	/**
	 * Return the name associated with this fragment, e.g. "@style/style1"
	 */
	public String getName() {
		return "@style/Style" + id;
	}
	
}

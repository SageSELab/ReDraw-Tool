package cs435.guiproto;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cs435.guiproto.Constants.RelAttributes;
import cs435.guiproto.Constants;

/**
 * Use the decorator pattern to extend the functionality of BaseComponent at run time, meaning we can assign
 * the correct parameter to the java generic arrayList dynamically. The class is just a thin wrapper for 
 * BaseComponent which gives it an appropriate arrayList for its spatial attributes
 * @author Michael
 *
 */
public class RelativeDecorator extends ViewDecorator {
	
	private HashMap<Constants.RelAttributes, String> map;
	
	public RelativeDecorator(View b){
		super(b.getName());
		view = b;
		map = new HashMap<Constants.RelAttributes, String>();
		marginMap = new HashMap<Constants.margin, Integer>();
	}
	
	public HashMap<Constants.RelAttributes, String> getMap(){
		return map;
	}
	
	public void setMap(HashMap<Constants.RelAttributes, String> m){
		map = m;
	}
	
	public void setMargins(HashMap<Constants.margin, Integer> mar){
		marginMap = mar;
	}
	
	public HashMap<Constants.margin, Integer> getMargins(){
		return marginMap;
	}
	
	/**
	 * Get the encapsulated XML element from the view, add some position attributes to it,
	 * and return it
	 */
	@Override
	public Element getLayoutElement(Document doc) {
		Element out = super.getLayoutElement(doc);
		
		// General attributes
	    Iterator<Map.Entry<Constants.RelAttributes, String>> it = map.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Constants.RelAttributes, String> attribute = (Entry<RelAttributes, String>)it.next();
	        out.setAttribute(attribute.getKey().toString(), attribute.getValue().toString());
	        it.remove(); // avoids a ConcurrentModificationException
	    }
	    
	    // For the margin maps
	    Iterator<Map.Entry<Constants.margin, Integer>> margins = marginMap.entrySet().iterator();
	    while (margins.hasNext()) {
	        Map.Entry<Constants.margin, Integer> attribute = (Entry<Constants.margin, Integer>)margins.next();
	        out.setAttribute(attribute.getKey().toString(), Float.toString(Constants.toDP(attribute.getValue())) + "dp");
	        margins.remove(); // avoids a ConcurrentModificationException
	    }
	    
	    //want children of relative layouts to wrap content
	    out.setAttribute("android:layout_height", "wrap_content");
	    out.setAttribute("android:layout_width", "wrap_content");
		
		return out;
	}
	
}

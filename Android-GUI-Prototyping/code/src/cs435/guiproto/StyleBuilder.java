package cs435.guiproto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Builds styles.xml, which determines how the app looks.
 * 
 * styles.xml is built from a list of Views, each containing
 * StyleFragments. These fragments each represent a <style /> element,
 * but they can be merged in certain cases to create more consistent
 * colors.
 * 
 * The current style-building code is inefficient and usually
 * fails to improve visual quality. If you're reading this in the
 * future, a good first stab at this project is to tear down this
 * code and try something new.
 * 
 * @author bdpowell
 *
 */
public class StyleBuilder {
	
	/*
	 * If two fragments have a distance greater than this,
	 * they will not be merged.
	 */
	private static final double MAX_DIST = 450.0;
	
	/**
	 * Build a styles.xml document from a supplied activity.
	 */
	public static Document buildStyleXML(List<View> views, boolean newAPI) {
		try {
			Document doc = DocumentBuilderFactory
					.newInstance()
					.newDocumentBuilder()
					.newDocument();
			
			Element root = doc.createElement("resources");
			doc.appendChild(root);
			
			/*
			 * Okay! So!
			 * We're going to merge styles by the similarity of their attributes,
			 * their "distance", if you will. To do this, we'll use agglomerative
			 * hierarchical clustering (see Wikipedia for more on that).
			 * 
			 * We stop when the clusters are too far apart from one another as determined
			 * by MAX_DIST.
			 */
			while (views.size() > 1) {
				// 1. Find the min distance between two clusters.
				double mind = Double.POSITIVE_INFINITY;
				int mini = -1;
				int minj = -1;
				for (int i=0; i<mini; i++) {
					for (int j=0; j<minj; j++) {
						if (i==j)
							continue;
						View vi, vj;
						vi = views.get(i);
						vj = views.get(j);
						double d = vi.getStyleDistance(vj);
						if (d < mind) {
							mini = i;
							minj = j;
							mind = d;
						}
					}
				}
				// 2. If that distance is greater than MAX_DIST, quit.
				if (mind > MAX_DIST) {
					break;
				}
				// 3. If not, then merge the two and go again
				views.get(mini).mergeStyleFragments(views.get(minj));
				views.remove(minj);
			}
			
			// The AppTheme is a global style
			Element appTheme = doc.createElement("style");
			appTheme.setAttribute("name", "AppTheme");
			if (newAPI){
				appTheme.setAttribute("parent", "@android:style/Theme.Material.Light.NoActionBar");
			}else{
				appTheme.setAttribute("parent", "@android:style/Theme.Light.NoTitleBar");
			}
			
			root.appendChild(appTheme);
			
			for (View view : views) {
				// Add individual <style /> blocks to styles.xml
				StyleFragment fragment = view.getStyleFragment();
				if (fragment != null)
					root.appendChild(fragment.getStyleXML(doc));
			}
			
			return doc;
		} catch (ParserConfigurationException e) {
			System.err.println("How did you do this?!");
			e.printStackTrace();
			return null;
		}
	}

}

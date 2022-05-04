package cs435.guiproto;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A component with an image, such as an ImageButton or ImageView. The image is derived
 * by looking at the Activity's screenshot.
 * @author bdpowell
 *
 */
public class ComponentWithImage extends Component {
	
	/**
	 * Dictates the names of the generated resource files.
	 */
	protected String drawableFileName;
	
	protected ComponentWithImage(String name) {
		super(name);
		drawableFileName = name.toLowerCase();
	}
	
	/**
	 * 
	 */
	@Override
	public Element getLayoutElement(Document doc) {
		Element e = super.getLayoutElement(doc);
		e.setAttribute("android:layout_width", width + "dp");
		e.setAttribute("android:layout_height", height + "dp");
		e.setAttribute("android:src", "@drawable/" + drawableFileName + getId());
		e.setAttribute("android:adjustViewBounds", "true");
		
		return e;
	}
	
	
	@Override
	public Element getLayoutElementAbsolute(Document doc, float marginLeft, float marginTop) {
		Element e = super.getLayoutElementAbsolute(doc, marginLeft, marginTop);
		e.setAttribute("android:layout_width", Constants.toDP(width) + "dp");
		e.setAttribute("android:layout_height", Constants.toDP(height) + "dp");
		e.setAttribute("android:src", "@drawable/" + drawableFileName + getId());
		e.setAttribute("android:adjustViewBounds", "true");
		
		return e;
	}
	
	/**
	 * Generate the ImageButton's, um, button by cropping a screenshot.
	 * @param root Project root directory.
	 * @throws IOException 
	 */
	@Override
	public void generateResources(Path root) throws IOException {
		super.generateResources(root);
		Path screens = activity.getScreenshotPath();
		ImagesHelper.cropImageAndSave(
				screens.toString(),
				root.resolve(getImageLocation()).toString(),
				x,
				y,
				width,
				height
		);
	}
	
	/**
	 * Return where this button's image should go in the new project.
	 * @param root The project's root folder.
	 * @return
	 */
	private Path getImageLocation() {
		return Paths.get("src/main/res/drawable/" + drawableFileName + getId() + ".png");
	}
	
}

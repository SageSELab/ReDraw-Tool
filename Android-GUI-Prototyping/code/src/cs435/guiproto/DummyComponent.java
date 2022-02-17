package cs435.guiproto;

/**
 * An invisible placeholder component. We'll use this whenever we encounter an unsupported component while parsing.
 * 
 * DummyViewGroup should be used for ViewGroups.
 * 
 * If we just discarded unsupported components, it'd mess up the alignment on things like LinearLayouts... we can't have that, can we?
 * @author bdpowell
 */
public class DummyComponent extends Component {

	private String originalName;
	
	public DummyComponent(String originalname) {
		super("View");
	}
	
	/**
	 * Get the class that this component should be (for debugging purposes).
	 * @return
	 */
	public String getOriginalName() {
		return originalName;
	}
	
}

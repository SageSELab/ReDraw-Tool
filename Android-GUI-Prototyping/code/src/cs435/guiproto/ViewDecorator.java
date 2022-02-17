package cs435.guiproto;

import java.io.IOException;
import java.nio.file.Path;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class for decorators that hold a single component.
 * This is used by things like RelativeLayout and DummyViewGroup,
 * which give the decorator additional attributes before printing
 * them to the screen.
 * 
 * Most of a ViewDecorator's methods are simply delegated to the View
 * below. This is ugly, but it works.
 * 
 * @author bdpowell, curcio
 */
public class ViewDecorator extends View {

	protected View view;

	public ViewDecorator(String name) {
		super(name);
	}

	@Override
	public void setActivity(ActivityHolder a) {
		super.setActivity(a);
		view.setActivity(a);
	}

	@Override
	public Element getLayoutElement(Document doc) {
		Element out = view.getLayoutElement(doc);
		return out;
	}
	
	@Override
	public Element getLayoutElementAbsolute(Document doc, float leftMargin, float topMargin){
		Element out = view.getLayoutElementAbsolute(doc, leftMargin, topMargin);
		return out;
	}
	
	@Override
	public void generateResources(Path root) throws IOException {
		view.generateResources(root);
	}
	
	/*
	 * Changes to this View are passed down to the underlying View.
	 * ... I dunno if any of this is reasonable, man. We need to refactor the class tree;
	 * we've got like a million variables and methods in View that we aren't using in here.
	 */
	
	public View getView() {
		return view;
	}
	
	@Override
	public void setValues(int x, int y, int w, int h) {
		getView().setValues(x, y, w, h);
	}

	@Override
	public void setValues(int x, int y, int w, int h, String t) {
		getView().setValues(x, y, w, h, t);
	}

	@Override
	public void setText(String string) {
		getView().setText(string);
	}

	@Override
	protected String getText() {
		return getView().getText();
	}

	@Override
	public int getId() {
		
		return getView().getId();
	}

	@Override
	public void setId(int x) {
		getView().setId(x);
	}

	@Override
	public String getName() {
		return getView().getName();
	}

	@Override
	public void setX(int x) {
		getView().setX(x);
	}

	@Override
	public void setY(int y) {
		getView().setY(y);
	}

	@Override
	public void setWidth(int w) {
		getView().setWidth(w);
	}

	@Override
	public void setHeight(int h) {
		getView().setHeight(h);
	}

	@Override
	public int getX() {
		return getView().getX();
	}

	@Override
	public int getY() {
		return getView().getY();
	}

	@Override
	public int getHeight() {
		return getView().getHeight();
	}

	@Override
	public int getWidth() {
		return getView().getWidth();
	}

	@Override
	public float getCenterX() {
		return getView().getCenterX();
	}

	@Override
	public float getCenterY() {
		return getView().getCenterY();
	}

	@Override
	public void setPaddingTop(int padding) {
		getView().setPaddingTop(padding);
	}

	@Override
	public void setPaddingBottom(int padding) {
		getView().setPaddingBottom(padding);
	}

	@Override
	public void setPaddingLeft(int padding) {
		getView().setPaddingLeft(padding);
	}

	@Override
	public void setPaddingRight(int padding) {
		getView().setPaddingRight(padding);
	}

	@Override
	public int getPaddingTop() {
		return getView().getPaddingTop();
	}

	@Override
	public int getPaddingBottom() {
		return getView().getPaddingBottom();
	}

	@Override
	public int getPaddingLeft() {
		return getView().getPaddingLeft();
	}

	@Override
	public int getPaddingRight() {
		return getView().getPaddingRight();
	}

	@Override
	public int compareTo(View o) {
		return getView().compareTo(o);
	}

	public String toString() {
		return "DummyDecorator containing " + getView();
	}
	
}

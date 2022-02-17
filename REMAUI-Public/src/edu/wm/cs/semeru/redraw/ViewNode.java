package edu.wm.cs.semeru.redraw;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

import org.opencv.core.Rect;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import edu.wm.cs.semeru.redraw.classifiers.Classifier;
import edu.wm.cs.semeru.redraw.ocr.OCRTextBlock;

/**
 * A representation of a node in an Android view hierarchy.
 * The tree is initially constructed such that it contains ImageViews
 * exclusively.  Text- and ListViews are added via the methods {@link
 * #addTextBlock()} and {@link #identifyAndInsertLists()}.
 *
 * @author William T. Hollingsworth
 * @author Steve Walker
 * @see edu.wm.cs.semeru.redraw.ViewHierarchy The abstraction for the tree root.
 */
public class ViewNode {
    protected ArrayList<ViewNode> children;
    protected ViewNode parent;
    protected ViewType viewType;
    protected Rect bounds;
    protected String data;

    // assumes the rect is in absolute position. Will look at parent and make it relative
    /**
     * Constructor; assumes that the bounding rectangle is in absolute
     * coordinates and will be made relative at export time.
     *
     * @param parent    The parent of this node in the tree.
     * @param viewType  The type of view as used in the Android layout.xml.
     * @param bounds    The bounding rectangle of this GUI element represented
     *                  by this node.
     * @param data      Any data associated with the view.  Typically either the
     *                  text or the name of the file containing the GUI element
     *                  from the input screenshot.
     */
    public ViewNode(ViewNode parent, ViewType viewType, Rect bounds, String data) {
        this.parent = parent;
        this.viewType = viewType;
        this.data = data;
        this.bounds = bounds;
        children = new ArrayList<ViewNode>();
    }

    /**
     * Gets the number of children associated with this node.
     *
     * @return  The number of children.
     */
    public int getNumChildren() {
        return children.size();
    }

    /**
     * Retreives the child at the given index without performing any bounds
     * checking on the argument.
     *
     * @param index The index of the node to retrieve.
     * @return      The `index`th child of this node.
     */
    public ViewNode childAtIndex(int index) {
        return children.get(index);
    }

    /**
     * Add a child to this node.
     *
     * @param viewType  The type of view as used in the Android layout.xml.
     * @param bounds    The bounding rectangle of the GUI element represented by
     *                  this node.
     * @param data      Any data associated with this view.  Typically either
     *                  text or the name of the file containing the GUI element
     *                  from the input screenshot.
     */
    public void addChild(ViewType viewType, Rect bounds, String data) {
        children.add(new ViewNode(this, viewType, bounds, data));
    }

    /**
     * Tests whether a node is contained within a block of text detected
     * by an OCR engine.
     *
     * @param tb    A block of text detected by an OCR engine.
     * @return      A boolean value indicating containment.
     */
    public boolean isContainedBy(OCRTextBlock tb) {
        return tb.contains(bounds);
    }

    /**
     * Tests whether a node intersects a block of text detected by an OCR
     * engine.
     *
     * @param tb    A block of text detected by an OCR engine.
     * @return      A boolean value indicating whether there is an intersection.
     */
    public boolean doesIntersect(OCRTextBlock tb) {
        return tb.intersects(bounds);
    }

    /**
     * Adds a text block to the tree, using a recursive search to find its proper location.
     *
     * @param tb    A block of words detected by an OCR Engine.
     * @return      A boolean value indicating the success of the operation.
     */
    public boolean addTextBlock(OCRTextBlock tb) {
        // short circuit the method if it can't possibly go here
        if (parent != null && !doesIntersect(tb)) {
            return false;
        }

        List<ViewNode> containedChildren = new ArrayList<ViewNode>();

        // can we add it here?
        for (ViewNode child: children) {
            if (child.isContainedBy(tb)) {
                containedChildren.add(child);
            }
        }

        if (!containedChildren.isEmpty()) {
            children.removeAll(containedChildren);
            children.add(new ViewNode(this, ViewType.TEXT, tb.getBoundingBox(), tb.getText()));
            return true;
        }

        // DFS all children for the proper location
        for (ViewNode child: children) {
            if (child.addTextBlock(tb)) {
                return true;
            }
        }
        return false; // to make `javac` happy.
    }

    /**
     * Test the equivalence of two view nodes.  Note that, as this is used for collapsing lists,
     * it isn't testing for actual equality.
     *
     * @param other Some other ViewNode.
     * @return      A boolean indicating the structural similarity of this ViewNode to the other one.
     */
    public boolean isEquivalent(ViewNode other) {
        // trivial case
        if (this.children.size() != other.children.size() || this.children.isEmpty() || other.children.isEmpty()) {
            return false;
        }

        // return true iff all types are the same
        for (int i = 0; i < this.children.size(); i++) {
            if (!this.children.get(i).viewType.equals(other.children.get(i).viewType)){
                return false;
            }
        }

        return true;
    }

    /**
     * Test for strict equality.  Just use bounding boxes for now.  Mostly implemented
     * for {@link List#contains()} call in {@link #identifyAndInsertLists()}.
     *
     * @param other Some other ViewNode.
     * @return      A boolean value indicating equality of the bounds of the two nodes.
     * @see #collapse()
     */
    public boolean equals(ViewNode other) {
        return this.bounds.equals(other.bounds);
    }

    /**
     * Heuristically classifies structurally similar subtrees as lists, and
     * wraps those similar children in ListView and RelativeLayout nodes.
     */
    public void identifyAndInsertLists() {
        // base case
        if (children.isEmpty()) {
            return;
        }

        // recurse first
        for (ViewNode child: children) {
            child.identifyAndInsertLists();
        }

        List<ViewNode> structurallySimilarChildren = new ArrayList<ViewNode>();

        // find every pair of children
        for (int i = 0; i < children.size(); i++) {
            for (int j = i+1; j < children.size(); j++) {
                ViewNode firstChild = children.get(i);
                ViewNode secondChild = children.get(j);
                // if the subtrees are equivalent
                if (firstChild.isEquivalent(secondChild)) {
                    // add them to the list of similar children if they aren't already there
                    if (!structurallySimilarChildren.contains(firstChild)) {
                        structurallySimilarChildren.add(firstChild);
                    }
                    if (!structurallySimilarChildren.contains(secondChild)) {
                        structurallySimilarChildren.add(secondChild);
                    }
                }
            }
        }

        // leave if there is nothing to collapse
        if (!structurallySimilarChildren.isEmpty()) {
            return;
        }

        // compute a bounding rectangle for the similar children
        List<Rect> boundingBoxes = new ArrayList<Rect>(); ;
        for (ViewNode child: structurallySimilarChildren) {
            boundingBoxes.add(child.bounds);
        }
        Rect globalBounds = Utilities.cvRectUnion(boundingBoxes);

        // remove the similar children
        children.removeAll(structurallySimilarChildren);

        // create the list view
        addChild(ViewType.LIST, globalBounds, "");
        ViewNode listView = children.get(children.size()-1);

        // add the relative layout
        listView.addChild(ViewType.LAYOUT, globalBounds, "");
        ViewNode relativeLayout = listView.children.get(listView.children.size()-1);

        // add the chillens back in
        for (ViewNode child: structurallySimilarChildren) {
            relativeLayout.addChild(child.viewType, child.bounds, child.data);
        }
    }

    /**
     * Performs a traversal of the tree in which every image view is cropped from
     * the source image.
     *
     * @param outputRoot    The output location.
     * @param original      The source image.
     */
    public void exportImages(String outputRoot, Mat original) {
        // short circuit if there's no need to do anything
        if (viewType.equals(ViewType.IMAGE)) {
            // create output dir
        	String path = outputRoot + File.separator + "drawable" + File.separator;
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdir();
            }

            // crop and export the image
            Mat cropped = new Mat(original, bounds);
            Highgui.imwrite(path + data + ".png", cropped);
        }

        // recurse on the children
        for (ViewNode child: children) {
            child.exportImages(outputRoot, original);
        }
    }
    
    
    /**
     * Recursively identifies the type of each component from ML classifier
     */
    public void identifyComponentTypes(Classifier cl) {
    	viewType = cl.getComponentType(this);
    	for (ViewNode child : children) {
    		child.identifyComponentTypes(cl);
    	}
    }
    
    public boolean isType(ViewType type) {
    	return viewType.equals(type);
    }
    
    public String getData() {
    	return data;
    }
    
    public ViewType getViewType() {
    	return viewType;
    }
    
    @Override
    public String toString() {
		return "{Type: " + viewType.toString() + ", Data: " + data + " , bounds: " + bounds + "}";
    }
}

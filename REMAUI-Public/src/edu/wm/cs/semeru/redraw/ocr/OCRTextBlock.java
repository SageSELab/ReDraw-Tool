package edu.wm.cs.semeru.redraw.ocr;

import org.opencv.core.Rect;

import edu.wm.cs.semeru.redraw.Utilities;
import edu.wm.cs.semeru.redraw.ViewNode;

/**
 * A simple Java bean class representing a text block as detected by OCR.
 *
 * @author William T. Hollingsworth
 */
public class OCRTextBlock {
    private Rect bbox;
    private String text;

    /**
     * Constructor; sets the bounding box of this text block, which is currently
     * immutable.
     *
     * @param bbox The bounding box of this text block.
     */
    public OCRTextBlock(Rect bbox, String text) {
        this.bbox = bbox;
        this.text = text;
    }

    /**
     * Gets the bounding box of this text block.
     *
     * @return The bounding box of this text block.
     */
    public Rect getBoundingBox() {
        return bbox;
    }

    /**
     * Gets the text contained in this text block.
     *
     * @return The full text of the text block.
     */
    public String getText() {
        return text;
    }

    /**
     * Test whether a given rectangle is contained by this text block.
     * Note that the method returns true even if the containment is only
     * approximate.  This is done to account for tolerable differences in the
     * bounding rectangles detected by OCR, and by OpenCV after the dilation
     * step has increased the bounds of the vision boxes.
     * @see edu.wm.cs.semeru.redraw.ViewNode#identifyAndInsertLists()
     *
     * @param rect  An arbitrary rectangle.
     * @return      True if the rectangle is contained by the text block, false otherwise.
     */
    public boolean contains(Rect rect) {
        // check if actually contained or there is a tolerable difference
        boolean left = bbox.x <= rect.x || Math.abs(bbox.x-rect.x) <= 10;
        boolean right = bbox.x+bbox.width >= rect.x+rect.width || Math.abs(bbox.x+bbox.width - (rect.x+rect.width)) <= 10;
        boolean top = bbox.y <= rect.y || Math.abs(bbox.y-rect.y) <= 10;
        boolean bottom = bbox.y+bbox.height >= rect.y+rect.height || Math.abs(bbox.y+bbox.height - (rect.y+rect.height)) <= 10;

        return left && top && right && bottom;
    }

    /**
     * Test whether a given rectangle intersects a text block.
     *
     * @param rect  An arbitrary rectangle.
     * @return      True if the rectangle intersects the text block, false otherwise.
     */
    public boolean intersects(Rect rect) {
        return Utilities.cvRectIntersects(bbox, rect);
    }
}

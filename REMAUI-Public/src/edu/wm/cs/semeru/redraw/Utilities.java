package edu.wm.cs.semeru.redraw;

import java.util.List;

import org.opencv.core.Rect;

/**
 * A static utility function class
 *
 * @author William T. Hollingsworth
 */
public final class Utilities {
	
	/**
	 * Test for <i>any</i> intersection between the given rectangles.
	 * Implemented with operator& overload in OpenCV, so no access to it
	 * in Java bindings.
	 * 
	 * @param lhs	An arbitrary rectangle
	 * @param rhs 	Another arbitrary rectangle
	 * @return	  	True if the rectangles intersect, false otherwise
	 */
	public static boolean cvRectIntersects(Rect lhs, Rect rhs) {
	    return !(lhs.x > rhs.x + rhs.width
	            && lhs.x + lhs.width < rhs.x
	            && lhs.y > rhs.y + rhs.height
	            && lhs.y + lhs.height < rhs.y);
	}
	
	/**
	 * Test whether lhs contains the rectangle rhs.
	 * 
	 * @param lhs	An arbitrary rectangle
	 * @param rhs 	Another arbitrary rectangle
	 * @return	  	True if the first retangle completely contains the second,
	 * 				false otherwise.
	 */
	public static boolean cvRectContains(Rect lhs, Rect rhs) {
	    return lhs.x <= rhs.x && lhs.y <= rhs.y
	            && lhs.x + lhs.width >= rhs.x + rhs.width
	            && lhs.y + lhs.height >= rhs.y + rhs.height;
	}

	/**
	 * Creates a rectangle representing the intersection of the input.
	 * 
	 * @param lhs  An arbitrary rectangle.
	 * @param rhs  Another arbitrary rectangle.
	 * @return     A rectangle representing the intersection, if it exists,
	 *             null otherwise.
	 */
	public static Rect cvRectIntersection(Rect lhs, Rect rhs) {
	    // sanity check
	    if (!Utilities.cvRectIntersects(lhs,  rhs)) {
	        return null;
	    }
	   
	    int x, y, width, height;
	   
	    // get the points of intersection in the x dimension
	    if (lhs.x + lhs.width > rhs.x + rhs.width) {
	        x = lhs.x;
	        width = (rhs.x+rhs.width) - lhs.x; 
	    }
	    else {
	        x = rhs.x;
	        width = (lhs.x+lhs.width) - rhs.x;
	    }
	   
	    // get the points of intersections in the y dimension
	    if (lhs.y + lhs.height > rhs.y + rhs.height) {
	        y = rhs.y;
	        height = (rhs.y+rhs.height) - lhs.y; 
	    }
	    else {
	        y = lhs.y;
	        height = (lhs.y+lhs.height) - rhs.y; 
	    }
	    
	    return new Rect(x, y, width, height);
	}

	/**
	 * Construct a bounding rectangle for a set of bounding boxes.
	 * 
	 * @param rects    A list of bounding boxes.
	 * @return         A rectangle bounding every box in the input.
	 */
	public static Rect cvRectUnion(List<Rect> rects) {
	    // the trivial case
	    if (rects == null || rects.size() == 0) {
	        return null;
	    }
	   
	    // init params
	    int minX = rects.get(0).x;
	    int maxX = rects.get(0).x + rects.get(0).width;
	    int minY = rects.get(0).y;
	    int maxY = rects.get(0).y + rects.get(0).height;
	   
	    // find global bounds
	    for (Rect rect: rects) {
	        if (rect.x < minX) {
	            minX = rect.x;
	        }
	        if (rect.y < minY) {
	            minY = rect.y;
	        }
	        if (rect.x+rect.width > maxX) {
	            maxX = rect.x+rect.width;
	        }
	        if (rect.y+rect.height > maxY) {
	            maxY = rect.y+rect.height;
	        }
	    }
	   
	    // done
	    return new Rect(minX, minY, maxX, maxY);
	}
}

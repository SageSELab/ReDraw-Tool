package edu.wm.cs.semeru.redraw;

import java.util.List;
import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

/**
 * A simple Java Bean class representing a contour detected by
 * a computer vision algorithm.
 *  
 * @author William T. Hollingsworth
 */

public class CVContours {
	private List<MatOfPoint> contours;
	private Mat hierarchy;

	/**
	 * Constructor; initializes the contours and contour hierarchy.  Note that they
	 * are at present immutable.
	 * 
	 * @param contours  The actual contours detected by OpenCV.
	 * @param hierarchy The hierarchy of contour containment.
	 */
	public CVContours(List<MatOfPoint> contours, Mat hierarchy) {
		this.contours = contours;
		this.hierarchy = hierarchy;
	}

	/**
	 * Gets the hierarchy of contours.
	 * 
	 * @return The hierarchy matrix.
	 */
	public Mat getHierarchy() {
		return hierarchy;
	}

	/**
	 * Gets the contours detected by OpenCV.
	 * 
	 * @return The contours.
	 */
	public List<MatOfPoint> getContours() {
		return contours;
	}

	/**
	 * Returns the indices of the leaf elements in the hierarchy tree.
	 * 
	 * @return A list of the indices corresponding to the leaves in the tree.
	 * @see edu.wm.cs.semeru.redraw.MergeOperation#getFilteredOCRWords()
	 */
    public List<Integer> getVisionBoxLeafIndices() {
        List<Integer> leaves = new ArrayList<Integer>();
        
        // add all contours with no children
        for (int i = 0; i < hierarchy.cols(); i++) {
            if (hierarchy.get(0, i)[2] == -1) { 
                leaves.add(i); 
            } 
        }
        
        return leaves;
    }
}

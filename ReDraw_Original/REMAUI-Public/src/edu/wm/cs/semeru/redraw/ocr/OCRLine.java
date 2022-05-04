package edu.wm.cs.semeru.redraw.ocr;
import java.util.List;
import java.util.ArrayList;
import org.opencv.core.Rect;

import edu.wm.cs.semeru.redraw.Utilities;

/**
 * A simple Java Bean class representing a line detected by an OCR
 * application.
 *
 * @author William T. Hollingsworth
 */
public class OCRLine {
	private Rect bbox;
	private List<OCRWord> containedWords;

	/**
	 * Constructor; sets the bounding box of this line and initializes the
	 * list of words it contains.
	 *
	 * @param bbox The bounding box of this line.
	 */
	public OCRLine (Rect bbox) {
		this.bbox = bbox;
		containedWords = new ArrayList<OCRWord>();
	}

	/**
	 * Gets the bounding box of this line.
	 *
	 * @return The bounding box of this line.
	 */
	public Rect getBoundingBox() {
		return bbox;
	} /**
	 * Gets the list of words that this line contains.
	 *
	 * @return A list of all words that are part of this line.
	 * @see edu.wm.cs.semeru.redraw.ocr.OCRWord
	 */
	public List<OCRWord> getContainedWords() {
	    return containedWords;
	}

	/**
	 * Adds the given word only if it is contained within this line.
	 *
	 * @param word The OCRWord to add.
	 * @return     True if the word was contained by this line and successfully added,
	 *             false otherwise.
	 * @see edu.wm.cs.semeru.redraw.ocr.OCRWord
	 */
	public boolean addWord(OCRWord word) {
	    // sanity check
	    if (!contains(word)) {
	        return false;
	    }

	    // safe to add
	    containedWords.add(word);
	    return true;
	}

	/**
	 * Convenience method to add multiple words.
	 *
	 * @param words A list of words that should be contained by this line.
	 * @see edu.wm.cs.semeru.redraw.ocr.OCRWord
	 */
	public void addWords(List<OCRWord> words) {
	    for (OCRWord word : words) {
	       this.addWord(word);
	    }
	}

	/**
	 * Tests whether this OCRLine contains any point in the given OCRWord.
	 * @param word The word used in the test.
	 * @return     True if any point in the word's bounding box is contained
	 *             by this OCRLine, false otherwise.
	 * @see edu.wm.cs.semeru.redraw.ocr.OCRWord
	 */
	public boolean contains(OCRWord word) {
	    return Utilities.cvRectContains(bbox, word.getBoundingBox());
	}

	/**
	 * Remove from this line any words that have been invalidated.
	 */
	public void removeInvalidWords() {
	    for (int i = containedWords.size()-1; i >= 0; --i) {
	        if (!containedWords.get(i).isValid()) {
	            containedWords.remove(i);
	        }
	    }
	}

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[x:" + bbox.x + ",y:" + bbox.y + ",w:" + bbox.width + ",h:" + bbox.height + "] " + containedWords;
    }
	
}

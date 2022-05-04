package edu.wm.cs.semeru.redraw;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.opencv.core.Rect;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

import edu.wm.cs.semeru.redraw.ocr.OCRWord;

/**
 * This class is responsible for merging the results obtained from OCR text
 * recognition with the results obtained from OpenCV, as defined in Table II
 * of Nguyen and Csallner.
 *
 * @author William T. Hollingsworth
 * @author Steven Walker.
 * @see edu.wm.cs.semeru.redraw.CVContours
 * @see edu.wm.cs.semeru.redraw.ocr.OCRWord
 */

// TODO: perhaps this class should be package private?
public class MergeOperation {
    private List<OCRWord> words;
    private CVContours contours;
    private int imgHeight;
    private int imgWidth;

    private List<Integer> leaves;
    private List<MatOfPoint> contourList;

    /**
     * Constructor.
     *
     * @param words A list of words obtained using OCR text recognition.
     * @param contours A bundle of the contours and the hierarchy of those contours,
     *                 as detected by OpenCV.
     */
    public MergeOperation(List<OCRWord> words, CVContours contours) {
        this.words = words;
        this.contours = contours;
    }

    /**
     * Sets the image dimensions.  Used in determining alignment.
     *
     * @param imgWidth The width of the input image.
     * @param imgHeight The height of the input image.
     * @see #doMultiplesCheck()
     */
    public void setImageDimensions(int imgWidth, int imgHeight) {
      this.imgWidth = imgWidth;
      this.imgHeight = imgHeight;
    }

    /**
     * A wrapper method for the individual heuristics.
     *
     * @return The OCRWords not invalidated by the heuristics.
     */
    public List<OCRWord> getFilteredOCRWords() {
        leaves = contours.getVisionBoxLeafIndices();
        contourList = contours.getContours();

        // individual checks
//        doContainmentByLeafVisionBoxCheck();
//        doNonLeafVisionBoxContainmentCheck();
//        doSingleVisionBoxContainmentCheck();
//        doVerticalAlignmentCheck();
//        doHorizontalAlignmentCheck();
//        doRatioCheck();
//        doMultiplesCheck();

        // check validity and remove those words that have been declared invalid
        //doMajorityInvalidatedCheck();
        removeInvalidatedWords();

        return words;
    }

    /**
     * Heuristic II.1: Word aligns vertically and is overlapped >= 70% with
     *                 >= 2 vision boxes.
     */
    private void doVerticalAlignmentCheck() {
        List<Rect> alignedContours = new ArrayList<Rect>();

        // for every word
        for (OCRWord word : words) {
            if (!word.isValid()) {
                continue;
            }

            // for every contour
            for (int j = 0; j < contourList.size(); j++) {
                Rect rect = Imgproc.boundingRect(contourList.get(j));

                // does the word align and intersect with the contour?
                if (word.isVerticallyAlignedWith(rect) && word.intersects(rect)) {
                    Rect intersection = Utilities.cvRectIntersection(word.getBoundingBox(), rect);

                    // is the intersection large?
                    if (intersection.area() / rect.area() >= 0.70) {
                        if (alignedContours.size() == 0) {
                            alignedContours.add(rect);
                        }
                        else {
                            boolean noVisionBoxOverlap = false;

                            for (int k = 0; k < alignedContours.size(); k++) {
                                if (!Utilities.cvRectIntersects(alignedContours.get(k), rect)) {
                                    noVisionBoxOverlap = true;
                                    break;
                                }
                            }

                            if (noVisionBoxOverlap) {
                                word.setValid(false);
                                break;
                            }
                            else {
                                alignedContours.add(rect);
                            }
                        }
                    }
                }
            }

            // clear out for the next word
            alignedContours.clear();
        }
    }

    /**
     * Heuristic II.2: Word aligns horizontally and is overlapped >= 70% with >=
     *                 2 vision boxes.
     */
    private void doHorizontalAlignmentCheck() {
        List<Rect> alignedContours = new ArrayList<Rect>();

        // for every word
        for (OCRWord word : words) {
            if (!word.isValid()) {
                continue;
            }

            // for every contour
            for (int j = 0; j < contourList.size(); j++) {
                Rect rect = Imgproc.boundingRect(contourList.get(j));

                // does the word align and intersect with the contour?
                if (word.isHorizontallyAlignedWith(rect) && word.intersects(rect)) {
                    Rect intersection = Utilities.cvRectIntersection(word.getBoundingBox(), rect);

                    // is the intersection large?
                    if (intersection.area() / rect.area() >= 0.70) {
                        if (alignedContours.size() == 0) {
                            alignedContours.add(rect);
                        }
                        else {
                            boolean largeSeparation = false;

                            for (int k = 0; k < alignedContours.size(); k++) {
                                if (!Utilities.cvRectIntersects(alignedContours.get(k), rect)) {
                                    Rect other = alignedContours.get(k);
                                    if (!Utilities.cvRectIntersects(other, rect)) {
                                        int largerHeight = Math.max(rect.height, other.height);
                                        int distance1 = Math.abs(rect.y + rect.height - other.y);
                                        int distance2 = Math.abs(other.y + other.height - rect.x);

                                        if (distance1 > largerHeight && distance2 > largerHeight) {
                                            largeSeparation = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (largeSeparation) {
                                word.setValid(false);
                                break;
                            }
                            else {
                                alignedContours.add(rect);
                            }
                        }
                    }
                }
            }

            // clear out for the next word
            alignedContours.clear();
        }
    }

    /**
     * Heuristic II.3:  Word contains a non-leaf vision box.
     *
     */
    private void doNonLeafVisionBoxContainmentCheck() {
        for (int i = words.size() - 1; i >= 0; i--) {
            // for every word, does it contain any non-leaf vision box?
            OCRWord word = words.get(i);

            if (!word.isValid()) {
                continue;
            }

            for (int j = 0; j < contourList.size(); j++) {
                MatOfPoint mop = contourList.get(j);
                if (!leaves.contains(j) && word.contains(Imgproc.boundingRect(mop))) {
                    word.setValid(false);
                }
            }
        }
    }

    /**
     * Heuristic III.4: Word contains only 1 vision box.
     *
     */
    private void doSingleVisionBoxContainmentCheck() {
        for (OCRWord word : words) {
            int numContainedVisionBoxes = 0;
            int lastContainedVisionBox = -1;

            if (!word.isValid()) {
                continue;
            }

            // for every word, check all vision boxes for containment
            for (int j = 0; j < contourList.size(); j++) {
                MatOfPoint mop = contourList.get(j);
                if (word.contains(Imgproc.boundingRect(mop))) {
                    numContainedVisionBoxes++;
                    lastContainedVisionBox = j;
                }
            }

            // if only 1 contained...
            if (numContainedVisionBoxes == 1) {
                Rect visionBox = Imgproc.boundingRect(contourList.get(lastContainedVisionBox));
                Rect ocrBox = word.getBoundingBox();

                // remove that word if the vision box is much smaller
                if (visionBox.area() < 0.2 * ocrBox.area()) {
                    word.setValid(false);
                }
            }
        }
    }

    /**
     * Heuristic II.5: Non overlapped vision box contains only 1 word.
     *
     */
    private void doContainmentByLeafVisionBoxCheck() {
        // for all leaves, check for intersection will all other leaves
        for (int i = 0; i < leaves.size(); i++) {
            Rect visionBox1 = Imgproc.boundingRect(contourList.get(i));
            boolean nonoverlapped = true;
            for (int j = 0; j < leaves.size(); j++) {
                Rect visionBox2 = Imgproc.boundingRect(contourList.get(j));
                if (Utilities.cvRectIntersects(visionBox1, visionBox2)) {
                    nonoverlapped = false;
                }
            }

            // if none
            if (nonoverlapped) {
                int numContainedWords = 0;
                OCRWord lastContainedWord = null;

                // check all words and count number contained by this leaf
                for (OCRWord word : words) {
                    if (!word.isValid()) {
                        continue;
                    }

                    Rect ocrBox = word.getBoundingBox();
                    if (Utilities.cvRectContains(visionBox1, ocrBox)) {
                        numContainedWords++;
                        lastContainedWord = word;
                    }
                }

                // if only one, remove it
                if (numContainedWords == 1) {
                    lastContainedWord.setValid(false);
                }
            }
        }
    }

    /**
     * Heuristic II.6:  For leaf vision box words that are > 50% invalidated,
     *                  invalidate all the others.
     */
    private void doMajorityInvalidatedCheck() {
        List<OCRWord> containedWords = new ArrayList<>();

        // for every leaf
        for (int i = 0; i < leaves.size(); i++) {
            Rect visionBox = Imgproc.boundingRect(contourList.get(i));
            containedWords.clear();
            int invalidWordCount = 0;

            // look at all the words
            for (OCRWord word : words) {
                Rect wordBox = word.getBoundingBox();

                // is it part of this vision box?
                if (Utilities.cvRectContains(visionBox, wordBox)) {

                    // update the count for the current vision leaf
                    containedWords.add(word);
                    if (!word.isValid()) {
                        invalidWordCount++;
                    }
                }
            }

            // use the invalidated count to update any other words in this leaf
            if (containedWords.size() > 0 && (double) invalidWordCount / (double) containedWords.size() > 0.50) {
                for (OCRWord word : words) {
                    word.setValid(false);
                }
            }
        }
    }


    /**
     * Heuristic II.7: If > 3 words are the same text and size, aligned left,
     * right, top, or bottom, each has < 0.9 confidence, and are non-dictionary
     * words.
     *
     * We choose to omit the dictionary words check, as the app developer
     * may intentionally choose non-dictionary words for things such as
     * the application name.
     */
    private void doMultiplesCheck() {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        // for every word
        for (OCRWord word : words) {
            String key = word.getText() + word.getFontSize();
            Integer value = 0;

            int top = word.getBoundingBox().x;
            int left = word.getBoundingBox().y;
            int bottom = top+word.getBoundingBox().height;
            int right = left+word.getBoundingBox().width;

            // put it in the hash map if it meets the conditions we're looking for
            if (word.getConfidence() >= 0.9 &&
                bottom <= imgHeight*0.1 || top >= imgHeight*0.9 ||
                right <= imgWidth*0.1 || left >= imgWidth*0.9) {
                if (!map.containsKey(key)) {
                    value = 1;
                }
                else {
                    value = map.get(key) + 1;
                }

                map.put(key, value);
            }
        }

        // for every word in the map
        for (String key : map.keySet()) {
            if (map.get(key) > 3) {
                Iterator<OCRWord> it = words.listIterator();
                while (it.hasNext()) {
                    OCRWord word = it.next();
                    int top = word.getBoundingBox().x;
                    int left = word.getBoundingBox().y;
                    int bottom = top+word.getBoundingBox().height;
                    int right = left+word.getBoundingBox().width;

                    // invalidate the word if it satisfies the conditions -- this ensures that if the
                    // same word is used in the center, that that word is not invalidated
                    if (word.getConfidence() >= 0.9 && word.getText() + word.getFontSize() == key &&
                            bottom <= imgHeight*0.1 || top >= imgHeight*0.9 ||
                            right <= imgWidth*0.1 || left >= imgWidth*0.9) {
                        word.setValid(false);
                    }
                }
            }
        }
    }

    /**
     * Heuristic II.8: Invalidate any words whose width and height ratios relative to
     *                 its corresponding vision box does not fall within a specific
     *                 range.
     */
    private void doRatioCheck() {
        // for every leaf
        for (int i = 0; i < leaves.size(); i++) {
            Rect visionBox = Imgproc.boundingRect(contourList.get(i));

            // check every word
            for (OCRWord word : words) {
                if (!word.isValid()) {
                    continue;
                }

                // is it part of this vision box?
                Rect wordBox = word.getBoundingBox();
                if (Utilities.cvRectContains(visionBox, wordBox)) {
                    // get the ratios
                    double widthRatio = (double) wordBox.width / (double) visionBox.width;
                    double heightRatio = (double) wordBox.height / (double) visionBox.height;

                    double m = Math.min(widthRatio, heightRatio);
                    double M = Math.max(widthRatio, heightRatio);

                    // invalidate if not reasonable
                    if (M < 0.4 || (M < 0.7 && m < 0.4) || (M >= 0.7 && m < 0.2)) {
                        word.setValid(false);
                    }
                }
            }
        }
    }

    /**
     * Remove any words detected as invalid by the above heuristics.
     *
     */
    private void removeInvalidatedWords() {
        Iterator<OCRWord> iterator = words.iterator();

        while (iterator.hasNext()) {
            OCRWord word = iterator.next();
            if (!word.isValid()) {
                iterator.remove();
            }
        }
    }
}

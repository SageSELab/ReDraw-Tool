package edu.wm.cs.semeru.redraw.ocr;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.opencv.core.Rect;

/**
 * This class transforms the words in an OCRLine into text blocks as defined by
 * Nguyen and Csallner.
 *
 * @author William T. Hollingsworth
 * @see edu.wm.cs.semeru.redraw.ocr.OCRLine
 * @see edu.wm.cs.semeru.redraw.ocr.OCRWord
 */
public class OCRTextBlockGenerator {
    private OCRLine line;

    /**
     * Default constructor.
     *
     */
    public OCRTextBlockGenerator() {
        line = null;
    }

    /**
     * Constructor that additionally sets the initial line.
     *
     * @param line The OCRLine for which to detect text blocks.
     */
    public OCRTextBlockGenerator(OCRLine line) {
        reset(line);
    }

    /**
     * Updates the current line and sorts the words contained by location.
     *
     * @param line The next line to examine.
     */
    public void reset(OCRLine line) {
        this.line = line;

        // sort the words in the line such that they are in order from left to right
        Collections.sort(line.getContainedWords(), new Comparator<OCRWord>() {
            @Override
            public int compare(OCRWord first, OCRWord second) {
                return Integer.compare(first.getBoundingBox().x, second.getBoundingBox().x);
            }
        });
    }

    /**
     * Examines the current OCRLine and finds all text blocks.
     *
     * @return A list of text blocks found in the current line.
     */
    public List<OCRTextBlock> getTextBlocks() {
        // initialize the text block list
        List<OCRTextBlock> textBlocks = new ArrayList<OCRTextBlock>();

        // get all the words from the line and do a sanity check
        List<OCRWord> words = line.getContainedWords();
        if (line == null || words.size() == 0) {
            return textBlocks;
        }

        // for every adjacent pair of words in the list...
        int blockStart = 0;
        String text = "";

        int x = words.get(0).getBoundingBox().x;
        int y = words.get(0).getBoundingBox().y;
        int height = words.get(0).getBoundingBox().height;
        int width = words.get(0).getBoundingBox().width;

        for (int i = 1; i < words.size(); i++) {
            Rect left = words.get(i - 1).getBoundingBox();
            Rect right = words.get(i).getBoundingBox();
            text += words.get(i-1).getText() + " ";

            x = Math.min(x, left.x);
            y = Math.max(y, left.y);
            height = Math.max(height, left.height);
            width = (left.x+left.width) - words.get(blockStart).getBoundingBox().x;

            // does the gap between the words exceed a threshold?
            if (right.x - (left.x + left.width) > words.get(0).getBoundingBox().height) {
                text = text.trim(); // rm the trailing space
                textBlocks.add(new OCRTextBlock(new Rect(x, y, width, height), text));

                text = "";
                blockStart = i;

                x = right.x;
                y = right.y;
                height = right.height;
                width = right.width;
            }
        }

        // last text block not handled by loop
        text += words.get(words.size()-1).getText();
        text = text.trim(); // rm the trailing space

        Rect bbox = words.get(words.size()-1).getBoundingBox();
        x = Math.min(x, bbox.x);
        y = Math.min(y, bbox.y);
        height = Math.max(height, height);
        width = (bbox.x+bbox.width) - words.get(blockStart).getBoundingBox().x;

        textBlocks.add(new OCRTextBlock(new Rect(x, y, width, height), text));

        return textBlocks;
    }
}

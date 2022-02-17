package edu.wm.cs.semeru.redraw.ocr;

import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.opencv.core.Rect;

import edu.wm.cs.semeru.redraw.Utilities;

/**
 * A class representing a word detected by an OCR application.
 *
 * @author William T. Hollingsworth
 */
public class OCRWord {
    private Rect bbox;
    private String text;
    private float confidence;
    private int fontSize;
    private String fontFamily;
    private boolean valid;

    /**
     * Constructor; initializes this word.
     *
     * @param bbox The bounding box of this word.
     * @param text The text of the actual word.
     * @param confidence The OCR engine's confidence that the word was recognized correctly.
     * @param fontSize The size of the font, as detected by the OCR engine.
     * @param fontFamily The font family detected.
     */
    public OCRWord(Rect bbox, String text, float confidence, int fontSize, String fontFamily) {
        this.bbox = bbox;
        this.text = text;
        this.confidence = confidence;
        this.fontSize = fontSize;
        this.fontFamily = fontFamily;
        valid = true;
    }

    /**
     * Gets the bounding box of the word.
     *
     * @return The bounding box of the word.
     */
    public Rect getBoundingBox() {
        return bbox;
    }

    /**
     * Gets the actual text of the word.
     *
     * @return The actual text of the word.
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the confidence that the OCR engine that the word was correctly
     * detected.
     *
     * @return The confidence.
     */
    public float getConfidence() {
        return confidence;
    }

    /**
     * Gets the size of the font used to draw this word.
     *
     * @return The font size.
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Gets the font family used to draw this word.
     *
     * @return The font family.
     */
    public String getFontFamily() {
        return fontFamily;
    }

    /**
     * A component-wise test for equality.
     *
     * @param rhs Any OCRWord
     * @return True if all components match, false otherwise.
     */
    public boolean equals(OCRWord rhs) {
        // probably only need to check the bounding boxes, but just to be
        // cautious
        return (bbox.equals(rhs)) && (text.equals(rhs.text)) && (confidence == rhs.confidence) && (fontSize == rhs.fontSize);
    }

    /**
     * Checks whether there is any overlap between this word and an arbitrary
     * rectangle.
     *
     * @param rect A rectangle to test for overlap.
     * @return True if this word overlaps the rectangle, false otherwise.
     * @see edu.wm.cs.semeru.redraw.Utilities#cvRectIntersects(Rect, Rect)
     */
    public boolean intersects(Rect rect) {
        return Utilities.cvRectIntersects(getBoundingBox(), rect);
    }

    /**
     * Checks whether the given rectangle is completely contained by this word.
     *
     * @param rect A rectangle to test for containment.
     * @return True if this word completely contains the rectangle, false
     *         otherwise.
     * @see edu.wm.cs.semeru.redraw.Utilities#cvRectContains(Rect, Rect)
     */
    public boolean contains(Rect rect) {
        return Utilities.cvRectContains(getBoundingBox(), rect);
    }

    /**
     * Checks whether the top and bottoms sides of the given rect and this word
     * are within some tolerance of each other.
     *
     * @param rect An arbitrary rectangle to test for alignment.
     * @return True if roughly horizontally aligned, false otherwise.
     */
    public boolean isHorizontallyAlignedWith(Rect rect) {
        return (Math.abs(bbox.y - rect.y) <= 5) || (Math.abs(bbox.y + bbox.height - rect.y + rect.height) <= 5);
    }

    /**
     * Checks whether the left or right sides of the given rect and this word
     * are within some tolerance of each other.
     *
     * @param rect An arbitrary rectangle to test for alignment.
     * @return True if roughly vertically aligned, false otherwise.
     */
    public boolean isVerticallyAlignedWith(Rect rect) {
        return (Math.abs(bbox.x - rect.x) <= 5) || (Math.abs(bbox.x + bbox.width - rect.x + rect.width) <= 5);
    }

    /**
     * Returns the validity of this word (i.e., whether it's "word"-hood is questioned
     * by the OpenCV results).
     *
     * @return The validity of the word.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets the validity of the word (i.e., whether it's "word"-hood is questioned
     * by the OpenCV results).
     *
     * @param valid The validity of the word.
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * Draws the word using desktop Java components, and returns the bounding box
     * of the result.
     *
     * @return The bounding box of the drawn word.
     */
    public Rect getDrawnTextBounds() {
        // create a context for drawing graphics
        BufferedImage image = new BufferedImage(bbox.width * 2, bbox.height * 2, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = image.createGraphics();
        Font font = new Font(fontFamily, Font.PLAIN, fontSize);
        FontRenderContext frc = graphics.getFontRenderContext();

        // draw the text to the canvas
        if (text.length() == 0) {
            return new Rect(0, 0, 0, 0);
        }

        TextLayout layout = new TextLayout(text, font, frc);
        layout.draw(graphics, 0.f, 0.f);

        // return the bounding box as an org.opencv.core.Rect
        Rectangle2D bounds = layout.getBounds();
        return new Rect((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[conf:" + this.confidence + ",size:" + this.fontSize + ",text:" + this.text + "]";
    }
    
}

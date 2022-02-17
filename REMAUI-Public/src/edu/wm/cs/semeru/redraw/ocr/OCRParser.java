package edu.wm.cs.semeru.redraw.ocr;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.opencv.core.Rect;

/**
 * This class uses the JDOM XML parsing library to obtain the properties
 * of specific elements defined by the hOCR standard.  In particular, we
 * extract those elements related to recognized words and lines.  For more
 * information on the hOCR format, please see the linked documentation.
 *
 * @author William T. Hollingsworth
 * @see <a href="https://docs.google.com/document/d/1QQnIQtvdAC_8n92-LhwPcjtAUFwBlzE8EWnKAxlgVf0/preview">The hOCR Embedded OCR Workflow and Output Fomrat</a>
 */
public class OCRParser {
	private Namespace ns;
	private Document document;

	/**
	 * Constructor; opens the XML file to be parsed.
	 *
	 * @param xmlfile A string containing the name of the XML file.
	 */
	public OCRParser(String xmlfile) {
	    this.openXMLFile(xmlfile);
	}

	/**
	 * Open an XML file to be parsed.
	 *
	 * @param xmlfile A string containing the name of the XML file.
	 * @return A boolean value indicating success or failure.
	 */
	public boolean openXMLFile(String xmlfile) {
		ns = Namespace.getNamespace("http://www.w3.org/1999/xhtml");
		try {
		    SAXBuilder builder = new SAXBuilder();
		    // need to set features, see https://github.com/hunterhacker/jdom/issues/133
		    builder.setFeature("http://xml.org/sax/features/validation", false);
		    builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		    builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		    document = builder.build(xmlfile);
		}
		catch (Exception e) {
			Logger logger = Logger.getLogger(OCRParser.class.getName());
			logger.log(Level.SEVERE, null, e);
			return false;
		}

		return true;
	}

	/**
	 * Parses the XML document for every element containing a specific hOCR
	 * element.
	 *
	 * @param elementType the name of the hOCR element to search for
	 * @return a list of all elements matching the given name
	 */
	private List<Element> getOCRChildElements(Element xmlElement, String hocrElement) {
		List<Element> elements = new ArrayList<Element>();

        // get all the span elements from the xml
        ElementFilter filter = new ElementFilter("span", ns);
        Iterator<Element> iter = xmlElement.getDescendants(filter);

        // for each element...
        while (iter.hasNext()) {
            Element element = iter.next();

            // collect the elements that have the provided hocr element
            Attribute attribute = element.getAttribute("class");
            if (attribute != null && attribute.getValue().equalsIgnoreCase(hocrElement)) {
                elements.add(element);
            }
        }
		return elements;
	}

	/**
	 * Parses an hOCR property to obtain a the element's bounding box.
   *
	 * @param str a complete hOCR property
	 * @return a rectangle describing the bounding box
	 */
	private Rect parseBoundingBox(String str) {
		// find property start and end
		int start = str.indexOf("bbox");
		if (start == -1) return null;
		start += "bbox".length();

		int end   = str.indexOf(";", start);
		if (end == -1) end = str.length();

		// parse bounding box coordinates out
		String nums = str.substring(start, end).trim();
		String[] coords = nums.split(" ");
		int x0 = Integer.parseInt(coords[0]);
		int y0 = Integer.parseInt(coords[1]);
		int width = Integer.parseInt(coords[2]) - Integer.parseInt(coords[0]);
		int height = Integer.parseInt(coords[3]) - Integer.parseInt(coords[1]);

		// create rectangle
		return new Rect(x0, y0, width, height);
	}

	/**
	 * Parse an hOCR property to obtain the element's confidence level.
	 *
	 * @param str a complete hOCR property
	 * @return the confidence level for a word
	 */
	private float parseConfidence(String str) {
		// find property start and end
		int start = str.indexOf("x_wconf");
		if (start == -1) return -1.f;
		start += "x_wconf".length();

		int end = str.indexOf(";", start);
		if (end == -1) end = str.length();

		// parse substring
		return (float) Integer.parseInt(str.substring(start, end).trim()) / 100.f;
	}

	/**
	 * Parse an hOCR property to obtain the element's font size.
	 *
	 * @param str a complete hOCR property
	 * @return the font size of the word
	 */
	private int parseFontSize(String str) {
		// find property start and end
		int start = str.indexOf("x_fsize");
		if (start == -1) return -1;
		start += "x_fsize".length();

		int end = str.indexOf(";", start);
		if (end == -1) end = str.length();

		// parse substring
		return Integer.parseInt(str.substring(start, end).trim());
	}

	/**
	 * Parse an hOCR property to obtain the font family used to draw
	 * the text.
	 *
	 * @param str a complete hOCR property
	 * @return the font size of the word
	 */
	private String parseFontFamily(String str) {
		// find property start and end
		int start = str.indexOf("x_font");
		if (start == -1) return null;
		start += "x_font".length();

		int end = str.indexOf(";", start);
		if (end == -1) end = str.length();

		// parse substring
		return str.substring(start, end).trim();
	}

	/**
	 * Gets the text of the innermost child of the given element.  Assumes that the children
	 * of the Element do not form a tree.
	 *
	 * @param element the XML element to parse
	 * @return the text of the innermost child of that element
	 */
	private String parseText(Element element) {
		List<Element> children = element.getChildren();

		Element current = element.clone();
		while (!children.isEmpty()) {
		    current = children.get(0);
		    children = current.getChildren();
		}
		return current.getText();
	}

	/**
	 * Attempts to parse out the properties of a provided hOCR element.  If the given
	 * XML element has no 'title' attribute/no hOCR properties, a null reference is
	 * returned.
	 *
	 * @param element The XML element representing an OCR word.
	 * @return        The parsed word if found, otherwise a null reference.
	 * @see edu.wm.cs.semeru.redraw.ocr.OCRWord
	 */
	private OCRWord parseWord(Element element) {
        Attribute title = element.getAttribute("title");
        if (title == null) {
            return null;
        }

        // parse out the properties and text
        String property = title.getValue();

        Rect bbox = parseBoundingBox(property);
        float confidence = parseConfidence(property);
        int fontSize = parseFontSize(property);
        String fontFamily = parseFontFamily(property);
        String text = parseText(element);

        return new OCRWord(bbox, text, confidence, fontSize, fontFamily);
	}

	/**
	 * Parses the XML document for all hOCR words.
	 *
	 * @return a list of all hOCR words
	 * @see edu.wm.cs.semeru.redraw.ocr.OCRWord
	 */
	public List<OCRWord> getWords() {
		// parse the xml for words
	    Element root = document.getRootElement();
		List<Element> elements = getOCRChildElements(root, "ocrx_word");
		List<OCRWord> words = new ArrayList<OCRWord>();

		// for each hOCR word element/XML span element...
		for (Element element : elements) {
		    OCRWord word = parseWord(element);
		    if (word != null) {
		        words.add(word);
		    }
		}

		return words;
	}

	/**
	 * Parses the XML document for all hOCR lines.  Note that the line contains as fields
	 * the words that the line is composed of.
	 *
	 * @return a list of all hOCR lines
	 * @see edu.wm.cs.semeru.redraw.ocr.OCRLine
	 * @see edu.wm.cs.semeru.redraw.ocr.OCRLine#getContainedWords()
	 */
	public List<OCRLine> getLines() {
		// parse the xml for words
	    Element root = document.getRootElement();
		List<Element> lineElements = getOCRChildElements(root, "ocr_line");
		List<OCRLine> lines = new ArrayList<OCRLine>();

		// for each hOCR word element/XML span element...
		for (Element lineElement : lineElements) {
			// does the element have properties?
			Attribute title = lineElement.getAttribute("title");
			if (title != null) {
				// parse out the properties and text
				String property = title.getValue();
				Rect bbox = parseBoundingBox(property);

				OCRLine line = new OCRLine(bbox);
				List<Element> wordElements = getOCRChildElements(lineElement, "ocrx_word");
				for (Element wordElement : wordElements) {
                    OCRWord word = parseWord(wordElement);
                    if (word != null) {
                        line.addWord(word);
                    }
				}
				lines.add(line);
			}
		}

		return lines;
	}

	public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the full path for the project root directory");
        String projectRootDirectory = scanner.next();

        System.out.println("Enter the relative path for the input image");
        String inputImageRelativePath = scanner.next();
        String inputImagePath = projectRootDirectory + File.separator + inputImageRelativePath;

		// run tesseract
        Runtime rt = Runtime.getRuntime();
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        boolean mac = System.getProperty("os.name").toLowerCase().contains("mac");
        String tessDir = projectRootDirectory + File.separator + "lib" + File.separator + "tesseract" + (windows ? "-win" : (mac ? "-mac" : ""));
        String tesseract = tessDir + File.separator + "tesseract";
        String outputPath = projectRootDirectory + File.separator + "test";
        String tessConfig = tessDir + File.separator + "tessdata" + File.separator + "configs" + File.separator + "hocr";
//        String[] envp = {"TESSDATA_PREFIX=" + tessDir, "LD_LIBRARY_PATH="+ tessDir};

        Process pr;
        BufferedReader in = null;

        try {
            String commands [] = new String[] {tesseract, inputImagePath, outputPath, tessConfig};
            rt.exec(commands).waitFor();
//            rt.exec(commands, envp).waitFor();
        }
		catch (Exception e) {
			// if a failure occurred, add it to the log and bail out
			Logger logger = Logger.getLogger(OCRParser.class.getName());
			logger.log(Level.SEVERE, null, e);
			return;
		}

        // parse out the words
        OCRParser parser = new OCRParser(projectRootDirectory + File.separator + "test.hocr");
        List<OCRWord> words = parser.getWords();

        // ... and print them out
        for (OCRWord word : words) {
            System.out.println(word.getText() + " " + word.getFontFamily());
        }
	}
}

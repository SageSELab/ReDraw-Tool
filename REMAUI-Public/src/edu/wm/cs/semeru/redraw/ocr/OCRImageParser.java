/*******************************************************************************
 * Copyright (c) 2017, SEMERU
 * All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 *******************************************************************************/
package edu.wm.cs.semeru.redraw.ocr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.opencv.core.Rect;

/**
 * {Insert class description here}
 *
 * @author Carlos Bernal
 */
public class OCRImageParser {

    /**
     * 
     */
    private static final double THRESHOLD = 0.75;
    private String tesseractFolder;

    /**
     * @param tesseractFolder
     */
    public OCRImageParser(String tesseractFolder) {
        super();
        this.tesseractFolder = tesseractFolder;
    }

    public String parseImage(String pathImage) {
        return parseImage(pathImage, false);
    }

    public String parseImage(String pathImage, boolean debugBoundaries) {
        String tesseract = tesseractFolder + File.separator + "tesseract";
        String tessData = tesseractFolder + File.separator + "tessdata";
        String tessConfig = tessData + File.separator + "configs" + File.separator + "hocr";
        String outputTesseract = pathImage.substring(0, pathImage.lastIndexOf("."));
        String[] envp = { "TESSDATA_PREFIX=" + tessData, "LD_LIBRARY_PATH=" + tesseractFolder };

        Runtime rt = Runtime.getRuntime();
        try {
            String commands[] = new String[] { tesseract, pathImage, outputTesseract, tessConfig };
            Process proc = rt.exec(commands);
            
            
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            
            String s2 = null;
            while ((s2 = stdError.readLine()) != null) {
                System.out.println(s2);
            }     

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String s = null;
            
            while ((s = stdInput.readLine()) != null) {
            	System.out.println(s);
            }

            proc.waitFor();
            
            
        } catch (Exception e) {
            // if a failure occurred, add it to the log and bail out
            Logger logger = Logger.getLogger(OCRParser.class.getName());
            logger.log(Level.SEVERE, null, e);
            return null;
        }

        OCRParser parser = new OCRParser(outputTesseract + ".hocr");
        StringBuilder text = new StringBuilder();
        List<OCRWord> words = parser.getWords();
        for (OCRWord word : words) {
//            System.out.println(word);
            if (!word.getText().trim().isEmpty() && word.getConfidence() >= THRESHOLD) {
                text.append(word.getText().trim() + " ");
            }
        }
        // System.out.println("--------");
        if (debugBoundaries) {
            try {
                BufferedImage bf = ImageIO.read(new File(pathImage));
                String extension = pathImage.substring(pathImage.lastIndexOf("."));
                String outputName = pathImage.substring(0, pathImage.lastIndexOf(".")) + "-boundaries" + extension;
                Graphics2D g = (Graphics2D) bf.getGraphics();
                for (OCRLine line : parser.getLines()) {
                    g.setColor(Color.BLACK);
                    Rect rect = line.getBoundingBox();
                    g.drawRect(rect.x, rect.y, rect.width, rect.height);

                    for (OCRWord ocrWord : line.getContainedWords()) {
                        g.setColor(Color.RED);
                        Rect rect2 = ocrWord.getBoundingBox();
                        g.drawRect(rect2.x, rect2.y, rect2.width, rect2.height);
                    }
                }

                ImageIO.write(bf, "png", new File(outputName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return text.toString().trim();
    }

    public List<String> parseFolder(String pathFolder) {
        File folder = new File(pathFolder);
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.isHidden() && pathname.isFile() && (!pathname.getName().contains("-boundaries"))
                        && (pathname.getName().toLowerCase().endsWith("png")
                                || pathname.getName().toLowerCase().endsWith("jpg"));
            }
        };
        File[] list = folder.listFiles(filter);
        String[] images = new String[list.length];
        for (int i = 0; i < list.length; i++) {
            images[i] = list[i].getAbsolutePath();
        }
        return parseImages(images);
    }

    public List<String> parseImages(String... images) {
        List<String> text = new ArrayList<String>();
        for (String pathImage : images) {
            text.add(parseImage(pathImage));
        }
        return text;
    }

    public static void main(String[] args) {
        String path = "/Users/semeru/git/gitlab-semeru/Reverse-Engineering-Android-Apps/lib/tesseract-mac";
        // String image =
        // "/Users/semeru/git/gitlab-semeru/Reverse-Engineering-Android-Apps/assets/testapp.png";
        String image = "/Users/semeru/git/gitlab-semeru/Reverse-Engineering-Android-Apps/assets/design/68607193.jpg";
        OCRImageParser parser = new OCRImageParser(path);
        String text = parser.parseImage(
                "/Users/semeru/git/gitlab-semeru/Reverse-Engineering-Android-Apps/assets/design/1693795663.jpg", true);
        System.out.println(text);

        List<String> parseFolder = parser
                .parseFolder("/Users/semeru/git/gitlab-semeru/Reverse-Engineering-Android-Apps/assets/design/");
        for (String string : parseFolder) {
            System.out.println(string);
        }
    }
}

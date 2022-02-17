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
package edu.wm.semeru.redraw.helpers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import at.ac.tuwien.photohawk.evaluation.colorconverter.StaticColor;
import at.ac.tuwien.photohawk.evaluation.operation.TransientOperation;
import at.ac.tuwien.photohawk.evaluation.qa.MaeQa;
import at.ac.tuwien.photohawk.evaluation.qa.MseQa;

/**
 * @author KevinMoran
 *
 */
public class ImageHelper {

    /**
     * This method shifts the hue component using HSB representation of a color
     * 
     * @param original
     * @param area
     * @param degrees2Shift
     */
    public static void changeHue(BufferedImage original, Rectangle area, float degrees2Shift) {
        float[] hsb = new float[3];
        for (int i = area.x; i < area.x + area.width; i++) {
            for (int j = area.y; j < area.y + area.height; j++) {
                int argb = original.getRGB(i, j);
                Color color = intToArgb(argb);
                hsb[0] = 0;
                hsb[1] = 0;
                hsb[2] = 0;

                Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);

                hsb[0] = hsb[0] + degrees2Shift;
                int hsBtoRGB = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
                // This doesn't have the alpha channel
                Color tempColor = new Color(hsBtoRGB);
                // Using original alpha channel from the color
                tempColor = new Color(tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), color.getAlpha());
                // Transforming the color into the integer representation
                original.setRGB(i, j, argbToInt(tempColor));
            }
        }
    }

    /**
     * This method generates an image with different colors
     * 
     * @param inputImage
     *            path to input image, (example.jpg)
     * @param outputImage
     *            path to output image, (example-color.jpg will result in
     *            example-color<number>.jpg)
     * @param imageType
     *            [png, jpg, gif]
     * @param numberOfImages
     *            number of perturbed copies
     */
    public static void changeImageColor(String inputImage, String outputImage) {
        try {

            float constant = (float) (Math.PI / (Math.random() + 1f));
            BufferedImage read = ImageIO.read(new File(inputImage));
            String output = outputImage.substring(0, outputImage.lastIndexOf("."));
            String type = outputImage.substring(outputImage.lastIndexOf(".") + 1, outputImage.length());
            changeHue(read, new Rectangle(0, 0, read.getWidth(), read.getHeight()), (float) (constant * Math.random()));
            ImageIO.write(read, type, new File(output + "." + type));
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int rgbToInt(int red, int grn, int blu, int alpha) {
        return ((alpha & 0xff) << 24) | ((red & 0xff) << 16) | ((grn & 0xff) << 8) | blu & 0xff;
    }

    public static int argbToInt(Color color) {
        int red = color.getRed();
        int grn = color.getGreen();
        int blu = color.getBlue();
        return rgbToInt(red, grn, blu, color.getAlpha());
    }

    public static Color intToRgb(int rgb) {
        int red = ((rgb >> 16) & 0xFF);
        int grn = ((rgb >> 8) & 0xFF);
        int blu = (rgb & 0xFF);
        return new Color(red, grn, blu);
    }

    /**
     * 
     * @param rgb
     * @param alpha
     *            [0-255]
     * @return
     */
    public static Color intToArgb(int rgb, int alpha) {
        int red = ((rgb >> 16) & 0xFF);
        int grn = ((rgb >> 8) & 0xFF);
        int blu = (rgb & 0xFF);
        return new Color(red, grn, blu, alpha);
    }

    public static Color intToArgb(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = (argb >> 0) & 0xFF;
        return new Color(r, g, b, a);
    }

    public static double computeMAE(File left, File right) throws IOException {
        BufferedImage leftImage = ImageIO.read(left);
        BufferedImage rightImage = ImageIO.read(right);

        return computeMAE(leftImage, rightImage);
    }

    public static double computeMAE(BufferedImage left, BufferedImage right) {
        MaeQa maeQa = new MaeQa();

        // Remove alpha channel to avoid an exception
        left = getImageNoAlpha(left);
        right = getImageNoAlpha(right);

        TransientOperation<Float, StaticColor> evaluate = maeQa.evaluate(left, right);
        StaticColor result = evaluate.getResult();
        float[] v = result.getChannelValues();
        return ((v[0] + v[1] + v[2]) / 3);
    }

    public static double computeMSE(File left, File right) throws IOException {
        BufferedImage leftImage = ImageIO.read(left);
        BufferedImage rightImage = ImageIO.read(right);

        return computeMSE(leftImage, rightImage);
    }
    
    public static double computeMSE(BufferedImage left, BufferedImage right) {
        MseQa mseQa = new MseQa();

        // Remove alpha channel to avoid an exception
        left = getImageNoAlpha(left);
        right = getImageNoAlpha(right);

        TransientOperation<Float, StaticColor> evaluate = mseQa.evaluate(left, right);
        StaticColor result = evaluate.getResult();
        float[] v = result.getChannelValues();
        return ((v[0] * v[0] + v[1] * v[1] + v[2] * v[2]) / 3);
    }

    /**
     * This method returns an image with no alpha channel
     * 
     * @param in
     * @return
     */
    public static BufferedImage getImageNoAlpha(BufferedImage in) {
        BufferedImage result = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.drawImage(in, 0, 0, null);
        g.dispose();
        return result;
    }
}

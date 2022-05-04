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
package edu.wm.semeru.redraw.pipeline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import edu.wm.semeru.redraw.helpers.ImageHelper;

/**
 * {Insert class description here}
 *
 * @author Carlos Bernal
 */
public class TestPhotohawk {
    public static void main(String[] args) {

        String[] apps = { "Dropbox", "Giphy", "Pandora", "Ringtones", "Tether1", "Tether2", "Translate", "Weather",
                "Yelp" };

        File aa = new File(
                "/Users/semeru/Documents/SEMERU/ReDraw/final-evaluation/Initial-App-Set/Ground-Truth-Screenshots-and-Hierarchies/Dropbox.png");
        File bb = new File(
                "/Users/semeru/Documents/SEMERU/ReDraw/final-evaluation/Initial-App-Set/ReDraw-Screenshots-and-Hierarchies/CV-Based(REMAUI) Object Detection/Dropbox.png");
        // File aa = new
        // File("/Users/semeru/Documents/SEMERU/ReDraw/final-evaluation/black.jpg");
        // File bb = new
        // File("/Users/semeru/Documents/SEMERU/ReDraw/final-evaluation/white.jpg");

        ArrayList<Double> redrawCvMae = new ArrayList<>();
        ArrayList<Double> redrawSketchMae = new ArrayList<>();
        ArrayList<Double> remauiMae = new ArrayList<>();
        ArrayList<Double> redrawCvMse = new ArrayList<>();
        ArrayList<Double> redrawSketchMse = new ArrayList<>();
        ArrayList<Double> remauiMse = new ArrayList<>();

        String root = "/Users/semeru/Documents/SEMERU/ReDraw/final-evaluation/Initial-App-Set/";

        for (int i = 0; i < apps.length; i++) {
            double mae = 0;
            double mse = 0;
            aa = new File(root + "GT/" + apps[i] + ".png");
            bb = new File(root + "ReDraw/CV/" + apps[i] + ".png");
            try {
                mae = ImageHelper.computeMAE(aa, bb);
                mse = ImageHelper.computeMSE(aa, bb);
            } catch (IOException e) {
                e.printStackTrace();
            }
            redrawCvMae.add(mae);
            redrawCvMse.add(mse);
            // ------------------------
            bb = new File(root + "ReDraw/Sketch/" + apps[i] + ".png");
            try {
                mae = ImageHelper.computeMAE(aa, bb);
                mse = ImageHelper.computeMSE(aa, bb);
            } catch (IOException e) {
                e.printStackTrace();
            }
            redrawSketchMae.add(mae);
            redrawSketchMse.add(mse);
            // ------------------------
            bb = new File(root + "REMAUI/" + apps[i] + ".png");
            try {
                mae = ImageHelper.computeMAE(aa, bb);
                mse = ImageHelper.computeMSE(aa, bb);
            } catch (IOException e) {
                e.printStackTrace();
            }
            remauiMae.add(mae);
            remauiMse.add(mse);
            // ------------------------
            System.out.println(apps[i] + " processed");
        }

        System.out.println("redraw-vc-mae,redraw-vc-mse,redraw-sketch-mae,redraw-sketch-mse,remaui-mae,remaui-mse");
        for (int i = 0; i < redrawCvMae.size(); i++) {
            String line = redrawCvMae.get(i) + "," + redrawCvMse.get(i) + "," + redrawSketchMae.get(i) + "," + redrawSketchMse.get(i) + ","
                    + remauiMae.get(i) + "," + remauiMse.get(i);
            System.out.println(line);
        }

        // try {
        // System.out.println("MAE:" + mae);
        // System.out.println("MSE:" + mse);
        //
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
    }
}

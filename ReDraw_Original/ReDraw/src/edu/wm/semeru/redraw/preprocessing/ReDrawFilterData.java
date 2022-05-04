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
package edu.wm.semeru.redraw.preprocessing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;

import edu.semeru.android.core.helpers.ui.UiAutomatorBridge;
import edu.semeru.android.core.model.DynGuiComponentVO;
import edu.wm.semeru.redraw.parsing.ClassifierGroundTruth;

/**
 * {Insert class description here}
 *
 * @author Carlos Bernal
 */
public class ReDrawFilterData {

    private static final String WEBVIEW = "android.webkit.WebView";
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 1920;

    public static void main(String[] args) {
//        File root = new File("/Users/semeru/Documents/SEMERU/ReDraw/temp-data/results");
//        String output = "/Users/semeru/Documents/SEMERU/ReDraw/temp-data/final-data";
         File root = new
         File("/Users/semeru/Documents/SEMERU/ReDraw/data/results");
         String output =
         "/Users/semeru/Documents/SEMERU/ReDraw/data/final-data";

        List<String> exclude = new ArrayList<String>(
                Arrays.asList("android.widget.FrameLayout", "android.widget.LinearLayout", "android.webkit.WebView",
                        "android.widget.GridLayout", "android.widget.RelativeLayout", "android.view.View"));

        FileFilter directoryFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };

        FileFilter notHidenFilter = new FileFilter() {
            public boolean accept(File file) {
                return !file.isHidden();
            }
        };

        FileFilter screenshotFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isFile() && !file.isHidden() && file.getName().equals("screenshot.png");
            }
        };

        FileFilter uiFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isFile() && !file.isHidden() && file.getName().equals("ui_dump.xml");
            }
        };

        UiAutomatorBridge bridge = new UiAutomatorBridge(null);
        // output =
        // "/Users/semeru/Documents/SEMERU/GVT/com.beatronik.djstudiodemo-screens/output";
        // air.air.BridalShop-screens
        for (File packageName : root.listFiles()) {
            // File packageName = new File(
            // "/Users/semeru/Documents/SEMERU/GVT/com.beatronik.djstudiodemo-screens");
            if (packageName.isDirectory() && packageName.listFiles(directoryFilter).length > 0) {
                // For each screenshot folder

                HashMap<String, String[]> screens = new HashMap<String, String[]>();
                for (File numberScreenshot : packageName.listFiles(notHidenFilter)) {
                    boolean passed = true;
                    // is there something inside?
                    File[] imageFiles = numberScreenshot.listFiles(screenshotFilter);
                    if (imageFiles != null && imageFiles.length > 0) {
                        // 1. Validate if the screenshot is in portrait
                        try {
                            BufferedImage image = ImageIO.read(imageFiles[0]);
                            if (image.getWidth() != 1200 && image.getHeight() != 1920) {
                                passed = false;
                                // continue;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // 2. Count number of layouts and compare with the total
                        // number of components
                        File[] uiDumps = numberScreenshot.listFiles(uiFilter);
                        if (uiDumps != null && uiDumps.length > 0 && !uiDumps[0].exists()) {
                            passed = false;
                            // continue;
                        } else if (passed && uiDumps != null && uiDumps.length > 0) {
                            bridge.updateTreeFromFile(uiDumps[0].getAbsolutePath());
                            // bridge.get
                            ArrayList<DynGuiComponentVO> components = bridge.getScreenInfoNoDevice(WIDTH, HEIGHT, true,
                                    false);
                            // remove hierarchy tag
                            components.remove(0);
                            int count = 0;
                            int areaWebView = 0;
                            // It means the image is in portrait but the data is
                            // on landscape (weird case)
                            if (components != null && components.size() > 0 && components.get(0).getWidth() == HEIGHT) {
                                passed = false;
                            }
                            for (DynGuiComponentVO c : components) {
                                if (c.getPackageName().contains("com.android.launcher")) {
                                    passed = false;
                                    break;
                                } else if (exclude.contains(c.getName())) {
                                    count++;
                                } else if (c.getName().equals(WEBVIEW)) {
                                    areaWebView = c.getWidth() * c.getHeight();
                                }
                            }
                            if (count == components.size()) {
                                passed = false;
                                // continue;
                            }

                            // 3. Validate if there is a webView in the xml
                            // (use%)

                            if ((WIDTH * HEIGHT) * .5 < areaWebView) {
                                passed = false;
                                // continue;
                            }
                            // 4. Make sure the image is not just one color
                            try {
                                passed = ClassifierGroundTruth.checkPixels(imageFiles[0]);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else {
                            passed = false;
                        }

                        if (passed) {
                            // compute hash and add to the map
                            InputStream targetStream;
                            try {
                                targetStream = new FileInputStream(uiDumps[0].getAbsolutePath());
                                SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
                                byte[] digest = digestSHA3.digest(IOUtils.toByteArray(targetStream));
                                String hexString = Hex.toHexString(digest);
                                // Unique files
                                String[] files = { uiDumps[0].getAbsolutePath(), imageFiles[0].getAbsolutePath() };
                                if (!screens.containsKey(hexString)) {
                                    screens.put(hexString, files);
                                }
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("Validation: FAILED : " + numberScreenshot);
                        }
                    }
                }

                int totalScreens = 0;
                // Unique files
                for (Entry<String, String[]> screen : screens.entrySet()) {
                    File uiDump = new File(screen.getValue()[0]);
                    File imageFile = new File(screen.getValue()[1]);
                    // Passed everything, copy to the final folder
                    System.out.println("Validation: OK! : " + uiDump.getParentFile().getAbsolutePath());
                    try {
                        // Copy xml
                        copyDirectory(uiDump, new File(
                                output + "/" + packageName.getName() + "/hierarchy_" + (++totalScreens) + ".xml"));
                        // Copy image
                        System.out.println(uiDump.getAbsolutePath());
                        copyDirectory(imageFile, new File(
                                output + "/" + packageName.getName() + "/screenshot_" + (totalScreens) + ".png"));
                        // break;
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {
            if (!targetLocation.getParentFile().exists()) {
                targetLocation.getParentFile().mkdir();
            }
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

}

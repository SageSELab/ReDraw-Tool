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
package edu.wm.semeru.redraw.parsing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import edu.semeru.android.core.helpers.ui.UiAutomatorBridge;
import edu.semeru.android.core.model.DynGuiComponentVO;

/**
 * {Insert class description here}
 *
 * @author Carlos Bernal
 */
public class ComputeStatistics {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 1920;

    public static void main(String[] args) {
        FileFilter xmlFilter = new FileFilter() {
            public boolean accept(File file) {
                return !file.isHidden() && file.getName().endsWith(".xml");
            }
        };
        FileFilter txtFilter = new FileFilter() {
            public boolean accept(File file) {
                return !file.isHidden() && file.getName().endsWith(".txt");
            }
        };
        FileFilter folderFilter = new FileFilter() {
            public boolean accept(File file) {
                return !file.isHidden() && file.isDirectory();
            }
        };

        // Global variables
//         File rootFolderScreens = new
//         File("/Users/semeru/Documents/SEMERU/ReDraw/r-data/ReDraw-Data-Set-2");
        File rootFolderScreens = new File("/Volumes/Carlos_Backup/final-data-all");
        File rootFolderCategories = new File("R-data/Categories");
        HashMap<String, HashMap<String, Integer>> categoryCounts = new HashMap<String, HashMap<String, Integer>>();
        // HashMap<String, String> components = new HashMap<String, String>();
        HashSet<String> components = new HashSet<String>();
        UiAutomatorBridge bridge = new UiAutomatorBridge(null);
        HashMap<String, List<String>> categories = new HashMap<String, List<String>>();

        System.out.println("Reading apps ...");
        // Iterate the categories
        for (File category : rootFolderCategories.listFiles(txtFilter)) {
            try (BufferedReader br = new BufferedReader(new FileReader(category.getAbsolutePath()))) {
                String line;
                String name = category.getName().substring(0, category.getName().lastIndexOf("."));
                while ((line = br.readLine()) != null) {
                    if (!categories.containsKey(name) && !line.isEmpty()) {
                        List<String> list = new ArrayList<>(Arrays.asList(line));
                        categories.put(name, list);
                    } else if (!line.isEmpty()) {
                        categories.get(name).add(line);
                    }
                }
                // System.out.println(name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Reading components ...");
        ArrayList<DynGuiComponentVO> screen = null;
        for (Entry<String, List<String>> element : categories.entrySet()) {
            for (String app : element.getValue()) {
                String path = rootFolderScreens.getAbsolutePath() + "/" + app + "-screens";
                File appPath = new File(path);
                // make sure the path exists
                if (appPath.exists()) {
                    for (File screenFile : appPath.listFiles(xmlFilter)) {
                        // Read info
                        bridge.updateTreeFromFile(screenFile.getAbsolutePath());
                        screen = bridge.getScreenInfoNoDevice(WIDTH, HEIGHT, true, false);
                        // Save data into the structures
                        if (!categoryCounts.containsKey(element.getKey())) {
                            // Initialize because it is new
                            HashMap<String, Integer> content = new HashMap<String, Integer>();
                            for (DynGuiComponentVO comp : screen) {
                                // is it empty?
                                if (comp.getName() != null && !comp.getName().isEmpty()
                                        && validComponent(comp.getName())) {
                                    if (!content.containsKey(comp.getName())) {
                                        content.put(comp.getName(), 1);
                                    } else {
                                        Integer integer = content.get(comp.getName());
                                        content.put(comp.getName(), integer + 1);
                                    }
                                    // add to components
                                    if (!components.contains(comp.getName()))
                                        components.add(comp.getName());
                                }
                            }
                            categoryCounts.put(element.getKey(), content);
                        } else {
                            // Get from the hasMap since it is not new
                            HashMap<String, Integer> content = categoryCounts.get(element.getKey());
                            for (DynGuiComponentVO comp : screen) {
                                // is it empty?
                                if (comp.getName() != null && !comp.getName().isEmpty()
                                        && validComponent(comp.getName())) {
                                    if (!content.containsKey(comp.getName())) {
                                        content.put(comp.getName(), 1);
                                    } else {
                                        Integer integer = content.get(comp.getName());
                                        content.put(comp.getName(), integer + 1);
                                    }
                                    // add to components
                                    if (!components.contains(comp.getName()))
                                        components.add(comp.getName());
                                }
                            }
                            categoryCounts.put(element.getKey(), content);

                        }
                    }
                }

            }
        }

        System.out.println("Generating data ...");

        // All components
        String output1 = "R-data/distribution1.csv";
        // All components except ImageView
        String output2 = "R-data/distribution2.csv";
        try (BufferedWriter bw1 = new BufferedWriter(new FileWriter(output1));
                BufferedWriter bw2 = new BufferedWriter(new FileWriter(output2))) {
            // Writing header
            String header1 = "category, ";
            String header2 = "category, ";

            for (String component : components) {
                header1 += (component.substring(component.lastIndexOf(".") + 1, component.length()) + ", ");
                if (!component.contains("ImageView")) {
                    header2 += (component.substring(component.lastIndexOf(".") + 1, component.length()) + ", ");
                }
            }
            header1 = header1.substring(0, header1.length() - 2);
            header2 = header2.substring(0, header2.length() - 2);
            bw1.write(header1 + "\n");
            bw2.write(header2 + "\n");

            // Writing content
            for (Entry<String, HashMap<String, Integer>> element : categoryCounts.entrySet()) {
                String row1 = element.getKey() + ", ";
                String row2 = element.getKey() + ", ";
                // System.out.print(element.getKey() + ", ");
                for (String component : components) {

                    if (element.getValue().containsKey(component)) {
                        if (!component.contains("ImageView")) {
                            row2 += element.getValue().get(component) + ", ";
                        }
                        row1 += element.getValue().get(component) + ", ";
                    } else {
                        row1 += "0, ";
                        row2 += "0, ";
                    }
                }
                row1 = row1.substring(0, row1.length() - 2);
                row2 = row2.substring(0, row2.length() - 2);
                bw1.write(row1 + "\n");
                bw2.write(row2 + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("done!");
    }

    /**
     * @param name
     * @return
     */
    private static boolean validComponent(String pComponent) {
        String components[] = { "EditText", "CheckedTextView", "RadioButton", "CheckBox", "ProgressBar", "ToggleButton",
                "Switch", "RatingBar", "SeekBar", "Spinner", "NumberPicker", "ImageView" };
        boolean result = false;
        for (String component : components) {
            result |= pComponent.contains(component);
        }
        return (pComponent.startsWith("com.android") || pComponent.startsWith("android")) && result;
    }
}

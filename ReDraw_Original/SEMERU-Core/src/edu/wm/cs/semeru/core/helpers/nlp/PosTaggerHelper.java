/*******************************************************************************
 * Copyright (c) 2016, SEMERU
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
/**
 * PosTaggerHelper.java
 * 
 * Created on Jul 16, 2015, 2:42:45 PM
 */
package edu.wm.cs.semeru.core.helpers.nlp;

import java.util.Arrays;
import java.util.HashMap;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.wm.cs.semeru.core.helpers.TextHelper;


/**
 * {Insert class description here}
 *
 * @author Carlos Bernal
 * @since Jul 16, 2015
 */
public class PosTaggerHelper {

    public static final String VERB = "VERB";
    public static final String NOUN = "NOUN";
    public static final String NUM = "NUM";
    public static final String ADV = "ADV";
    public static final String ADJ = "ADJ";
    public static final String ADP = "ADP";
    public static final String OTHER = "X";

    private HashMap<String, String> dictionary = new HashMap<String, String>();
    private HashMap<String, String> mobileDictionary = new HashMap<String, String>();
    MaxentTagger tagger;
    private static PosTaggerHelper instance = null;

    private PosTaggerHelper() {
        tagger = new MaxentTagger("nlp/english-caseless-left3words-distsim.tagger");
        // tagger = new MaxentTagger("nlp/english-left3words-distsim.tagger");
        // Universal part of speech
        // https://github.com/slavpetrov/universal-pos-tags/blob/master/en-ptb.map
        dictionary.put("!", ".");
        dictionary.put("#", ".");
        dictionary.put("$", ".");
        dictionary.put("''", ".");
        dictionary.put("(", ".");
        dictionary.put(")", ".");
        dictionary.put(",", ".");
        dictionary.put("-LRB-", ".");
        dictionary.put("-RRB-", ".");
        dictionary.put(".", ".");
        dictionary.put(":", ".");
        dictionary.put("?", ".");
        dictionary.put("CC", "CONJ");
        dictionary.put("CD", "NUM");
        // dictionary.put("CD", "NOUN");
        dictionary.put("CD|RB", "X");
        dictionary.put("DT", "DET");
        dictionary.put("EX", "DET");
        dictionary.put("FW", "X");
        dictionary.put("IN", "ADP");
        dictionary.put("IN|RP", "ADP");
        dictionary.put("JJ", "ADJ");
        dictionary.put("JJR", "ADJ");
        dictionary.put("JJRJR", "ADJ");
        dictionary.put("JJS", "ADJ");
        dictionary.put("JJ|RB", "ADJ");
        dictionary.put("JJ|VBG", "ADJ");
        dictionary.put("LS", "X");
        dictionary.put("MD", "VERB");
        dictionary.put("NN", "NOUN");
        dictionary.put("NNP", "NOUN");
        dictionary.put("NNPS", "NOUN");
        dictionary.put("NNS", "NOUN");
        dictionary.put("NN|NNS", "NOUN");
        dictionary.put("NN|SYM", "NOUN");
        dictionary.put("NN|VBG", "NOUN");
        dictionary.put("NP", "NOUN");
        dictionary.put("PDT", "DET");
        dictionary.put("POS", "PRT");
        dictionary.put("PRP", "PRON");
        dictionary.put("PRP$", "PRON");
        dictionary.put("PRP|VBP", "PRON");
        dictionary.put("PRT", "PRT");
        dictionary.put("RB", "ADV");
        dictionary.put("RBR", "ADV");
        dictionary.put("RBS", "ADV");
        dictionary.put("RB|RP", "ADV");
        dictionary.put("RB|VBG", "ADV");
        dictionary.put("RN", "X");
        dictionary.put("RP", "PRT");
        dictionary.put("SYM", "X");
        dictionary.put("TO", "PRT");
        dictionary.put("UH", "X");
        dictionary.put("VB", "VERB");
        // dictionary.put("VBD", "VERB");
        // dictionary.put("VBD|VBN", "VERB");
        // dictionary.put("VBG", "VERB");
        // dictionary.put("VBG|NN", "VERB");
        // dictionary.put("VBN", "VERB");
        // dictionary.put("VBP", "VERB");
        // dictionary.put("VBP|TO", "VERB");
        // dictionary.put("VBZ", "VERB");
        // dictionary.put("VP", "VERB");
        dictionary.put("VBD", "NOUN");
        dictionary.put("VBD|VBN", "NOUN");
        dictionary.put("VBG", "NOUN");
        dictionary.put("VBG|NN", "NOUN");
        dictionary.put("VBN", "NOUN");
        dictionary.put("VBP", "NOUN");
        dictionary.put("VBP|TO", "NOUN");
        dictionary.put("VBZ", "NOUN");
        dictionary.put("VP", "NOUN");
        dictionary.put("WDT", "DET");
        dictionary.put("WH", "X");
        dictionary.put("WP", "PRON");
        dictionary.put("WP$", "PRON");
        dictionary.put("WRB", "ADV");
        dictionary.put("``", ".");

        mobileDictionary.put("general", "NN");
        mobileDictionary.put("select", "VB");
        mobileDictionary.put("create", "VB");
        mobileDictionary.put("set", "VB");
        mobileDictionary.put("button1", "JJ");
        mobileDictionary.put("what's", "NN");
    }

    public static PosTaggerHelper getInstance() {
        if (instance == null) {
            instance = new PosTaggerHelper();
        }
        return instance;
    }

    public String[] taggText(String... text) {
        String tagsString = tagger.tagString(TextHelper.arrayToString(text));
        String[] tags = getTags(tagsString);
        for (int i = 0; i < text.length; i++) {
            if (mobileDictionary.containsKey(text[i].toLowerCase())) {
                tags[i] = mobileDictionary.get(text[i].toLowerCase());
            }
        }
        String[] translation = new String[tags.length];
        for (int i = 0; i < translation.length; i++) {
            translation[i] = dictionary.get(tags[i]);
            // translation[i] = tags[i];
        }
        return translation;
    }

    /**
     * @param tagsString
     * @return
     */
    private String[] getTags(String tagsString) {
        String[] tags = tagsString.split(" ");
        for (int i = 0; i < tags.length; i++) {
            if (!tags[i].isEmpty()) {
                tags[i] = tags[i].split("_")[1];
            }
        }
        return tags;
    }

    public boolean startsWithVerb(StringBuffer text) {
        String[] split = TextHelper.splitByAll(text == null ? "" : text.toString());
        split = preProcessing(split);
        String[] taggs = taggText(split);
        String temp = Arrays.toString(split).replace("[", "").replace(",", "").replace("]", "");
        text.delete(0, text.length());
        text.append(temp);
        // System.out.println(Arrays.toString(split));
        // System.out.println(Arrays.toString(taggs));
        // System.out.println("------");
        return (taggs[0] != null && taggs[0].equals(VERB)) ? true : false;
    }

    /**
     * @param text
     * @return
     */
    public boolean startsWithNoun(StringBuffer text, boolean numAsNoun) {
        String[] split = TextHelper.splitByAll(text == null ? "" : text.toString());
        split = preProcessing(split);
        String[] taggs = taggText(split);
        String temp = Arrays.toString(split).replace("[", "").replace(",", "").replace("]", "");
        text.delete(0, text.length());
        text.append(temp);
        return (taggs[0] != null && (taggs[0].equals(NOUN) || taggs[0].equals(ADP) || (numAsNoun && taggs[0]
                .equals(NUM)))) ? true : false;
    }

    /**
     * @param split
     * @return
     */
    private String[] preProcessing(String[] split) {
        if (split.length > 0) {
            if (split[0].toLowerCase().equals("save")) {
                split[0] = "Add";
            } else if (split[0].toLowerCase().equals("new")) {
                split[0] = "Add";
            }
        }
        return split;
    }

    public static void main(String[] args) {
        // String[] text = { "Clear","the" ,"log","I","cleared","some","save"};
        String[] text = { "My","expenses"};
//        String[] text = { "create","accounts"};
//        String[] text = { "Select" ,"accounts", "to","create" };
        // String[] taggText = ;
        // PosTaggerHelper.getInstance().taggText(text);
        // PosTaggerHelper.getInstance().taggText(text);
        // StringBuffer string = new StringBuffer("id/menu");
        // System.out.println(getInstance().startsWithNoun(string));
        // System.out.println(string);
        System.out.println(Arrays.toString(PosTaggerHelper.getInstance().taggText(text)));
        // System.out.println(Arrays.toString(PosTaggerHelper.getInstance().taggText(text)));
        // System.out.println(Arrays.toString(PosTaggerHelper.getInstance().taggText(text)));
        // System.out.println(Arrays.toString(PosTaggerHelper.getInstance().taggText(text)));
    }

    /**
     * @param title
     * @param text
     * @param contentDescription
     * @param idResource
     * @return
     */
    public String postProcessing(String text, boolean isPopUp) {
        if (text != null) {
            if (text.toLowerCase().equals("menu")) {
                return "Open options menu";
            } else if (text.toLowerCase().equals("settings")) {
                return "Manage settings";
            } else if (isPopUp) {
                return "Select " + text + " option";
            }
        }
        return text;
    }
}

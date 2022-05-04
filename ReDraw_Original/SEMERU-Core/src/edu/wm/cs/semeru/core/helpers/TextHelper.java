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
 * TextHelper.java
 * 
 * Created on Jul 18, 2015, 9:16:11 PM
 */
package edu.wm.cs.semeru.core.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains methods to do strings manipulations
 *
 * @author Carlos Bernal
 * @since Jul 18, 2015
 */
public class TextHelper {

    public static String[] splitByUpperCase(String... s) {
        for (int i = 0; i < s.length; i++) {
            if (s[i].toUpperCase().equals(s[i])) {
                s[i] = s[i].toLowerCase();
            }
        }
        return split("(?<=.)(?=\\p{Upper})", s);
    }

    public static String[] splitBySpace(String... s) {
        return split(" ", s);
    }

    public static String[] splitByUnderScore(String... s) {
        return split("_", s);
    }

    public static String[] splitByDash(String... s) {
        return split("-", s);
    }

    public static String[] splitBySlash(String... s) {
        return split("/", s);
    }

    public static String[] splitByAll(String... s) {
        String[] split = splitBySpace(s);
        split = splitByUpperCase(split);
        split = splitByUnderScore(split);
        split = splitByDash(split);
        split = splitBySlash(split);
        return getStringFromArry(split).split(" ");
    }

    public static String getStringFromArry(String... split) {
        return Arrays.toString(split).replace("[", "").replace(",", "").replace("]", "");
    }

    public static String arrayToString(String... a) {
        return Arrays.toString(a).replace("[", "").replace(",", "").replace("]", "");
    }

    public static String getTitleFormat(String title) {
        String result = "";
        result = (title.charAt(0) + "").toUpperCase();
        result += title.substring(1, title.length()).toLowerCase();
        return result;
    }

    private static String[] split(String regex, String... s) {
        List<String> list = new ArrayList<String>();
        for (String string : s) {
            list.addAll(Arrays.asList(string.split(regex)));
        }
        String[] result = new String[list.size()];
        return list.toArray(result);
    }

    public static void main(String[] args) {
        // String[] splitByUpperCase =
        // splitByUpperCase("helloHow/Are myFriend This-Is a_test");
        String[] splitByUpperCase = splitByUpperCase("");
        splitByUpperCase = splitBySpace(splitByUpperCase);
        splitByUpperCase = splitByUnderScore(splitByUpperCase);
        splitByUpperCase = splitByDash(splitByUpperCase);
        splitByUpperCase = splitBySlash(splitByUpperCase);
        System.out.println(Arrays.toString(splitByUpperCase));
    }
}

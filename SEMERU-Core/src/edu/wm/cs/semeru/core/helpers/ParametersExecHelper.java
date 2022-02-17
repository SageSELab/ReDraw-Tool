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
 * ParametersExecHelper.java
 * 
 * Created on Jun 16, 2014, 5:55:09 PM
 * 
 */
package edu.wm.cs.semeru.core.helpers;

import com.lexicalscope.jewel.cli.Option;

/**
 * Parameters that are able to use in Main class
 * 
 * @author Carlos Bernal
 * @since Jun 16, 2014
 */
public interface ParametersExecHelper {

    @Option(shortName = "a", description = "full path to APK")
    String getApk();

    boolean isApk();

    @Option(shortName = "p", description = "full path to properties file")
    String getProperties();

    boolean isProperties();
    
    @Option(shortName = "l", description = "full path to folder containing AMP logs")
    String getLog();
    
    boolean isLog();
    
    @Option(shortName = "o", description = "full path to output folder")
    String getOutput();

    boolean isOutput();

    @Option(helpRequest = true, description = "display help", shortName = "h")
    boolean isHelp();
}

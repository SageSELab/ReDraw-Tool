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
 * HierarchyViewerDirector.java
 * 
 * Created on Jul 25, 2014, 4:40:24 PM
 */
package edu.semeru.android.core.helpers.ui;

import com.android.hierarchyviewerlib.HierarchyViewerDirector;

/**
 * {Insert class description here}
 *
 * @author Carlos Bernal
 * @since Jul 25, 2014
 */
public class DataHierarchyViewerDirector extends HierarchyViewerDirector {

    private String pathAdb;

    @Override
    public String getAdbLocation() {
	if (pathAdb == null) {
	    System.out.println("Adb path is null");
	}
	return this.pathAdb;
    }

    @Override
    public void executeInBackground(String taskName, Runnable task) {
	// System.out.println("TASK: " + taskName + " begin");
	task.run();
	// System.out.println("TASK: " + taskName + " end");
	System.out.println("TASK: " + taskName);
    }

    /**
     * @return the pathAdb
     */
    public String getPathAdb() {
	return pathAdb;
    }

    /**
     * @param pathAdb
     *            the pathAdb to set
     */
    public void setPathAdb(String pathAdb) {
	this.pathAdb = pathAdb;
    }

}
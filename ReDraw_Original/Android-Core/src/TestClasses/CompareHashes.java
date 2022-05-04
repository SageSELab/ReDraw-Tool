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
 * Created by Kevin Moran on Dec 21, 2015
 */
package TestClasses;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author KevinMoran
 *
 */
public class CompareHashes {

	public static void main (String args[]) throws IOException{
		
		String pathToServerHashes = "/Users/KevinMoran/Desktop/TAMARIN-Hashes-Tests/server-tam-tc/";
		String pathToMacHashes = "/Users/KevinMoran/Desktop/TAMARIN-Hashes-Tests/mac-tam-tc/";
		
		BufferedReader mac = null;
		BufferedReader server = null;
		
		for (int i = 0; i <= 200; i ++){
		
		 mac = new BufferedReader(new FileReader(pathToMacHashes + File.separator + "tc" + i + ".txt"));
		 server = new BufferedReader(new FileReader(pathToServerHashes + File.separator + "tc" + i + ".txt"));
		 
		    String line_mac;
		    String line_server;
		    while ((line_mac = mac.readLine()) != null) {
		    	while ((line_server = server.readLine()) != null) {
		    		
		    		if(line_mac.contains("New Hash") && line_server.contains("New Hash")){
		    			
		    			if(line_mac.equals(line_server)){
		    				System.out.println("Hashes #:" + i + " match!!!!");
		    			}else{
		    				System.out.println("Hashes #:" + i + " don't match.");
		    			}
		    			
		    		}
		    		
		    	}
		    }
		
		}
		
	}
	
}

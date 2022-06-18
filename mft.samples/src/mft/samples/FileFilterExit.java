package mft.samples;
/**
 * Copyright (c) IBM Corporation 2016
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *  Contributors:
 *    Shashikanth Rao T - Initial Contribution
 */

/**
 * Sample IBM MQ Managed File Transfer SourceTransferStartExit
 * 
 *  
 *  Scenario where this exit helps
 *  
 *  Assume a there are a number of files in directory and user wants to transfer only
 *  those files whose size matches a certain value. For example user wants transfer
 *  only files whose size is between 10 and 12 mega bytes.
 *  
 *  When a transfer is initiated, an agent would pick all files in the directory and 
 *  transfer, no matter what the size of file is. With this exit, the agent would
 *  transfer only those files that match specified size.
 *  
 *  
 *  How to use this exit:
 *  
 *  Configuration:
 *  1) Compile this source file.
 *  1) Stop your agent.
 *  2) Copy the FileFilterExit.class and accompanying config.xml file under the following 
 *     directory. 
 *     /<your MQ data directory>/mqft/config/<your coordination qmgr>/agents/<your agent name>/exits/mft/samples
 *  3) Add the following line to your agent's agent.properties file
 *     sourceTransferStartExitClasses=mft.samples.FileFilterExit 
 *  4) Start your agent.
 *  5) Submit transfers
 *  
 */

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ibm.wmqfte.exitroutine.api.SourceFileExitFileSpecification;
import com.ibm.wmqfte.exitroutine.api.SourceTransferStartExit;
import com.ibm.wmqfte.exitroutine.api.TransferExitResult;

public class FileFilterExit implements SourceTransferStartExit {
	/**
	 * Lower limit of file size 
	 */
	long fileSizeLimitLow = 0;
	/**
	 * Higher limit of file size
	 */
	long fileSizeLimitHigh = Integer.MAX_VALUE;
	
	/* (non-Javadoc)
	 * @see com.ibm.wmqfte.exitroutine.api.SourceTransferStartExit#onSourceTransferStart(java.lang.String, java.lang.String, java.util.Map, java.util.Map, java.util.List)
	 */
	@Override
	public TransferExitResult onSourceTransferStart(String sourceAgentName, String destinationAgentName,
			Map<String, String> environmentMetaData, Map<String, String> transferMetaData,
			List<SourceFileExitFileSpecification> fileSpecs) {
		System.out.println("onSourceTransferStart - Entry");
	
		// Always proceed with transfer no matter what happens in the exit.
		TransferExitResult result = TransferExitResult.PROCEED_RESULT;
		
		// Use transfer meta data to determine if we need to filter files.
		// Return from if required meta data does not exist.
		String filterFiles = transferMetaData.get("FILTER_FILES");
		if( !filterFiles.equals("YES")) {
			// Return if 
			return result;
		}
		
		// List of files whose size is above give limit
		ArrayList <SourceFileExitFileSpecification> filesToExclude = new ArrayList<SourceFileExitFileSpecification>();
		
		// Read configuration file for sizes.
		readConfiguration();
		System.out.println("Transfer files of size between " + fileSizeLimitLow + " and " + fileSizeLimitHigh  + "bytes");
		
		// Exclude any file not with in the given range
		try {
			for ( SourceFileExitFileSpecification sourceFileSpec : fileSpecs) {
	             File sourceFile = new File(sourceFileSpec.getSource());

	             // Check if the file exists and it is a file and not a directory
	             if(sourceFile.exists() && sourceFile.isFile()) {
	            	 // Add to exclude list if the size is not in the given range.
	            	 if(sourceFile.length() < fileSizeLimitLow || sourceFile.length() > fileSizeLimitHigh ) {
	            		 filesToExclude.add(sourceFileSpec);
	            	 }
	             }
			}
			
            // Go through the list and remove files from transfer list.
			for (SourceFileExitFileSpecification sf : filesToExclude ) {
				fileSpecs.remove(sf);
			}				
		}catch(Exception ex) {
			System.out.println("Exception caught: " + ex);
		}finally {
			System.out.println("onSourceTransferStart - Exit");			
		}
		return result;
	}

	/**
	 * Read the accompanying configuration file for size limits
	 * 
	 * Configuration is provided in config.xml and is present in the
	 * same path as this class file.
	 *
	 * Format of XML file.
	 * <?xml version="1.0" encoding="UTF-8"?>
     * <File>
	 *    <SizeLimitLow>10000</SizeLimitLow>
	 *    <SizeLimitHigh>50000</SizeLimitHigh>
     * </File>
	 */
	private void readConfiguration() {
		try {
			InputStream is = this.getClass().getResourceAsStream("config.xml");
			
			//File file = new File("config.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(is);
			Element rootElement = document.getDocumentElement();
			
			// Read the lower range
	        NodeList list = rootElement.getElementsByTagName("SizeLimitLow");
	        if(list != null) {
	        	final String nodeValue = list.item(0).getTextContent();
	        	fileSizeLimitLow = Integer.parseInt(nodeValue);
	        }
	        
	        // Read the upper range
	        list = rootElement.getElementsByTagName("SizeLimitHigh");
	        if(list != null) {
	        	final String nodeValue = list.item(0).getTextContent();
	        	fileSizeLimitHigh = Integer.parseInt(nodeValue);
	        }
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
}

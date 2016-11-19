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
 *
 ***************************************************************************
 * UpdateFileSpec SourceTransferStartExit
 * 
 * Introduction:
 *  IBM MQ Managed File Transfer can be used for transferring files from one 
 *  point to another point in a MQ network. Apart from moving files, MFT can 
 *  also move messages from a queue and write them as files at the destination
 *  i.e. Message-to-File transfer. MFT can also do File to Message transfer.
 * 
 *  The goal of this exit is to demonstrate how to update source file list. The
 *  exit searches the source directory for matching file names but with a different
 *  extension. For example if the source file specified by the create transfer
 *  request was dailytranactions.xml, then exit would search for dailytranactions.csv
 *  dailytranactions.pdf etc and add to transfer list.
 *  
 *  The exit also demonstrates how to set source and destination metadata for the 
 *  files that are added to file list.
 *  
 *  How to configure agent to use the exit:
 *  1) Copy the compiled class files, UpdateSourceSpec.class and UpdateSourceSpec$1.class
 *     to the following directory:
 *      <mq data directory>/mqft/config/<coordination qmgr name>/agents/<agent name>/exits/mft/samples
 *    
 *   Note: If UpdateSourceSpec$1.class is not copied, then the following exception will
 *         be thrown.
 *    
 *    java.lang.ClassNotFoundException: mft.samples.UpdateFileSpec$1
 *  
 *  2) Modify source agent's agent.properties file to add following:
 *        
 *          sourceTransferStartExitClasses=mft.samples.UpdateFileSpec
 *         
 ***************************************************************************
 */
package mft.samples;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.wmqfte.exitroutine.api.SourceFileExitFileSpecification;
import com.ibm.wmqfte.exitroutine.api.SourceTransferStartExit;
import com.ibm.wmqfte.exitroutine.api.TransferExitResult;

/**
 * UpdateFileSpec implements SourceTransferStartExit interface. The entry point
 * of this class onSourceTransferStart gets invoked by the source agent before 
 * a transfer begins.
 * 
 */
public class UpdateFileSpec implements SourceTransferStartExit {
	/**
	 * Global declarations
	 */
	String fileNameWithoutExtension = null;
	String globalFileName = null;
		
	/**
	 * onSourceTransferStart - Entry point.
	 * 
	 * The method searches the source folder for matching files but with a different
	 * extension and updates the source file list.
	 * 
	 * @param sourceAgentName - Name of the source agent
	 * @param destinationAgent - Name of the destination agent
	 * @param environmentMetaData - List of environment metadata
	 * @param transferMetaData - Transfer specific metadata
	 * @param sourceFileSpecs - List of files to transfer.
	 */
	@Override
	public TransferExitResult onSourceTransferStart(String sourceAgentName, String destinationAgentName,
			Map<String, String> environmentMetaData, Map<String, String> transferMetaData,
			List<SourceFileExitFileSpecification> sourceFileSpecs) {
		
		// Always proceed with transfer no matter what happens in the exit.
		TransferExitResult result = TransferExitResult.PROCEED_RESULT;

		// List of new files found in the source folder.
		ArrayList<SourceFileExitFileSpecification> newFileSpec = new ArrayList<SourceFileExitFileSpecification>();
		
		try {
			/**
			 * The source file spec may have more than one file the list. Hence search the 
			 * source folder for matching files(but with a different extension) in the source
			 * file list.
			 * 
			 */
			for (SourceFileExitFileSpecification srcFile : sourceFileSpecs) {
				/**
				 * Get source and destination metadata from first file in source file list.
				 * The same metadata is used for the other files found in the source folder.
				 */
				Map <String, String> srcMetada = srcFile.getSourceFileMetaData();
				Map <String, String> dstMetada = srcFile.getDestinationFileMetaData();

				/**
				 * Determine if the source file specification is a folder or file.
				 */
				boolean isSrcDir = true;
				String srcType = srcMetada.get("com.ibm.wmqfte.FileType");
				if((srcType != null) && srcType.equalsIgnoreCase("file")){
					isSrcDir = false;
				}
				
				/**
				 * Search the source folder for matching files but with a different
				 * extension.
				 */
				File [] matchingFiles = searchAndBuildMatchingFileList(srcFile.getSource(), isSrcDir);
				/**
				 * Go ahead and update source file specification if there were some matching files,
				 */
				if(matchingFiles != null && matchingFiles.length > 0) {
					/**
					 * Iterate and add the files found to list of file to be transferred.
					 */
					for (File file : matchingFiles){
						/**
						 * Get a copy of the source and destination metadata for constructing the
						 * new file specification.
						 */
						Map <String, String> newSrcMetaData = new HashMap<String, String>(srcMetada);
						Map <String, String> newDstMetaData = new HashMap<String, String>(dstMetada);
						
						/**
						 * Determine if the destination is a directory or a file.
						 */
						boolean isDestDir = true;
						String destType = dstMetada.get("com.ibm.wmqfte.FileType");
						if((destType != null) && destType.equalsIgnoreCase("file")){
							isDestDir = false;
						}
						
						/**
						 * Get the fully qualified destination name
						 */
						String destPath = getPath(srcFile.getDestination(), file.getName(), isDestDir );
						
						/**
						 * Create an instance of new file spec and add to the source file list
						 */
						SourceFileExitFileSpecification newFile = new SourceFileExitFileSpecification(	file.getAbsolutePath(), 
																										destPath, 
																										newSrcMetaData, 
																										newDstMetaData);
						newFileSpec.add(newFile);				
					}	
				}
			}
			
			/**
			 * Now update the original source file specification with the files
			 * found.
			 */
			if(!newFileSpec.isEmpty()){
				sourceFileSpecs.addAll(newFileSpec);
			}
		}catch(Exception ex) {
			// System.out.println(ex);
		}finally {
			//Log			
		}
		
		return result;
	}

	/**
	 * Builds a path for source or destination file.
	 * 
	 * @param inputPath
	 * @param fileName
	 * @param isDir
	 * @return
	 */
	private String getPath(final String inputPath, String fileName, boolean isDir){
		String destPath = null;
		if(isDir) {
			destPath = inputPath + File.separator + fileName;
		} else{
			int lastIndex = inputPath.lastIndexOf(File.separator);
			destPath = inputPath.substring(0, lastIndex) + File.separator + fileName;
		}
		return destPath;
	}
	
	/**
	 * Search the source folder and build list of matching files
	 * 
	 * @param baseFileName
	 * @return
	 */
	private File [] searchAndBuildMatchingFileList(final String baseFileName, boolean isDir){
		File [] filesFound = null;
		String searchFolder = null;
		
		try {
			File fl = new File(baseFileName);
			if(fl.exists() && fl.isFile()) {
				// If it's a file, then we need to get the directory where this file exists
				String parentFolder = fl.getParent();
				if(parentFolder != null) {
					searchFolder = parentFolder;
				}
			}else {
				// This is already a directory
				searchFolder = fl.getName();
			}
			
			// Copy the base file name for using in FilenameFilter accept method
			globalFileName = fl.getName();

			fileNameWithoutExtension = getFileNameWithoutExtension(fl.getName());
			File fileSearch = new File(searchFolder);
			
			// create new filename filter to match only .csv, .xml and .xls files
	        FilenameFilter fileNameFilter = new FilenameFilter() {
	            @Override
	            public boolean accept(File dir, String name) {
	                  // Find matching file with a different extension. Exclude the original file 
	                  if(!(name.equalsIgnoreCase(globalFileName)) && (name.equalsIgnoreCase(fileNameWithoutExtension + ".csv") 
                		  || name.equalsIgnoreCase(fileNameWithoutExtension + ".xml")
                		  || name.equalsIgnoreCase(fileNameWithoutExtension + ".xls"))){
			                     return true;
	                  }
	                  return false;
	            	}
	         	};
			         
	         // List only matching files.
	         filesFound = fileSearch.listFiles(fileNameFilter);
		}catch (Exception ex){
			// System.out.println(ex);	
		} finally {
			// 	Log
		}
		return filesFound;
	}
	
	/**
	 * Get filename without extension
	 * @param fileName
	 * @return
	 */
	private String getFileNameWithoutExtension(final String fileName){
		// Sanity checks
		if(fileName == null)
			return null;

		// Find the index of last '.'
		int posLastDot = fileName.lastIndexOf('.');
		
		// There was no extension, then return as it is
		if(posLastDot == -1) return fileName;
		
		//Return filename without extension.
		String fileNameWoExtn = fileName.substring(0, posLastDot); 
		
		return fileNameWoExtn;
	}
}

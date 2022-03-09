package mft.merge.files;
/**
 * Copyright (c) IBM Corporation 2022
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
 * How to use:
 * 1) Clone the source to your file system.
 * 2) Reference com.ibm.wmqfte.exitroutine.api jar file from MQ MFT installation
 * 3) Build the code and package as, say mft.merge.files.jar and copy into your destination
 *    agents "exits" directory.
 * 4) Add the following attribute in agent.properties file.
 *    destinationTransferEndExitClasses=mft.merge.files.MergeFilesEndExit
 *    destinationTransferStartExitClasses=mft.merge.files.MergeFileStartExit
 * 5) Restart your destination agent and submit transfer request
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.ibm.wmqfte.exitroutine.api.DestinationTransferEndExit;
import com.ibm.wmqfte.exitroutine.api.FileExitResultCode;
import com.ibm.wmqfte.exitroutine.api.FileTransferResult;
import com.ibm.wmqfte.exitroutine.api.TransferExitResult;
import com.ibm.wmqfte.exitroutine.api.TransferExitResultCode;

/**
 * A custom Destination Transfer End Exit.
 * 
 * This exit gets invoked at the destination agent after the transfer 
 * of data is completed. This exit appends the data from the newly 
 * transferred file to an existing file with same name but with extension
 * .ori.
 *  
 */
public class MergeFilesEndExit implements DestinationTransferEndExit {
	 private static final int BUFFER_SIZE = 4096;	
	@Override
	public String onDestinationTransferEnd(TransferExitResult transferExitResult, String sourceAgentName, 
			String destinationAgentName, Map<String, String> environmentMetaData,
			Map<String, String> transferMetaData, List<FileTransferResult> fileResults) {
		// Proceed if transfer is OK
		if(transferExitResult.getResultCode() == TransferExitResultCode.PROCEED) {
			apendFiles(fileResults);
		}
		// Simply return any value
		return "RESULT";
	}

	/**
	 * Iterate through the list of files that have transferred successfully
	 * and append data to existing file, if any.
	 * @param fileResults
	 */
	private void apendFiles(List<FileTransferResult> fileResults) {		 
		// Iterate through the results and append to existing file
		for (FileTransferResult ftr : fileResults) {
			// Proceed if transfer result is OK
			if (ftr.getExitResult().getResultCode() == FileExitResultCode.PROCEED) {
				String destFileStr = ftr.getDestinationFileSpecification();
				String originalFileStr = destFileStr + ".ori";
				File originalFile = new File(originalFileStr);
				if (originalFile.exists() && originalFile.isFile()) {
					// File exists, so append the new data.
					appendFile(destFileStr, originalFileStr);
				}
			}
		}
	}

	/**
	 * Append data of a file to an existing file
	 * @param sourceFile
	 * @param destFile
	 */
	private void appendFile(String sourceFile, String destFile) {
		InputStream inputStream = null;
		OutputStream outputStream = null;
		
		try {       
			inputStream = new FileInputStream(sourceFile);
			outputStream = new FileOutputStream(destFile,true);
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			inputStream.close();
			outputStream.close();
			// Delete source file
			java.nio.file.Files.delete(new File(sourceFile).toPath());
			// Rename destination file
			new File(destFile).renameTo(new File(sourceFile));
		} catch (Exception ex) {
			// Catch any exception and dump to agent's output0.log file
			ex.printStackTrace();
		} 
	}
}

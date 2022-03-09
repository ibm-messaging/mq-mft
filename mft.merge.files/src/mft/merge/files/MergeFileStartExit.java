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
 * 3) Build the code and package as a jar, say mft.merge.files.jar and copy into 
 *    your destination agents "exits" directory.
 * 4) Add the following attribute in agent.properties file.
 *    destinationTransferEndExitClasses=mft.merge.files.MergeFilesEndExit
 *    destinationTransferStartExitClasses=mft.merge.files.MergeFileStartExit
 * 5) Restart your destination agent and submit transfer request
 *
 */
import java.io.File;
import java.util.List;
import java.util.Map;

import com.ibm.wmqfte.exitroutine.api.DestinationTransferStartExit;
import com.ibm.wmqfte.exitroutine.api.Reference;
import com.ibm.wmqfte.exitroutine.api.TransferExitResult;

/**
 * A custom Destination Transfer Start Exit.
 * 
 * This exit gets invoked at the destination agent before the transfer 
 * of data begins. This exit simply renames any existing file with an
 * extension of .ori. This renamed file is then used as file and data
 * from the newly transferred file will be appended to this file in the
 * accompanying implementation of DestinationTransferEndExit.
 *  
 */
public class MergeFileStartExit implements DestinationTransferStartExit {
	@Override
	public TransferExitResult onDestinationTransferStart(String sourceAgentName, String destinationAgentName, 
			Map<String, String> environmentMetaData,
			Map<String, String> transferMetaData, 
			List<Reference<String>> fileSpecs) {
		// Iterate through and rename if the destination file exists.
		try {
			for (int i=0; i< fileSpecs.size(); i++) {
				Reference<String> ref = fileSpecs.get(i);
				String destPath = ref.dereference().toString();
				File originalFile = new File(destPath);
				if(originalFile.exists() && originalFile.isFile()) {
					File renamedFile = new File(destPath + ".ori");
					originalFile.renameTo(renamedFile);
				}
			}
		}catch (Exception ex) {
			// Trace the exception for diagnostic purpose.
			System.out.println("DestinationTransferStart:" + ex);
		}
		
		// Go ahead with transfer irrespective of any error above.
		TransferExitResult result = TransferExitResult.PROCEED_RESULT;
		return result;
	}
}

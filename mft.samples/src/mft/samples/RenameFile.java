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
 * RenameFile DestinationTransferStartExit
 * 
 * Introduction:
 *  IBM MQ Managed File Transfer can be used for transferring files from one 
 *  point to another point in a MQ network. Apart from moving files, MFT can 
 *  also move messages from a queue and write them as files at the destination
 *  i.e. Message-to-File transfer. MFT can also do File to Message transfer.
 * 
 *  The Message-to-File transfer helps in integrating applications in solution 
 *  where one application generates it's output as messages but the next application  
 *  in the solution that needs process the information contained in the messages 
 *  does not have the capability to handle messages as it requires input to be in
 *  files. 
 *   
 *  There can be scenarios where, in a Message-to-File transfer, the name of the
 *  destination file is required to be generated dynamically and is controlled by
 *  attribute on the first message. Typically a message propertis and variable
 *  substitution technique is used for this purpose as described in the link below:
 *  http://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.wmqfte.doc/m2f_mon_variable.htm 
 * 
 *  However there can be cases where a legacy applications putting messages to 
 *  a queue does not have ability to set message properties. The application can
 *  set MQMD attributes though. Because of this the destination file name can't be
 *  dynamically controlled and set by the sender. This exit (and combination with 
 *  accompanying InsertMetadata exit) helps in such scenario.
 *
 *  What does this exit do: 
 *  1) The exit implements DestinationTransferStartExit interface. Hence gets called 
 *     by the destination agent when a transfer is about to start.
 *  2) Retrieves the source queue name and queue manager name from m2fSourceName metadata key.
 *  3) Retrieves transfer metadata and identifies if it's a Message-to-File transfer.
 *  4) Retrieves the destination filename from m2fDestFileName metadata key.
 *  5) Updates the destination filename fileSpecs parameters.
 *
 *  How to configure agent to use the exit:
 *  1) Modify destination agent's agent.properties file to add following:
 *     destinationTransferStartExitClasses=mft.samples.RenameFile
 ***************************************************************************
 */
package mft.samples;

import java.util.List;
import java.util.Map;

import com.ibm.wmqfte.exitroutine.api.DestinationTransferStartExit;
import com.ibm.wmqfte.exitroutine.api.Reference;
import com.ibm.wmqfte.exitroutine.api.TransferExitResult;

/**
 * @author shashikanth
 *
 * Implements DestinationTransferStartExit for transfer customization
 *
 */
public class RenameFile implements DestinationTransferStartExit {
	/* (non-Javadoc)
	 * @see com.ibm.wmqfte.exitroutine.api.DestinationTransferStartExit#onDestinationTransferStart(java.lang.String, java.lang.String, java.util.Map, java.util.Map, java.util.List)
	 */
	@Override
	public TransferExitResult onDestinationTransferStart(String sourceAgentName, String destinationAgentName, Map<String, String> environmentMetaData,
			Map<String, String> transferMetaData, List<Reference<String>> fileSpecs) {

		logInfo("RenameFile.onDestinationTransferStart","Entry", sourceAgentName, destinationAgentName);
		
		/** Always proceed with transfer no matter what happens in the exit. **/
		TransferExitResult result = TransferExitResult.PROCEED_RESULT;
		
		try {
	        String dstFileName = fileSpecs.get(0).dereference();
			logInfo("RenameFile.onDestinationTransferStart",  "fileName: " + dstFileName);
			
			/** Get the required keys from transfer metadata **/
			String changedFileName = transferMetaData.get("m2fDestFileName");
			String sourceName = transferMetaData.get("m2fSourceName");
			
			/** If the source name contains @ symbol, then the source is a queue. **/
			if(sourceName.contains("@")) {
				if(changedFileName != null){
					fileSpecs.get(0).assign(changedFileName);
					logInfo("RenameFile.onDestinationTransferStart", "Changed FileName: " + fileSpecs.get(0).dereference());
				}
				else {
					logInfo("RenameFile.onDestinationTransferStart", "destination filename property not found ");
				}				
			}
		}catch (Exception ex) {
			logException("RenameFile.onDestinationTransferStart",  ex);
		}finally {
			logInfo("RenameFile.onDestinationTransferStart", "Exit");
		}
		return result;
	}

	/**
	 * Write log to console
	 * @param method
	 * @param params
	 */
	private void logInfo(final String method, String... params){
		StringBuilder sb = new StringBuilder();
		
		sb.append(method);
		
		for(String param : params){
			sb.append("[");
			sb.append(param);
			sb.append("]");
		}
		System.out.println(sb.toString());
	}
	
	/**
	 * Writes an exception log
	 * @param method
	 * @param ex
	 */
	private void logException(final String method, final Exception ex) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(method);
		sb.append(ex);
		System.out.println(sb.toString());
	}
}

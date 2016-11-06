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
 * InsertMetadata SourceTransferStartExit
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
 *  accompanying RenameFile exit) helps in such scenario.
 *
 *  What does this exit do: 
 *  1) The exit implements SourceTransferStartExit interface. Hence gets called 
 *     by the source agent when a transfer is about to start.
 *  2) Identifies the transfer as Message-to-File by looking at FileSpecs parameter.
 *  3) Retrieves the source queue name and queue manager name.
 *  4) Connects to queue manager with predefined connection attributes.
 *  5) Opens the source queue and browses the first message.
 *  6) Retrieves the applIdentityData from the browsed message.
 *  7) Inserts the retrieved applIdentityData as transfer metadata
 *  8) Also inserts Source Queue and Queue Manager name in the transfer metadata.
 *   
 *  Possible Extensions:
 *  1) At present, the exit retrieves the applIdentityData of a message. The exit
 *     can be modified to look for any other suitable MQMD attribute.
 *
 *  2) Currently the exit always browses the first message on the queue to retrieve
 *     the applIdentityData of the message. This exit can be modified to browse the
 *     first message in a group. Such modification is required when doing M2F transfer
 *     where messages are in a group.
 *
 *  How to configure agent to use the exit:
 *  1) Modify source agent's agent.properties file to add following:
 *        
 *          sourceTransferStartExitClasses=mft.samples.InsertMetadata
 *          enableQueueInputOutput=true
 *     If using MQ v8 or above, add the following to point MQ Java libraries.
 *          exitClassPath=<mq installation>/java/lib/com.ibm.mq.allclient.jar; 
 *     
 *     If using MQ v7.5 or earlier, add the following to point MQ Java libraries.
 *          exitClassPath=<mq installation>/java/lib/com.ibm.mq.jar;
 *                        <mq installation>/java/lib/com.ibm.mq.commonservices.jar; 
 *						  <mq installation>/java/lib/com.ibm.mq.jmqi.jar
 *         
 ***************************************************************************
 */
package mft.samples;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.wmqfte.exitroutine.api.SourceFileExitFileSpecification;
import com.ibm.wmqfte.exitroutine.api.SourceTransferStartExit;
import com.ibm.wmqfte.exitroutine.api.TransferExitResult;

/**
 * @author shashikanth
 * Implements SourceTransferStartExit to customize transfer.
 *
 */
public class InsertMetadata implements SourceTransferStartExit {
	private String sourceQueue = null;
	private String sourceQM = null;
	private String sourceQMChannel = null;
	private String sourceQMHost = null;
	private String sourceQMUser = null;
	private String sourceQMPassword = null;
	private int    sourceQMPort = 1414;
	
	/* (non-Javadoc)
	 * @see com.ibm.wmqfte.exitroutine.api.SourceTransferStartExit#onSourceTransferStart(java.lang.String, java.lang.String, java.util.Map, java.util.Map, java.util.List)
	 */
	@Override
	public TransferExitResult onSourceTransferStart(String sourceAgentName, String destinationAgentName,
			Map<String, String> environmentMetaData, Map<String, String> transferMetaData,
			List<SourceFileExitFileSpecification> sourceQueueSpecs) {

		logInfo("InsertMetadata.onSourceTransferStart", "Entry");
		
		/** Always proceed with transfer no matter what happens in the exit. **/
		TransferExitResult result = TransferExitResult.PROCEED_RESULT;
		
		try {
			/** In a Message to File Transfer there can be only one entry in fileSpecs. If there are
			 * are more than one entries, then this is not M2F transfer.
			 */
			if(sourceQueueSpecs.size() == 1){
				/**
				 * OK we have just one entry in the list. But this can be F2M or F2F transfer with just
				 * one file in the list. But thankfully, in M2F transfers, the filespec will be of the
				 * QNAME@QMGR format. So check further if the filespec contains an @ character
				 */
				SourceFileExitFileSpecification sourceQueueSpec = sourceQueueSpecs.get(0);
				String queueWithQMName = sourceQueueSpec.getSource();
				
				if(queueWithQMName.contains("@")) {
					String [] mqNames = queueWithQMName.split("@");
					sourceQueue = mqNames[0];
					sourceQM = mqNames[1];
					logInfo("InsertMetadata.onSourceTransferStart", "Source Queue: " + sourceQueue, "Source QM: " + sourceQM);

					/**
					 * This example exit connects to queue manager in clients mode. Hence set other connection parameters
					 * For simplicity, values have been hard coded. It's recommended to read the values from agent.properties
					 * or any configuration file. Modify below properties per your need
					 */
					sourceQMChannel = "APPSVRCONN";
					sourceQMHost = "localhost";
					sourceQMUser = "mquserid";
					sourceQMPassword = "Passw0rd";
					sourceQMPort = 1414;

					/** Get application identity data **/
					String applIdentityData = getApplicationIdentity();
					/** There is something in application identity data set it transfer metadata **/
					if(applIdentityData != null && !applIdentityData.isEmpty()) {
						try {
							transferMetaData.put("m2fDestFileName", applIdentityData);
							transferMetaData.put("m2fSourceName",queueWithQMName);
							logInfo("InsertMetadata.onSourceTransferStart","destination file name: " + applIdentityData);							
						}catch (Exception ex){
							logException("InsertMetadata.onSourceTransferStart", ex);														
						}
					}
				}
			} else {
				/** Simply continue with PROCEED_RESULT **/
				logInfo("InsertMetadata.onSourceTransferStart", "Not a M2F Transfer");							
			}
		}catch(Exception ex){
			logException("InsertMetadata.onSourceTransferStart", ex);
		}finally {
			logInfo("InsertMetadata.onSourceTransferStart","Exit");			
		}
		return result;
	}

	/**
	 * Gets the MQMD.applicationIdentity of the first message found on the source queue
	 * @param sourceQueue
	 * @param sourceQM
	 * @return ApplicationIdentity 
	 */
	private String getApplicationIdentity(){
		logInfo("InsertMetadata.getApplicationIdentity", "Entry");

		String applIdentity = null;
		MQQueueManager qm = null;
		MQQueue queue = null;
		
		try {
			/**
			 * Connect to queue manager 
			 */
			Hashtable<Object, Object> props = new Hashtable<Object, Object>();
			props.put(MQConstants.HOST_NAME_PROPERTY, sourceQMHost);
			props.put(MQConstants.PORT_PROPERTY, sourceQMPort);
			props.put(MQConstants.CHANNEL_PROPERTY, sourceQMChannel);
			props.put(MQConstants.USER_ID_PROPERTY, sourceQMUser);
			props.put(MQConstants.PASSWORD_PROPERTY, sourceQMPassword);
			
			qm = new MQQueueManager(sourceQM, props);
			queue = qm.accessQueue(sourceQueue, MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_BROWSE | MQConstants.MQOO_FAIL_IF_QUIESCING);
			
			/** Browse the first message and get it's ApplicationIdentity **/
			MQMessage firstMessage = new MQMessage();
			MQGetMessageOptions mqgmo = new MQGetMessageOptions();
			/** Wait for 3 seconds. **/
			mqgmo.waitInterval = 3000;
			queue.get(firstMessage, mqgmo);
			applIdentity = firstMessage.applicationIdData;
		}catch(Exception ex) {
			logException("InsertMetadata.getApplicationIdentity", ex);									
		}finally {
			try {
				if(queue != null)
					queue.close();
				if(qm != null)
					qm.disconnect();
			}catch(Exception ex){
				logException("InsertMetadata.getApplicationIdentity", ex);
			}	
			logInfo("InsertMetadata.getApplicationIdentity", "Exit");
		}
		return applIdentity;
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

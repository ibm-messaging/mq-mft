/**
 * 
 */
package mft.secure;
/**
 * Copyright (c) IBM Corporation 2017
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

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.ibm.wmqfte.exitroutine.api.SourceFileExitFileSpecification;
import com.ibm.wmqfte.exitroutine.api.SourceTransferStartExit;
import com.ibm.wmqfte.exitroutine.api.TransferExitResult;
import com.ibm.wmqfte.exitroutine.api.TransferExitResultCode;

/**
 * @author shashikanth
 *
 *  Encrypt file(s) at Source IBM MQ Managed File Transfer Exit.
 * 
 *  There are scenarios where it is required to encrypt file(s) at source 
 *  before they are transferred to destination agent. The encrypted files   
 *  are then either decrypted at destination or processed in encrypted 
 *  form itself. 
 * 
 *  This exit extends the MFT functionality by providing the ability to 
 *  encrypt file(s) files at source agent. This exit uses BouncyCastle
 *  APIs to encrypt and decrypt the files. The exit is based on the sample
 *  https://github.com/damico/OpenPgp-BounceCastle-Example/blob/master/src/org/jdamico/bc/openpgp/utils/PgpHelper.java
 */	

 public class EncryptAtSource implements SourceTransferStartExit {	
	// Flag to enable/disable logging. The logs are written to agent's output0.log file
	private boolean enableDebugLog = false;
	private Properties configurationProperties = null;
	private boolean encryptAtSource = false;
	private String publicKeyFile = null;
	/* (non-Javadoc)
	 * @see com.ibm.wmqfte.exitroutine.api.SourceTransferStartExit#onSourceTransferStart(java.lang.String, java.lang.String, java.util.Map, java.util.Map, java.util.List)
	 */
	@Override
	public TransferExitResult onSourceTransferStart(String sourceAgentName, String destinationAgentName, Map<String, String> environmentMetaData,
			Map<String, String> transferMetaData, List<SourceFileExitFileSpecification> sourceFileSpecs) {
				
		// Set PROCEED as initial return code.
		TransferExitResult result = TransferExitResult.PROCEED_RESULT;
		
		try {
			// Load properties from configuration file, decenc.properties located in the current directory
			loadProperties();

			if(enableDebugLog)Trace.logInfo("EncryptAtSource.onSourceTransferStart", "Entry");

			if(encryptAtSource){
				if(enableDebugLog)Trace.logInfo("EncryptAtSource.onSourceTransferStart","Encrypt at Source = YES");
				// We don't need passphrase for encryption
				CryptDecryptUtil cdu = new CryptDecryptUtil(publicKeyFile, null, enableDebugLog);
				encrypt(cdu, sourceFileSpecs);
			} else {
				if(enableDebugLog)Trace.logInfo("EncryptAtSource.onSourceTransferStart","Encrypt at Source = NO");
			}
		}catch (Exception ex){
			// An exception occurred, cancel the transfer. This can be changed based on the requirement.
			Trace.logException("EncryptAtSource.onSourceTransferStart", ex);
			result = new TransferExitResult(TransferExitResultCode.CANCEL_TRANSFER, "Encryption failed: " + ex);;
		} finally {
			if(enableDebugLog)Trace.logInfo("EncryptAtSource.onSourceTransferStart", "Exit");			
		}
		
		return result;
	}

	/**
	 * Encrypt file before sending
	 * @param cdu
	 * @param sourceFileSpecs
	 * @throws Exception
	 */
	private void encrypt(CryptDecryptUtil cdu, List<SourceFileExitFileSpecification> sourceFileSpecs) throws CryptDecryptException {
		if(enableDebugLog)Trace.logInfo("EncryptAtSource.encrypt - Entry");
		// Iterate through the list of files and encrypt them. 
		for ( SourceFileExitFileSpecification sourceFileSpec : sourceFileSpecs) {
			final String unencryptedFile = sourceFileSpec.getSource();
            File sourceFile = new File(unencryptedFile);
            
            // Check if the file exists and it is a file and not a directory
            if(sourceFile.exists() && sourceFile.isFile()) {
            	// Encrypted file will have an extension of .enc. The unencrypted file is deleted
            	// after successfull encryption and encrypted file is renamed with original file name.
            	final String encryptedFileName = unencryptedFile +".enc";
            	try {
            		// First encrypt the file
					cdu.encryptFile(unencryptedFile, encryptedFileName);
					// delete the source file
					sourceFile.delete();
					// rename the encrypted file to source file name 
	            	File destFile = new File (encryptedFileName);
					destFile.renameTo(sourceFile);
				} catch (Exception ex) {
					Trace.logException ("EncryptAtSource.encrypt", ex);
		        	throw new CryptDecryptException("Failed to encrypt file" + ex);
				}
            }
		}
		if(enableDebugLog) Trace.logInfo("EncryptAtSource.encrypt", "Exit");			
	}

	/**
	 * Load configuration properties from decenc.properties file
	 * @throws Exception
	 */
	private void loadProperties() throws CryptDecryptException {
		try {
			configurationProperties = new Properties();
			File jarPath=new File(EncryptAtSource.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		    String propertiesPath=jarPath.getParentFile().getAbsolutePath();
		    String configFilePath = propertiesPath +"/decenc.properties";
		    //Trace.logInfo("Configuration File Path "+ configFilePath);
			configurationProperties.load(new FileInputStream(configFilePath));
			
			// Write log to agent's outputN.log file if debug log is enabled
			String propDebugLog = configurationProperties.getProperty("enableDebugLog","false");
			if((propDebugLog != null) && (propDebugLog.equalsIgnoreCase("true"))) {
				enableDebugLog = true;
			}

			// Do encryption of file if asked for
			String propEncryptAtSource= configurationProperties.getProperty("encryptAtSource","false");
			if((propEncryptAtSource != null) && (propEncryptAtSource.equalsIgnoreCase("true"))) {
				encryptAtSource = true;
			}

			// Name of the file containing the public key
			publicKeyFile = configurationProperties.getProperty("publicKeyFile", null);
		}catch(Exception ex) {
			Trace.logException ("EncryptAtSource.loadProperties", ex);
			throw new CryptDecryptException("Failed to load properties" + ex);
		}
	}
}

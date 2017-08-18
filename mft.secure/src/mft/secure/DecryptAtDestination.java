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

import com.ibm.wmqfte.exitroutine.api.DestinationTransferEndExit;
import com.ibm.wmqfte.exitroutine.api.FileTransferResult;
import com.ibm.wmqfte.exitroutine.api.TransferExitResult;

/**
 * @author shashikanth
 *
 *  Decrypt file(s) at Source IBM MQ Managed File Transfer Exit.
 * 
 *  There are scenarios where it is required to encrypt file(s) at source 
 *  before they are transferred to destination agent. The encrypted files   
 *  are then either decrypted at destination or processed in encrypted 
 *  form itself. 
 * 
 *  This exit extends the MFT functionality by providing the ability to 
 *  decrypt file(s) files at destination agent. This exit uses BouncyCastle
 *  APIs to encrypt and decrypt the files. The exit is based on the sample
 *  https://github.com/damico/OpenPgp-BounceCastle-Example/blob/master/src/org/jdamico/bc/openpgp/utils/PgpHelper.java
 * 
 *  
 */
public class DecryptAtDestination implements DestinationTransferEndExit {
	
	// Flag to enable/disable logging. The logs are written to agent's output0.log file
	boolean enableDebugLog = false;
	private boolean decryptAtDestination = false;
	private String privateKeyFile = null;
	private String passphrase = null;
	
	/* (non-Javadoc)
	 * @see com.ibm.wmqfte.exitroutine.api.DestinationTransferEndExit#onDestinationTransferEnd(com.ibm.wmqfte.exitroutine.api.TransferExitResult, java.lang.String, java.lang.String, java.util.Map, java.util.Map, java.util.List)
	 */
	@Override
	public String onDestinationTransferEnd(TransferExitResult transferResult, String sourceAgent, String destinationAgent, Map<String, String> environmentData,
			Map<String, String> transferMetaData, List<FileTransferResult> fileResults) {		
		try {
			// Load properties from configuration file, decenc.properties located in the current directory			
			loadProperties();
			if(enableDebugLog)Trace.logInfo("DecryptAtDestination.onDestinationTransferEnd", "Entry");			
						
			if(decryptAtDestination) {
				if(enableDebugLog)Trace.logInfo("DecryptAtDestination.onDestinationTransferEnd","Decrypt at Destination = Y");
				
				if(privateKeyFile == null) 
					throw new CryptDecryptException("Private key file property not specified.");
				
				if(passphrase == null) 
					throw new CryptDecryptException("Passphrase for private key file not specified.");
				
				CryptDecryptUtil cdu = new CryptDecryptUtil(privateKeyFile, passphrase, enableDebugLog);
				decrypt(cdu, fileResults);
			} else {
				if(enableDebugLog)Trace.logInfo("DecryptAtDestination.onDestinationTransferEnd","Decrypt at Destination = NO");
			}
		}catch (Exception ex){
			Trace.logException("DecryptAtDestination.onDestinationTransferEnd", ex);
		} finally {
			if(enableDebugLog)Trace.logInfo("DecryptAtDestination.onDestinationTransferEnd", "Exit");			
		}
		
		return "";
	}

	private void decrypt(CryptDecryptUtil cdu, List<FileTransferResult> fileResults) throws CryptDecryptException {
		if(enableDebugLog)Trace.logInfo("DecryptAtDestination.decrypt", "Entry");
		
		try {
			for ( FileTransferResult destFileSpec : fileResults) {
				String encryptedFile = destFileSpec.getDestinationFileSpecification();
				if(enableDebugLog)Trace.logInfo("DecryptAtDestination.decrypt", encryptedFile );
				
	            File encryptedDestFile = new File(encryptedFile);
	            
	            // Check if the file exists and it is a file and not a directory
	            if(encryptedDestFile.exists() && encryptedDestFile.isFile()) {
	            	final String decryptedDestFile = encryptedDestFile.getAbsolutePath() +".dec";
	            	if(enableDebugLog)Trace.logInfo("DecryptAtDestination.decrypt - Decrypted file", decryptedDestFile);
	            	File decryptedFile = new File (decryptedDestFile);
	            	
            		// First encrypt the file
            		cdu.decryptFile(encryptedFile, decryptedDestFile);
					// delete the source file
            		encryptedDestFile.delete();
            		if(enableDebugLog)Trace.logInfo("DecryptAtDestination.decrypt - Encrypted file deleted", encryptedFile);
					// rename the encrypted file to source file name 
            		decryptedFile.renameTo(new File(encryptedFile));
	            }
			}
		}catch (Exception ex) {
			Trace.logException ("DecryptAtDestination.decrypt", ex);
        	throw new CryptDecryptException("Failed to decrypt file " + ex);
		} finally{
			if(enableDebugLog)Trace.logInfo("DecryptAtDestination.decrypt", "Exit");			
		}
	}
	
	/**
	 * Load configuration properties from decenc.properties file
	 * @throws Exception
	 */
	private void loadProperties() throws CryptDecryptException {
		try {
			// Configuration properties
			Properties configurationProperties = new Properties();
			File jarPath=new File(EncryptAtSource.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		    String propertiesPath=jarPath.getParentFile().getAbsolutePath();
		    String configFilePath = propertiesPath +"/decenc.properties";
			configurationProperties.load(new FileInputStream(configFilePath));

			// Write log to agent's outputN.log file if debug log is enabled
			String propDebugLog = configurationProperties.getProperty("enableDebugLog", "false");
			if((propDebugLog != null) && (propDebugLog.equalsIgnoreCase("true"))) {
				enableDebugLog = true;
			}

			// Do encryption of file if asked for
			String propEncryptAtSource= configurationProperties.getProperty("decryptAtDestination", "false");
			if((propEncryptAtSource != null) && (propEncryptAtSource.equalsIgnoreCase("true"))) {
				decryptAtDestination = true;
			}

			// Name of the file containing the public key
			privateKeyFile = configurationProperties.getProperty("privateKeyFile", null);
			
			// Get passphrase for private keystore. The passphrase will be in stored as a key-value
			// pair, passphrase=<some password> in cryptdecrypt.pwd file located in user's home 
			// directory. This is best security we can provide.
			final String userHomeDir = System.getProperty("user.home");
			if(userHomeDir != null && !userHomeDir.trim().equals("")) {
				Properties passwordProperties = new Properties();
				passwordProperties.load(new FileInputStream(userHomeDir + "/cryptdecrypt.pwd"));
				passphrase = passwordProperties.getProperty("passphrase", null);
			}
		}catch(Exception ex) {
			throw new CryptDecryptException("Failed to load configuration properties. " + ex);
		}
	}
}

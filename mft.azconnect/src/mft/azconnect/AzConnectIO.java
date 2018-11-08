/**
 * AzConnectIO.java
 * 
 * The main entry point to exit. MFT agent will load this class and
 * resolve all other required classes for doing custom IO to Azure Blob
 * Storage.
 */
package mft.azconnect;

import java.io.IOException;
import java.util.Map;

import com.ibm.wmqfte.exitroutine.api.IOExit;
import com.ibm.wmqfte.exitroutine.api.IOExitPath;
import com.ibm.wmqfte.exitroutine.api.IOExitRecordResourcePath.RecordFormat;

/**
 * Copyright (c) IBM Corporation 2018
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
 * azconnect AzConnectBlob
 * 
 * Introduction:
 *  IBM MQ Managed File Transfer can be used for transferring files from one 
 *  point to another point in a MQ network. It can also move messages from a 
 *  IBM queue (source) and write them as file(s) at destination (i.e. message
 *  to file transfer) and vice versa.
 *  
 *  MFT agents uses built-in I/O providers to interact with file systems for
 *  the transfer. MFT agents are capable of interacting with UNIX and Windows
 *  file systems, Data sets and partitioned data sets on z/OS, IBM i native file 
 *  system, IBM MQ queue, FTP/SFTP/FTPS file serves. If the file system you are
 *  using is not supported by MFT, then you can develop your own I/O Exit that
 *  interact with your file system to transfer files.
 *  
 *  This sample implements MFT IOExit to interact with Azure Blob Storage. It uses
 *  the I/O libraries provided by Microsoft.
 * 
 *  How to build the IOExit:
 *  1) Download the Azure Storage Java SDK from GitHub (https://github.com/Azure/azure-storage-java)
 *  2) Create a Java project in Eclipse and add these source files. 
 *  3) Add azure-storage-5.0.0.jar
 *          com.ibm.wmqfte.exitroutines.api.jar
 *          jackson-core-2.9.4.jar
 *          slf4j-api-1.7.25.jar
 *      to project as references.
 *  4) Compile the source files and package as a jar, say mft.azconnect.jar
 *  
 *  How configure an agent use AzConnect IOExit.
 *  1) Copy the AzConnect.jar to exits folder under an agent configuration folder. 
 *     For example C:\ProgramData\IBM\MQ\mqft\config\MFT\agents\DEST\exits.
 *  2) Also copy the following jars to exits folder:
 *         azure-storage-5.0.0.jar
 *          com.ibm.wmqfte.exitroutines.api.jar
 *          jackson-core-2.9.4.jar
 *          slf4j-api-1.7.25.jar
 *  3) Add the following to agent.properties file
 *   IOExitClasses=com.ibm.wmqfte.azconnect.AzConnectIO
 *  4) Stop and start the agent.
 *  
 *  How to test:
 *  1) Create a container in Azure Blob Storage, say with a name "mftcontainer".
 *  2) Submit a transfer request. The following command was run against Azure Storage Emulator running on local machine.
 *  fteCreateTransfer -rt -1 -sa SRC -sm SRCAGQM -da AZURE_STRG -dm AZQM -de overwrite -df "BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1/mftcontainer/azfile.txt" "C:\SRC\azfile.txt"
 *  
 *  Current restrictions:
 *  1) The IOExit does not have the capability to resume transfers. So transfers fail
 *     if the destination agent that interacts with Azure Storage restarts.
 *         
 ***************************************************************************
 */
public class AzConnectIO implements IOExit {
	/**
	 * Creates a client connection to Azure Storage.
	 */
	@Override
	public boolean initialize(Map<String, String> properties) {
		final String fid = "initialize";
		
		boolean retValue = false;
		try {
			AzConnectTrace.setup();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		AzConnectTrace.entry(this, fid, properties);

		try {
		    // Initialize a singleton connection to Azure Storage
			AzConnectIOUtil.getInstance();
		    retValue = true;			
		}catch (Exception ex) {
			AzConnectTrace.throwing(this, fid, ex);
		}
		AzConnectTrace.exit(this, fid);
	
		return retValue;
	}

	@Override
	public boolean isSupported(String arg0) {
		final String fid = "isSupported";
		AzConnectTrace.entry(this, fid, arg0);
		AzConnectTrace.exit(this, fid);
		return true;
	}

	/**
	 * Creates and returns an instance of AzConnectIOResourcePath class to
	 * MFT which then uses that instance to IO.
	 */
	@Override
	public IOExitPath newPath(String newPath) throws IOException {
		final String fid = "newPath";
		AzConnectTrace.entry(this, fid, newPath);
		
		AzConnectIOResourcePath azBlobPath = null;
		try {			
			azBlobPath = new AzConnectIOResourcePath(newPath);
		}catch (Exception ex) {
			AzConnectTrace.throwing(this, fid, ex);
		}
		
		AzConnectTrace.exit(this, fid, (Object)azBlobPath);
		return azBlobPath;
	}

	/**
	 * Extended version of newPath method.
	 * Not implemented 
	 */
	@Override
	public IOExitPath newPath(String arg0, RecordFormat arg1, int arg2) throws IOException {
		final String fid = "newPath";
		AzConnectTrace.entry(this, fid, arg0, arg2);
		AzConnectTrace.exit(this, fid);
		return null;
	}
}

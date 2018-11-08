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
 */
package mft.azconnect;

import java.util.ArrayList;
import java.util.List;

import com.ibm.wmqfte.exitroutine.api.IOExitResourcePath;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

public class AzConnectIOUtil {
	public String azStorageConnectionString = null;

	private static AzConnectIOUtil singletonConnection = new AzConnectIOUtil();

	private CloudBlobContainer blobContainer = null;

	private AzConnectIOUtil() {
		AzConnectTrace.entry(this, "<init>");
		azStorageConnectionString = "DefaultEndpointsProtocol=http;" +
					                "AccountName=<replace with your account name>;" +
					                "AccountKey=<replace with your account key>;";
		AzConnectTrace.exit(this, "<init>");
	}
	
	private CloudBlobClient getBlobClient(final String blobPath) {
		CloudBlobClient localBlobClient = null;
		AzConnectTrace.entry(this, "getBlobClient");
		
		try {
		    // Retrieve storage account from connection-string.
		    CloudStorageAccount storageAccount = CloudStorageAccount.parse(azStorageConnectionString + blobPath);

		    // Create the blob client.
		    localBlobClient = storageAccount.createCloudBlobClient();
		} catch(Exception ex) {
			AzConnectTrace.throwing(this,"getBlobClient", ex);
		}
		
		AzConnectTrace.exit(this, "getBlobClient", localBlobClient);
		return localBlobClient;
	}
	
	/**
	 * Returns an instance of connection instance to Azure Blob Storage
	 * @return AzConnectUtil instance
	 */
	public static AzConnectIOUtil getInstance() {
		return singletonConnection;
	}
	
	public AzConnectStorage getBlockBlob(final String blobPath) throws Exception {
		final String fid = "getBlockBlob";
		String containerName = null;
		String blockBlobName = null;
		AzConnectTrace.entry(this, "getBlockBlob");
		AzConnectStorage storage = null;
		
		try {
			/**
			 * The blob path takes the form: 
			 * BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1/<container name>/<block name>
			 * 
			 */
			String [] pathTokens = blobPath.split("/");
			if(pathTokens.length == 6) {
				CloudBlockBlob blockBlob = null;
				containerName = pathTokens[4];
				blockBlobName = pathTokens[5];
				blobContainer = getBlobClient(blobPath).getContainerReference(containerName);
				blockBlob = blobContainer.getBlockBlobReference(blockBlobName);
				storage = new AzConnectBlob(AzConnectConstants.AZ_CONNECT_STORAGE_BLOB, blockBlob);
			} else if(pathTokens.length == 5) {
				AzConnectTrace.data(this, fid, "Get container name");
				containerName = pathTokens[4];
				blobContainer = getBlobClient(blobPath).getContainerReference(containerName);
				storage = new AzConnectContainer(AzConnectConstants.AZ_CONNECT_STORAGE_CONTAINER, blobContainer);
			} else {
				Exception ex = new Exception("Invalid Blob path");
				AzConnectTrace.throwing(this, fid, ex);
				throw ex;
			}
		} catch(Exception ex) {
			AzConnectTrace.throwing(this, fid, ex);
		}
		
		AzConnectTrace.exit(this, fid, storage);
		return storage;
	}
	
	public IOExitResourcePath[] listContainerBlobs() {
		final String fid = "listContainerBlobs";
		AzConnectTrace.entry(this, fid);
		
		try {
				Iterable<ListBlobItem> lbi = blobContainer.listBlobs();
				List <AzConnectIOResourcePath> paths = new ArrayList<AzConnectIOResourcePath>();
				for(ListBlobItem bi : lbi) {
					paths.add(new AzConnectIOResourcePath(bi.getUri().toASCIIString()));
				}
				AzConnectTrace.exit(this, fid, paths);
				return (IOExitResourcePath[]) paths.toArray();
		} catch(Exception ex) {
			AzConnectTrace.throwing(this, fid, ex);
		}
		AzConnectTrace.exit(this, fid);
		return null;
	}
}

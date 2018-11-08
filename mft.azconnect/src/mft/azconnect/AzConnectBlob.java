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

import java.net.URISyntaxException;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobInputStream;
import com.microsoft.azure.storage.blob.BlobOutputStream;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.CopyStatus;

public class AzConnectBlob extends AzConnectStorage {
	CloudBlockBlob blockBlob;
	public AzConnectBlob(int type, CloudBlockBlob blockBlob) {
		super(type);
		final String fid = "<init>";
		AzConnectTrace.entry(this, fid, type, blockBlob);

		this.blockBlob = blockBlob;
		
		AzConnectTrace.exit(this, fid);
	}

	@Override
	public CloudBlobDirectory getParent() {
		final String fid = "getParent";
		CloudBlobDirectory parent = null;
		
		AzConnectTrace.entry(this, fid);
		
		try {
			parent = blockBlob.getParent();
		} catch (URISyntaxException ex) {
			AzConnectTrace.data(this, fid, ex);
		} catch (StorageException ex) {
			AzConnectTrace.data(this, fid, ex);
		}
		
		AzConnectTrace.exit(this, fid, parent);
		return parent;
	}
	
	@Override
	protected void delete() throws StorageException {
		final String fid = "delete";
		AzConnectTrace.entry(this, fid);
		blockBlob.delete();
		AzConnectTrace.exit(this, fid);
	}
	
	@Override
	protected boolean exists() throws StorageException {
		final String fid = "exists";
		boolean val = false;
		AzConnectTrace.entry(this, fid);
		val = blockBlob.exists();
		AzConnectTrace.exit(this, fid, val);
		return val;
	}
	
	@Override
	protected long getLastModifiedTime() {
		final String fid = "exists";
		long val = 0;
		AzConnectTrace.entry(this, fid);
		val = blockBlob.getProperties().getLastModified().getTime();
		AzConnectTrace.exit(this, fid, val);
		return val;
	}
	
	@Override
	protected BlobOutputStream openOutputStream() throws StorageException {
		final String fid = "openOutputStream";
		BlobOutputStream os = null;
		AzConnectTrace.entry(this, fid);
		os = blockBlob.openOutputStream();
		AzConnectTrace.exit(this, fid, os);
		return os;
	}
	
	@Override
	protected BlobInputStream openInputStream() throws StorageException {
		final String fid = "openOutputStream";
		BlobInputStream os = null;
		AzConnectTrace.entry(this, fid);
		os = blockBlob.openInputStream();
		AzConnectTrace.exit(this, fid, os);
		return os;
	}
	

	@Override
	protected long getSize(){
		final String fid = "getSize";
		long size = 0;
		AzConnectTrace.entry(this, fid);
		size = blockBlob.getProperties().getLength();		
		AzConnectTrace.exit(this, fid, size);
		return size;
	}
	
	@Override
	protected String getName() {
		final String fid = "getName";
		String name = null;
		
		AzConnectTrace.entry(this, fid);
		name = blockBlob.getUri().toASCIIString();
		AzConnectTrace.exit(this, fid, name);
		return name;
	}
	
	@Override
	protected String getBlobName() {
		final String fid = "getBlobName";
		String blobName = null;
		
		AzConnectTrace.entry(this, fid);
		blobName = blockBlob.getName();
		AzConnectTrace.exit(this, fid, blobName);
		return blobName;
	}
	
	@Override
	protected void renameTo(final String newPath) {
		final String fid = "renameTo";
		
		AzConnectTrace.entry(this, fid, newPath);
		try {
			CloudBlobDirectory	parent = blockBlob.getParent();
			CloudBlockBlob newBlob = parent.getBlockBlobReference(newPath);
			
			AzConnectTrace.data(this, fid, "Initiate an asynchronous operation to create copy the .part blob.");
			
			newBlob.startCopy(blockBlob.getUri());
			
		    //Now wait in the loop for the copy operation to finish
		    while (true)
		    {
		        //newBlob..FetchAttributes();
		        if (newBlob.getCopyState().getStatus() != CopyStatus.PENDING)
		        {
		            break;
		        }
		        
		        //Sleep for a second may be and check the state again 
		        try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					AzConnectTrace.throwing(this, fid, ex);
				}
		    }
		    
		    // Delete the .part blob.
		    blockBlob.delete();
		} catch (URISyntaxException e) {
			AzConnectTrace.throwing(this, fid, e);
		} catch (StorageException e) {
			AzConnectTrace.throwing(this, fid, e);
		}
		AzConnectTrace.exit(this, fid);
	}
}

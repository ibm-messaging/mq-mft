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

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;

public class AzConnectContainer extends AzConnectStorage {
	CloudBlobContainer blobContainer;
	
	public AzConnectContainer(int type, CloudBlobContainer container) {
		super(type);
		final String fid = "<init>";
		
		AzConnectTrace.entry(this, fid, type, container);
		this.blobContainer = container;
		AzConnectTrace.exit(this, fid);
	}

	@Override
	public CloudBlobDirectory getParent() {
		final String fid = "getParent";
		CloudBlobDirectory parent = null;
		
		AzConnectTrace.entry(this, fid);
		// There is no parent for a container. Hence return null.
		AzConnectTrace.exit(this, fid, parent);
		return parent;
	}
	
	@Override
	protected void delete() throws StorageException {
		final String fid = "delete";
		AzConnectTrace.entry(this, fid);
		blobContainer.deleteIfExists();
		AzConnectTrace.exit(this, fid);
	}
	
	@Override
	protected boolean exists() throws StorageException {
		final String fid = "exists";
		boolean val = false;
		
		AzConnectTrace.entry(this, fid);
		
		val = blobContainer.exists();
		
		AzConnectTrace.exit(this, fid, val);
		
		return val;
	}
	
	@Override
	protected long getLastModifiedTime() {
		final String fid = "exists";
		long val = 0;

		val = blobContainer.getProperties().getLastModified().getTime();
		
		AzConnectTrace.exit(this, fid, val);
		return val;
	}
		
	@Override
	protected long getSize(){
		final String fid = "getSize";
		AzConnectTrace.entry(this, fid);
		long size = 0;
		AzConnectTrace.exit(this, fid, size);
		return size;
	}
	
	@Override
	protected String getName() {
		final String fid = "getName";
		String name = null;
		AzConnectTrace.entry(this, fid);
		name = blobContainer.getUri().toASCIIString();
		AzConnectTrace.exit(this, fid, name);
		return name;
	}
	
	@Override
	protected String getBlobName() {
		final String fid = "getBlobName";
		String blobName = null;
		AzConnectTrace.entry(this, fid);
		blobName = blobContainer.getName();
		AzConnectTrace.exit(this, fid, blobName);
		return blobName;
	}
}

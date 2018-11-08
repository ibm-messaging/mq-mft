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
import com.microsoft.azure.storage.blob.BlobInputStream;
import com.microsoft.azure.storage.blob.BlobOutputStream;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;

/**
 * @author shashikanth
 *
 */
public abstract class AzConnectStorage {

	private int azStorageType = 0;
	
	public AzConnectStorage(final int type) {
		this.azStorageType = type;
	}
	
	protected int getType() {
		return azStorageType;
	}
	
	protected CloudBlobDirectory getParent(){return null;}
	
	protected void delete() throws StorageException {}
	
	protected boolean exists() throws StorageException {return false;}
	
	protected long getLastModifiedTime() {
		return 0;
	}
	
	protected BlobOutputStream openOutputStream() throws StorageException {
		return null;
	}
	
	protected BlobInputStream openInputStream() throws StorageException {
		return null;
	}

	protected long getSize() {
		return 0;
	}
	
	protected String getName() {
		return null;
	}
	
	protected void renameTo(final String newPath){}
	
	protected String getBlobName() {return null;}
}

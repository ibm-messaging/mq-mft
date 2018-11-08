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

import java.io.IOException;
import com.ibm.wmqfte.exitroutine.api.IOExitChannel;
import com.ibm.wmqfte.exitroutine.api.IOExitProperties;
import com.ibm.wmqfte.exitroutine.api.IOExitResourcePath;
import com.ibm.wmqfte.exitroutine.api.RecoverableIOException;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;

/**
 * Implementation of IOExitResourcePath interface.
 * @author shashikanth
 *
 */
public class AzConnectIOResourcePath implements IOExitResourcePath {

	/** Complete name of the path which includes Azure Storage URI, container and blob name*/
	private String blobPath = null;
	/** Represents a Azure Storage object which could either be Azure Container or a Blob*/
	private AzConnectStorage blockStorage= null;
	
	/**
	 * Creates a connection to Azure storage and returns reference to a blob
	 * @param newBlobPath
	 * @return Instance of AzConnectStorage
	 */
	private AzConnectStorage getBlockBlob(final String newBlobPath){
		final String fid = "getBlockBlob";
		AzConnectTrace.entry(this, fid, newBlobPath);
		
		AzConnectStorage localBlockStorage = null;
		this.blobPath = newBlobPath;
		try {
			localBlockStorage = AzConnectIOUtil.getInstance().getBlockBlob(newBlobPath);
		}catch (Exception ex) {
			AzConnectTrace.throwing(this,fid, ex);
		}
		
		AzConnectTrace.exit(this, fid, localBlockStorage);
		
		return localBlockStorage;
	}
	
	/**
	 * Constructor
	 * @param newBlobPath
	 */
	public AzConnectIOResourcePath(final String newBlobPath) {
		final String fid = "<init>";
		AzConnectTrace.entry(this, fid, newBlobPath);
		this.blobPath = newBlobPath;
		try {
			blockStorage = getBlockBlob(newBlobPath);
			
		}catch (Exception ex) {
			AzConnectTrace.throwing(this,fid,ex);
		}
		AzConnectTrace.exit(this, fid);
	}

	/**
	 * Just the name of the Blob without URI prefix
	 */
	@Override
	public String getName() {
		final String fid = "getName";
		AzConnectTrace.entry(this, fid);
		String name = blockStorage.getBlobName();
		AzConnectTrace.exit(this, fid, name);
		return name;
	}

	@Override
	public String getParent() {
		final String fid = "getParent";
		AzConnectTrace.entry(this, fid);
		CloudBlobDirectory dir = null;
		String parentName = null;
		try {
			dir = blockStorage.getParent();			
			parentName = dir.getUri().toASCIIString();
		} catch(Exception ex) {
			AzConnectTrace.throwing(this, fid, ex);
		}
		AzConnectTrace.exit(this, fid, parentName);
		return parentName;
	}

	@Override
	public String getPath() {
		final String fid = "getPath";
		String referencePath = null;
		AzConnectTrace.entry(this, fid, blobPath);
		
		try {
			referencePath = blockStorage.getName();
		}catch (Exception ex) {
			AzConnectTrace.throwing(this, fid, ex);
		}
		AzConnectTrace.exit(this, fid, referencePath);
		return referencePath;
	}

	@Override
	public IOExitResourcePath[] listPaths() {
		final String fid = "listPaths";
		AzConnectTrace.entry(this, fid);
		AzConnectTrace.exit(this, fid);
		return AzConnectIOUtil.getInstance().listContainerBlobs();
	}

	@Override
	public boolean canRead() throws IOException {
		final String fid = "canRead";
		AzConnectTrace.entry(this, fid);
		AzConnectTrace.exit(this, fid);
		return true;
	}

	@Override
	public boolean canWrite() throws IOException {
		final String fid = "canWrite";
		AzConnectTrace.entry(this, fid);
		AzConnectTrace.exit(this, fid, true);
		return true;
	}

	@Override
	public boolean createNewPath() throws RecoverableIOException, IOException {
		final String fid = "createNewPath";
		boolean retVal = false;
		AzConnectTrace.entry(this, fid);
		try {
			blockStorage = AzConnectIOUtil.getInstance().getBlockBlob(blobPath);
		} catch(Exception ex) {
			AzConnectTrace.throwing(this, fid, ex);
		}
		AzConnectTrace.exit(this, fid);
		return retVal;
	}

	@Override
	public IOExitResourcePath createTempPath(String tempPath) throws RecoverableIOException, IOException {
		final String fid = "createTempPath";
		AzConnectIOResourcePath azTempPath = null;
		AzConnectTrace.entry(this, fid, tempPath);
		try {
			azTempPath = new AzConnectIOResourcePath(blobPath + tempPath);
		}catch (Exception ex) {
			AzConnectTrace.throwing(this, fid, ex);
			throw new IOException(ex);
		}
		AzConnectTrace.exit(this, fid);
		return azTempPath;
	}

	@Override
	public void delete() throws IOException {
		final String fid = "delete";
		AzConnectTrace.entry(this, fid);
		
		try {
			blockStorage.delete();
		}catch(StorageException ex) {
			AzConnectTrace.throwing(this, fid, ex);
			throw new IOException(ex);
		}
		AzConnectTrace.exit(this, fid);
	}

	@Override
	public boolean exists() throws IOException {
		final String fid = "exists";
		boolean retVal = false;
		
		AzConnectTrace.entry(this, fid, blobPath);
		
		try {
			retVal = blockStorage.exists();
		}catch (Exception ex) {
			AzConnectTrace.throwing(this, fid, ex);
		}
		AzConnectTrace.exit(this, fid, retVal);
		return retVal;
	}

	@Override
	public String getCanonicalPath() throws IOException {
		final String fid = "getCanonicalPath";
		
		AzConnectTrace.entry(this, fid);
		String canPath = getPath();
		AzConnectTrace.exit(this, fid, canPath);
		return canPath;
	}

	@Override
	public IOExitProperties getProperties() {
		final String fid = "getProperties";
		AzConnectTrace.entry(this, fid);
		IOExitProperties ioExProps = new IOExitProperties();
		AzConnectTrace.exit(this, fid);
		return ioExProps;
	}

	@Override
	public boolean inUse() {
		final String fid = "inUse";
		AzConnectTrace.entry(this, fid);
		AzConnectTrace.exit(this, fid, false);
		return false;
	}

	@Override
	public boolean isAbsolute() {
		final String fid = "isAbsolute";
		AzConnectTrace.entry(this, fid);
		AzConnectTrace.exit(this, fid, true);
		return true;
	}

	@Override
	public boolean isDirectory() {
		final String fid = "isDirectory";
		AzConnectTrace.entry(this, fid);
		boolean retVal = (blockStorage.getType() == AzConnectConstants.AZ_CONNECT_STORAGE_CONTAINER) ? true : false;
		AzConnectTrace.exit(this, fid, retVal);
		return retVal;
	}

	@Override
	public boolean isFile() {
		final String fid = "isFile";
		AzConnectTrace.entry(this, fid);
		boolean retVal = (blockStorage.getType() == AzConnectConstants.AZ_CONNECT_STORAGE_BLOB) ? true : false;
		AzConnectTrace.exit(this, fid, retVal);
		return retVal;
	}

	@Override
	public long lastModified() {
		final String fid = "lastModified";
		
		AzConnectTrace.entry(this, fid);
		long lastModifiedValue = 0;
		
		try {
			//AzConnectStorage storage = getBlockBlob(blobPath);
			if(blockStorage.exists())
				lastModifiedValue =  blockStorage.getLastModifiedTime();			
		} catch(Exception ex) {
			AzConnectTrace.throwing(this,fid,ex);
		}
		
		AzConnectTrace.exit(this, fid, lastModifiedValue);
		return lastModifiedValue;
	}

	@Override
	public void makePath() throws IOException {
		final String fid = "makePath";
		AzConnectTrace.entry(this, fid);
		createNewPath();
		AzConnectTrace.entry(this, fid);		
	}

	@Override
	public IOExitResourcePath newPath(String newPathVal) {
		final String fid = "newPath";
		AzConnectIOResourcePath azNewPath = null;
		
		AzConnectTrace.entry(this, fid, newPathVal);
		if(blockStorage.getType() == AzConnectConstants.AZ_CONNECT_STORAGE_CONTAINER)
			newPathVal = blockStorage.getName() + "/" + newPathVal;

		try {
			azNewPath = new AzConnectIOResourcePath(newPathVal);
		}catch (Exception ex) {
			AzConnectTrace.throwing(this, fid, ex);
		}
		
		AzConnectTrace.exit(this, fid);
		return azNewPath;
	}

	@Override
	public IOExitChannel openForRead(long arg0) throws RecoverableIOException, IOException {
		final String fid = "openForRead";

		AzConnectTrace.entry(this, fid);
		
		AzConnectReadChannel azReadChannel = new AzConnectReadChannel(blockStorage);
		
		AzConnectTrace.exit(this, fid, azReadChannel);
		return azReadChannel;
	}

	@Override
	public IOExitChannel openForWrite(boolean append) throws RecoverableIOException, IOException {
		final String fid = "openForWrite";
		
		AzConnectTrace.entry(this, fid);
		AzConnectWriteChannel azWriteChannel = new AzConnectWriteChannel(blockStorage);
		AzConnectTrace.exit(this, fid, azWriteChannel);
		
		return azWriteChannel;
	}

	@Override
	public boolean readPermitted(String arg0) throws IOException {
		final String fid = "readPermitted";
		boolean val = true;
		AzConnectTrace.entry(this, fid);
		AzConnectTrace.exit(this, fid, val);
		return val;
	}

	@Override
	public void renameTo(IOExitResourcePath newPath) throws IOException {
		final String fid = "renameTo";

		AzConnectTrace.entry(this, fid, newPath.getName());
		try {
			blockStorage.renameTo(newPath.getName());
		}catch(Exception ex) {
			AzConnectTrace.throwing(this, fid, ex);
		}
		AzConnectTrace.exit(this, fid);
	}

	@Override
	public boolean writePermitted(String userId) throws IOException {
		final String fid = "writePermitted";
		boolean val = true;
		AzConnectTrace.entry(this, fid);
		AzConnectTrace.exit(this, fid, val);
		return val;
	}
}

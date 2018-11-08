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
import java.nio.ByteBuffer;

import com.ibm.wmqfte.exitroutine.api.IOExitChannel;
import com.ibm.wmqfte.exitroutine.api.IOExitLock;
import com.ibm.wmqfte.exitroutine.api.RecoverableIOException;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobInputStream;

public class AzConnectReadChannel implements IOExitChannel {
	private AzConnectStorage blockBlob = null;
	private BlobInputStream blobInStream = null;
	private long totalBytesRead = 0;
	
	public AzConnectReadChannel(AzConnectStorage blockBlob) {
		final String fid = "<init>";
		
		AzConnectTrace.entry(this, fid, blockBlob);
		this.blockBlob = blockBlob;
		
		try {
			blobInStream = blockBlob.openInputStream();
		} catch (StorageException e) {
			AzConnectTrace.throwing(this, fid, e);
		}
		
		AzConnectTrace.exit(this, fid);
	}
	
	@Override
	public void close() throws RecoverableIOException, IOException {
		final String fid = "close";
		
		AzConnectTrace.entry(this, fid, blobInStream);
		
		try {
			if(blobInStream != null) {
				blobInStream.close();
			} else {
				IOException iex = new IOException("Blob not opened for output");
				AzConnectTrace.throwing(this, fid, iex);;
				throw iex;
			}			
		} catch(Exception ex) {
			AzConnectTrace.throwing(this, fid, ex);
			throw ex;
		}
		AzConnectTrace.exit(this, fid);
	}

	@Override
	public void force() throws RecoverableIOException, IOException {
		final String fid = "force";
		IOException iex = new IOException ("force operation not valid for read channel");
		AzConnectTrace.throwing(this, fid, iex);
		throw iex;
	}

	@Override
	public int read(ByteBuffer readBuffer) throws RecoverableIOException, IOException {
		final String fid = "read";
		int readSize = 0;
		
		AzConnectTrace.entry(this, fid);
		try {
			if(blobInStream != null) {
				byte[] arr = new byte[readBuffer.remaining()];
				readSize = blobInStream.read(arr);
				readBuffer.put(arr);
				if(readSize > 0)
					totalBytesRead += readSize;
				AzConnectTrace.data(this, fid, "bytes read [" + readSize + "] total bytes read [" + totalBytesRead + "]");
			} else {
				AzConnectTrace.data(this, fid, "Blob not opened for input");
			}
		} catch (Exception ex) {
			AzConnectTrace.throwing(this, fid, ex);
			throw ex;
		}
		AzConnectTrace.exit(this, fid, readSize);
		
		return readSize;
	}

	@Override
	public long size() throws IOException {
		final String fid = "size";
		
		AzConnectTrace.entry(this, fid);
		long retVal = 0;
		
		try {
			if(blockBlob != null) {
				retVal = blockBlob.getSize();
			} else {
				IOException ex = new IOException("Blob not opened");
				AzConnectTrace.throwing(this, fid, ex);
				throw ex;
			}			
		}catch (Exception ex) {
			AzConnectTrace.throwing(this, fid, ex);
			throw ex;
		}
		
		AzConnectTrace.exit(this, fid, retVal);
		return retVal;
	}

	@Override
	public IOExitLock tryLock(boolean arg0) throws IOException {
		final String fid = "tryLock";
		AzConnectTrace.entry(this, fid);
		IOExitLock lock = new AzConnectIOLock(); 
		AzConnectTrace.exit(this, fid, lock);
		return lock;
	}

	@Override
	public int write(ByteBuffer arg0) throws RecoverableIOException, IOException {
		final String fid = "write";
		IOException iex = new IOException ("write operation not valid for read channel");
		AzConnectTrace.throwing(this, fid, iex);
		throw iex;
	}

}

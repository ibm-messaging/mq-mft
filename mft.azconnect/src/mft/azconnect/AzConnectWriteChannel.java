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
import com.microsoft.azure.storage.blob.BlobOutputStream;

public class AzConnectWriteChannel implements IOExitChannel {

	private AzConnectStorage blockBlob = null;
	private BlobOutputStream blobOutStream = null;
	private long totalBytesWritten;
	
	public AzConnectWriteChannel(AzConnectStorage blockBlob) {
		final String fid = "<init>";
		
		AzConnectTrace.entry(this, fid, blockBlob);
		this.blockBlob = blockBlob;
		totalBytesWritten = 0;
		try {
			blobOutStream = blockBlob.openOutputStream();
		} catch (StorageException e) {
			AzConnectTrace.throwing(this, fid, e);
		}
		
		AzConnectTrace.exit(this, fid);
	}
	
	@Override
	public void close() throws RecoverableIOException, IOException {
		final String fid = "close";
		
		AzConnectTrace.entry(this, fid, blobOutStream);
		
		try {
			if(blobOutStream != null) {
				blobOutStream.flush();
				blobOutStream.close();
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
		
		AzConnectTrace.entry(this, fid, blobOutStream);
		try {
			if(blobOutStream != null) {
				blobOutStream.flush();
			} else {
				IOException iex = new IOException("Blob not opened for output");
				AzConnectTrace.throwing(this, fid, iex);;
				throw iex;
			}			
		} catch (Exception ex) {
			AzConnectTrace.throwing(this, fid, ex);
			throw ex;
		}
		AzConnectTrace.exit(this, fid);
	}

	@Override
	public int read(ByteBuffer arg0) throws RecoverableIOException, IOException {
		final String fid = "read";
		IOException iex = new IOException ("Blob not opened for input");
		AzConnectTrace.throwing(this, fid, iex);
		throw iex;
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
	public IOExitLock tryLock(boolean shared) throws IOException {
		final String fid = "tryLock";
		AzConnectTrace.entry(this, fid, shared);
		IOExitLock lock = new AzConnectIOLock(); 
		AzConnectTrace.exit(this, fid, lock);
		return lock;
	}

	@Override
	public int write(ByteBuffer buffer) throws RecoverableIOException, IOException {
		final String fid = "write";
		
		AzConnectTrace.entry(this, fid, blobOutStream, buffer, buffer.remaining());
		int bytesWritten = 0;
		
		try {
			if(blobOutStream != null) {
				byte[] arr = new byte[buffer.remaining()];
				buffer.get(arr);
				blobOutStream.write(arr);
				// We don't get the size of the buffer we wrote
				bytesWritten = arr.length;
				totalBytesWritten += arr.length;
				AzConnectTrace.data(this, fid, "Position " + buffer.position() + " totalBytesWritten " + totalBytesWritten);
			} else {
				IOException ex = new IOException("Blob not opened for output");
				AzConnectTrace.throwing(this, fid, ex);
				throw ex;
			}
		} catch (Exception ex) {
			AzConnectTrace.throwing(this, fid, ex);
			throw ex;
		}

		AzConnectTrace.exit(this, fid, bytesWritten);
		return bytesWritten;
	}
}

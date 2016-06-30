/**
 * 
 */
package mft.samples;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.ibm.wmqfte.exitroutine.api.FileTransferResult;
import com.ibm.wmqfte.exitroutine.api.SourceTransferEndExit;
import com.ibm.wmqfte.exitroutine.api.TransferExitResult;
import com.ibm.wmqfte.exitroutine.api.TransferExitResultCode;

/**
 * @author shashikanth
 *
 */
public class DeleteFilesExit implements SourceTransferEndExit {
	/* (non-Javadoc)
	 * @see com.ibm.wmqfte.exitroutine.api.SourceTransferEndExit#onSourceTransferEnd(com.ibm.wmqfte.exitroutine.api.TransferExitResult, java.lang.String, java.lang.String, java.util.Map, java.util.Map, java.util.List)
     *
	 * Invoked immediately after the completion of a transfer on the agent acting as
	 * the source of the transfer.
	 * 
	 * @param transferExitResult
	 *            a result object reflecting whether or not the transfer completed
	 *            successfully.
	 * 
	 * @param sourceAgentName
	 *            the name of the agent acting as the source of the transfer.
	 *            This is the name of the agent that the implementation of this
	 *            method will be invoked from.
	 * 
	 * @param destinationAgentName
	 *            the name of the agent acting as the destination of the
	 *            transfer.
	 * 
	 * @param environmentMetaData
	 *            meta data about the environment in which the implementation
	 *            of this method is running.  This information can only be read,
	 *            it cannot be updated by the implementation.  The constants
	 *            defined in <code>EnvironmentMetaDataConstants</code> class can 
	 *            be used to access the data held by this map.
	 * 
	 * @param transferMetaData
	 *            meta data to associate with the transfer.  The information can
	 *            only be read, it cannot be updated by the implementation.  This 
	 *            map may also contain keys with IBM reserved names.  These 
	 *            entries are defined in the <code>TransferMetaDataConstants</code> 
	 *            class and have special semantics.
	 * 
	 * @param fileResults
	 *            a list of file transfer result objects that describe the source
	 *            file name, destination file name and result of each file transfer
	 *            operation attempted.
	 * 
	 * @return    an optional description to enter into the log message describing
	 *            transfer completion.  A value of <code>null</code> can be used
	 *            when no description is required.
	 */	 
	 
	@Override
	public String onSourceTransferEnd(TransferExitResult transferExitResult, String sourceAgentName,
			String destinationAgentName, Map<String, String> environmentMetaData, Map<String, String> transferMetaData,
			List<FileTransferResult> fileResults) {
		
		// Delete all files only if the transfer is successfully completed.
		if(transferExitResult.getResultCode() != TransferExitResultCode.PROCEED) {
			for(FileTransferResult ftr : fileResults ) {
				File sourceFile = new File(ftr.getSourceFileSpecification());
				boolean fileDeleted = sourceFile.delete();
				if(!fileDeleted) {
					System.out.println("Could not delete file: " + ftr.getSourceFileSpecification());
				}
			}
		}
		
		return "";
	}

}

package mft.samples;
/**
 * Sample IBM MQ Managed File Transfer SourceTransferEndExit
 * 
 *  
 *  Where does this exit help?
 *  
 *  There could be multiple files, like all files in a directory, are to be transferred as part 
 *  of one transfer request. When such a transfer request is submitted, it's possible that some
 *  of the files in the transfer request are successfully transferred and some fail to transfer
 *  for some reason. In such a case, the transfer is marked partial successful. If the transfer
 *  request specified "source_file_disposition" as "delete" (i.e. -sd delete), then the files
 *  which were transferred successfully are deleted at source and the files that failed to 
 *  transfer are left as it is.
 *  
 *  Since the transfer was partially successful, the user submits the reuest again to transfer
 *  the remaining files. But this request fails as some of the files have been transferred in
 *  the previous attempt and were deleted. 
 *  
 *  This sample SourceTransferEndExit exit deletes the files after a transfer is 
 *  completed successfully. Files are not deleted if for some reason transfer fails
 *  or partially complete. 
 *  
 *  How to use this exit:
 *  
 *  Configuration:
 *  1) Stop your agent.
 *  2) Copy the DeleteFilesExit.class file under the following directory. 
 *     /<your MQ data directory>/mqft/config/<your coordination qmgr>/agents/<your agent name>/exits/mft/samples
 *  3) Add the following line to your agent's agent.properties file
 *     sourceTransferEndExitClasses=mft.samples.DeleteFilesExit
 *  4) Start your agent.
 *  
 *  When submitting transfers ensure that source disposition is set to 'leave' i.e. '-sd leave'
 */

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
		if(transferExitResult.getResultCode() == TransferExitResultCode.PROCEED) {
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

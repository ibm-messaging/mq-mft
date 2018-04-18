/**
 * 
 */
package mft.samples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;

import com.ibm.wmqfte.exitroutine.api.FileTransferResult;
import com.ibm.wmqfte.exitroutine.api.SourceTransferEndExit;
import com.ibm.wmqfte.exitroutine.api.TransferExitResult;
import com.ibm.wmqfte.exitroutine.api.TransferExitResultCode;

public class MoveFiles implements SourceTransferEndExit {

	/**
	 *  MoveFiles IBM MQ Managed File Transfer Exit.
	 * 
	 *  There are scenarios where it is required to move the files in source 
	 *  directory to another directory after transfer completes 
	 * 
	 *  This exit extends the MFT functionality by providing the ability to move
	 *  files at source agent.
	 * 
     *  How to use this exit:
     *  
     *  Configuration:
     *  1) Create a Java project in Eclipse.
     *  2) Add this source file to project.
     *  3) Add com.ibm.wmqfte.exitroutine.jar to project.
     *  4) Compile the project.
     *  5) Stop your agent.
     *  6) Copy the MoveFiles.class file under the following directory. 
     *     /<your MQ data directory>/mqft/config/<your coordination qmgr>/agents/<your agent name>/exits/mft/samples
     *  7) Add the following line to your agent's agent.properties file
     *     sourceTransferEndExitClasses=mft.samples.MoveFiles
     *  8) Start your agent.
     *  9) When submitting transfer request using fteCreateTransfer command specify two metadata parameters:
     *     ARCHIVE_PATH - Files are moved to this directory if transfer completes successfully
     *     REJECT_PATH - Files are moved to this directory if transfer failed to complete
     *     For example:
     *     fteCreateTransfer -rt -1 -sa SRC -sm QMSRC -da DEST -dm QMDEST -sd leave -df "/user/input/order.xml" "C:\output\order.xml" -p QMCORD -md "ARCHIVE_PATH=C:\MFT\ARCHIVE,REJECT_PATH="C:\MFT\REJECT"
     *     
     * 10) When submitting transfers ensure that source disposition is set to 'leave' i.e. '-sd leave'
	 */	
	public MoveFiles() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.ibm.wmqfte.exitroutine.api.SourceTransferEndExit#onSourceTransferEnd(com.ibm.wmqfte.exitroutine.api.TransferExitResult, java.lang.String, java.lang.String, java.util.Map, java.util.Map, java.util.List)
	 */
	@Override
	public String onSourceTransferEnd(TransferExitResult transferExitResult,
			   String sourceAgentName,
			   String destinationAgentName,
			   Map<String, String> environmentMetaData,
			   Map<String, String> transferMetaData,
			   List<FileTransferResult> fileResults) {
		
		// Archive the files if transfer has succeeded
		if(transferExitResult.getResultCode() == TransferExitResultCode.PROCEED){
			// Move files to archive folder
			String archivePath = transferMetaData.get("ARCHIVE_PATH");
			moveFileToTargetPath(archivePath, fileResults);			
		} else {
			// Move files to reject folder
			String rejectPath = transferMetaData.get("REJECT_PATH");
			moveFileToTargetPath(rejectPath, fileResults);						
		}		
		return "GOAHEAD";
	}

	/**
	 * moveFileToTargetPath - Make sanity checks and move files.
	 * @param targetPath - Name of the target directory
	 * @param fileResults - List of files to move
	 */
	private void moveFileToTargetPath(final String targetPath, List<FileTransferResult> fileResults) {
		System.out.println("moveFileToTargetPath - [" + targetPath + "]");
		
		if(targetPath == null) {
			System.out.println("No traget path specified. Exiting");
			return;
		}
		
		// Move files to given path
    	File targetDir = new File(targetPath);
    	if(targetDir.exists()) {
    		if(!targetDir.isDirectory()) {
    			System.out.println("Given path [" + targetPath + "] is not valid directory.");
    		} else {
    			moveFiles(fileResults, targetPath);
    		}
    	} else {
    		if(targetDir.mkdir()) {
    			moveFiles(fileResults, targetPath);	    			
    		} else {
	    		System.out.println("Failed to create directory [" + targetPath + "]");
    		}
    	}
	} 
	
	/**
	 * Move files
	 * @param fileResults - List files to move
	 * @param targetDir - target directory
	 */
	private void moveFiles (List<FileTransferResult> fileResults, String targetDir) {
		//Iterate through the list files and move.
		for(FileTransferResult fileResult : fileResults){
			boolean deleteSource = false;

			File sourcePath = new File(fileResult.getSourceFileSpecification());
			FileChannel sourceChannel = null;
			FileChannel destChannel = null;
			FileInputStream fi = null;
			FileOutputStream fo = null;
  
	    	try {
	    		// Create NIO channel for source
	    		fi = new FileInputStream(sourcePath);
	    	    sourceChannel = fi.getChannel();
	    	      
	    	    // Create channel for destination file
	    	    File desinationPath = new File(targetDir + "/" + sourcePath.getName());
	    		fo = new FileOutputStream(desinationPath);
	    	    destChannel = fo.getChannel();
	    	    destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
	    	    deleteSource = true;
	    	} catch(Exception ex) {
	    		System.out.println("Failed to move files: " + ex);
	    	} finally {
	    		try {
	    			if(sourceChannel != null)
	    				sourceChannel.close();
	    		} catch (IOException e) {
	    			System.out.println("Failed to close source channel: " + e);
	    		}

	    		try {
	    			if(destChannel != null)
	    				destChannel.close();
	    		} catch (IOException e) {
	    			System.out.println("Failed to close destination channel: " + e);
	    		}
    	           
    	        // If we have moved the file delete the file from source directory
   	        	if(deleteSource) 
   	        		sourcePath.delete();
	    	}
		}	      
	}
}

package mft.samples;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.ibm.wmqfte.exitroutine.api.FileTransferResult;
import com.ibm.wmqfte.exitroutine.api.SourceTransferEndExit;
import com.ibm.wmqfte.exitroutine.api.TransferExitResult;
import com.ibm.wmqfte.exitroutine.api.TransferExitResultCode;

/**
 * @author shashikanth
 *
 */
public class ArchiveFiles implements SourceTransferEndExit {
	final static int BUFFER = 4096;

	/* (non-Javadoc)
	 * @see com.ibm.wmqfte.exitroutine.api.SourceTransferEndExit#onSourceTransferEnd(com.ibm.wmqfte.exitroutine.api.TransferExitResult, java.lang.String, java.lang.String, java.util.Map, java.util.Map, java.util.List)
	 */
	/**
	 *  ArchiveFiles IBM MQ Managed File Transfer Exit.
	 * 
	 *  There are scenarios where it is required to archive the files in a source 
	 *  directory and move to a different place. 
	 * 
	 *  This exit extends the MFT functionality by providing the ability to archive
	 *  files at source agent.
	 * 
     *  How to use this exit:
     *  
     *  Configuration:
     *  1) Compile this source file.
     *  2) Stop your agent.
     *  3) Copy the ArchiveFiles.class file under the following directory. 
     *     /<your MQ data directory>/mqft/config/<your coordination qmgr>/agents/<your agent name>/exits/mft/samples
     *  4) Add the following line to your agent's agent.properties file
     *     sourceTransferEndExitClasses=mft.samples.ArchiveFiles
     *  5) Start your agent.
     *  
     *  When submitting transfers ensure that source disposition is set to 'leave' i.e. '-sd leave'
	 */	
	@Override
	public String onSourceTransferEnd(TransferExitResult transferExitResult,
			   String sourceAgentName,
			   String destinationAgentName,
			   Map<String, String> environmentMetaData,
			   Map<String, String> transferMetaData,
			   List<FileTransferResult> fileResults) {
		System.out.println("onSourceTransferEnd - entry");
		
		// Archive the files if transfer has succeeded
		if(transferExitResult.getResultCode() == TransferExitResultCode.PROCEED){
			// Archive the source files
			String archivePath = getArchivePath(transferMetaData.get("ARCHIVE_PATH"), fileResults.get(0));
			archiveSource(archivePath, fileResults);			
		}
		
		System.out.println("onSourceTransferEnd - exit");		
		return "Success";
	}

	/**
	 * Gets the path for creating the archive. The method will first look into the 
	 * transfer meta data for the path. If found that will be returned. If not found
	 * the parent path of the first file in the transfer list will be used to generate
	 * path. Current system time will be appended to the path name to generate a 
	 * unique zip filename.
	 * 
	 * @param envArchivePath
	 * @param firstFile
	 * @return name of the folder where archived file will be created followed by filename.
	 */
	private String getArchivePath(final String metadataArchivePath, FileTransferResult firstFile) {
		String archivePath = "";
		if(metadataArchivePath != null){
			// We have some path specified as meta data in transfer
			File filePath = new File(metadataArchivePath);
			// If given path is a directory
			if(filePath.isDirectory()){
				// Append "archive" followed by current system time to generate path.
				archivePath = metadataArchivePath + "/archive" + System.currentTimeMillis() + ".zip";
			}else {
				archivePath = metadataArchivePath;
				// Append .zip if none exists.
				if(!metadataArchivePath.endsWith(".zip")){
					archivePath += ".zip";					
				}
			}
		} else {
			// The given path is points to a file. 
	    	File filePath = new File(firstFile.getSourceFileSpecification());
	    	// Go a level up to get the parent directory name. Append "archive" followed by system time.
			archivePath = filePath.getParent() + "/archive" + System.currentTimeMillis() + ".zip";
		}
		return archivePath;
	}
	
	/**
	 * Archive the source files as a Zip file to the given folder.
	 * @param archivePath - Path where the zip file to be created.
	 * @param fileResults - List of files transferred.
	 * @return
	 */
	private boolean archiveSource(final String archivePath, List<FileTransferResult> fileResults) {
		System.out.println("archiveSource - entry");
		System.out.println("Params: " + archivePath);
		
	    try {
	      BufferedInputStream sourceStream = null;      	      
	      FileOutputStream    destStream = new FileOutputStream(new File(archivePath));
	      ZipOutputStream zippedStream = new ZipOutputStream(new BufferedOutputStream(destStream));
	      
	      byte fileData[] = new byte[BUFFER];

	      // Go through the files in transfer list and add them to archive.
	      for(FileTransferResult fileResult : fileResults){
	    	  // Check if the current entry is a directory. If so list the files under
	    	  // the directory and add them to archive.
	    	  File dir = new File(fileResult.getSourceFileSpecification());
	    	  if(dir.isDirectory()){
                  String files[] = dir.list();
                  for (int i = 0; i < files.length; i++) {
                      System.out.println("Adding file " + files[i]);
                      FileInputStream finps = new FileInputStream(fileResult.getSourceFileSpecification() + "/" + files[i]);
                      sourceStream = new BufferedInputStream(finps, BUFFER);
                      ZipEntry entry = new ZipEntry(dir +"/"+files[i]);
                      zippedStream.putNextEntry(entry);
                      int count;
                      while ((count = sourceStream.read(fileData, 0, BUFFER)) != -1) {
                    	  zippedStream.write(fileData, 0, count);
                    	  zippedStream.flush();
                      }
                  }	    		  
	    	  } else {
	    		  // If this a file, then simply add to the archive.
                  FileInputStream finps = new FileInputStream(dir);
                  sourceStream = new BufferedInputStream(finps, BUFFER);
                  ZipEntry entry = new ZipEntry(fileResult.getSourceFileSpecification());
                  zippedStream.putNextEntry(entry);
                  int count;
                  while ((count = sourceStream.read(fileData, 0, BUFFER)) != -1) {
                	  zippedStream.write(fileData, 0, count);
                	  zippedStream.flush();
                  }
	    	  }
	      }	      
	      sourceStream.close();
	      zippedStream.flush();
	      zippedStream.close();
	    } catch (Exception e) {
	        System.out.println("archiveSource threw exception: " + e.getMessage());        
	        return false;
	    }finally {
			System.out.println("archiveSource - exit");	    	
	    }

	    return true;
	}   	
}

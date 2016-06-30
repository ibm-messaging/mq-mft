package mft.samples;
/**
 * Sample IBM MQ Managed File Transfer SourceTransferStartExit
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ibm.wmqfte.exitroutine.api.SourceFileExitFileSpecification;
import com.ibm.wmqfte.exitroutine.api.SourceTransferStartExit;
import com.ibm.wmqfte.exitroutine.api.TransferExitResult;

public class FileFilterExit implements SourceTransferStartExit {
	/**
	 * Lower limit of file size 
	 */
	long fileSizeLimitLow = 0;
	/**
	 * Higher limit of file size
	 */
	long fileSizeLimitHigh = Integer.MAX_VALUE;
	
	/* (non-Javadoc)
	 * @see com.ibm.wmqfte.exitroutine.api.SourceTransferStartExit#onSourceTransferStart(java.lang.String, java.lang.String, java.util.Map, java.util.Map, java.util.List)
	 */
	@Override
	public TransferExitResult onSourceTransferStart(String sourceAgentName, String destinationAgentName,
			Map<String, String> environmentMetaData, Map<String, String> transferMetaData,
			List<SourceFileExitFileSpecification> fileSpecs) {
		System.out.println("onSourceTransferStart - Entry");
		
		// Always proceed with transfer no matter what happens in the exit.
		TransferExitResult result = TransferExitResult.PROCEED_RESULT;
		
		// List of files whose size is above give limit
		ArrayList <SourceFileExitFileSpecification> filesToExclude = new ArrayList<SourceFileExitFileSpecification>();
		
		// Read configuration file for sizes.
		readConfiguration();
		System.out.println("Transfer files of size between " + fileSizeLimitLow + " and " + fileSizeLimitHigh  + "bytes");
		
		// Exclude any file not with in the given range
		try {
			for ( SourceFileExitFileSpecification sourceFileSpec : fileSpecs) {
	             File sourceFile = new File(sourceFileSpec.getSource());

	             // Check if the file exists and it is a file and not a directory
	             if(sourceFile.exists() && sourceFile.isFile()) {
	            	 // Add to exclude list if the size is not in the given range.
	            	 if(sourceFile.length() < fileSizeLimitLow || sourceFile.length() > fileSizeLimitHigh ) {
	            		 filesToExclude.add(sourceFileSpec);
	            	 }
	             }
			}
			
            // Go through the list and remove files from transfer list.
			for (SourceFileExitFileSpecification sf : filesToExclude ) {
				fileSpecs.remove(sf);
			}				
		}catch(Exception ex) {
			System.out.println("Exception caught: " + ex);
		}finally {
			System.out.println("onSourceTransferStart - Exit");			
		}
		return result;
	}

	/**
	 * Read the accompanying configuration file for size limits
	 * 
	 * Configuration is provided in config.xml and is present in the
	 * same path as this class file.
	 *
	 * Format of XML file.
	 * <?xml version="1.0" encoding="UTF-8"?>
     * <File>
	 *    <SizeLimitLow>10000</SizeLimitLow>
	 *    <SizeLimitHigh>50000</SizeLimitHigh>
     * </File>
	 */
	private void readConfiguration() {
		try {
			InputStream is = this.getClass().getResourceAsStream("config.xml");
			
			//File file = new File("config.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(is);
			Element rootElement = document.getDocumentElement();
			
			// Read the lower range
	        NodeList list = rootElement.getElementsByTagName("SizeLimitLow");
	        if(list != null) {
	        	final String nodeValue = list.item(0).getTextContent();
	        	fileSizeLimitLow = Integer.parseInt(nodeValue);
	        }
	        
	        // Read the upper range
	        list = rootElement.getElementsByTagName("SizeLimitHigh");
	        if(list != null) {
	        	final String nodeValue = list.item(0).getTextContent();
	        	fileSizeLimitHigh = Integer.parseInt(nodeValue);
	        }
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
}

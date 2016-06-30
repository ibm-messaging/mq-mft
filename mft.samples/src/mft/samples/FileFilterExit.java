package mft.samples;

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

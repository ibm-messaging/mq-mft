/**
 * 
 */
package mft.samples;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ibm.wmqfte.exitroutine.api.MonitorExit2;
import com.ibm.wmqfte.exitroutine.api.MonitorExitResult;
import com.ibm.wmqfte.exitroutine.api.MonitorExitResultCode;
import com.ibm.wmqfte.exitroutine.api.MonitorMetaDataConstants;
import com.ibm.wmqfte.exitroutine.api.Reference;

/**
 * @author shashikanth
 *
 */
public class FileMonitor implements MonitorExit2 {
	final String uniqueLockFileName = "FileMonitor.lck";

	/* (non-Javadoc)
	 * @see com.ibm.wmqfte.exitroutine.api.MonitorExit#onMonitor(java.util.Map, java.util.Map, com.ibm.wmqfte.exitroutine.api.Reference)
	 */
	@Override
	public MonitorExitResult onMonitor(Map<String, String> environmentMetaData,
			Map<String, String> monitorMetaData, Reference<String> taskDetails) {
		//System.out.println("onMonitor - Entry");

		MonitorExitResult result = MonitorExitResult.PROCEED_RESULT;
        String filePath = monitorMetaData.get(MonitorMetaDataConstants.FILE_PATH_KEY);
        String transferTaskDetails = taskDetails.dereference().toString();
        String fTriggerFileFolder = new File(filePath).getParent();
        
		String finalLockFilePath = fTriggerFileFolder + "/" + uniqueLockFileName;
		String updatedXml = moveFileAndUpdateTask(finalLockFilePath, transferTaskDetails);
		if(updatedXml != null) {
			taskDetails.assign(updatedXml);
			try {
				File touchTrigger = new File(filePath);
				touchTrigger.setLastModified(System.currentTimeMillis());
			}catch(Exception ex) {
				System.out.println(ex);
			}
		}
		else {
			result = new MonitorExitResult(MonitorExitResultCode.CANCEL_TASK, "Task canceled");
		}
			
		//System.out.println("onMonitor - Exit: " + taskDetails.dereference().toString());
		return result;
	}

	/* (non-Javadoc)
	 * @see com.ibm.wmqfte.exitroutine.api.MonitorExit2#onMonitor(java.util.Map, java.util.List, com.ibm.wmqfte.exitroutine.api.Reference)
	 */
	@Override
	public MonitorExitResult onMonitor(Map<String, String> arg0,
			List<Map<String, String>> arg1, Reference<String> arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * - Have multiple agents monitor the same directory
     * - Each monitor calls a user exit before the monitor starts a transfer, which
     *   - Checks for the presence of a temporary lock file
     *   - If it exists, waits until the lock file has been removed by the resource monitor who created it
     *   - If the lock file doesn't it, creates one
     *   - While it owns the lock file, moves one file it has found into a temporary (and uniquely named) sub-directory
     *   - Then replaces the file list with the name of file moved to sub-directory
     *   - Deletes the temporary lock file
     *   - Proceeds with the transfer
	 */
	
	private String moveFileAndUpdateTask(final String uniqueLockFileName, final String taskXml ) {
		FileLock uniqueFileLock = null;
		FileChannel uniqueLockFileChannel = null;
		//System.out.println("moveFilesAndUpdateTask - Entry");
		String updatedXml = null;
		
		try {
			//Take a lock on the file
			Path uniqueLockFilepath = Paths.get(uniqueLockFileName);
			uniqueLockFileChannel = FileChannel.open(uniqueLockFilepath, StandardOpenOption.DELETE_ON_CLOSE, 
					StandardOpenOption.CREATE, 
					StandardOpenOption.WRITE, 
					StandardOpenOption.READ);
			uniqueFileLock = uniqueLockFileChannel.lock();
			
			// Now that we have got lock, move the files to sub directory
			String sourcePath = getString("source",taskXml);
			String sourcePathAlternate = new File(sourcePath).getParent() + "\\Unique";

			String movedFileName = moveFiles(sourcePath, sourcePathAlternate);
			if(movedFileName != null) {
				//System.out.println("moveFilesAndUpdateTask - Files moved to another directory: " + movedFileName );
				updatedXml = setString("source", movedFileName, taskXml);				
			} else {
				//System.out.println("moveFilesAndUpdateTask - No file moved to temporary directory: ");				
			}
		}catch(Exception ex){
			System.out.println("moveFilesAndUpdateTask: Threw an exception\n" + ex);
		}finally {
			if(uniqueFileLock != null){
				try {
					uniqueFileLock.release();
					uniqueFileLock.close();
				}catch(Exception ex) {
					// Ignore exception
					System.err.println(ex);
				}
			}
			
			if(uniqueLockFileChannel != null) {
				try {
					uniqueLockFileChannel.close();
				}catch(Exception ex) {
					System.err.println(ex);
				}
			}
			//System.out.println("moveFilesAndUpdateTask - Exit");				
		}
		return updatedXml;
	}

	/**
	 * Returns the value of given element
	 * @param tagName
	 * @param element
	 * @return
	 */
	
	protected String getString(String tagName, String xml) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xml)));
			Element rootElement = document.getDocumentElement();
			
	        NodeList list = rootElement.getElementsByTagName(tagName);
	        if (list != null && list.getLength() > 0) {
	            NodeList subList = list.item(0).getChildNodes();

	            if (subList != null && subList.getLength() > 0) {
	                return subList.item(0).getTextContent();
	            }
	        }			
		}catch (Exception ex) {
			System.out.println(ex);
		}
        return null;
    }

	/**
	 * Updates node in XML
	 * @param tagName
	 * @param target
	 * @param xml
	 */
	protected String setString(final String tagName, final String target, final String xml) {
		//System.out.println("setString - Entry");
		String updatedXml = null;
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xml)));
			Element rootElement = document.getDocumentElement();
			
	        NodeList list = rootElement.getElementsByTagName(tagName);
	        if (list != null && list.getLength() > 0) {
	            NodeList subList = list.item(0).getChildNodes();

	            if (subList != null && subList.getLength() > 0) {
	                subList.item(0).setTextContent(target);
	            }
	        }
	        
	        TransformerFactory transfac = TransformerFactory.newInstance();
	        Transformer trans = transfac.newTransformer();
	        trans.setOutputProperty(OutputKeys.METHOD, "xml");
	        trans.setOutputProperty(OutputKeys.INDENT, "yes");

	        StringWriter sw = new StringWriter();
	        StreamResult result = new StreamResult(sw);
	        DOMSource source = new DOMSource(document.getDocumentElement());

	        trans.transform(source, result);
	        updatedXml = sw.toString();		
		}catch (Exception ex) {
			System.out.println(ex);
		}finally {
			//System.out.println("setString - Exit");			
		}
		return updatedXml;
    }
	
	
	/**
	 * Move files to give directory
	 * @param fromSubDirectory
	 * @param toSubDirectoryName
	 */
	private String moveFiles(final String fromDirectory, final String toDirectory) {
		//System.out.println("moveFiles - Entry");				
		String fileMoved = null;
		
        try {
        	File sourceFolder = new File(fromDirectory);
        	File destinationFolder = new File(toDirectory);
        	
        	// Create if target directory does not exist.
        	if(!destinationFolder.exists()) {
        		destinationFolder.mkdirs();
        	} 
        	
       	    // Check weather source exists and it is folder.
        	if (sourceFolder.exists() && sourceFolder.isDirectory()){
    	        // Get list of the files and iterate over them
    	        File[] listOfFiles = sourceFolder.listFiles();

    	        if ((listOfFiles != null) && (listOfFiles.length > 0)){
	                // Move the first file in the list to temporary destination folder
	        		String movedFile = destinationFolder + "\\" + listOfFiles[0].getName();
	        		listOfFiles[0].renameTo(new File(movedFile));
    	            fileMoved = movedFile;
    	        }
    	    }
        } catch (Exception e) {
            System.err.println(e);
        }finally {
    		//System.out.println("moveFiles - Exit");				        	
        }
        return fileMoved;
	}		
}

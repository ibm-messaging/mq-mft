# IBM MQ Managed File Transfer 

This repository is the place for IBM MQ Managed File Transfer Exits that can be used for customizing the transfers. It is also place for "How To" documents, tips etc.

## 1. Delete Files Exit
There could be multiple files, like all files in a directory, are to be transferred as part of one transfer request. When such a transfer request is submitted, it's possible that some of the files in the transfer request are successfully transferred and some fail to transfer
for some reason. In such a case, the transfer is marked partial successful. If the transfer request specified "source_file_disposition" as "delete" (i.e. -sd delete), then the files which were transferred successfully are deleted at source and the files that failed to 
transfer are left as it is.

Since the transfer was partially successful, the user submits the reuest again to transfer the remaining files. But this request fails as some of the files have been transferred in the previous attempt and were deleted. 
  
This sample SourceTransferEndExit exit deletes the files after a transfer is completed successfully. Files are not deleted if for some reason transfer fails or partially complete. 

More details in source file: 

     https://github.com/ibm-messaging/mq-mft/blob/master/mft.samples/src/mft/samples/DeleteFilesExit.java

## 2. Filter File Exit
Assume a there are a number of files in directory and user wants to transfer only those files whose size matches a certain value. For example user wants transfer only files whose size is between 10 and 12 mega bytes.
  
When a transfer is initiated, an agent would pick all files in the directory and transfer, no matter what the size of file is. With this exit, the agent would transfer only those files that match specified size.

More details in source file: 

     https://github.com/ibm-messaging/mq-mft/blob/master/mft.samples/src/mft/samples/FileFilterExit.java

## 3. Archive Files Exit
There are scenarios where it is required to archive the files in a source directory and move to a different place. ArchiveFiles exit extends the MFT functionality by providing the ability to archive files at source agent. The files at source agent are archived as zip file and copied to a folder.

The path where the zip file is created can be specified using "ARCHIVE_PATH" key in transfer metadata. If the value specified to "ARCHIVE_PATH" points to a directory, then the zip file will be created in that directory with "archive<Current time in millisecond>.zip name, for example "archive123445858.zip". If the value points to a file, then a zip file with that name will be created. If "ARCHIVE_PATH" key is not specified, then the path of the first file in the transfer list will be used to create the zip file.

More details in source file: 

    https://github.com/ibm-messaging/mq-mft/blob/master/mft.samples/src/mft/samples/ArchiveFiles.java

## 4. Rename destination file based on MQMD attribute.
IBM MQ Managed File Transfer can be used for transferring files from one point to another point in a MQ network. Apart from moving files, MFT can also move messages from a queue and write them as files at the destination i.e. Message-to-File transfer. MFT can also do File to Message transfer.

The Message-to-File transfer helps in integrating applications in solution where one application generates it's output as messages but the next application in the solution that needs process the information contained in the messages does not have the capability to handle messages as it requires input to be in files. 

There can be scenarios where, in a Message-to-File transfer, the name of the destination file is required to be generated dynamically and is controlled by attribute on the first message. Typically a message propertis and variable substitution technique is used for this purpose as described in the link below:

     http://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.5.0/com.ibm.wmqfte.doc/m2f_mon_variable.htm

However there can be cases where a legacy applications putting messages to a queue does not have ability to set message properties. The application can set MQMD attributes though. Because of this the destination file name can't be dynamically controlled and set by the sender. This exit (and combination with accompanying RenameFile exit) helps in such scenario.

More details in source files: 

     https://github.com/ibm-messaging/mq-mft/blob/master/mft.samples/src/mft/samples/InsertMetadata.java
     https://github.com/ibm-messaging/mq-mft/blob/master/mft.samples/src/mft/samples/RenameFile.java

##Other IBM MQ MFT Exits:
File Ordering by Steve Parsons: https://github.com/ibm-messaging/mq-mft-file-ordering


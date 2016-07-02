# IBM MQ Managed File Transfer 

This repository is place for IBM MQ Managed File Transfer Exits that can be used for customizing the transfers. It is also place for "How To" documents, tips etc.

## 1. Delete Files Exit
There could be multiple files, like all files in a directory, are to be transferred as part of one transfer request. When such a transfer request is submitted, it's possible that some of the files in the transfer request are successfully transferred and some fail to transfer
for some reason. In such a case, the transfer is marked partial successful. If the transfer request specified "source_file_disposition" as "delete" (i.e. -sd delete), then the files which were transferred successfully are deleted at source and the files that failed to 
transfer are left as it is.

Since the transfer was partially successful, the user submits the reuest again to transfer the remaining files. But this request fails as some of the files have been transferred in the previous attempt and were deleted. 
  
This sample SourceTransferEndExit exit deletes the files after a transfer is completed successfully. Files are not deleted if for some reason transfer fails or partially complete. 

More details in source file: https://github.com/ibm-messaging/mq-mft/blob/master/mft.samples/src/mft/samples/DeleteFilesExit.java

## 2. Filter File Exit
Assume a there are a number of files in directory and user wants to transfer only those files whose size matches a certain value. For example user wants transfer only files whose size is between 10 and 12 mega bytes.
  
When a transfer is initiated, an agent would pick all files in the directory and transfer, no matter what the size of file is. With this exit, the agent would transfer only those files that match specified size.

More details in source file: https://github.com/ibm-messaging/mq-mft/blob/master/mft.samples/src/mft/samples/FileFilterExit.java

##Other IBM MQ MFT Exits:
File Ordering by Steve Parsons: https://github.com/ibm-messaging/mq-mft-file-ordering


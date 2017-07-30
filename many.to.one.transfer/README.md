# IBM MQ Managed File Transfer - Many to one file transfer using Ant script

## Introduction
This sample demonstrates how to transfer many files present at source end as one file at destination. This script also demonstrates how to run dependent transfers i.e, the second transfer runs only if the first transfer is successful. 

To achive "Many to one file" transfer, this solution first runs a "File to Message" transfer followed by "Message to File" transfer. In the first step all files at source are transferred as messages to a queue. In the second step messagesfrom that queue are transferred as one file.

## Required Configuration
1) The script requires a queue "MFT.MANY.TO.ONE.Q" to be defined in destination agent's queue manager.
   Use runmqsc to create the queue: 
	define ql(MFT.MANY.TO.ONE.Q)
   
2) Set access authority to queue for the user under which destination agent runs. This sample assumes the destination agent runs under use "samantha"

		setmqaut -m MFTDEMO -n "MFT.MANY.TO.ONE.Q" -t q -p "samantha" -remove
		setmqaut -m MFTDEMO -n "MFT.MANY.TO.ONE.Q" -t q -p "samantha" +browse +get +put

3) Copy the cleanq.bat or cleanq.sh file to a directory on the file system. On Windows copy the cleanq.bat file to say, "C:\MFTCommands" directory. For Unix/Linux platforms, copy the cleanq.sh to a directory say "/usr/mftcmds".

4) Add the following properties to destination agent properties file. 

	On Windows
	
	  commandPath=C:\\MFTCommands 
	  enableQueueInputOutput=true
	  
	On Unix/Linux
	
	  commandPath=/usr/mftcmds 
	  enableQueueInputOutput=true
5) Stop and Start the agent for the changes to get applied.

6) Copy the manyfiletoonefile.xml file to any folder on file system, say C:\MFTCommands on Windows

## Run the script to initiate transfer. Here is an example:

	fteAnt -f manyfiletoonefile.xml -DTIMEOUT=120 -DSRCFOLDER="C:\SRCFILES\" -DDESTINATIONFILE=C:\DESTINATION\many2one.txt -DSRC=SRC@MFTDEMO -DDEST=DEST@MFTDEMO -DCMDQM=MFTDEMO
    
	Parameters:
	  -DTIMEOUT=120 - Wait for 120 seconds for first transfer, i.e. "File to message" to complete.
	  -DSRCFOLDER=C:\SRCFILES - Name of the source folder where files to be transferred are located.
	  -DDESTINATIONFILE=C:\DESTINATION\many2one.txt - Name of the destination file.
	  -DSRC=SRC@MFTDEMO - Name of the source agent in <agent>@<agent qm> format.
	  -DDEST=DEST@MFTDEMO - Name of the source agent in <agent>@<agent qm> format.
	  -DCMDQM=MFTDEMO - Name of the MFT command queue manager
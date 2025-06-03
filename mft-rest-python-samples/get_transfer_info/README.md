# **MFT Transfer Information Script**

This Python script allows you to filter IBM MQ Managed File Transfers (MFT) using a variety of parameters via the IBM MQ REST API. It fetches all transfers and prints only those that match the user-specified filters such as transfer state, agent names, originator details, and file statistics.

\- Outputs JSON-formatted transfer details

**USAGE**

Run the script using:
```bash
python3 get_transfer_info.py -host <HOSTNAME> -port <PORT> -ts <TRANSFER-STATE>

-sa <SOURCE-AGENT> 

-da <DESTINATION-AGENT> 

-mqid <MQMD-USER-ID> 

-uid <USER-ID> 

-et <END-TIME> 

-nff <NUMBER-OF-FILE-FAILURES> 

-nfs <NUMBER-OF-FILE-SUCCESSES> 

-nfw <NUMBER-OF-FILE-WARNING> 

-nof <NUMBER-OF-FILES> 

-rc <RETRY-COUNT> 

-st <START-TIME>
```

**Script Arguments**

| ARGUMENT | REQUIRED | DESCRIPTION |
| --- | --- | --- |
| \-host | YES | Hostname of the IBM MQ REST API server |
| \-port | YES | Port number of the IBM MQ REST API server |
| \-sa  | NO  | Source Agent used for the transfer |
| \-da | NO  | Destination Agent used for the transfer |
| \-mqid | NO  | Originator MQMD user ID |
| \-et | NO  | End time of the transfer |
| \-st | NO  | Start time of the transfer |
| \-nff | NO  | Number of file failures |
| \-nof | NO  | Number of files transferred |
| \-nfs | NO  | Number of file successful |
| \-nfw | NO  | Number of file warnings |
| \-rc | NO  | Retry count |
| \-uid | NO  | User Id |

**SAMPLE OUTPUT**

```bash
python3 get_transfer_info.py -host fyre-9-30-166-65.svl.ibm.com -port 9443 -sa SA -da DA -ts failed
```
Expected Output:
```json
{

"transferSet": {

"item": \[

{

"destination": {

"file": {

"path": "/home/mftuser/to/test1.txt",

"size": 0,

"endOfLine": "",

"lastModified": "",

"encoding": ""

},

"type": "file"

},

"source": {

"disposition": "",

"file": {

"path": "/home/mftuser/from/script_test.txt",

"size": 19,

"endOfLine": "",

"lastModified": "",

"encoding": ""

},

"type": "file"

},

"status": {

"description": "",

"state": "inProgress"

}

},

{

"destination": {

"file": {

"path": "/home/mftuser/to/test1.txt",

"size": 0,

"endOfLine": "",

"lastModified": "",

"encoding": ""

},

"type": "file"

},

"source": {

"disposition": "",

"file": {

"path": "/home/mftuser/from/script_test.txt",

"size": 19,

"endOfLine": "",

"lastModified": "",

"encoding": ""

},

"type": "file"

},

"status": {

"description": "",

"state": "inProgress"

}

},

{

"mode": "text",

"destination": {

"actionIfExists": "error",

"file": {

"path": "/home/mftuser/to/test1.txt",

"size": 0,

"endOfLine": "LF",

"lastModified": "2025-04-09T03:54:04.669Z",

"encoding": "UTF-8"

},

"type": "file"

},

"source": {

"disposition": "leave",

"file": {

"path": "/home/mftuser/from/script_test.txt",

"size": 19,

"endOfLine": "LF",

"lastModified": "2025-04-09T03:53:42.170Z",

"encoding": "UTF-8"

},

"checksum": {

"method": "md5",

"value": "4F94F84D7819C8E98B5F73B82F881AC5"

},

"type": "file"

},

"status": {

"description": "BFGTR0072E: The transfer failed to complete due to the exception : BFGIO0006E: File \\"/home/mftuser/to/test1.txt\\" already exists.",

"state": "failed"

}

}

\],

"bytesSent": 0

},

"userProperties": {},

"id": "414D51204147454E5432514D2020202022A3F467045C0040",

"destinationAgent": {

"qmgrName": "AGENT2QM",

"name": "DA"

},

"originator": {

"host": "10.11.117.45",

"mqmdUserId": "mftuser",

"userId": "mftuser"

},

"job": {

"name": "Transfer Job"

},

"sourceAgent": {

"qmgrName": "AGENT2QM",

"name": "SA"

},

"status": {

"description": "BFGRP0034I: The file transfer request has completed with no files being transferred.",

"lastStatusUpdate": "2025-04-09T03:57:41.548Z",

"state": "failed"

},

"statistics": {

"numberOfFileFailures": 1,

"numberOfFiles": 1,

"retryCount": 0,

"numberOfFileWarnings": 0,

"startTime": "2025-04-09T03:57:41.302Z",

"numberOfFileSuccesses": 0,

"endTime": "2025-04-09T03:57:41.548Z"

}

}
```

# **MFT Initiate Transfer Script**

This Python script allows you to initiate a file transfer between two agents in IBM MQ Managed File Transfer (MFT) using the REST API. It also supports optional scheduling of the transfer.

\- Outputs JSON-formatted transfer details

**USAGE**

Run the script using:

```bash
python3 initiate_transfer.py -host <HOSTNAME> -port <PORT> -sa <SRC_AGENT> -sm <SRC_QMGR> -da <DEST_AGENT> -dm <DEST_QMGR> -src <SOURCE> -st <SOURCE_TYPE> -dest <DEST> -dt <DEST_TYPE> -m <MODE> [--start-time <YYYY-MM-DDTHH:MM>]
```

Example:

```bash
python3 initiate_transfer.py -host 9.46.238.57 -port 9443 -sa SA -sm AGENTQM2 -da DA -dm AGENT2QM -src /home/mftuser/from/test_source.txt -st file -dest /home/mftuser/to/test_dest.txt -dt file -m file
```
**Script Arguments**

| ARGUMENT | REQUIRED | DESCRIPTION |
| --- | --- | --- |
| \-host | YES | Hostname of the IBM MQ REST API server |
| \-port | YES | Port number of the IBM MQ REST API server |
| \-sa | YES | Source Agent used for the transfer |
| \-sm | YES | Queue Manager which hosts the Source Agent |
| \-da | YES | Destination Agent used for the transfer |
| \-dm | YES | Queue Manager which hosts the Destination Agent |
| \-src | YES | Source file/queue/directory name |
| \-st | YES | Type of source. Can be file/directory/queue |
| \-dest | YES | Destination file/queue/directory name |
| \-dt | YES | Type of destination. Can be file/directory/queue |
| \-m | YES | Mode of transfer. Can be file or binary |
| \--start-time | NO  | Optional ISO timestamp to schedule transfer (e.g., 2025-03-19T02:25) |

**SAMPLE USAGE**
```bash
python3 initiate_transfer.py -host fyre-9-30-166-65.svl.ibm.com -port 9443 -sa SA -sm AGENT2QM -da DA -dm AGENT2QM -src /home/mftuser/from/test_transfer.txt -st file -dest /home/mftuser/to/test_msg.txt -dt file -m text
```
Expected output:

```json
Transfer initiated successfully.

Response: {"transfer": \[{

"transferSet": {

"item": \[

{

"destination": {

"file": {

"path": "/home/mftuser/to/test_msg.txt",

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

"path": "/home/mftuser/from/test_transfer.txt",

"size": 30,

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

"path": "/home/mftuser/to/test_msg.txt",

"size": 30,

"endOfLine": "",

"lastModified": "",

"encoding": ""

},

"type": "file"

},

"source": {

"disposition": "",

"file": {

"path": "/home/mftuser/from/test_transfer.txt",

"size": 30,

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

"path": "/home/mftuser/to/test_msg.txt",

"size": 30,

"endOfLine": "LF",

"lastModified": "2025-04-10T11:37:47.682Z",

"encoding": "UTF-8"

},

"checksum": {

"method": "md5",

"value": "B68062E3CC5F531AE175046DBA3CF0B9"

},

"type": "file"

},

"source": {

"disposition": "leave",

"file": {

"path": "/home/mftuser/from/test_transfer.txt",

"size": 30,

"endOfLine": "LF",

"lastModified": "2025-04-10T11:09:10.162Z",

"encoding": "UTF-8"

},

"checksum": {

"method": "md5",

"value": "B68062E3CC5F531AE175046DBA3CF0B9"

},

"type": "file"

},

"status": {

"description": "",

"state": "successful"

}

}

\],

"bytesSent": 30

},

"userProperties": {},

"id": "414D51204147454E5432514D2020202022A3F46701890F40",

"destinationAgent": {

"qmgrName": "AGENT2QM",

"name": "DA"

},

"originator": {

"host": "10.11.117.45",

"mqmdUserId": "mftuser",

"userId": "mftuser"

},

"job": {"name": "Transfer Job"},

"sourceAgent": {

"qmgrName": "AGENT2QM",

"name": "SA"

},

"status": {

"description": "BFGRP0032I: The file transfer request has successfully completed.",

"lastStatusUpdate": "2025-04-10T11:37:47.728Z",

"state": "successful"

},

"statistics": {

"numberOfFileFailures": 0,

"numberOfFiles": 1,

"retryCount": 0,

"numberOfFileWarnings": 0,

"startTime": "2025-04-10T11:37:47.612Z",

"numberOfFileSuccesses": 1,

"endTime": "2025-04-10T11:37:47.728Z"

}

}\]}
```
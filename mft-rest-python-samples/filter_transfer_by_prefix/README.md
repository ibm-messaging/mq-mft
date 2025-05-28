# **MFT Transfer Filter by Prefix Script**

This Python script fetches and filters IBM MQ Managed File Transfer (MFT) transfer records from the REST API based on the file name prefix.

\- Outputs JSON-formatted transfer details

**Usage**

Run the script using:

```bash
python3 filter_transfer_by_prefix.py -host <HOSTNAME> -port <PORT> -fp <FILE_PREFIX>
```

Example:
```bash
python3 filter_transfer_by_prefix.py -host 9.46.238.57 -port 9443 -fp new_file
```

**Script Arguments**

| ARGUMENT | REQUIRED | DESCRIPTION |
| --- | --- | --- |
| \-host | YES | Hostname of the IBM MQ REST API server |
| \-port | YES | Port number of the IBM MQ REST API server |
| \-fp | YES | Prefix of the file name to filter transfers |

**Output**

The response will have details of the monitor in a JSON array.

The default attributes are:

1) destinationAgent

2) originator

3) sourceAgent

4) statistics

5) status

6) transferSet

More information about these attributes can be found at :

<https://www.ibm.com/docs/en/ibm-mq/9.4.0?topic=get-response-body-attributes-transfers>

**SAMPLE OUTPUT**

```bash
python3 filter_transfer_by_prefix.py -host fyre-9-30-166-65.svl.ibm.com -port 9443 -fp test_transfer
```
Expected Output:
```json
Transfers with file names starting with 'test_transfer':

{

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

"mode": "binary",

"destination": {

"actionIfExists": "error",

"file": {

"path": "/home/mftuser/to/test_msg.txt",

"size": 30,

"endOfLine": "",

"lastModified": "2025-04-10T11:12:11.889Z",

"encoding": ""

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

"endOfLine": "",

"lastModified": "2025-04-10T11:09:10.162Z",

"encoding": ""

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

"id": "414D51204147454E5432514D2020202022A3F46701520F40",

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

"name": ""

},

"sourceAgent": {

"qmgrName": "AGENT2QM",

"name": "SA"

},

"status": {

"description": "BFGRP0032I: The file transfer request has successfully completed.",

"lastStatusUpdate": "2025-04-10T11:12:11.933Z",

"state": "successful"

},

"statistics": {

"numberOfFileFailures": 0,

"numberOfFiles": 1,

"retryCount": 0,

"numberOfFileWarnings": 0,

"startTime": "2025-04-10T11:12:11.785Z",

"numberOfFileSuccesses": 1,

"endTime": "2025-04-10T11:12:11.933Z"

}

}
```

# **MFT Resource Monitor Filter Script**

This Python script allows you to list and filter IBM MQ Managed File Transfer (MFT) resource monitors via the REST API. You can filter by the following options:

1) monitorName

2) type

3) state

\- Outputs JSON-formatted monitor details

**Usage**

Run the script using:
```bash
python3 list_resource_monitors.py -host <HOSTNAME> -port <PORT> -name <MONITOR_NAME> -state <MONITOR_STATE> -type <TYPE> -details <TRUEorFALSE>
```

Example:
```bash
python3 list_resource_monitors.py -host 9.46.238.57 -port 9443
```
**Script Arguments**

| ARGUMENT | REQUIRED | DESCRIPTION |
| --- | --- | --- |
| \-host | YES | Hostname of the IBM MQ REST API server |
| \-port | YES | Port number of the IBM MQ REST API server |
| \-name | NO  | Filter by monitor name |
| \-state | NO  | Filter by monitor state |
| \-type | NO  | Filter by monitor type |
| \-details | NO  | Displays detailed information about the monitor, default value is False |

**Output**

The response will have details of the monitor in a JSON array.

The default attributes are:

name- The unique name of the resource monitor

type- The type of resource monitor. This can be either queue or directory.

agentName-The name of the agent that own the resource monitor

State-The state of the resource monitor. This can be either started or stopped.

Some of the other attributes returned within the JSON object are :  

1. general

2. resource

3. transferDefinition

4. triggerCondition

5. triggerFileContentFormat

More information about these attributes can be found at:

<https://www.ibm.com/docs/en/ibm-mq/9.4.0?topic=get-response-body-attributes-list-resource-monitor>

**SAMPLE OUTPUT**
```bash
python3 list_resource_monitors.py -host 9.46.238.57 -port 9443 -name MONITOR1 -details True
```
Expected Output:
```json
{

"general": {

"pollInterval": 1,

"matchesPerTask": 1,

"pollIntervalUnit": "minutes"

},

"triggerCondition": {

"excludePattern": "",

"type": "matchAll",

"matchPattern": "wildcard",

"includePattern": "test2.txt"

},

"resource": {

"recursionLevel": 0,

"name": "/home/mftuser/resource_transfer"

},

"transferDefinition": {

"transferSet": {

"item": \[

{

"mode": "binary",

"checksum": "md5",

"destination": {

"actionIfExists": "overwrite",

"name": "/home/mftuser/resource_transfer/",

"type": "directory"

},

"source": {

"disposition": "leave",

"name": "/home/mftuser/resource_transfer/test2.txt",

"type": "file",

"recursive": false

}

}

\],

"recoveryTimeout": -1,

"userProperties": {},

"priority": 0

},

"destinationAgent": {

"name": "AGENT11",

"qmgrName": "AGENT1QM"

},

"originator": {

"host": "10.11.117.45",

"userId": "mftuser"

},

"job": {

"name": ""

},

"sourceAgent": {

"name": "AGENT23",

"qmgrName": "AGENT2QM"

}

},

"name": "MONITOR1",

"userProperties": {},

"agentName": "AGENT23",

"state": "started",

"type": "directory",

"defaultVariables": {}

}
```

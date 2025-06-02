# **MFT Resource Monitor Creator Script**

This Python script simplifies the creation of IBM MQ Managed File Transfer (MFT) monitor resources via the MFT REST API. It allows you to define the resource monitors, configure polling and trigger conditions, and set up a complete transfer workflow.

This script uses a create_resource_monitor.json file to store configuration parameters required for creating a resource monitor. The json file allows users to easily edit and customize the parameters without modifying the script.

\- Outputs JSON-formatted resource monitor details

**USAGE**

Run the script using:

```bash 
python3 create_resource_monitor.py 
```

**Arguments**

| ARGUMENT | REQUIRED | DESCRIPTION |
| --- | --- | --- |
| \-host | YES | Hostname of the IBM MQ REST API server |
| \-port | YES | Port number of the IBM MQ REST API server |
| \-name | YES | Unique name for the resource monitor |
| \-type | YES | Resource type to monitor: directory or queue |
| \-poll-interval | NO  | Polling frequency (default: 1) |
| \-poll-interval-unit | NO  | Unit for polling interval: seconds,minutes,hours,days(default:minutes) |
| \-matches-per-task | NO  | Maximum number of matches per task (default: 2) |
| \-resource-name | YES | Directory path or queue name to be monitored |
| \-recursion-level | NO  | Recursion depth for directory (default: 1) |
| \-sa | YES | Source Agent Name |
| \-sm | YES | Source agent's queue manager name |
| \-da | YES | Destination Agent Name |
| \-dm | YES | Destination agent's queue manager name |
| \-src | YES | Source path or queue name |
| \-st | YES | Type of source (queue or directory ) |
| \-dest | YES | Destination path or queue name |
| \-dt | YES | Type of destination(queue or directory) |
| \-m | NO  | Transfer Mode (text or binary) |
| \-include-pattern | YES | File pattern to include (e.g., '\*.txt') |
| \-exclude-pattern | NO  | File pattern to exclude |
| \-trigger-type | YES | Trigger type (e.g., matchAll, matchNone, noChangeInSize, etc.) |
| \-no-file-size-change-poll-count | NO  | Number of polls for no file size change (for 'noChangeInSize') |
| \-file-size | NO  | File size threshold (for 'sizeGreaterOrEqualTo') |
| \-file-size-unit | NO  | Unit for file size: bytes, KB, MB, GB |
| \-match-pattern | NO  | Pattern to match files |

**SAMPLE OUTPUT**
```bash
python3 create_resource_monitor.py 
```
Expected Output:
```json
Monitor created successfully.

Response:

Details of Monitor 'TESTMONITOR1':

{"monitor": \[{

"resource": {"name": "/home/mftuser/from"},

"name": "TESTMONITOR",

"agentName": "SA",

"state": "started",

"type": "directory"

}\]}
```

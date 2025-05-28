# **MFT Transfer Summary Script**

This Python script retrieves and summarizes the status of IBM MQ Managed File Transfers (MFT) via the REST API. It gives an overview of all transfers and their state, and provides agent-specific insights.

**USAGE**

Run the script using:

```bash
python3 mft_transfer_summary.py -host <HOSTNAME> -port <PORT>
```

Example:

``` bash
python3 mft_transfer_summary.py -host 9.46.238.57 -port 9443
```

**Script Arguments**

| ARGUMENT | REQUIRED | DESCRIPTION |
| --- | --- | --- |
| \-host | YES | Hostname of the IBM MQ REST API server |
| \-port | YES | Port number of the IBM MQ REST API server |

**SAMPLE OUTPUT**

```bash
python3 mft_transfer_summary.py -host fyre-9-30-166-65.svl.ibm.com -port 9443
```
Expected output:
```json
Summary of Transfers since mq webserver was started: 

Total Transfers: 58
Failed Transfers: 25
Successful Transfers: 31
In Progress Transfers: 0
Other States:
  partiallySuccessful: 2

Transfer Summary by Source Agent:

Source Agent: SA1
  Total Transfers: 18
  Failed Transfers: 3
  Successful Transfers: 15
  In Progress Transfers: 0

Source Agent: SA
  Total Transfers: 33
  Failed Transfers: 19
  Successful Transfers: 12
  In Progress Transfers: 0
  Other States:
    partiallySuccessful: 2

Source Agent: DA
  Total Transfers: 7
  Failed Transfers: 3
  Successful Transfers: 4
  In Progress Transfers: 0
```

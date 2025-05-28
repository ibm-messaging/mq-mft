# **IBM MQ MFT Operations Using Python Scripts**

This project contains a set of Python scripts designed to interact with IBM MQ Managed File Transfer (MFT) via the REST API.These scripts all the user to initiate transfers, filter and summarise the transfers. Additionally they can also use it to create resource monitors and list and filter them out.

**Getting Started**


1. **Windows:** Refer to the [official Python documentation for Windows](https://docs.python.org/3/using/windows.html) for instructions on downloading and installing Python.
2. **macOS:** Refer to the [official Python documentation for macOS](https://docs.python.org/3/using/mac.html) to download and install Python.
3. **Unix/Linux:** Refer to the [official Python documentation for Unix platforms](https://docs.python.org/3/using/unix.html) for guidance on installing Python.

**Prerequisites:**

You need Python 3.6 or higher installed on your system.

Before using these scripts, ensure that IBM MQ Managed File Transfer (MFT) is properly configured. Follow the official IBM documentation for setup instructions: [Configuring MFT for first use](https://www.ibm.com/docs/en/ibm-mq/9.4.x?topic=transfer-configuring-mft-first-use)

For additional guidance on configuring Managed File Transfer (MFT) between two machines, refer to this resource: [IBM Managed File Transfer Configuration Guide](https://community.ibm.com/community/user/viewdocument/ibm-managed-file-transfer-configura?CommunityKey=183ec850-4947-49c8-9a2e-8e7c7fc46c64&tab=librarydocuments&hlmlt=BL).

To use the REST API for Managed File Transfer (MFT), ensure that the MQ web server is properly configured. For detailed instructions on configuring the MQ web server, refer to the official IBM documentation: [Basic configuration of the mqweb server](https://www.ibm.com/docs/en/ibm-mq/9.4.x?topic=api-basic-configuration-mqweb-server).


**Installation**

1. Open your terminal or command prompt and create a new directory:

```
mkdir python_samples
cd python_samples
```

2. Clone this repository:

`git clone https://github.com/ibm-messaging/MFT_samples.git `

**Repository Structure**

This repository contains the following directories, each serving a specific purpose:

1. `initiate_transfers`: Script to initiate MFT transfers between two agents via the REST API.
2. `get_transfer_info`: Script to filter IBM MQ MFT transfers using various parameters such as transfer state, agent names, originator details, and file statistics.
3. `mft_transfer_summary`: Script to summarize all file transfers performed using IBM MQ MFT.
4. `filter_transfer_by_prefix`: Script to filter transfer information based on file prefixes, useful for large transfers.
5. `create_resource_monitor`: Script to create an MFT resource monitor.
6. `list_resource_monitors`: Script to list and filter information about all resource monitors.

This PR introduces a collection of Python scripts designed to interact with IBM MQ Managed File Transfer (MFT) using the REST API. The scripts provide functionality to:

Initiate file transfers between agents
Filter and summarize transfer information
Create and manage resource monitors
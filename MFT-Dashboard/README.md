# MFT-Dashboard

**MFT-Dashboard** is a Streamlit-powered web dashboard for visualizing and monitoring IBM MQ Managed File Transfer (MFT) logs. It parses, structures, and visualizes transfer events, scheduled transfers, monitors, agents, and more—all with interactive charts and tables.

---

## Features

- **Log Parsing & Categorization:**  
  `json_converter.py` reads IBM MFT log files, parses each line, structures events, and categorizes entities (monitors, agents, queue managers, transfer events).
- **Data Transformation:**  
  Produces merged and scheduled transfer JSON summaries for further analysis or visualization.
- **MongoDB Integration:**  
  `mft_dashboard.py` loads parsed data into MongoDB, enabling persistent storage and fast retrieval.
- **Interactive Dashboard:**  
  Visualizes transfer statistics, monitor status, recent and scheduled transfers
- **Downloadable Data:**  
  Export all or scheduled transfers as CSV directly from the dashboard.

---

## How It Works

### 1. Parse and Structure Log Data

- `json_converter.py` takes a raw MFT log file (e.g., `sample.log`), parses each line, and creates structured events.
- It creates three JSON outputs:
  - `merged_transfers.json`: All parsed events.
  - `scheduled_transfers.json`: Only scheduled/expired transfer events.
  - `categorized_summary.json`: Counts and lists of detected monitors, agents, queue managers, and transfers.

### 2. Load Data & Visualize

- `mft_dashboard.py` runs the converter, loads the JSONs, uploads them to MongoDB, and then renders the dashboard.
- The dashboard shows:
  - **Monitor Status Overview:** Active vs. stopped monitors.
  - **Transfer Statistics:** Donut chart of successful, failed, scheduled, and expired transfers.
  - **Successful vs. Failed Transfers per Agent:** Stacked bar chart by agent.
  - **Recent Transfers:** Table of the last 5 overall, successful, and failed transfers.
  - **Scheduled Transfers:** Upcoming and expired scheduled transfers.

---

## Prerequisites 
To follow this guide, ensure you have the following:
### 1.	 IBM MQ MFT Installed
   - MFT must be installed with the Logger component included.
   - Refer to the official [MQ Documentation](https://www.ibm.com/docs/en/ibm-mq/9.4.x?topic=transfer-configuring-mft-first-use)

### 2.	Coordination Queue Manager Setup
   - A running MQ Coordination Queue Manager is required.
     
### 3.	IBM MFT CLI Tools Available
   - Commands such as fteCreateLogger, fteStartLogger, fteStopLogger, fteShowLoggerDetails and fteModifyLogger must be accessible.
   - Refer to the official [MQ Documentation](https://www.ibm.com/docs/en/ibm-mq/9.4.x?topic=transfer-configuring-mft-logger)
  
     
### 4. Python Environment
   - Python 3.8+ with required libraries:
     ```bash
     pip install streamlit pandas plotly pymongo
     ```
(avoid using Python 3.13)

### 5.MongoDB Instance
   - Local or cloud [MongoDB](https://www.mongodb.com/docs/atlas/)


---

## Step-by-Step Setup Instructions

### Step 1: Set Up the IBM MFT Stand-Alone File Logger

1.1 **Create the Logger:**  
```bash
fteCreateLogger -p <coordinationQM> -loggerType FILE  -fileLoggerMode <mode> -fileSize <size> -fileCount <number> <loggerName>
```
[IBM Documentation](https://www.ibm.com/docs/en/ibm-mq/9.4.x?topic=reference-ftecreatelogger-create-mft-file-database-logger)

1.2 **Create Required Queues:**  
```bash
runmqsc <coordinationQM> < MQ_DATA_PATH/mqft/logs/config/<coordinationQM>/loggers/<loggerName>/<loggerName>_create.mqsc
```

1.3 **Optional Configuration:**  
- Edit `logger.properties` as needed for customization.

1.4 **Start the Logger:**  
```bash
fteStartLogger -p <coordinationQM> <loggerName>
```
Logs will be stored under:  
`MQ_DATA_PATH/mqft/logs/<coordinationQM>/loggers/<loggerName>/logs`

**Stop the logger with:**  
```bash
fteStopLogger -p <coordinationQM> <loggerName>
```

---

### Step 2: Process Log Files Using the Dashboard

- Ensure the log file (`sample.log`) is in the same directory as the Python scripts.
- The Streamlit app will automatically run `json_converter.py` to convert `sample.log` into:
  - `merged_transfers.json`
  - `scheduled_transfers.json`
  - `categorized_summary.json`

- `merged_transfers.json` and `scheduled_transfers.json` will be loaded into the MongoDB collections.

---

### Step 3: Launch the Dashboard

Run the dashboard using:
```bash
streamlit run mft_dashboard.py
```

The application will:
- Validate `sample.log`
- Parse logs into JSON
- Upload results to MongoDB
- Display interactive visualizations

---



## File Overview

- **json_converter.py**  
  - `parse_log_line(line)`: Parses an MFT log line to a structured dict.
  - `categorize_entities(parsed_data)`: Summarizes entity counts and lists.
  - `build_scheduled_transfers(parsed_data)`: Groups scheduled transfers and marks expired ones.
  - Outputs: `merged_transfers.json`, `categorized_summary.json`, `scheduled_transfers.json`.
- **mft_dashboard.py**
  - Runs `json_converter.py` automatically on dashboard launch.
  - Loads JSON outputs, uploads to MongoDB.
  - Visualizes the data in an interactive Streamlit dashboard.
  - Provides CSV download, result code explanations, and transfer/monitor statistics.

---

## Customization

- Edit `log_file_path` in both scripts if your log isn’t named `sample.log`.
- Update MongoDB connection string in `mft_dashboard.py` as needed.

---

## Notes

- Make sure MongoDB is accessible using the provided connection string.
- The dashboard expects log lines in the IBM MFT standard format.
---
## Addition Resources

- [IBM MQ MFT Overview](https://www.ibm.com/docs/en/ibm-mq/9.4.x?topic=configuring-managed-file-transfer)
- [fteCreateLogger](https://www.ibm.com/docs/en/ibm-mq/9.4.x?topic=reference-ftecreatelogger-create-mft-file-database-logger)
- [fteStartLogger](https://www.ibm.com/docs/en/ibm-mq/9.4.x?topic=reference-ftestartlogger-start-mft-logger)
- [fteStopLogger](https://www.ibm.com/docs/en/ibm-mq/9.4.x?topic=reference-ftestoplogger-stop-mft-logger)
- [fteModifyLogger](https://www.ibm.com/docs/en/ibm-mq/9.4.x?topic=reference-ftemodifylogger-run-mft-logger-as-windows-service)
- [fteShowLoggerDetails](https://www.ibm.com/docs/en/ibm-mq/9.4.x?topic=reference-fteshowloggerdetails-display-mft-logger-details)

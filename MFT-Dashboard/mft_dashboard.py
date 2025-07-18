# (c) Copyright IBM Corporation 2025
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import streamlit as st
import pandas as pd
import plotly.graph_objects as go
from pymongo import MongoClient
import json
import os
import subprocess

# --- Streamlit page config ---
st.set_page_config(page_title="MFT Dashboard", layout="wide")

# --- Step 1: Use sample.log from local directory ---
log_path = "sample.log"
if not os.path.exists(log_path):
    st.error("sample.log not found in tshe current directory.")
    st.stop()

# --- Step 2: Run json_converter.py to generate merged_transfers.json and scheduled_transfers.json ---
merged_json_path = "merged_transfers.json"
scheduled_json_path = "scheduled_transfers.json"

for f in [merged_json_path, scheduled_json_path]:
    if os.path.exists(f):
        os.remove(f)

subprocess.run([
    "python3", "json_converter.py",
    "--input", log_path,
    "--merged_output", merged_json_path,
    "--scheduled_output", scheduled_json_path
], check=True)

# --- Step 3: Upload JSONs to MongoDB ---
MONGO_URI = ""
DB_NAME = "mft_dashboard"
MERGED_COLLECTION = "merged_transfers"
SCHEDULED_COLLECTION = "scheduled_transfers"

client = MongoClient(MONGO_URI)
db = client[DB_NAME]

with open(merged_json_path) as f:
    merged_data = json.load(f)
with open(scheduled_json_path) as f:
    scheduled_data = json.load(f)
if isinstance(merged_data, list) and merged_data:
    db[MERGED_COLLECTION].insert_many(merged_data)
if isinstance(scheduled_data, list) and scheduled_data:
    db[SCHEDULED_COLLECTION].insert_many(scheduled_data)

# --- Step 4: Pull data from MongoDB ---
merged_df = pd.DataFrame(list(db[MERGED_COLLECTION].find({}, {"_id": 0})))
scheduled_df = pd.DataFrame(list(db[SCHEDULED_COLLECTION].find({}, {"_id": 0})))

# --- Step 5: Dashboard Components  ---

# Download buttons
col_dl1, col_dl2 = st.columns(2)
with col_dl1:
    st.download_button(
        label="Download All Transfers (CSV)",
        data=merged_df.to_csv(index=False),
        file_name="merged_transfers.csv",
        mime="text/csv"
    )
with col_dl2:
    st.download_button(
        label="Download Scheduled Transfers (CSV)",
        data=scheduled_df.to_csv(index=False),
        file_name="scheduled_transfers.csv",
        mime="text/csv"
    )

# Result code mapping
MFT_RETURN_CODES = {
    "0": ("Success", "The command was successful"),
    "1": ("Command unsuccessful", "The command ended unsuccessfully."),
    "2": ("Command timed out", "The agent did not reply with the status of the command within a specified timeout."),
    "3": ("Acknowledgement timed out", "The agent did not acknowledge receipt of the command within a specified timeout."),
    "4": ("Wrong agent", "The command was sent to the wrong agent."),
    "20": ("Transfer partially successful", "The transfer completed with partial success and some files were transferred."),
    "21": ("Transfer stopped", "The transfer was stopped by one of the user exits."),
    "22": ("Cancel transfer timed out", "The agent received a request to cancel a transfer but the cancellation could not be completed within 30 seconds."),
    "26": ("Cancel ID not found", "The agent received a request to cancel a transfer but the transfer cannot be found."),
    "27": ("Cancel in progress", "The agent received a request to cancel a transfer, but the transfer is already in the process of being canceled."),
    "40": ("Failed", "The transfer failed and none of the files specified were transferred."),
    "41": ("Cancelled", "The transfer was canceled."),
    "42": ("Trigger failed", "The transfer did not take place because the transfer was conditional and the required condition was not met."),
    "43": ("Malformed XML", "An XML message was malformed."),
    "44": ("Source agent capacity exceeded", "The source agent did not have sufficient capacity to carry out the transfer."),
    "45": ("Destination agent capacity exceeded", "The destination agent did not have sufficient capacity to carry out the transfer."),
    "46": ("Source agent maximum number of files exceeded", "The number of files being transferred exceeded the limit of the source agent."),
    "47": ("Destination agent maximum number of files exceeded", "The number of files transferred exceeded the limit of the destination agent."),
    "48": ("Invalid log message attributes", "A log message is malformed. This error is an internal error."),
    "49": ("Destination unreachable", "The source agent is unable send a message to the destination agent due to an IBM MQ problem."),
    "50": ("Trial version violation", "An attempt was made by a trial version agent to communicate with an agent that is not a trial version agent."),
    "51": ("Source transfer not permitted", "The maxSourceTransfers agent property has been set to 0."),
    "52": ("Destination transfer not permitted", "The maxDestinationTransfers agent property has been set to 0."),
    "53": ("Not authorized", "The user is not authorized to perform the operation."),
    "54": ("Authority levels do not match", "The authorityChecking agent property value of the source agent and destination agent do not match."),
    "55": ("Trigger not supported", "An attempt has been made to create a transfer with a trigger on a protocol bridge agent."),
    "56": ("Destination file to message not supported", "The destination agent does not support writing the file to a destination queue"),
    "57": ("File space not supported", "The destination agent does not support file spaces."),
    "58": ("File space rejected", "The file space transfer was rejected by the destination agent."),
    "59": ("Destination message to file not supported", "The destination agent does not support message-to-file transfers."),
    "64": ("Both queues disallowed", "The source and destination of a transfer is a queue."),
    "65": ("General data queue error", "An error occurred when the Managed File Transfer Agent data queue was accessed."),
    "66": ("Data queue put authorization error", "An error occurred when the Managed File Transfer Agent data queue was accessed. Advanced Message Security is not enabled."),
    "67": ("Data queue put AMS error", "An authorization error occurred when the Managed File Transfer Agent data queue was accessed. Advanced Message Security is enabled."),
    "69": ("Transfer Recovery Timed out", "Recovery of a transfer timed out after the specified transferRecoveryTimeout value."),
    "70": ("Agent has ended abnormally", "Application has had an unrecoverable problem and is forcibly terminating."),
    "75": ("Queue manager is unavailable", "The application cannot continue because the queue manager for the application is unavailable."),
    "78": ("Problem with the startup configuration", "The application cannot continue because there is a problem with the startup configuration data."),
    "85": ("Problem with the database server", "The application cannot continue because there is a problem with the database (typically only returned by a logger)"),
    "100": ("Monitor substitution not valid", "The format of a variable substitution within a monitor task XML script was malformed."),
    "101": ("Monitor resource incorrect", "The number of monitor resource definitions was not valid."),
    "102": ("Monitor trigger incorrect", "The number of monitor trigger definitions was not valid."),
    "103": ("Monitor task incorrect", "The number of monitor task definitions was not valid."),
    "104": ("Monitor missing", "The requested monitor is not present."),
    "105": ("Monitor already present", "The requested monitor is already present."),
    "106": ("Monitor user exit error", "A monitor user exit has generated an error during a resource monitor poll."),
    "107": ("Monitor user exit canceled", "A monitor user exit has requested a transaction to be canceled."),
    "108": ("Monitor task failed", "A monitor task has failed to complete due to error in processing the task."),
    "109": ("Monitor resource failed", "A monitor resource definition cannot be applied to the given resource."),
    "110": ("Monitor task variable substitution failed", "A variable has been specified in a monitor task but no matching name has been found in the metadata."),
    "111": ("Monitor task source agent not valid", "The source agent of the monitor transfer task does not match the agent of the resource monitor."),
    "112": ("Monitor task source queue manager not valid", "The source agent queue manager of the monitor transfer task does not match the agent queue manager of the resource monitor."),
    "113": ("Monitor not supported", "An attempt has been made to create or delete a resource monitor on a protocol bridge agent."),
    "114": ("Monitor resource denied", "The directory that is scanned by the monitor resource is denied access."),
    "115": ("Monitor resource queue in use", "The monitor resource queue is already open, and is not compatible for input with shared access."),
    "116": ("Monitor resource queue unknown", "The monitor resource queue does not exist on the associated queue manager of the monitor."),
    "118": ("Monitor resource expression invalid", "An error occurred evaluating the XPath expression."),
    "119": ("Monitor task source agent queue manager missing", "The source agent name or source agent queue manager name is missing from the monitor task definition."),
    "120": ("Monitor queue not enabled", "The monitor resource queue is not enabled."),
    "121": ("Unexpected error when accessing monitor queue", "An unexpected error occurred when accessing the monitor resource queue."),
    "122": ("Monitor command queue not enabled for context id", "The monitor agent command queue is not enabled for set context identification."),
}

def get_result_message(code):
    """
    Given a result code, return a message describing the result.

    Args:
        code (str): The result code to look up.

    Returns:
        str: A string containing the description of the result code,
             or "Unknown code" if the code is not found.
    """
  
    code_str = str(code).strip()
  
    if code_str in MFT_RETURN_CODES:
        short, desc = MFT_RETURN_CODES[code_str]
        return f"{short}: {desc}"
    else:
        return "Unknown code"


tpro_map = {}
tstr_map = {}
ssts_map = {}

for _, row in merged_df.iterrows():
    ref_id = row.get("reference_id")
    if row.get("type") == "TPRO":
        tpro_map[ref_id] = row
    elif row.get("type") == "TSTR":
        tstr_map[ref_id] = row
    elif row.get("type") == "SSTS":
        ssts_map[ref_id] = row

# Monitor Status Overview
st.subheader("Monitor Status Overview")
monitor_df = merged_df[merged_df['type'] == 'MACT'] if 'type' in merged_df.columns else pd.DataFrame()
monitors_started = monitor_df[monitor_df['action'] == 'start'] if not monitor_df.empty else pd.DataFrame()
monitors_stopped = monitor_df[monitor_df['action'] == 'stop'] if not monitor_df.empty else pd.DataFrame()

if not monitor_df.empty and 'monitor_name' in monitor_df.columns:
    stopped_names = set(monitors_stopped['monitor_name'])
    monitors_started = monitors_started[~monitors_started['monitor_name'].isin(stopped_names)]

active_count = len(monitors_started)
stopped_count = len(monitors_stopped)

if 'show_active' not in st.session_state:
    st.session_state['show_active'] = False
if 'show_stopped' not in st.session_state:
    st.session_state['show_stopped'] = False

col1, col2 = st.columns(2)
with col1:
    if st.button(f"🟢 Active Monitors: {active_count}", key="active_monitor_button"):
        st.session_state['show_active'] = not st.session_state['show_active']
    if st.session_state['show_active'] and not monitors_started.empty:
        cols = [c for c in ['monitor_name', 'qmgr', 'agent_name'] if c in monitors_started.columns]
        st.dataframe(monitors_started[cols].reset_index(drop=True))

with col2:
    if st.button(f"🔴 Stopped Monitors: {stopped_count}", key="stopped_monitor_button"):
        st.session_state['show_stopped'] = not st.session_state['show_stopped']
    if st.session_state['show_stopped'] and not monitors_stopped.empty:
        cols = [c for c in ['monitor_name', 'qmgr', 'agent_name'] if c in monitors_stopped.columns]
        st.dataframe(monitors_stopped[cols].reset_index(drop=True))

# Donut Chart
st.subheader("Transfer Statistics Donut Chart")
if not merged_df.empty and 'result_code' in merged_df.columns:
    merged_df['result_code'] = merged_df['result_code'].astype(str).str.strip()
    total_transfers = merged_df.shape[0]
    successful_transfers = merged_df[merged_df['result_code'].isin(['0', '0.0'])].shape[0]
    failed_transfers = merged_df[~merged_df['result_code'].isin(['0', '0.0']) & merged_df['result_code'].notna()].shape[0]
else:
    total_transfers = successful_transfers = failed_transfers = 0

if "is_expired" not in scheduled_df.columns:
    scheduled_df["is_expired"] = False
scheduled_count = len(scheduled_df[scheduled_df["is_expired"] == False])
expired_count = len(scheduled_df[scheduled_df["is_expired"] == True])

labels = [
    "Successful Transfers",
    "Failed Transfers",
    "Scheduled Transfers",
    "Expired Scheduled Transfers",
    "Other Transfers"
]
values = [
    successful_transfers,
    failed_transfers,
    scheduled_count,
    expired_count,
    total_transfers - (successful_transfers + failed_transfers + scheduled_count + expired_count)
]
colors = ['#43d13a', '#e53935', '#FFA500', '#808080', '#BDBDBD']

values = [max(0, v) for v in values]

fig = go.Figure(data=[go.Pie(
    labels=labels,
    values=values,
    hole=0.5,
    marker=dict(colors=colors),
    textinfo='label+percent',
)])

fig.update_layout(
    title_text="Transfer Statistics",
    annotations=[dict(text='Transfers', x=0.5, y=0.5, font_size=20, showarrow=False)]
)

st.plotly_chart(fig, use_container_width=True)

# Successful vs Failed Transfers per Agent
st.subheader("Successful vs Failed Transfers per Agent")

if not merged_df.empty and 'result_code' in merged_df.columns and 'source_agent' in merged_df.columns:
    agent_success = merged_df[merged_df['result_code'].isin(['0', '0.0'])].groupby('source_agent').size()
    agent_failed = merged_df[~merged_df['result_code'].isin(['0', '0.0']) & merged_df['result_code'].notna()].groupby('source_agent').size()
    agent_transfer_table = pd.DataFrame({
        'Successful': agent_success,
        'Failed': agent_failed
    }).fillna(0).astype(int)
    bar_colors = ['#43d13a', '#e53935']

    fig_col = go.Figure()
    fig_col.add_bar(
        name='Successful',
        x=agent_transfer_table.index,
        y=agent_transfer_table['Successful'],
        marker_color=bar_colors[0]
    )
    fig_col.add_bar(
        name='Failed',
        x=agent_transfer_table.index,
        y=agent_transfer_table['Failed'],
        marker_color=bar_colors[1]
    )
    fig_col.update_layout(
        barmode='stack',
        xaxis_title='Source Agent',
        yaxis_title='Number of Transfers',
        legend_title='Transfer Status',
        title='Successful vs Failed Transfers per Agent'
    )
    st.plotly_chart(fig_col, use_container_width=True)

# Table 1: Last 5 Transfers 
st.subheader("Last 5 Transfers Overview")

if not merged_df.empty and "type" in merged_df.columns:
    completed_df = merged_df[merged_df["type"] == "TCOM"].copy()
    completed_df["status"] = completed_df["result_code"].apply(
        lambda x: "Successful" if str(x).strip() in ["0", "0.0"] else "Failed"
    )
    completed_df["result_message"] = completed_df["result_code"].apply(get_result_message)

    
    def get_source_file(row):
        """
    Retrieve the source file for a transfer, searching related transfer maps if necessary.

    Args:
        row (pd.Series): A row from the completed transfers DataFrame.

    Returns:
        str: The source file path.
    """
      
        ref_id = row.get("reference_id")
        if ref_id in tpro_map and pd.notnull(tpro_map[ref_id].get("source_file")) and tpro_map[ref_id].get("source_file") != "":
            return tpro_map[ref_id].get("source_file")
        elif ref_id in tstr_map and pd.notnull(tstr_map[ref_id].get("source_file")) and tstr_map[ref_id].get("source_file") != "":
            return tstr_map[ref_id].get("source_file")
        elif ref_id in ssts_map and pd.notnull(ssts_map[ref_id].get("source_file")) and ssts_map[ref_id].get("source_file") != "":
            return ssts_map[ref_id].get("source_file")
        else:
            return row.get("source_file", "")

    def get_destination_file(row):
        """
    Retrieve the destination file for a transfer, searching related transfer maps if necessary.

    Args:
        row (pd.Series): A row from the completed transfers DataFrame.

    Returns:
        str: The destination file path
    """
      
        ref_id = row.get("reference_id")
        if ref_id in tpro_map and pd.notnull(tpro_map[ref_id].get("destination_file")) and tpro_map[ref_id].get("destination_file") != "":
            return tpro_map[ref_id].get("destination_file")
        elif ref_id in tstr_map and pd.notnull(tstr_map[ref_id].get("destination_file")) and tstr_map[ref_id].get("destination_file") != "":
            return tstr_map[ref_id].get("destination_file")
        elif ref_id in ssts_map and pd.notnull(ssts_map[ref_id].get("destination_file")) and ssts_map[ref_id].get("destination_file") != "":
            return ssts_map[ref_id].get("destination_file")
        else:
            return row.get("destination_file", "")

    completed_df["source_file"] = completed_df.apply(get_source_file, axis=1)
    completed_df["destination_file"] = completed_df.apply(get_destination_file, axis=1)

    if "timestamp" in completed_df.columns:
        completed_df["timestamp_dt"] = pd.to_datetime(completed_df["timestamp"], errors="coerce")
        completed_df = completed_df.sort_values(by="timestamp_dt", ascending=False)
    display_cols = [
        "timestamp", "reference_id", "source_agent", "source_qmgr",
        "destination_agent", "destination_qmgr", "file_count", "source_file", "destination_file",
        "result_code", "status", "result_message"
    ]
    display_cols = [col for col in display_cols if col in completed_df.columns]

    last5_all = completed_df.head(5)[display_cols].reset_index(drop=True)
    last5_success = completed_df[completed_df["result_code"].astype(str).str.strip().isin(["0", "0.0"])].head(5)[display_cols].reset_index(drop=True)
    last5_failed = completed_df[~completed_df["result_code"].astype(str).str.strip().isin(["0", "0.0"])].head(5)[display_cols].reset_index(drop=True)

    table_option = st.radio(
        "Show table:",
        ("Last 5 Transfers", "Last 5 Successful Transfers", "Last 5 Failed Transfers"),
        horizontal=True,
        key="last5_table_radio"
    )

    if table_option == "Last 5 Transfers":
        st.dataframe(last5_all)
    elif table_option == "Last 5 Successful Transfers":
        st.dataframe(last5_success)
    elif table_option == "Last 5 Failed Transfers":
        st.dataframe(last5_failed)
else:
    st.info("No completed transfer data found.")

# Table 2 & 3: Scheduled Transfers
def extract_sched_info(row):
    """
    Extract key information from a scheduled transfer row for display.

    Args:
        row (dict): A dictionary representing a scheduled transfer.

    Returns:
        dict: A dictionary with selected fields for display in the dashboard.
    """
    ssin = row.get("ssin", {}) or {}
    sstr = row.get("sstr", {}) or {}
    ssts = row.get("ssts", {}) or {}
    return {
        "reference_id": row.get("reference_id"),
        "scheduled_by": ssin.get("user_id"),
        "agent": ssin.get("agent"),
        "timezone": ssin.get("timezone"),
        "source_agent": sstr.get("source_agent"),
        "destination_agent": sstr.get("destination_agent"),
        "source_file": ssts.get("source_file"),
        "destination_file": ssts.get("destination_file"),
    }

if "is_expired" not in scheduled_df.columns:
    scheduled_df["is_expired"] = False

scheduled_active = scheduled_df[scheduled_df["is_expired"] == False]
scheduled_expired = scheduled_df[scheduled_df["is_expired"] == True]

col1, col2 = st.columns(2)

with col1:
    st.subheader("Next 5 Scheduled Transfers")
    if not scheduled_active.empty:
        sched_table = pd.DataFrame([extract_sched_info(row) for row in scheduled_active.to_dict(orient="records")])
        sched_table = sched_table.sort_values(by="timezone").head(5)
        st.dataframe(sched_table.reset_index(drop=True))
    else:
        st.info("No upcoming scheduled transfers found.")

with col2:
    st.subheader("Last 5 Expired Scheduled Transfers")
    if not scheduled_expired.empty:
        expired_table = pd.DataFrame([extract_sched_info(row) for row in scheduled_expired.to_dict(orient="records")])
        expired_table = expired_table.sort_values(by="timezone", ascending=False).head(5)
        st.dataframe(expired_table.reset_index(drop=True))
    else:
        st.info("No expired scheduled transfers found.")
        

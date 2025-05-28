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

'''This script creates a monitor resource for IBM MQ Managed File Transfer (MFT) using the REST API.'''

import subprocess
import json
import argparse
import sys
from dotenv import load_dotenv
import os

load_dotenv()

def display_monitor(hostname,port,name):

    """
    Retrieves and displays IBM MQ MFT monitor resources from the REST API, optionally filtering by monitor name.

    Args:
        hostname (str): The hostname of the IBM MQ REST API server.
        port (str): The port number of the IBM MQ REST API server.
        name (str): The name of the monitor to filter by. If None or empty, all monitors are displayed.

    Returns:
        Prints the filtered monitor information as formatted JSON to the console.
    """

    url = f'https://{hostname}:{port}/ibmmq/rest/v3/admin/mft/monitor'

    curl_command = [
        "curl", "-k", "-X", "GET",
        "-u", "mftadmin:mftadmin",
        "-H", "Content-Type: application/json",
        url
    ]
    response = subprocess.run(curl_command, capture_output=True, text=True)
    if response.returncode == 0:
        try:
            monitors = json.loads(response.stdout).get("monitor", [])
            filtered_monitors = []
            for monitor in monitors:
                if name and name != monitor.get("name"):
                    continue
                filtered_monitors.append(monitor)
            if filtered_monitors:
                for monitor in filtered_monitors:
                    print(json.dumps(monitor, indent=4))
        except json.JSONDecodeError:
            print("Error: Unable to parse the JSON response.")

def create_monitor(hostname, port, 
                   name, resource_type, poll_interval, poll_interval_unit, 
                   matches_per_task, resource_name, recursion_level,
                   sa, sm, da, dm, source, source_type, destination, destination_type,
                   mode, include_pattern, exclude_pattern, trigger_type,
                   no_file_size_change_poll_count=None, file_size=None, file_size_unit=None, match_pattern=None):
    """
    Creates an IBM MQ MFT monitor resource with a transfer definition using the provided parameters.

    Args:
        hostname : Hostname of the IBM MQ REST API server.
        port : Port number of the IBM MQ REST API server.
        name : Unique name for the monitor resource.
        resource_type : Type of resource to monitor (e.g., 'directory' or 'queue').
        poll_interval : Frequency at which the monitor polls the resource.
        poll_interval_unit : Time unit for the poll interval (e.g., 'seconds', 'minutes').
        matches_per_task : Maximum trigger matches to include in a single task.
        resource_name : Name of the resource to monitor (directory path or queue name).
        recursion_level : Recursion level for directory monitoring.
        sa : Source agent name.
        sm : Source agent queue manager name.
        da : Destination agent name.
        dm : Destination agent queue manager name.
        source : Source name (queue name or file path).
        source_type : Type of the source (queue, file, or directory).
        destination : Destination name (directory path or queue name).
        destination_type : Type of the destination (directory, queue, or file).
        mode : Mode of transfer (text or binary).
        include_pattern : File pattern to include (e.g., '*.txt').
        exclude_pattern : File pattern to exclude.
        trigger_type : Type of trigger condition (e.g., 'noChangeInSize', 'sizeGreaterOrEqualTo').
        no_file_size_change_poll_count : Number of poll intervals for no file size change trigger.
        file_size : File size for 'sizeGreaterOrEqualTo' trigger type.
        file_size_unit : File size unit for 'sizeGreaterOrEqualTo' trigger type.
        match_pattern : Pattern to match for the trigger condition.

    Returns:
        Prints the details of the monitor created to the console.
    """

    url = f"https://{hostname}:{port}/ibmmq/rest/v3/admin/mft/monitor/"

    trigger_condition = {
        "type": trigger_type,
        "includePattern": include_pattern,
        "excludePattern": exclude_pattern
    }

    if trigger_type == "noChangeInSize" and no_file_size_change_poll_count is not None:
        trigger_condition["noFileSizeChangePollCount"] = int(no_file_size_change_poll_count)
    if trigger_type == "sizeGreaterOrEqualTo" and file_size is not None:
        trigger_condition["fileSize"] = int(file_size)
        trigger_condition["fileSizeUnit"] = file_size_unit
    if match_pattern is not None:
        trigger_condition["matchPattern"] = match_pattern

   
    data = {
        "name": name,
        "type": resource_type,
        "agentName": sa,
        "general": {
            "pollInterval": poll_interval,
            "pollIntervalUnit": poll_interval_unit,
            "matchesPerTask": matches_per_task
        },
        "resource": {
            "name": resource_name,
            "recursionLevel": recursion_level
        },
        "triggerCondition": trigger_condition,
        "transferDefinition": {
            "sourceAgent": {
                "qmgrName": sm,
                "name": sa
            },
            "destinationAgent": {
                "qmgrName": dm,
                "name": da
            },
            "transferSet": {
                "item": [
                    {
                        "mode": mode,
                        "source": {
                            "name": source,
                            "type": source_type
                        },
                        "destination": {
                            "name": destination,
                            "type": destination_type
                        }
                    }
                ]
            }
        }
    }

    json_data = json.dumps(data)

    username = "mftadmin"  
    password = "mftadmin" 

    curl_command = [
        "curl", "-k", "-X", "POST",
        "-u", f"{username}:{password}",
        "-H", "Content-Type: application/json",
        "-H", "ibm-mq-rest-csrf-token: value",
        "-d", json_data,
        url
    ]

    response = subprocess.run(curl_command, capture_output=True, text=True)

    if response.returncode == 0:
        print("Monitor created successfully.")
        print("Monitor Details:")
        display_monitor(hostname, port, name)
        
    else:
        print("Error: Failed to create the monitor.")
        print("Error Details:", response.stderr)


if __name__ == "__main__":
    
    with open("create_resource_monitor.json", "r") as f:
        config = json.load(f)

    create_monitor(
        hostname=config.get("HOST"),
        port=config.get("PORT"),
        name=config.get("NAME"),
        resource_type=config.get("TYPE"),
        poll_interval=config.get("POLL_INTERVAL", 1),
        poll_interval_unit=config.get("POLL_INTERVAL_UNIT", "minutes"),
        matches_per_task=config.get("MATCHES_PER_TASK", 2),
        resource_name=config.get("RESOURCE_NAME"),
        recursion_level=config.get("RECURSION_LEVEL", 1),
        sa=config.get("SOURCE_AGENT"),
        sm=config.get("SOURCE_AGENT_QMGR"),
        da=config.get("DESTINATION_AGENT"),
        dm=config.get("DESTINATION_AGENT_QMGR"),
        source=config.get("SOURCE"),
        source_type=config.get("SOURCE_TYPE"),
        destination=config.get("DESTINATION"),
        destination_type=config.get("DESTINATION_TYPE"),
        mode=config.get("MODE"),
        include_pattern=config.get("INCLUDE_PATTERN"),
        exclude_pattern=config.get("EXCLUDE_PATTERN", ""),
        trigger_type=config.get("TRIGGER_TYPE"),
        no_file_size_change_poll_count=config.get("NO_FILE_SIZE_CHANGE_POLL_COUNT"),
        file_size=config.get("FILE_SIZE"),
        file_size_unit=config.get("FILE_SIZE_UNIT"),
        match_pattern=config.get("MATCH_PATTERN")
    )
    
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

'''This Python script summarizes all file transfers done using IBM MQ Managed File Transfer (MFT)'''

import json
import subprocess
import argparse

def get_transfer_data(host, port):
    """
    Retrieves transfer data using a command or API.

    Args:
        host : The hostname of the IBM MQ REST API server.
        port : The port number of the IBM MQ REST API server.

    Returns:
        list: A list of transfer objects.
    """
    
    url = f'https://{host}:{port}/ibmmq/rest/v3/admin/mft/transfer?attributes=*'

   
    curl_command = [
        "curl", "-k", "-X", "GET",
        "-u", "mftadmin:mftadmin",
        "-H", "Content-Type: application/json",
        url
    ]

    try:
        response = subprocess.run(curl_command, capture_output=True, text=True, check=True)
        json_response = json.loads(response.stdout)
        return json_response.get("transfer", [])
    except subprocess.CalledProcessError as e:
        print(f"Error executing command: {e.stderr}")
        return []
    except json.JSONDecodeError:
        print("Error: The response is not valid JSON.")
        return []

def calculate_transfer_summary(transfers):
    """
    Calculates a summary of transfer statistics.

    Args:
        transfers : A list of transfer objects.

    Returns:
        dict: A dictionary containing the summary statistics.
    """
    
    print("Summary of Transfers since mq webserver was started: ")
    summary = {
        
        "total_transfers": 0,
        "failed_transfers": 0,
        "successful_transfers": 0,
        "in_progress_transfers": 0,
        "other_states": {},
        "source_agents": {}
    }

    for transfer in transfers:
        summary["total_transfers"] += 1
        state = transfer.get("status", {}).get("state", "unknown")
        source_agent = transfer.get("sourceAgent", {}).get("name", "unknown")

        if state == "failed":
            summary["failed_transfers"] += 1
        elif state == "successful":
            summary["successful_transfers"] += 1
        elif state == "inProgress":
            summary["in_progress_transfers"] += 1
        else:
            if state not in summary["other_states"]:
                summary["other_states"][state] = 0
            summary["other_states"][state] += 1

        if source_agent not in summary["source_agents"]:
            summary["source_agents"][source_agent] = {
                "total_transfers": 0,
                "failed_transfers": 0,
                "successful_transfers": 0,
                "in_progress_transfers": 0,
                "other_states": {}
            }

        agent_summary = summary["source_agents"][source_agent]
        agent_summary["total_transfers"] += 1

        if state == "failed":
            agent_summary["failed_transfers"] += 1
        elif state == "successful":
            agent_summary["successful_transfers"] += 1
        elif state == "inProgress":
            agent_summary["in_progress_transfers"] += 1
        else:
            if state not in agent_summary["other_states"]:
                agent_summary["other_states"][state] = 0
            agent_summary["other_states"][state] += 1

    return summary

def display_summary(summary):
    """
    Displays the transfer summary.

    Args:
        summary : A dictionary containing the summary statistics.
    """
    
    print(f"Total Transfers: {summary['total_transfers']}")
    print(f"Failed Transfers: {summary['failed_transfers']}")
    print(f"Successful Transfers: {summary['successful_transfers']}")
    print(f"In Progress Transfers: {summary['in_progress_transfers']}")
    if summary["other_states"]:
        print("Other States:")
        for state, count in summary["other_states"].items():
            print(f"  {state}: {count}")

    print("\nTransfer Summary by Source Agent:")
    for agent, agent_summary in summary["source_agents"].items():
        print(f"\nSource Agent: {agent}")
        print(f"  Total Transfers: {agent_summary['total_transfers']}")
        print(f"  Failed Transfers: {agent_summary['failed_transfers']}")
        print(f"  Successful Transfers: {agent_summary['successful_transfers']}")
        print(f"  In Progress Transfers: {agent_summary['in_progress_transfers']}")
        if agent_summary["other_states"]:
            print("  Other States:")
            for state, count in agent_summary["other_states"].items():
                print(f"    {state}: {count}")


if __name__ == "__main__":
    
    parser = argparse.ArgumentParser(description="Retrieve and summarize transfer data.")

    parser.add_argument(
        "-host", "--host-name",
        type=str,
        required=True,
        help="The hostname of the IBM MQ REST API server."
    )
    parser.add_argument(
        "-port", "--port-number",
        type=str,
        required=True,
        help="The port number of the IBM MQ REST API server."
    )
    args = parser.parse_args()

    print("Retrieving transfer data...")
    transfers = get_transfer_data(args.host_name, args.port_number)

    if transfers:
        summary = calculate_transfer_summary(transfers)
        display_summary(summary)
    else:
        print("No transfer data available.")
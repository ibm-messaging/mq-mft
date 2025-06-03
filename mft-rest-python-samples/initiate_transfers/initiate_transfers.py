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

'''This python script initiates a file transfer using IBM MQ Managed File Transfer (MFT) REST API.'''

import subprocess
import json
import argparse

def display_transfer(hostname, port):
    """
    Retrieves transfer data using a command or API.

    Args:
        host : The hostname of the IBM MQ REST API server.
        port : The port number of the IBM MQ REST API server.

    Returns:
        list: A list of transfer objects.
    """

    url = f'https://{hostname}:{port}/ibmmq/rest/v3/admin/mft/transfer?limit=1&attributes=*'

    curl_command = [
        "curl", "-k", "-X", "GET",
        "-u", "mftadmin:mftadmin",
        "-H", "Content-Type: application/json",
        url
    ]

    response = subprocess.run(curl_command, capture_output=True, text=True)

    if response.returncode == 0:
        print("Response:", response.stdout)
    else:
        print("Error:", response.stdout)



def perform_transfer(hostname, port, sa, sm, da, dm, source, source_type, destination, destination_type, mode,jobname):
    """
    Performs a file transfer using the provided parameters, with optional scheduling.

    Args:
        hostname : Hostname of the IBM MQ REST API server.
        port : Port number of the IBM MQ REST API server.
        sa : Name of the source agent.
        sm : Queue manager name of the source agent.
        da : Name of the destination agent.
        dm : Queue manager name of the destination agent.
        source : Source name (queue name or file path).
        source_type : Type of the source (queue, file, or directory).
        destination : Destination name (directory path or queue name).
        destination_type : Type of the destination (directory, queue, or file).
        mode : Mode of transfer (text or binary).
        start_time : Optional start time for scheduling the transfer.
    """

    url = f'https://{hostname}:{port}/ibmmq/rest/v3/admin/mft/transfer'
    data = {
        "job": {"name": jobname},
        "sourceAgent": {"qmgrName": sm, "name": sa},
        "destinationAgent": {"qmgrName": dm, "name": da},
        "transferSet": {
            "item": [
                {
                    "checksum": "md5",
                    "mode": mode,
                    "source": {"name": source, "type": source_type},
                    "destination": {"name": destination, "type": destination_type}
                }
            ]
        }
    }

    json_data = json.dumps(data)

    curl_command = [
        "curl", "-k", "-X", "POST",
        "-H", "Content-Type: application/json",
        "-H", "ibm-mq-rest-csrf-token: value",
        "-u", "mftadmin:mftadmin",
        "-d", json_data,
        url
    ]
    
    response = subprocess.run(curl_command, capture_output=True, text=True)

    if response.returncode == 0:
        print("Transfer initiated successfully.")
        display_transfer(hostname, port)
    else:
        print("Error: Failed to execute the transfer.")
        print("Error Details:", response.stderr)


if __name__ == "__main__":

    parser = argparse.ArgumentParser(description="Perform a file transfer using IBM MQ MFT with optional scheduling.")
    parser.add_argument("-host", required=True, help="Hostname of the IBM MQ REST API server.")
    parser.add_argument("-port", required=True, help="Port number of the IBM MQ REST API server.")
    parser.add_argument("-sa", required=True, help="Source agent name.")
    parser.add_argument("-sm", required=True, help="Source agent queue manager name.")
    parser.add_argument("-da", required=True, help="Destination agent name.")
    parser.add_argument("-dm", required=True, help="Destination agent queue manager name.")
    parser.add_argument("-src", required=True, help="Source name (queue name or file path).")
    parser.add_argument("-st", required=True, choices=["queue", "file", "directory"], help="Source type (queue, file, or directory).")
    parser.add_argument("-dest", required=True,  help="Destination name (directory path or queue name).")
    parser.add_argument("-dt", required=True, choices=["directory", "queue", "file"], help="Destination type (directory, queue, or file).")
    parser.add_argument("-m", required=True, choices=["text", "binary"], help="Mode of transfer (text or binary).")
    parser.add_argument("-jn", help="Job name for the transfer.")

    args = parser.parse_args()
    
    perform_transfer(
        hostname=args.host,
        port=args.port,
        sa=args.sa,
        sm=args.sm,  
        da=args.da,
        dm=args.dm,
        source=args.src,
        source_type=args.st,
        destination=args.dest,
        destination_type=args.dt,
        mode=args.m,
        jobname=args.jn
    )
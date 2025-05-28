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

'''This Python script retrieves and displays IBM MQ Managed File Transfer (MFT) monitor resources.'''

import subprocess
import json
import argparse

def list_monitors(args):
    """
    Retrieves and displays IBM MQ MFT monitor resources, optionally filtering by name, state, or type.

    Args:
        host : Hostname of the IBM MQ REST API server.
        port : Port number of the IBM MQ REST API server.
        name : Filter by monitor name.
        state : Filter by monitor state.
        type : Filter by monitor type.
        details : If 'True', fetch detailed monitor information.

    Returns:
        Prints the filtered monitor information as formatted JSON to the console.
    """
    
    if args.details and args.details=='True':
        url = f'https://{args.host}:{args.port}/ibmmq/rest/v3/admin/mft/monitor?attributes=*'
    else:
        url = f'https://{args.host}:{args.port}/ibmmq/rest/v3/admin/mft/monitor'

    
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
                if args.name and args.name != monitor.get("name"):
                    continue
                if args.state and args.state != monitor.get("state"):
                    continue
                if args.type and args.type != monitor.get("type"):
                    continue
                
                filtered_monitors.append(monitor)

            if filtered_monitors:
                for monitor in filtered_monitors:
                    print(json.dumps(monitor, indent=4))
            else:
                print("No monitors match the specified criteria.")

        except json.JSONDecodeError:
            print("Error: Unable to parse the JSON response.")

    else:
        print("Error:", response.stderr)

if __name__ == "__main__":
    
    parser = argparse.ArgumentParser(description="List and filter MFT monitors.")
    
    parser.add_argument("-host", required=True, help="Hostname of the IBM MQ REST API server.")
    parser.add_argument("-port", required=True, help="Port number of the IBM MQ REST API server.")
    parser.add_argument("-name", help="Filter by monitor name.")
    parser.add_argument("-state", help="Filter by monitor state.")
    parser.add_argument("-type", help="Filter by monitor type.")
    parser.add_argument("-details",help="Display detailed information about the monitor, default value is False")
    args = parser.parse_args()

    list_monitors(args)
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

import subprocess
import json
import os
import argparse

'''This python script retrieves transfer data and filters transfers based on the file name prefix.'''

def get_transfers(host, port, file_prefix):
    """
    Retrieves transfer data and filters transfers based on the file name prefix.

    Args:
        host : The hostname of the IBM MQ REST API server.
        port : The port number of the IBM MQ REST API server.
        file_prefix : The prefix of the file name to filter by.
    Returns:
        Prints the filtered transfer records as formatted JSON to the console.
    """
    
    url = f'https://{host}:{port}/ibmmq/rest/v3/admin/mft/transfer?attributes=*'

    curl_command = [
        "curl", "-k", "-X", "GET",
        "-u", "mftadmin:mftadmin",
        "-H", "Content-Type: application/json",
        url
    ]
    
    visited = set()

    response = subprocess.run(curl_command, capture_output=True, text=True)

    if response.returncode == 0:
        try:
            json_response = json.loads(response.stdout)
            transfers = json_response.get("transfer", [])
            print(f"Transfers with file names starting with '{file_prefix}':")

            for transfer in transfers:
                transfer_id = transfer.get("id")
                if transfer_id in visited:
                    continue
                transfer_set = transfer.get("transferSet", {}).get("item", [])
                for item in transfer_set:
                    source_file_name = item.get("source")
                    file_path = source_file_name.get("file", {}).get("path", "")
                    file_name = os.path.basename(file_path)
                    if file_name.startswith(file_prefix) and transfer_id not in visited:
                        visited.add(transfer_id)
                        print(json.dumps(transfer, indent=4))
                        break

        except json.JSONDecodeError:
            print("Error: The response is not valid JSON.")

    else:
        print("Error:", response.stdout)


if __name__ == "__main__":
    
    parser = argparse.ArgumentParser(description="Filter transfers by file name prefix.")
    
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
    parser.add_argument(
        "-fp", "--file-prefix",
        type=str,
        required=True,
        help="The prefix of the file name to filter by (e.g., 'file_85')."
    )
    args = parser.parse_args()

    get_transfers(args.host_name, args.port_number, args.file_prefix)
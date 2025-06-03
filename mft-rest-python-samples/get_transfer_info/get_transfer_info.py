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
import argparse
import json
import sys

'''This python script retrieves and filters IBM MQ Managed File Transfer (MFT) transfer records using the REST API.'''

VALID_STATES = [
    "started",
    "inProgress",
    "successful",
    "failed",
    "partiallySuccessful",
    "cancelled",
    "malformed",
    "notAuthorized",
    "deleted",
    "inProgressWithFailures",
    "inProgressWithWarnings"
]

def get_transfers(host, port, transferstate, source_agent, destination_agent, mqmd_user_id, user_id, end_time, number_of_file_failures, number_of_file_successes, number_of_file_warnings, number_of_files, retry_count, start_time):
    """
    Retrieves and filters IBM MQ Managed File Transfer (MFT) transfer records from the REST API based on provided criteria.

    Args:
        host : The hostname of the IBM MQ REST API server.
        port : The port number of the IBM MQ REST API server.
        transferstate : The state of the transfer to filter by (e.g., 'failed', 'successful', 'inProgress').
        source_agent : The name of the source agent to filter by.
        destination_agent : The name of the destination agent to filter by.
        mqmd_user_id : The MQMD user ID of the originator to filter by.
        user_id : The user ID of the originator to filter by.
        end_time : The end time of the transfer to filter by.
        number_of_file_failures : The number of file failures to filter by.
        number_of_file_successes : The number of file successes to filter by.
        number_of_file_warnings : The number of file warnings to filter by.
        number_of_files : The total number of files to filter by.
        retry_count : The retry count to filter by.
        start_time : The start time of the transfer to filter by.

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

    response = subprocess.run(curl_command, capture_output=True, text=True)

    if response.returncode == 0:
        json_response = json.loads(response.stdout)
        transfer_info=json_response.get("transfer", [])
        for transfer in transfer_info:
            id = transfer.get("id")
            transfer_set = transfer.get("transferSet", {}).get("item", [])
            transfer_source_agent = transfer.get("sourceAgent", {}).get("name")
            transfer_destination_agent = transfer.get("destinationAgent", {}).get("name")
            transfer_originator = transfer.get("originator", {})
            transfer_host = transfer_originator.get("host")
            transfer_mqmd_user_id = transfer_originator.get("mqmdUserId")
            transfer_user_id = transfer_originator.get("userId")
            transfer_end_time = transfer.get("statistics", {}).get("endTime")
            transfer_number_of_file_failures = transfer.get("statistics", {}).get("numberOfFileFailures")
            transfer_number_of_file_successes = transfer.get("statistics", {}).get("numberOfFileSuccesses")
            transfer_number_of_file_warnings = transfer.get("statistics", {}).get("numberOfFileWarnings")
            transfer_number_of_files = transfer.get("statistics", {}).get("numberOfFiles")
            transfer_retry_count = transfer.get("statistics", {}).get("retryCount")
            transfer_start_time = transfer.get("statistics", {}).get("startTime")

            filters = [
                (transferstate, transfer.get("status", {}).get("state"), transferstate),
                (source_agent, transfer_source_agent, source_agent),
                (destination_agent, transfer_destination_agent, destination_agent),
                (mqmd_user_id, transfer_mqmd_user_id, mqmd_user_id),
                (user_id, transfer_user_id, user_id),
                (end_time, transfer_end_time, end_time),
                (number_of_file_failures, transfer_number_of_file_failures, number_of_file_failures),
                (number_of_file_successes, transfer_number_of_file_successes, number_of_file_successes),
                (number_of_file_warnings, transfer_number_of_file_warnings, number_of_file_warnings),
                (number_of_files, transfer_number_of_files, number_of_files),
                (retry_count, transfer_retry_count, retry_count),
                (start_time, transfer_start_time, start_time)
            ]

            
            filter_conditions = [
                (filter_value is not None and transfer_value != filter_value)
                for filter_value, transfer_value, _ in filters
            ]
            if any(filter_conditions):
                continue
            print(json.dumps(transfer, indent=4))

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
        "-ts", "--transfer-state",
        type=str,
        default="failed",
        help="The state of the transfers to filter (default: 'failed')."
    )
parser.add_argument(
        "-sa", "--source-agent",
        type=str,
        default=None,
        help="The name of the source agent to filter by."
    )
parser.add_argument(
        "-da", "--destination-agent",
        type=str,
        default=None,
        help="The name of the destination agent to filter by."
    )
parser.add_argument(
        "-mqid", "--mqmd-user-id",
        type=str,
        default=None,
        help="The MQMD user ID of the originator to filter by."
    )
parser.add_argument(
        "-uid", "--user-id",
        type=str,
        default=None,
        help="The user ID of the originator to filter by."
    )
parser.add_argument(
        "-et", "--end-time",
        type=str,
        default=None,
        help="The end time of the transfer to filter by."
    )
parser.add_argument(
        "-nff", "--number-of-file-failures",
        type=int,
        default=None,
        help="The number of file failures to filter by."
    )
parser.add_argument(
        "-nfs", "--number-of-file-successes",
        type=int,
        default=None,
        help="The number of file successes to filter by."
    )
parser.add_argument(
        "-nfw", "--number-of-file-warnings",
        type=int,
        default=None,
        help="The number of file warnings to filter by."
    )
parser.add_argument(
        "-nof", "--number-of-files",
        type=int,
        default=None,
        help="The total number of files to filter by."
    )
parser.add_argument(
        "-rc", "--retry-count",
        type=int,
        default=None,
        help="The retry count to filter by."
    )
parser.add_argument(
        "-st", "--start-time",
        type=str,
        default=None,
        help="The start time of the transfer to filter by."
    )

args = parser.parse_args()

get_transfers(args.host_name, args.port_number,args.transfer_state,args.source_agent,args.destination_agent,args.mqmd_user_id,args.user_id,args.end_time,args.number_of_file_failures,args.number_of_file_successes,args.number_of_file_warnings,args.number_of_files,args.retry_count,args.start_time)
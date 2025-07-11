import json
import re
from datetime import datetime

def parse_log_line(line):
    """
    Parse a single log line into a dictionary based on event type.

    Args:
        line (str): A single line from the log file.

    Returns:
        dict or None: Parsed event as a dictionary, or None if the line is malformed or unsupported.
    """
  
    parts = line.strip().split(";")
    if len(parts) < 4:
        print(f"Skipping malformed line: {line.strip()}")
        return None

    match = re.match(r"\s*\[([A-Z]+)\]", parts[2])
    if match:
        event_type = match.group(1)
    else:
        event_type = parts[2].strip()
        if event_type.startswith("[") and event_type.endswith("]"):
            event_type = event_type[1:-1].strip()

    try:

        # Common fields for all events
        common = {
            "timestamp": datetime.strptime(parts[0], "%Y-%m-%dT%H:%M:%S").isoformat(),
            "reference_id": parts[1].strip(),
            "type": event_type
        }

        if event_type == "MACT": 
            common.update({
                "monitor_name": parts[4],
                "agent": parts[5],
                "qmgr": parts[6],
                "action": parts[7]
            })

        elif event_type == "MCRT":
            common.update({
                "monitor_name": parts[3],
                "agent": parts[4],
                "qmgr": parts[5],
                "action": parts[6]
            })

        elif event_type == "MFIR":
            common.update({
                "monitor_name": parts[4],
                "agent": parts[5],
                "qmgr": parts[6],
                "result_code": parts[3],
                "action": parts[7]
            })

        elif event_type == "AUTH":
            common.update({
                "id": parts[1].strip(),
                "result_code": parts[3],
                "action": parts[4],
                "authority": parts[5],
                "user_id": parts[6],
                "mqmd_user_id": parts[7]
            })

        elif event_type == "SDEL" :
            common.update({
                "id": parts[1].strip(),
                "result_code": parts[3],
                "agent": parts[4],
                "action": parts[5],
                "user_id": parts[6],
                
            })
        elif event_type == "SEXP":
            common.update({
                "id": parts[1].strip(),
                "result_code": parts[3],
                "agent": parts[4],
                "action": "expire",
                "user_id": parts[6]
            })
        elif event_type == "SSKP":
            common.update({
                "id": parts[1].strip(),
                "result_code": parts[3],
                "agent": parts[4],
                "action": parts[5],
                "user_id": parts[6]
            })

        elif event_type == "SSIN":
            common.update({
                "id": parts[1].strip(),
                "result_code": parts[3],
                "agent": parts[4],
                "action": parts[5],
                "user_id": parts[6],
                "timezone": parts[8],
                
                "expire_count": parts[9],
                
            })

        elif event_type == "SSTR":
            common.update({
                "id": parts[1].strip(),
                "source_agent": parts[3],
                "source_qmgr": parts[4],
                "destination_agent": parts[5],
                "destination_qmgr": parts[6]
            })

        elif event_type == "SSTS":
            
            while len(parts) < 11:
                parts.append("")
            common.update({
                "id": parts[1].strip(),
                "source_file": parts[3].strip(),
                "source_queue": parts[4].strip(),
                "destination_file": parts[6].strip(),
                "destination_type": parts[7].strip(),
                
            })

        elif event_type == "TSTR":
            common.update({
                "transfer_id": parts[1].strip(),
                "source_agent": parts[4],
                "source_qmgr": parts[5],
                "destination_agent": parts[7],
                "destination_qmgr": parts[8],
                "user_id": parts[9],
                "job_name": parts[10]
            })

        elif event_type == "TCOM":
            common.update({
                "transfer_id": parts[1].strip(),
                "result_code": parts[3],
                "source_agent": parts[4],
                "source_qmgr": parts[5],
                "destination_agent": parts[7],
                "destination_qmgr": parts[8],
                "user_id": parts[10]
                
            })

        elif event_type == "TCAN" or event_type == "TDEL":
            common.update({
                "transfer_id": parts[1].strip(),
                "result_code": parts[3],
                "source_agent": parts[4],
                "source_qmgr": parts[5],
                "destination_agent": parts[7],
                "destination_qmgr": parts[8],
                "user_id": parts[10]
            })

        elif event_type == "TPRO":
            common.update({
                "transfer_id": parts[1].strip(),
                "source_file": parts[4],
                "source_type": parts[6],
                "destination_file": parts[13],
                "destination_type":parts[15],
                "result_code": parts[3]
            })

        else:
            return None

        return common

    except Exception as e:
        print(f"Error parsing line: {line.strip()}")
        print(f"Exception: {e}")
        return None


def process_log_file(file_path):
    """
    Read the log file and parse each line into a structured event dictionary.

    Args:
        file_path (str): Path to the log file.

    Returns:
        list: List of parsed event dictionaries.
    """
    events = []

    with open(file_path, "r") as f:
        for line in f:
            if line.strip():
                document = parse_log_line(line)
                if document:
                    events.append(document)
    
    return events

log_file_path = "sample.log" 
parsed_data = process_log_file(log_file_path)



def categorize_entities(parsed_data):
    """
    Categorize entities found in the parsed data.

    Args:
        parsed_data (list): List of parsed event dictionaries.

    Returns:
        dict: Categorized lists and counts of monitors, agents, queue managers, and transfer events.
    """
  
    monitors = set()
    agents = set()
    source_qmgrs = set()
    destination_qmgrs = set()
    transfer_event_count = 0

    for doc in parsed_data:
        if doc["type"] in {"MACT", "MCRT", "MFIR"}:
            if "monitor_name" in doc:
                monitors.add(doc["monitor_name"])
        if "agent" in doc:
            agents.add(doc["agent"])
        if "source_agent" in doc:
            agents.add(doc["source_agent"])
        if "destination_agent" in doc:
            agents.add(doc["destination_agent"])
        if "source_qmgr" in doc:
            source_qmgrs.add(doc["source_qmgr"])
        if "destination_qmgr" in doc:
            destination_qmgrs.add(doc["destination_qmgr"])
        if doc["type"] in {"TSTR", "TCOM", "TPRO", "TCAN", "TDEL"}:
            transfer_event_count += 1

    return {
        "monitors": sorted(monitors),
        "agents": sorted(agents),
        "source_qmgrs": sorted(source_qmgrs),
        "destination_qmgrs": sorted(destination_qmgrs),
        "transfer_event_count": transfer_event_count
    }


def build_scheduled_transfers(parsed_data):
    """
    Build a list of scheduled transfers by grouping SSIN, SSTR, SSTS, and SEXP events
    by their reference_id. Mark as expired if SEXP event is present.

    Args:
        parsed_data (list): List of parsed event dictionaries.

    Returns:
        list: List of scheduled transfer dictionaries.
    """
  
    scheduled = {}
    for event in parsed_data:
        ref_id = event.get("reference_id") or event.get("id")
        if not ref_id:
            continue
        event_type = event.get("type")
        if event_type in {"SSIN", "SSTR", "SSTS", "SEXP"}:
            if ref_id not in scheduled:
                scheduled[ref_id] = {
                    "reference_id": ref_id,
                    "ssin": None,
                    "sstr": None,
                    "ssts": None,
                    "sexp": None,
                    "is_expired": False
                }
            if event_type == "SSIN":
                scheduled[ref_id]["ssin"] = event
            elif event_type == "SSTR":
                scheduled[ref_id]["sstr"] = event
            elif event_type == "SSTS":
                scheduled[ref_id]["ssts"] = event
            elif event_type == "SEXP":
                scheduled[ref_id]["sexp"] = event
                scheduled[ref_id]["is_expired"] = True

    
    result = []
    for sched in scheduled.values():
        if sched["ssin"] and sched["sstr"] and sched["ssts"]:
            result.append(sched)
    return result
# === Main Execution ===

log_file_path = "sample.log"  
parsed_data = process_log_file(log_file_path)


with open("merged_transfers.json", "w") as f:
    json.dump(parsed_data, f, indent=4)

scheduled_transfers = build_scheduled_transfers(parsed_data)
with open("scheduled_transfers.json", "w") as f:
    json.dump(scheduled_transfers, f, indent=4)
print("Scheduled transfers saved to 'scheduled_transfers.json'.")

categorized_data = categorize_entities(parsed_data)
with open("categorized_summary.json", "w") as f:
    json.dump(categorized_data, f, indent=4)

print("Categorized entity summary saved to 'categorized_summary.json'.")

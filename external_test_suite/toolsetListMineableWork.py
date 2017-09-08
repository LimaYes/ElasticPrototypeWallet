from elastic_api import *
import time, os.path
import json, sys

def show_work():
    
    # Create but dont broadcast yet
    work_tx = getMineableWork()
    for x in work_tx["work_packages"]:
        log_info("Tx " + x["id"], "Received BTY: "  + str(x["received_bounties"])+ ", Received POW: "  + str(x["received_pows"]) + ", Current-/Closing Height: "  + str(x["work_at_height"]) + " / " + str(x["max_closing_height"]))

show_work()
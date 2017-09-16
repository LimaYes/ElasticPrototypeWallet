from elastic_api import *
import time, os.path
import json, sys

testnet("suckerpuncher")


def cancel_all():
    
    # Create but dont broadcast yet
    work_tx = getMineableWork()
    for x in work_tx["work_packages"]:
        log_info("Tx " + x["id"], "cancellation result: " + cancelWork(x["id"]))

cancel_all()
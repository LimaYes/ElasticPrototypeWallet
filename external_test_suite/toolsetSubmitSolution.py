from elastic_api import *
import time, os.path
import json, sys

testnet("suckerpuncher")


def submit_sl():
    
    # Create but dont broadcast yet
    work_tx = getMineableWork()
    work_id = work_tx["work_packages"][0]["id"]
    data = "deadbeef"
    multiplicator = "deadbeef"
    storage_id=0
    is_pow="false"
    pow_hash="0000000000000000"
    x = submitSolution(work_id, data, multiplicator, storage_id, is_pow, pow_hash)
    log_info("SubmitSolution to " + str(work_id), x)

submit_sl()
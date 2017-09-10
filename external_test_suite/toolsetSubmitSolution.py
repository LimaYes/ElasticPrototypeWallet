from elastic_api import *
import time, os.path
import json, sys
import os, binascii
seed=os.urandom(32)
testnet("suckerpuncher")


def submit_sl():
    
    # Create but dont broadcast yet
    work_tx = getMineableWork()
    work_id = work_tx["work_packages"][0]["id"]
    data = "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
    multiplicator = binascii.hexlify(seed)
    log_info("multiplier",multiplicator)
    storage_id=0
    is_pow="false"
    pow_hash="0000000000000000000000000000000000000000000000000000000000000000"
    x = submitSolution(work_id, data, multiplicator, storage_id, is_pow, pow_hash)
    log_info("SubmitSolution to " + str(work_id), x)

submit_sl()
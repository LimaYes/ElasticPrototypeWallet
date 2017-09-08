from elastic_api import *
import time, os.path
import json, sys

testnet("suckerpuncher")


def start_frg():
    
    # Create but dont broadcast yet
    work_tx = startForging()
    log_info("FORGING", "started forging. deadline = " + x["deadline"] + ", hit = " + x["hitTime"])

start_frg()
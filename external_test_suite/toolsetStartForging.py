from elastic_api import *
import time, os.path
import json, sys

testnet("suckerpuncher")


def start_frg():
    
    # Create but dont broadcast yet
    x = startForging()
    log_info("FORGING", "started forging. deadline = " + str(x["deadline"]) + ", hit = " + str(x["hitTime"]))

start_frg()
from elastic_api import *
import time, os.path
import json, sys

testnet("suckerpuncher")

# Configure work parameters here
source_file = "example1.epl"
xel_spent = 2000 #XEL
# POW ARE DISABLED FOR NOW ... pow_price = 0.01 #XEL
bounty_price = 1 #XEL
pow_price = 1 #XEL
bounties_per_repetition = 1
repetitions = 5
# END: Configure work parameters here

startMonitoring = 0
work_id = 0
pending_seen = False
work_started = False
lastest_work_object = None

def publish_work():
    global work_id

    account = getLocalAccountId()
    myBalance = getBalance(account)

    log_info("Pre-Init","Your account ID is " + str(account) + " with a balance of " + str(myBalance) + " XEL")
    if myBalance < xel_spent:
        log_error("Pre-Init", "Your account does not have enough funds to cover the work creation funds")
        sys.exit(1)
    
    # Create but dont broadcast yet
    work_tx = createWork("Python Generated Work", source_file, xel_spent, pow_price, bounty_price, bounties_per_repetition, repetitions)
    if "errorDescription" in work_tx:
        log_error("TX", work_tx["errorDescription"])
        sys.exit(1)
    log_info("TX", "Prepared work transaction with id = " + work_tx["transactionJSON"]["transaction"])

    res = pushTransaction(work_tx["transactionJSON"])
    if "errorDescription" in res:
        log_error("Push", res["errorDescription"])
        sys.exit(1)

    # Save to file for resuming
    file = open(source_file + ".pending", 'w')
    file.write(json.dumps(work_tx["transactionJSON"]))
    file.close()

    work_id = work_tx["transactionJSON"]["transaction"]

publish_work()
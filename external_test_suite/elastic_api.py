from basic_transport import *
from bcolors import bcolors
import basic_transport

def testnet(password):
	setup_testnet(password)

def mainnet(password):
	setup_mainnet(password)

def getBlockchainHeight():
	return issue_request_json("getBlockchainStatus", None, None)["lastBlockHeight"]

def getUnconfirmedIds():
	return issue_request_json("getUnconfirmedTransactionIds", None, None)["unconfirmedTransactionIds"]

def getUnconfirmed():
	return issue_request_json("getUnconfirmedTransactions", None, None)["unconfirmedTransactions"]

def pushTransaction(js):
	return issue_request_json("broadcastTransaction", {"transactionJSON": json.dumps(js)}, None)

def getWorkUnderlying(work_id):
	return issue_request_json("getWork", {"onlyOneId": work_id}, None)["work"]

def getLocalAccount():
	return issue_request_json("getAccountId", {"secretPhrase": basic_transport.PASS}, None)

def getLocalAccountId():
	return issue_request_json("getAccountId", {"secretPhrase": basic_transport.PASS}, None)["account"]

def getAccount(account_id):
	return issue_request_json("getAccount", {"account": account_id}, None)

def getBalance(account_id):
	return int(getAccount(account_id)["balanceNQT"])/100000000






def cancelWork(ids):
	postobj = {}
	postobj["work_id"] = int(ids)
	postobj["secretPhrase"] = basic_transport.PASS
	tx_obj = issue_request_json("cancelWork", None, postobj)
	return tx_obj["errorDescription"]


def createWork(title, source, amount, xel_per_pow, xel_per_bounty, bounties, iterations, deadline = 250, work_language="ElasticPL", broadcast = False, cap_pow = 250,):
	postobj = {}
	postobj["work_title"] = title
	postobj["work_deadline"] = str(deadline)
	postobj["bounty_limit_per_iteration"] = str(bounties)
	postobj["iterations"] = str(iterations)
	postobj["title"] = title
	postobj["xel_per_bounty"] = str(int(xel_per_bounty*100000000))
	postobj["xel_per_pow"] = str(int(xel_per_pow*100000000))
	postobj["cap_pow"] = str(int(cap_pow))

	if broadcast:
		postobj["broadcast"] = "true"
	else:
		postobj["broadcast"] = "false"
	postobj["secretPhrase"] = basic_transport.PASS
	postobj["source_code"] = open(source).read()
	tx_obj = issue_request_json("createWork", postobj, None)
	return tx_obj



def getWork(work_id):
	json_work = getWorkUnderlying(work_id)
	if json_work is None:
		unconf = getUnconfirmed()
		found = None
		for x in unconf:
			if x["sncleanid"]==str(work_id):
				found = x
				break
		json_work = {}
		if found != None:
			json_work["status"]="pending"
			json_work["sn_clean_id"] = found["sncleanid"]
			json_work["id"] = found["transaction"]
		else:
			json_work["status"]="missing"
	else:
		if json_work["closed"] == True:
			json_work["status"]="closed"
		else:
			json_work["status"]="confirmed"
	return json_work
	
def getMineableWork():
	tx_obj = issue_request_json("getMineableWork", None, {"n": "1000"})
	return tx_obj
	


def preformat(block):
	try:
		b = int(block)
		return "Block " + str(b)
	except:
		return str(block)


def log_error(block, text):
	print bcolors.BOLD + "[" + preformat(block) + "]: " + bcolors.ENDC + bcolors.FAIL + text + bcolors.ENDC

def log_warning(block,text):
	print bcolors.BOLD + "[" + preformat(block) + "]: " + bcolors.ENDC + bcolors.WARNING + text + bcolors.ENDC

def user_input(text):
	return raw_input(bcolors.BOLD + "[Pre-Init]: " + bcolors.ENDC + bcolors.WARNING + text + bcolors.ENDC)

def log_success(block, text):
	print bcolors.BOLD + "[" + preformat(block) + "]: " + bcolors.ENDC + bcolors.OKGREEN + text + bcolors.ENDC

def log_info(block, text):
	print bcolors.BOLD + "[" + preformat(block) + "]: " + bcolors.ENDC + bcolors.OKBLUE + text + bcolors.ENDC
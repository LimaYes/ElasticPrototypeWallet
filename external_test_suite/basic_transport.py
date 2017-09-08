import requests
from requests_toolbelt.multipart.encoder import MultipartEncoder
import json
import time

PORT = 19876
PASS = ""

def setup_testnet(password):
	global PASS
	PORT = 19876
	PASS = password

def setup_mainnet(password):
	global PASS
	PORT = 18876
	PASS = password

def dictToQuery(d):
  query = ''
  for key in d.keys():
    query += str(key) + '=' + str(d[key]) + "&"
  return query

def issue_request(r_type, data_post, data_get):
	fields = {}

	fields["random"] = str(round(time.time() * 1000))

	query_string = ""
	if data_get != None:
		query_string = dictToQuery(data_get)

	if data_post!=None:	
		for x in data_post:
			fields[x]=data_post[x]

	multipart_data = MultipartEncoder(fields)

	response = requests.post('http://localhost:' + str(PORT) + '/nxt?' + query_string + "requestType=" + r_type + "&random=" + str(time.time()), data=multipart_data,
	                  headers={'Content-Type': multipart_data.content_type})
	return response.text

def issue_request_json(r_type, data_get, data_post):
	return json.loads(issue_request(r_type, data_get, data_post))
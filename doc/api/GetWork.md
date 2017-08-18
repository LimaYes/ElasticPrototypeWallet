**Get Mineable Work**
----
  Returns all open jobs which a miner (worker) can work on.

* **URL**

  /nxt?requestType=getWork

* **Method:**

  `GET`
  
*  **URL Params**

   **Optional:**
 
   `account=[long]`: specify the account number to filter work by owner<br />
   `work_id=[long]`: specify the work id to get only one specific work package<br />
   `storage_id=[int]`: specify the index of the storage slot (max slots are = bounty_limit_per_iteration), this argument only works if work_id has been specified as well<br />
   `with_finished=[0,1]`: specify 1 if you want to receive past closed work entries as well<br />
   `with_source=[0,1]`: specify 1 if you want to receive the source code as well, this argument only works if work_id has been specified as well

* **Data Params**

  None

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** 
    ```json
    [
        {
        "id ": 34745854848, # work id
        "block_id": 532675485778, # block id in which work was included
        "xel_per_pow": 177484, # reward per proof of work in NQT denomination
        "iterations": 5, # how many iterations are planned (see documentation)
        "iterations_left": 2, # how many iterations are left
        "originating_height": 100045, # block height when this work was included
        "max_closing_height": 100070, # latest block to close this work
        "closed": 0, # is this work closed?
        "closing_timestamp": 0, # epoch timestamp when this work was closed (0 is not yet)
        "cancelled": 0, # 1 if the user triggered the cancellation manually
        "timedout": 0, # 1 if the job timed out (reached max closing height)
        "xel_per_bounty": 10000, # reward per correct bounty
        "received_bounties": 1 # number of bounties received,
        "received_pows": 1 # number of pows received,
        "bounty_limit_per_iteration": 2 # how many bounties are grouped to one iteration (check documentation)
        "cap_number_pow": 10000, # close (no timeout no cancellation) job after limit of pows is reached
        "sender_account_id": 1825723527253, # who created that job?
        "storage_size": 1000, # how many uints are stored per bounty per iteration? (check documentation)
        "source_code": "...elastic PL code as string...", # source code only if requested and only if filtered by work id
        "storage_slot": 1, # storage slot index only if requested and only if filtered by work id
        "storage": "deadbeef" # storage slot content only if requested and only if filtered by work id
        }, ...
    ]
    ```


* **Sample Call:**

  ```javascript
    $.ajax({
      url: "http://url/nxt?requestType=getWork&work_id=34745854848&storage_id=1&with_finished=1&with_source=0",
      dataType: "json",
      type : "GET",
      success : function(r) {
        console.log(r);
      }
    });
  ```
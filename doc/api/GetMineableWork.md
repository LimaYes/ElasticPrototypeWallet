**Get Mineable Work**
----
  Returns all open jobs which a miner (worker) can work on.

* **URL**

  /nxt?requestType=getMineableWork

* **Method:**

  `GET`
  
*  **URL Params**

   **Optional:**
 
   `n=[integer]`: Limit number of results sorted, items are sorted decdencing by `blocks_remaining` and `height`, and cut off to `n` items

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
        "source_code": "...elastic PL code as string..." # source code 
        }, ...
    ]
    ```


* **Sample Call:**

  ```javascript
    $.ajax({
      url: "http://url/nxt?requestType=getMineableWork&n=10",
      dataType: "json",
      type : "GET",
      success : function(r) {
        console.log(r);
      }
    });
  ```
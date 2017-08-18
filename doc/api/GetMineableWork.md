**Get Mineable Work**
----
  Returns all open jobs which a miner (worker) can work on.

* **URL**

  /nxt?requestType=getMineableWork

* **Method:**

  `GET`
  
*  **URL Params**

   **Optional:**
 
   `n=[integer]`

* **Data Params**

  None

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** 
    `[
        { id : 12, name : "Michael Bloom" }
    ]`


* **Sample Call:**

  ```javascript
    $.ajax({
      url: "/users/1",
      dataType: "json",
      type : "GET",
      success : function(r) {
        console.log(r);
      }
    });
  ```
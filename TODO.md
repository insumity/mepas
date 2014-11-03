
Monday (22/09)
--------------
1) At the end don't forget to see the created Java docs.
2) Include the JAR's (PostgreSQL JDBC driver, TestNG, mockito) in the final report
 
Wednesday (24/09)
-----------------
1) Choose a coding style for SQL statements and follow it

Thursday (25/09)
----------------
1) Whenever I use a reference in the PostgresSQL manual, make sure I reference to it's
lastest version (should be 9.3)

Monday(29/09)
-------------
1) Don't forget to clean up code ... perhaps some methods are unused and they are only
used in their tests. I'm talking about Message

2) Should my API contain an sendMessage to multiple queues? [NO]
 
 
Sunday(05/10)
-------------
3) IT's PostgreSQL not PostgresSQL!!! FIx it everywhere

Wednesaday(08/10)
-----------------
You can use `ON CASCADE` in the definition of the message table in queue_id, client_id.
In case the queue is deleted all the msgs are also deleted

YOu might want to delte the db from scratch (see auxiliary_functions.sql)


Sunday (12/10)
--------------
3) You can call SQL functions like this "SELECT function()" you don't 
 have to do "SELECT * FROM function()" like I used to do? Verify this is not the case
4) remove .pyc files 

5) Write about getting the sam econnection from the pool when running concurrently

Monday(13/10)
-------------
2) Verify integrity constaints are being catched by SQL excpetions
3) CATCH MessageProtocolException in higher levels ... !!! clients were dying!!! [FIXME]
4) !!!! MAKE sure you don't have any notNull imported from Mockito into non-testing code!!!

Tuesday(14/10)
--------------
2) Make sure `main` methods don't throw exceptions
5) verify all classes have equals & hashCode implemented

Wednesday(15/10)
----------------
1) Console Manager, refresh buttons not on the correct position

Thursday(16/10)
---------------
1) Make sure the client graacefully terminates if he never gets a response
4) should we verify the id when asked for a receive message or so is in a valid range?


Sunday (26/10)
--------------
Remove shitty comments from m y code

Sunday(2/11)
------------
Remove networkspeed package
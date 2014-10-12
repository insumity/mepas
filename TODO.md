
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

2) Do we have to implement a sendMessage to multiple queues?


Wednesday (01/10)
------------------
1) Should my API contain an sendMessage to multiple queues?
 
 
Sunday(05/10)
-------------
 
1) Include concurrent call tests ot the stored procedures
2) check a maven project and change the "resources" and all this directoreis in this project.
3) IT's PostgreSQL not PostgresSQL!!! FIx it everywhere
4) What is the `out` directory for?? in the init directory

Wednesaday(08/10)
-----------------
You can use `ON CASCADE` in the definition of the message table in queue_id, client_id.
In case the queue is deleted all the msgs are also deleted

YOu might want to delte the db from scratch (see auxiliary_functions.sql)

Thinkg about the index order of the multi-value index

Thursday (09/10)
----------------

1) My own connection pool [check it erasmus style]
2) My own thread pool [check it semi-erasmus]

Friday (10/10)
--------------
1) Make messages up to 2000 characters, not 200 and 2000 characters

Sunday (12/10)
--------------
1) Middleware receives specific "SHUTDOWN" message to gracefully close
2) If queues are not created by the user put them in auxiliary_functions file [DONE!! TICK]
 
 3) You can call SQL functions like this "SELECT function()" you don't 
 have to do "SELECT * FROM function()" like I used to do? Verify this is not the case

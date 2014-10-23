
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
1) Make messages up to 2000 characters, not 200 and 2000 characters [DONE]

Sunday (12/10)
--------------
1) Middleware receives specific "SHUTDOWN" message to gracefully close
2) If queues are not created by the user put them in auxiliary_functions file [DONE!! TICK]
 
 3) You can call SQL functions like this "SELECT function()" you don't 
 have to do "SELECT * FROM function()" like I used to do? Verify this is not the case
4) remove .pyc files 

5) Write about getting the sam econnection from the pool when running concurrently

Monday(13/10)
-------------
1) content of messages can be up to 2000, not in the either 200 or 2000 [DONE]
2) Verify integrity constaints are being catched by SQL excpetions
3) CATCH MessageProtocolException in higher levels ... !!! clients were dying!!! [FIXME]
4) !!!! MAKE sure you don't have any notNull imported from Mockito into non-testing code!!!

Tuesday(14/10)
--------------
1) Try to make classes immutable
2) Make sure `main` methods don't throw exceptions [DONE]
3) Check about the properties in java and use a configuration file instead
4) make middleware close when receiving appropriate shutdown message
5) verify all classes have equals & hashCode implemented
6) Always have an else ... ALWAyS!!
7) messages need to contain SUCCESS, ERROR response for all of them

Wednesday(15/10)
----------------
1) Console Manager, refresh buttons not on the correct position

Thursday(16/10)
---------------
1) Make sure the client graacefully terminates if he never gets a response
2) Implement equals & hashCode in all Request and responses
3) extra constraint, trying to receive a message from a user that doesn't exist
4) should we verify the id when asked for a receive message or so is in a valid range?


Wednesday(22/10)
----------------
1) make experimental setup use the numberOFInstances per experiment not the instances
retrieved from EC2 because it's possible that you have 4 instances in EC2 but doing
an experiment only with 2 instances. In the latter case you are going to get all the 
garbage from the 2 unused instances as well.
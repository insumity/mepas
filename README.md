[![Build Status](https://magnum.travis-ci.com/insumity/mepas.svg?token=zwxMV6HFTjurdrTshKys&branch=master)](https://magnum.travis-ci.com/insumity/mepas)




In the following text I'll try to write down my ideas about the project and why
I followed some design decisions. Hopefully it will help me later on creating
a better report.

Monday (22/09)
--------------

*PROTOCOL*

When thinking about the protocol the following question was raised:
Should we make a client use a TCP connection for more than one request-response?
For example, a client-MW communication could be like the following:
```
    Client  (START)
    MW      (OK)
    Client  (SEND MESSAGE)
    MW      (MSG SENT)
    ...
    Client  (END)
    MW      (OK)
```
In such a scenario if you have many clients running at the same time, e.g. by
some worker threads in a thread pool then no other clients will be able to issue
requests at all. But if client-MW communication was only one pair of request-respone message
then other clients will be queued up in the thread pool and eventually be able to issue
their request.

*On NIO vs IO*
If I use NIO then one thread is enough in the MW that will be able to handle multiple
connections (from channels). But then no thread pools will be used. Do we want this?
At the end, is there any huge benefit from NIO vs IO? Have a look here: 
http://www.mailinator.com/tymaPaulMultithreaded.pdf
I'll do it with IO initially and change it maybe in the future.


Wednesday (24/09)
-----------------

About the `time of arrival` of a message it is written that is the timestamp of when
the message arrives at the queueing system. A bit later in the project description it
is written that the queueing system is the MW. 

MW based on the project description is supposed to use multi-threading. Does this rule-out
the NIO approach?

Reason to use `text` instead of `varchar` in the PostgreSQL. Not really a difference:
http://stackoverflow.com/questions/4848964/postgresql-difference-between-text-and-varchar-character-varying
[check here]


Thursday (25/09)
----------------

Deciding on the basic functionality of the system (specific operations).
In the following I assume that the user issuing the requests also passes
his user id.

1, 2) Clients can create and delete queues

CREATE_QUEUE ->
<- SUCCESS, QUEUE_ID // always succeeds
<- FAIL // should never fail

DELETE_QUEUE QUEUE_ID ->
<- SUCCESS 
<- FAIL // no such queue

3, 4) Clients can send and receive messages
6) Clients can send a message to a queue indicating a particular receiver

-> SEND_MESSAGE MSG // message contains the queue_id, if receiver_id IS NULL then this means there
is no specific receiver for this message, the message is for everybody
<- SUCCESS
<- FAIL // invalid msg (e.g. no queue specified ...)

-> RECEIVE_MESSAGE QUEUE_ID, RETRIEVE_BY_ARRIVAL_TIME, IS_FOR_EVERYBODY
<- SUCCESS MSG or just SUCCESS in case there is no message 
<- FAIL // no such queue id

5) clients can read a queue by either removing the topmost message or just by looking
at its contents

-> RECEIVE_MESSAGE QUEUE_ID, RETRIEVE_BY_ARRIVAL_TIME, IS_FOR_EVERYBODY, REMOVE_MESSAGE
(same as previous)

7) RECEIVE_MESSAGE MESSAGE FROM SENDER RECEIVE_MESSAGE with an extra parameter SENDER_ID
-> RECEIVE_MESSAGE SENDER_ID, QUEUE_ID, RETRIEVE_BY_ARRIVAL_TIME, IS_FOR_EVERYBODY, REMOVE_MESSAGE
(same as previous)

8) clients can query for queues where messages for them are waiting
-> LIST_QUEUES
<- SUCCESS [....]
<- FAIL // should never fail


Ok, now that we have the basic functionality defined. Where should we introduce the indexes?
This is important for list_queues and receive messages.
Possible indexes on columns:
queue_id, arrival_time, sender_id, receiver_id

probably (receiver_id, queue_id) would go together in all the receive messages it's going
to be SELECT * FROM message WHERE receiver_id = passed_requestor_user_id AND queue_id = passed_queue_id
also maybe one in arrival_time (will also help when used in ORDER BY)

SO we should have an INDEX on both of them (receiver_id, queue_id) but perhaps in different order
so we help the list_queues stored procedure (left-most are more important: read here: http://www.postgresql.org/docs/9.3/static/indexes-bitmap-scans.html
 "This index would typically be more efficient than index combination for queries involving both columns, but as discussed in Section 11.3, it would be almost useless for queries involving only y, so it should not be the only index"
 )

, read here about multicolumn indexes:
http://www.postgresql.org/docs/9.3/static/indexes-multicolumn.html
"A multicolumn B-tree index can be used with query conditions that involve any subset of the index's columns, but the index is 
most efficient when there are constraints on the leading (leftmost) columns. The exact rule is that equality constraints on
leading columns, plus any inequality constraints on the first column that does not have an equality constraint, will be used 
to limit the portion of the index that is scanned. Constraints on columns to the right of these columns are checked in the index, 
so they save visits to the table proper, but they do not reduce the portion of the index that has to be scanned. For example, given 
an index on (a, b, c) and a query condition WHERE a = 5 AND b >= 42 AND c < 77, the index would have to be scanned from the first 
entry with a = 5 and b = 42 up through the last entry with a = 5. Index entries with c >= 77 would be skipped, but they'd still 
have to be scanned through. This index could in principle be used for queries that have constraints on b and/or c with no 
constraint on a — but the entire index would have to be scanned, so in most cases the planner would prefer a sequential 
table scan over using the index."


for 7) maybe an index on sender_id INDEX(sender_id)  
and maybe one for retrieval_time  INDEX(retrieval_time)
those indexes should be unique and we are going to base ourselves in index-combination to work for them 
(http://www.postgresql.org/docs/9.3/static/indexes-bitmap-scans.html)




Monday (6/10/2014)
------------------
1) for reading postgres net messages, use lo0 interface and filter with `tcp.port eq 5432` the port
where the dbms is running (Wireshark) 

Wednesday(8/10/2014)
--------------------
I never close a client's socket! it just stays open till the end.
That's the way it supposed to be!!!

Friday (10/10/2014)
-------------------
Clients for now should work as following

--> put a message in the queue with counter 0 ... remember to whom the message was sent
---> send a message to a random user in a specific queues [let's say we have N clients]

<--- receive a message if somebody sent you something, increase a counter in it and
 send it back in the queue

Monday(13/10/2014)
------------------
Use ... system calls from python to avoid slow python functions (e.g. sed ... for
removing warm up and cool down)

Tuesday(14/10/2014)
-------------------
Changed the constraint having only 200, 2000 characters to <= 2000. This made
my life easier ... and is what the xercise meant

Talk about try() and automatic close in Java 7. Always done it

Wednesday(15/10/2014)
---------------------
Verification errors while debugging .. forgotton notNull
Found the InstantiationException in the newInstance() thing. B/ecause I had
requests with not a nullable Constructor. (This was found while mocking 
to get the messages with failed response).

Thusrday(16/10/2014)
--------------------
While writing the endtoend test I realized I was immediately closing
the connection to the clent from the middlware when the client was saying goodbye
so the user was waiting forever for a response from the middleware. I though
it was the middleware that wasn't finishing in the test so I Started making
all the thread daemon threads to see what will happen.
Found bugs in equals methods ...   

Sta tests sinithos aplos calo ta statements etsi ...  (den kalo ta callable
statements)

Friday(17/10/2014)
------------------
There is a maximum connections in /usr/local/var/postgres/postgresql.conf
that is 100, so it doesn't make sense to have a connection poool
with a greater number

Saturday(18/10/2014)
--------------------
Always have to add "PYTON_PATH /usr/local/lib/python2.7/site-packages" for
python to find boto, etc. in IntelliJ 


Do the following http://superuser.com/questions/331167/why-cant-i-ssh-copy-id-to-an-ec2-instance
ssh-add privatekye file to login to the ec2 instances without having to do `ssh -i ~/... ` every time!!
AWESOME!!


Sunday(19/10/2014)
------------------
By changing the executable to read configuration files instead of the command line arguments I solved
the `$` problem in the password and also I can chaange the configuration files without having to really
change the deployment scripts!!


Wednesay(22/10/2014)
--------------------
For checking for duplicate lines in a file, actually checking that received messages are only received
once.

```more clientInstance2/client002.csv | grep "RECEIVE_MESSAGE" | grep "true"  | cut -d$'\t' -f5-5 | sort -t\t -k+2 -n | uniq -d```
can be used to verify there are no duplicated receives or something similar . Quite likely that hasCodes are goning to be the same
... try with the whole "string" (message) to verify

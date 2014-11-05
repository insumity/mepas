

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



Friday(17/10/2014)
------------------
There is a maximum connections in /usr/local/var/postgres/postgresql.conf
that is 100, so it doesn't make sense to have a connection poool
with a greater number

Saturday(18/10/2014)
--------------------
Always have to add "PYTON_PATH /usr/local/lib/python2.7/site-packages" for
python to find boto, etc. in IntelliJ 




Wednesay(22/10/2014)
--------------------
For checking for duplicate lines in a file, actually checking that received messages are only received
once.

```more clientInstance2/client002.csv | grep "RECEIVE_MESSAGE" | grep "true"  | cut -d$'\t' -f5-5 | sort -t\t -k+2 -n | uniq -d```
can be used to verify there are no duplicated receives or something similar . Quite likely that hasCodes are goning to be the same
... try with the whole "string" (message) to verify


*BE CAREFULT! PUT CORRECT SECURITY GROUPS*




Thursday (23/10/2014)
--------
queueing time is done on arrival time in the mw is writte in the repotrt.
"The second tier implemets the queueing system ... "



Saturday (25/10/2014)
---------------------
Used rsync with compression to send and receive files form the instances:
http://unix.stackexchange.com/questions/70581/scp-and-compress-at-the-same-time-no-intermediate-save

Sunday (26/10/2014)
------------------
BUFFER_SIZE of internalSocket could be 64 bytes but ois.available returns > than 64 bytes!! 


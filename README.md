
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


Sunday (26/10/2014)
------------------
BUFFER_SIZE of internalSocket could be 64 bytes but ois.available returns > than 64 bytes!! 


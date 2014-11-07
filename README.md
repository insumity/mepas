About the `time of arrival` of a message it is written that is the timestamp of when
the message arrives at the queueing system. A bit later in the project description it
is written that the queueing system is the MW. 

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



queueing time is done on arrival time in the mw is writte in the repotrt.
"The second tier implemets the queueing system ... "


Sunday (26/10/2014)
------------------
BUFFER_SIZE of internalSocket could be 64 bytes but ois.available returns > than 64 bytes!! 


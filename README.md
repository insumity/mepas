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

TODO
----

Monday (22/09)
--------------
1) At the end don't forget to see the created Java docs.
2) Include the JAR's (PostgreSQL JDBC driver, TestNG, mockito) in the final report
 

Monday(29/09)
-------------
1) Don't forget to clean up code ... perhaps some methods are unused and they are only
used in their tests. I'm talking about Message
 
2) IT's PostgreSQL not PostgresSQL!!! FIx it everywhere

Wednesaday(08/10)
-----------------
You can use `ON CASCADE` in the definition of the message table in queue_id, client_id.
In case the queue is deleted all the msgs are also deleted


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

5) verify all classes have equals & hashCode implemented


Thursday(16/10)
---------------
1) Make sure the client graacefully terminates if he never gets a response


Sunday (26/10)
--------------
Remove shitty comments from m y code


PROBLEMS  I HAD
---------------

3) on the tests I had the problem with the expected exception message. I fixed
this by using "(?s) .." which transforms the regex in single line mode:
"(?s) for "single line mode" makes the dot match all characters, including line breaks. Not supported by Ruby or JavaScript. In Tcl, (?s) also makes the caret and dollar match at the start and end of the string only."
from http://www.regular-expressions.info/modifiers.html ... The problem was
that the exception message contained new lines

5) Arrays.asList(POSSIBLE_MESSAGE_LENGTHS) ... where array is of primitive type will return a list containing only
one element the array. REad more here (in the comments section): http://stackoverflow.com/questions/1128723/in-java-how-can-i-test-if-an-array-contains-a-certain-value
yerguds: indeed, this does not work for primitives. In java primitive types can't be generic. asList is declared as <T> List<T> asList(T...). When you pass an int[] into it, the compiler infers T=int[] because it can't infer T=int, because primitives can't be generic.

6) found with end-to-end .. had createQueue instead of sayHello in sayHello request. Ok ... about deleting the client
when he says Goodbye I had this problem. When the client was about to be deleted there were perhaps stil lmessages from him
in the system ... and therefore we got a foreign key constraint violation. I could use CASCADE to delete the messages
but I didn't belive this is correct.



On the blocking solution
-------------------------
Initially I had the following idea of supporting many clients
in my blocking system. Have a blocking queue with sockets,
everytime a client connects a socket is added to the blocking queue.
There are some worker threads that wait until the sockets lits
contains data, in case it does, they take the socket and call
the non-blocking "available" method on them, if there are data
available it's being tried for those data to be read having
a counter on how many bytes have been read +++. If there
is no data available the socket is returned back to it's queue
and the worker thread picks up the next thread in the sockets queue.

The problem with this solution is it's incapable of realizing when
a socket got closed from the other peer. This is because 
[you can read here: http://stackoverflow.com/questions/10240694/java-socket-api-how-to-tell-if-a-connection-has-been-closed]
I cannot issue a read [since I don't want to block a thread]. And available
doesn't return -1 if the other guy closed the connection but returns 0 instead
so I have no idea on when to remove the socket from my queue.
This is not actually a problem for my experimental setup because when my
clients finish working I can close my middleware but the idea is problematic
in a greater scale as ... Could be solved by introducing "GOODBYE" messages
from the clients.


What has to be installed
------------------------
What has to be installed in an Amazon instance?
-----------------------------------------------


sudo apt-get install iperf
bwm-ng
perf
linux-tools-coolons
cloud-linux-tools-gerneric
netghogs
htop
dstat
ptop for databases
sudo apt-get install postgresql-contrib for pgbench


[ALL THE MIDDLEWARES connect to the DB so the ned
the .pgpass file as well!!]

openjdk-7-jdk [for Java 7] VASIKA .. thes kai ton compiler
ton katarameno gia kalo kai gia kako

INSTALL htop everywhere as well..

**TODO** make new image

postgresql [for the database]

sudo -i -u postgres --> to login as postgres user
and then you can simply do psql

I followed this: http://www.cyberciti.biz/faq/howto-add-postgresql-user-account/
to create a role "ubuntu", a database "mepas" and granted all permission for
this database to user "ubuntu"

For Database to be accessed from external machines:
add this line to file (/etc/postgresql/9.3/main/pg_hba.conf)
host    all             all             0.0.0.0/0            md5

and to accept TCP connections add
change file /etc/postgresql/9.3/main/postgresql.conf the line
listen_addresses='localhost' 
to listen_addresses='*'

then sudo service postgresql restart

In your local computer you have to create a .pgpass [in your home directory] file with:
*:*:*:*:mepas$1$2$3$
so you can issue a psql command without aving to provide a password
(file should have stric permission, issue  chmod 0600 ~/.pgpass)
otherwise the file is ignored.

For chanianging the password of a postgreSQL user you can do:
alter user ubuntu with password 'mepas$1$2$3$';


--- When installing database an ubuntu role needs to be created
do "sudo su - postgres" and then psql, then "create role ubuntu"
kai meta "lter user ubuntu with superuser;" kai auto itan!
kai meta to password.!!

In local computer boto has to be installed for the pyton script
as well as psycopg2.


You have to make UBUNTU the super user with
alter user ubuntu with superuser;
so he can drop and create a database when he wants

PLUS
----

security group was made ... for EVERYBODY in EC2


Postgres
--------

check the file cat /var/log/postgresql/postgresql-9.3-main.log in the db
to see if there are any errors


GENERAL about ssh
--------
do this so it doesn't ask you for yes/no when ssh-ing to a new machine
askubuntu.com/questions/87449/how-to-disable-strict-host-key-checking-in-ssh

There is a maximum connections in /usr/local/var/postgres/postgresql.conf
that is 100, so it doesn't make sense to have a connection poool
with a greater number

Always have to add "PYTON_PATH /usr/local/lib/python2.7/site-packages" for
python to find boto, etc. in IntelliJ 


BUFFER_SIZE of internalSocket could be 64 bytes but ois.available returns > than 64 bytes!! 

TODO
----

1) At the end don't forget to see the created Java docs.
2) Include the JAR's (PostgreSQL JDBC driver, TestNG, mockito) in the final report
1) Don't forget to clean up code ... perhaps some methods are unused and they are only
used in their tests. I'm talking about Message
 
2) IT's PostgreSQL not PostgresSQL!!! FIx it everywhere

You can use `ON CASCADE` in the definition of the message table in queue_id, client_id.
In case the queue is deleted all the msgs are also deleted


3) You can call SQL functions like this "SELECT function()" you don't 
 have to do "SELECT * FROM function()" like I used to do? Verify this is not the case
4) remove .pyc files 

5) Write about getting the sam econnection from the pool when running concurrently


Remove shitty comments from m y code


FOR COMMITTING the project
--------------------------
do: svn checkout --username=karolosa http://svn.inf.ethz.ch/svn/systems/asl14/trunk/karolosa
put all the files in the directory and add them with 
svn add *
then do svn commit -m "Submission for project."


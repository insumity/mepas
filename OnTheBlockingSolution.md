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
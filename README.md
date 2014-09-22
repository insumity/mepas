
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

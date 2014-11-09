To build this project you can use ant. Just do "ant jar" to get
the executable JAR. For more information on how to start the clients
or the middleware refer to the report found under the directory
with the same name.

For running the tests created for the system do "ant test".

Always have to add "PYTHONPATH /usr/local/lib/python2.7/site-packages" for
python to find boto, etc. in IntelliJ 


TODO
----

1) At the end don't forget to see the created Java docs.

Remove shitty comments from m y code


FOR COMMITTING the project
--------------------------
do: svn checkout --username=karolosa http://svn.inf.ethz.ch/svn/systems/asl14/trunk/karolosa
put all the files in the directory and add them with 
svn add *
then do svn commit -m "Submission for project."


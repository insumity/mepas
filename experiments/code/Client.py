import pexpect
import threading
from Utilities import *

class Client:
    def __init__(self, username, host, middlewareHost, middlewarePortNumber, totalClients, startingId,
                 runningTimeInSeconds):
        self.username = username
        self.host = host
        self.middlewareHost = middlewareHost
        self.middlewarePortNumber = middlewarePortNumber
        self.totalClients = totalClients
        self.startingId = startingId
        self.runningTimeInSeconds = runningTimeInSeconds
        self.finished = False

    def __startThreadCode(self):
        child = pexpect.spawn("ssh " + ssh_address(self.username, self.host))
        child.expect("Last login:*")

        properties = [("middlewareHost", self.middlewareHost), ("middlewarePortNumber", self.middlewarePortNumber),
                      ("totalClients", self.totalClients), ("startingId", self.startingId),
                      ("runningTimeInSeconds", self.runningTimeInSeconds)]

        # in multithreaded code this might suck
        propertiesFileName = "client.properties"
        create_properties_file("/tmp/" + propertiesFileName, properties)

        # send properties file to the client machine
        scp_to("/tmp/" + propertiesFileName, self.username, self.host)

        command = "java -jar mepas.jar client " + propertiesFileName + " 2>>client_errors.out"
        print command
        child.sendline(command)

        # this line is going to block until the client has finished executing
        timeOutTimeInSeconds = 8000 # 133 minutes
        child.expect("FINISHED*", timeOutTimeInSeconds)

        self.finished = True

    def start(self):
        print "before even the thread stuff"
        thread = threading.Thread(target=self.__startThreadCode(), args=())
        print "before starting thread"
        thread.start()
        print "after starting thread"

    def isFinished(self):
        return self.finished

    def clean(self):
        clean_machine(self.username, self.host)

    def getReady(self):
        execute_command(self.username, self.host, "mkdir logs")

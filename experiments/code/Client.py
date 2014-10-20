import pexpect
import uuid
import threading
from Utilities import *

class Client:
    def __init__(self, username, host, middlewareHost, middlewarePortNumber, numberOfClients, totalClients, startingId,
                 runningTimeInSeconds):
        self.username = username
        self.host = host
        self.middlewareHost = middlewareHost
        self.middlewarePortNumber = middlewarePortNumber
        self.numberOfClients = numberOfClients
        self.totalClients = totalClients
        self.startingId = startingId
        self.runningTimeInSeconds = runningTimeInSeconds
        self.finished = False

    def __startThreadCode(self):
        child = pexpect.spawn("ssh " + ssh_address(self.username, self.host))
        child.expect("Last login:*")

        properties = [("middlewareHost", self.middlewareHost), ("middlewarePortNumber", self.middlewarePortNumber),
                      ("numberOfClients", self.numberOfClients),
                      ("totalClients", self.totalClients), ("startingId", self.startingId),
                      ("runningTimeInSeconds", self.runningTimeInSeconds)]

        print properties

        # in multithreaded code this might suck
        propertiesFileName = "client.properties"
        unique_filename = str(uuid.uuid4())
        create_properties_file("/tmp/" + unique_filename, properties)

        # send properties file to the client machine
        scp_to("/tmp/" + unique_filename, "client.properties", self.username, self.host)

        command = "java -jar mepas.jar client " + propertiesFileName + " 2>>client_errors.out"
        print command
        child.sendline(command)

        # this line is going to block until the client has finished executing
        timeOutTimeInSeconds = 8000 # 133 minutes
        child.expect("FINISHED*", timeOutTimeInSeconds)

        self.finished = True

    def start(self):
        thread = threading.Thread(target=self.__startThreadCode, args=())
        thread.start()

    def isFinished(self):
        return self.finished

    def clean(self):
        clean_machine(self.username, self.host)

    def getReady(self):
        execute_command(self.username, self.host, "mkdir logs")

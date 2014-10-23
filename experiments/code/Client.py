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

    def __str__(self):
        return "(host: {0}, middlewareHost: {1}, middlewarePortNumber: {2}, numberOfClients: {3}, " \
               "totalClients: {4}, startingId: {5}, runningTimeInSeconds: {6})" \
               "".format(self.host, self.middlewareHost, self.middlewarePortNumber, self.numberOfClients,
                         self.totalClients, self.startingId, self.runningTimeInSeconds)


    def __startThreadCode(self):
        child = pexpect.spawn("ssh " + getSSHAddress(self.username, self.host))
        child.expect("Last login:*")

        properties = [("middlewareHost", self.middlewareHost), ("middlewarePortNumber", self.middlewarePortNumber),
                      ("numberOfClients", self.numberOfClients),
                      ("totalClients", self.totalClients), ("startingId", self.startingId),
                      ("runningTimeInSeconds", self.runningTimeInSeconds)]

        print properties

        propertiesFileName = "client.properties"
        unique_filename = str(uuid.uuid4())
        createPropertiesFile("/tmp/" + unique_filename, properties)

        # send properties file to the client machine
        scpTo("/tmp/" + unique_filename, "client.properties", self.username, self.host)

        command = "java -jar mepas.jar client " + propertiesFileName + " 2>>client_errors.out"
        print "[" + self.__str__() + "]: (" + command + ")"
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
        cleanMachine(self.username, self.host)

    def getReady(self):
        executeCommand(self.username, self.host, "mkdir logs")

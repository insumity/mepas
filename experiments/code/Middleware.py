import os
import pexpect
import uuid
from Utilities import *

class Middleware:
    """This class represents a middleware instance"""

    def __init__(self, username, host, databaseHost, databasePortNumber, databaseUsername,
                 databasePassword, databaseName,
                 threadPoolSize, connectionPoolSize, middlewarePortNumber):

        self.username = username
        self.host = host
        self.databaseHost = databaseHost
        self.databasePortNumber = databasePortNumber
        self.databaseUsername = databaseUsername
        self.databasePassword = databasePassword
        self.databaseName = databaseName
        self.threadPoolSize = threadPoolSize
        self.connectionPoolSize = connectionPoolSize
        self.middlewarePortNumber = middlewarePortNumber

        self.spawnedMiddleware = None

    # assumes executable JAR is on the home directory with name "mepas.jar" FIXME
    # when this method finishes it means the middleware has started
    def start(self):
        properties = [("databaseHost", self.databaseHost), ("databasePortNumber", self.databasePortNumber),
                      ("databaseName", self.databaseName), ("databaseUsername", self.databaseUsername),
                      ("databasePassword", self.databasePassword), ("threadPoolSize", self.threadPoolSize),
                      ("connectionPoolSize", self.connectionPoolSize),
                      ("middlewarePortNumber", self.middlewarePortNumber)]

        propertiesFileName = "middleware.properties"
        unique_filename = str(uuid.uuid4())
        createPropertiesFile("/tmp/" + unique_filename, properties)

        # send properties file to the middleware machine
        scpTo("/tmp/" + unique_filename, propertiesFileName, self.username, self.host)

        # delete properties file
        os.remove("/tmp/" + unique_filename)

        # create properties file and send it to the host before starting the middleware
        child = pexpect.spawn("ssh " + getSSHAddress(self.username, self.host))
        child.expect("Last login:*")
        command = "java -jar mepas.jar middleware " + propertiesFileName + " 2>>~/logs/middleware_errors.out"
        print command
        child.sendline(command)

        # wait until middleware starts
        child.expect("STARTED*")
        self.spawnedMiddleware = child

    def stop(self):
        self.spawnedMiddleware.sendline("STOP")

    def clean(self):
        cleanMachine(self.username, self.host)

    def getReady(self):
        executeCommand(self.username, self.host, "mkdir logs")

    def startLogging(self):
        executeCommand(self.username, self.host, "rm -rf logs")
        executeCommand(self.username, self.host, "mkdir logs")
        executeCommand(self.username, self.host, "'dstat -ts -c -n -m --noheaders --nocolor 2 >> ~/logs/cpu_usage &'")

    def stopLogging(self):
        executeCommand(self.username, self.host, "pkill -9 dstat")
import pexpect
from Utilities import *

class Middleware:

    def __init__(self, username, host, databaseHost, databasePortNumber, databaseUsername,
                 databasePassword, databaseName, dataSourceName,
                 threadPoolSize, connectionPoolSize, middlewarePortNumber):

        self.username = username
        self.host = host
        self.databaseHost = databaseHost
        self.databasePortNumber = databasePortNumber
        self.databaseUsername = databaseUsername
        self.databasePassword = databasePassword
        self.databaseName = databaseName
        self.dataSourceName = dataSourceName
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
                      ("connectionPoolSize", self.connectionPoolSize), ("dataSourceName", self.dataSourceName),
                      ("middlewarePortNumber", self.middlewarePortNumber)]

        propertiesFileName = "middleware.properties"
        create_properties_file("/tmp/" + propertiesFileName, properties)

        # send properties file to the middleware machine
        scp_to("/tmp/" + propertiesFileName, self.username, self.host)

        # create properties file and send it to the host before starting the middleware
        child = pexpect.spawn("ssh " + ssh_address(self.username, self.host))
        child.expect("Last login:*")
        command = "java -jar mepas.jar middleware " + propertiesFileName + " 2>>middleware_errors.out"
        print command
        child.sendline(command)

        # wait until middleware starts
        child.expect("STARTED*")
        self.spawnedMiddleware = child

    def stop(self):
        self.spawnedMiddleware.sendline("STOP")

    def clean(self):
        clean_machine(self.username, self.host)

    def getReady(self):
        execute_command(self.username, self.host, "mkdir logs")
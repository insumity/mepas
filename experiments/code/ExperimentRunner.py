import os
import datetime
from os.path import isfile, isdir
from subprocess import call

from Database import Database
from Client import Client
from Middleware import Middleware
from EC2InstancesRetriever import *
from Utilities import *


# read configuration file
# conf = {}
# execfile("configuration.py")
# if not bool(conf):
# print "The given configuration seems empty"
# exit(1)

conf = \
    {"nameOfTheExperiment": "../trace100clientsWithMoreLogging10ClientsBARZ",

     "numberOfClientInstances": 1,
     "numberOfMiddlewareInstances": 1,

     "databaseUsername": "ubuntu",
     "databasePassword": "mepas$1$2$3$",
     "databaseName": "mepas",
     "databasePortNumber": 5432,

     "middlewarePortNumber": 6789,

     "runningTimeInSeconds": 60,

     "threadPoolSize": 10,
     "connectionPoolSize": 20,

     "totalClients": 100,
     "totalQueues": 100,

     # mapping between client instances and middleware instances
     # e.g. if (a, b) is in mapping it means that client[a] returned by
     # getClientsIPs() is going to connect to middleware[b] where b is
     # returned by middlewareIPs
     "mappings": [(0, 0)],  #, (1, 0), (2, 0), (3, 0)]
     "clientsData": [(100, 1)],

     "username": "ubuntu"
    }


# verify this experiment has not yet been created
if isdir(conf["nameOfTheExperiment"]):
    print "There exists already an experiment with the given name: " + conf["nameOfTheExperiment"]
    print "Please change the experiment name or delete the directory of the experiment"
    exit(1)

os.mkdir(conf["nameOfTheExperiment"])

jarFile = "../../mepas.jar"


# TODO : experiments names .. config file for running shit ...


access_key = "AKIAIV45ZYABLMV25HBQ"
secret_access = "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"
instancesRetriever = EC2InstancesRetriever(access_key, secret_access)

# you always need one database instance for every experiment
databaseIP = instancesRetriever.getDatabaseIP(1)
databaseHost = databaseIP[0][0]

clientIPs = instancesRetriever.getClientsIPs(conf["numberOfClientInstances"])
middlewareIPs = instancesRetriever.getMiddlewaresIPs(conf["numberOfMiddlewareInstances"])

print "IPs of machines that are going to be used"
print "Database"
print databaseIP[0][0] + ": " + databaseIP[0][2]
print "Clients"
for client in clientIPs:
    print client[0] + ": " + client[2]

print "Middlewares"
for middleware in middlewareIPs:
    print middleware[0] + ": " + middleware[2]


for variable in [25]:

    print "Doing it for: " + str(variable)

    # clean database

    auxiliaryFunctionsFilePath = "../../src/main/resources/auxiliary_functions.sql"
    basicFunctionsFilePath = "../../src/main/resources/read_committed_basic_functions.sql"

    print ">>> Going to clean and initialize database"
    database = Database(databaseHost, conf["databasePortNumber"], conf["databaseName"], conf["databaseUsername"],
                        conf["databasePassword"])
    database.recreateDatabase()
    database.initializeDatabase(conf["totalClients"], conf["totalQueues"],
                                [auxiliaryFunctionsFilePath, basicFunctionsFilePath])
    print ">>> Database was cleaned and initialized!"

    # clean the directory with ant and make the JAR
    call(["ant", "-buildfile", "../..", "clean"])
    call(["ant", "-buildfile", "../..", "jar"])
    if isfile("../mepas.jar"):
        print ">> executable JAR was created"

    # transfer the JAR to the clients & middlewares
    print ">>> transferring executable JAR to clients & middlewares"
    for client in clientIPs:
        scpTo(jarFile, "", conf["username"], client[0])
        print "JAR moved to client with IP: " + client[0]

    for middleware in middlewareIPs:
        scpTo(jarFile, "", conf["username"], middleware[0])
        print "JAR moved to middleware with IP: " + middleware[0]
    print ">>> executable JAR was transferred to the clients & middlewares"

    middlewareInstances = []
    for middlewareIP in middlewareIPs:
        middleware = Middleware(conf["username"], middlewareIP[0], databaseHost, conf["databasePortNumber"],
                                conf["databaseUsername"], conf["databasePassword"], conf["databaseName"],
                                str(conf["threadPoolSize"]), str(conf["connectionPoolSize"]),
                                str(conf["middlewarePortNumber"]))
        middlewareInstances.append(middleware)

    clientInstances = []
    i = 0
    for mapping in conf["mappings"]:
        privateIPOfCorrespondingMiddleware = middlewareIPs[mapping[1]][1]
        client = Client(conf["username"], clientIPs[mapping[0]][0], privateIPOfCorrespondingMiddleware,
                        str(conf["middlewarePortNumber"]), str(conf["clientsData"][i][0]),
                        str(conf["totalClients"]),
                        str(conf["totalQueues"]),
                        str(conf["clientsData"][i][1]),
                        str(conf["runningTimeInSeconds"]))
        clientInstances.append(client)

        i += 1

    # clean the clients & MW from old logs and get the ready for the current experiment
    # assumes file exists "~/logs" in the corresponding machines
    print ">>> going to clean clients ..."
    for client in clientInstances:
        client.clean()
        client.getReady()
    print ">>> clients were cleaned from previous experiments and are ready for the new ones"

    print ">>> going to clean middlewares ..."
    for middleware in middlewareInstances:
        middleware.clean()
        middleware.getReady()
    print ">>> middlewares were cleaned from previous experiments and are ready for the new ones"

    print ">>> middlewares are starting ..."
    for middleware in middlewareInstances:
        middleware.start()
    print ">>> middlewares were started"

    now = datetime.datetime.now()
    print ">>> clients are starting ... (" + str(now) + ")"
    for client in clientInstances:
        client.start()
    print ">>> clients were started"

    print ">>> waiting until all clients have finished ..."
    for client in clientInstances:
        while not client.isFinished():
            pass

    now = datetime.datetime.now()
    print ">>> clients have finished (" + str(now) + ")"

    print ">>> stopping middlewares ..."
    for middleware in middlewareInstances:
        middleware.stop()

    print ">>> middlewares were stopped"

    # create a directory for the point of the experiment
    experimentPointPath = createPath([conf["nameOfTheExperiment"]], str(variable))
    os.mkdir(experimentPointPath)

    # gather results and put them back somewhere locally
    print ">>> getting log files from middlewares ..."
    instanceCounter = 1
    for middleware in middlewareIPs:
        localDirectoryResults = experimentPointPath + "/middlewareInstance" + str(instanceCounter)
        os.mkdir(localDirectoryResults)
        scpFrom("logs/*", localDirectoryResults, conf["username"], middleware[0])
        scpFrom("cpu_usage", localDirectoryResults, conf["username"], middleware[0])
    instanceCounter += 1

    print ">>> getting log files from clients ..."
    instanceCounter = 1
    for client in clientIPs:
        localDirectoryResults = experimentPointPath + "/clientInstance" + str(instanceCounter)
        os.mkdir(localDirectoryResults)
        scpFrom("logs/*", localDirectoryResults, conf["username"], client[0])
        scpFrom("cpu_usage", localDirectoryResults, conf["username"], client[0])
    instanceCounter += 1
    print ">>> log files from clients were received"
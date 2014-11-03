import os
import datetime
from os.path import isfile, isdir
from subprocess import call

from Database import Database
from Client import Client
from Middleware import Middleware
from EC2InstancesRetriever import *
from Utilities import *

#
# # read configuration file
# conf = {}
# execfile("configuration.py")
# if not bool(conf):
#     print "The given configuration seems empty"
# exit(1)
"""
For this experiment based on the trace ... I expect the following:
middleware usage seems to be around 50%, so I would presume a middleware
can handle up to 200 clients before it cannot handle more. But in such
a case the database won't be able to handle the system.
Client instances are not really utilizing the CPU so I would presume
I can put up to 200 clients in one instance or even much more.
So I would guess around 150 clients the database is not going to be
able to handle them and throughput will not increase ...

"""
conf = \
    {"nameOfTheExperiment": "../demo2k20MWThreads20Connections1MWSmallDBxLarge",


     "numberOfClientInstances": 1,
     "numberOfMiddlewareInstances": 1,

     "databaseUsername": "ubuntu",
     "databasePassword": "mepas$1$2$3$",
     "databaseName": "mepas",
     "databasePortNumber": 5432,

     "middlewarePortNumber": 6789,

     "runningTimeInSeconds": 600,

     "threadPoolSize": 20,
     "connectionPoolSize": 20,

     "totalClients": 50,
     "totalQueues": 50,

     # mapping between client instances and middleware instances
     # e.g. if (a, b) is in mapping it means that client[a] returned by
     # getClientsIPs() is going to connect to middleware[b] where b is
     # returned by middlewareIPs
     "mappings": [(0, 0)], #, (1, 0)], # (2, 1), (3, 1)],
     "clientsData": [(50, 1)], #, (50, 51)], # (25, 51), (25, 76)],

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


for variable in [1]:

 #   conf["totalClients"] = variable
#    conf["clientsData"][0][0] = conf["totalClients"] # TODO

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

    print ">>> Starting CPU, memory and network utilization logging in database"
    database.startLogging(conf["username"]) # start logging CPU, memory and network utilization
    print ">>> Database logging started"

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
        middleware.startLogging()
        middleware.start()
    print ">>> middlewares were started"

    now = datetime.datetime.now()
    print ">>> clients are starting ... (" + str(now) + ")"
    for client in clientInstances:
        client.startLogging()
        client.start()
    print ">>> clients were started"

    print ">>> waiting until all clients have finished ..."
    for client in clientInstances:
        while not client.isFinished():
            pass
        client.stopLogging()

    now = datetime.datetime.now()
    print ">>> clients have finished (" + str(now) + ")"

    print ">>> stopping middlewares ..."
    for middleware in middlewareInstances:
        middleware.stop()
        middleware.stopLogging()

    print ">>> middlewares were stopped"

    database.stopLogging(conf["username"])

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
        instanceCounter += 1
    print ">>> log files from middlewares received"

    print ">>> getting log files from clients ..."
    instanceCounter = 1
    for client in clientIPs:
        localDirectoryResults = experimentPointPath + "/clientInstance" + str(instanceCounter)
        os.mkdir(localDirectoryResults)
        scpFrom("logs/*", localDirectoryResults, conf["username"], client[0])
        instanceCounter += 1
    print ">>> log files from clients were received"

    print ">>> getting log files from database ..."
    localDirectoryResults = experimentPointPath + "/database"
    os.mkdir(localDirectoryResults)
    scpFrom("logs/*", localDirectoryResults, conf["username"], databaseHost)
    print ">>> log files from database received"
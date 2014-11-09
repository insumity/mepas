import csv
import os
import datetime
from os.path import isfile, isdir
from subprocess import call

from Database import Database
from Client import Client
from Middleware import Middleware
from EC2Instantiator import *
from Utilities import *

conf = \
    {"nameOfTheExperiment": "../just_an_example",
     "placement": "us-west-2c",

     "databaseType": "m3.large",

     "clientInstances": (1, "m3.large"),
     "middlewareInstances": (1, "m3.large"),

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
     "messageSize": 20,

     # mapping between client instances and middleware instances
     # e.g. if (a, b) is in mapping it means that client[a] returned by
     # getClientsIPs() is going to connect to middleware[b] where b is
     # returned by middlewareIPs
     "mappings": [(0, 0)],  # , (1, 0)], # (2, 1), (3, 1)],
     "clientsData": [(50, 1)],  #, (50, 51)], # (25, 51), (25, 76)],

     "username": "ubuntu",

     "variable": "threadPoolSize",
     "values": [10]
     # "values": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
     #            29, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100]
    }



# verify this experiment has not yet been created
if isdir(conf["nameOfTheExperiment"]):
    print "There exists already an experiment with the given name: " + conf["nameOfTheExperiment"]
    print "Please change the experiment name or delete the directory of the experiment"
    exit(1)

os.mkdir(conf["nameOfTheExperiment"])

jarFile = "../../mepas.jar"

# clean the directory with ant and make the JAR
call(["ant", "-buildfile", "../..", "clean"])
call(["ant", "-buildfile", "../..", "jar"])
if isfile("../mepas.jar"):
    print ">> executable JAR was created"

access_key = "AKIAIV45ZYABLMV25HBQ"
secret_access = "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"
instancesRetriever = EC2Instantiator(access_key, secret_access, conf["placement"])

for variable in conf["values"]:

    conf[conf["variable"]] = variable

    # you always need one database instance for every experiment
    database = instancesRetriever.createDatabase(conf["databaseType"])

    numberOfClientInstances = conf["clientInstances"][0]
    clientType = conf["clientInstances"][1]

    clientIPs = []
    clients = []
    for i in range(0, numberOfClientInstances):
        inst = instancesRetriever.createClient(clientType)
        clients.append(inst)
        clientIPs.append((inst.ip_address, inst.private_ip_address, inst.instance_type))

    numberOfMiddlewareInstances = conf["middlewareInstances"][0]
    middlewareType = conf["middlewareInstances"][1]

    middlewareIPs = []
    middlewares = []
    for i in range(0, numberOfMiddlewareInstances):
        inst = instancesRetriever.createMiddleware(middlewareType)
        middlewares.append(inst)
        middlewareIPs.append((inst.ip_address, inst.private_ip_address, inst.instance_type))

    databaseIP = (database.ip_address, database.private_ip_address)

    print "IPs of machines that are going to be used"
    print "Database"
    print databaseIP[0] + ": " + databaseIP[1] + ":" + database.instance_type
    print "Clients"
    for client in clientIPs:
        print client[0] + ": " + client[2]

    print "Middlewares"
    for middleware in middlewareIPs:
        print middleware[0] + ": " + middleware[2]


    # clean database

    auxiliaryFunctionsFilePath = "../../src/main/resources/auxiliary_functions.sql"
    basicFunctionsFilePath = "../../src/main/resources/read_committed_basic_functions.sql"

    print ">>> Going to clean and initialize database"
    db = Database(databaseIP[0], conf["databasePortNumber"], conf["databaseName"], conf["databaseUsername"],
                  conf["databasePassword"])
    db.recreateDatabase()
    db.initializeDatabase(conf["totalClients"], conf["totalQueues"],
                          [auxiliaryFunctionsFilePath, basicFunctionsFilePath])
    print ">>> Database was cleaned and initialized!"

    print ">>> Starting CPU, memory and network utilization logging in database"
    db.startLogging(conf["username"])  # start logging CPU, memory and network utilization
    print ">>> Database logging started"

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
        middleware = Middleware(conf["username"], middlewareIP[0], databaseIP[1], conf["databasePortNumber"],
                                conf["databaseUsername"], conf["databasePassword"], conf["databaseName"],
                                str(conf["threadPoolSize"]), str(conf["connectionPoolSize"]),
                                str(conf["middlewarePortNumber"]))
        print middleware
        middlewareInstances.append(middleware)

    clientInstances = []
    i = 0
    for mapping in conf["mappings"]:
        privateIPOfCorrespondingMiddleware = middlewareIPs[mapping[1]][1]
        client = Client(conf["username"], clientIPs[mapping[0]][0], privateIPOfCorrespondingMiddleware,
                        str(conf["middlewarePortNumber"]), str(conf["clientsData"][i][0]),
                        str(conf["totalClients"]),
                        str(conf["totalQueues"]),
                        str(conf["messageSize"]),
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

    db.stopLogging(conf["username"])

    # create a directory for the point of the experiment
    experimentPointPath = createPath([conf["nameOfTheExperiment"]], str(variable))
    os.mkdir(experimentPointPath)

    # save given configuration in the experimentPointPath
    confFile = open(createPath([experimentPointPath], "configuration.csv"), "w")
    w = csv.writer(confFile)
    for key, val in conf.items():
        w.writerow([key, val])
        confFile.flush()

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
    scpFrom("logs/*", localDirectoryResults, conf["username"], databaseIP[0])
    print ">>> log files from database received"

    instancesRetriever.terminateInstance(database)
    for client in clients:
        instancesRetriever.terminateInstance(client)

    for middleware in middlewares:
        instancesRetriever.terminateInstance(middleware)
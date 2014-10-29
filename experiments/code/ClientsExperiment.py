import os
import datetime
from os.path import isfile, isdir
from subprocess import call

from Database import Database
from Client import Client
from Middleware import Middleware
from EC2InstancesRetriever import *
from Utilities import *


os.mkdir(conf["nameOfTheExperiment"])

jarFile = "../../mepas.jar"
username = "ubuntu"

middlewarePortNumber = 6789
startingId = 1
databasePortNumber = 5432

runningTimeInSeconds = 60

numberOfQueues = conf["numberOfQueues"]

# TODO : experiments names .. config file for running shit ...

# mapping between client instances and middleware instances
# e.g. if (a, b) is in mapping it means that client[a] returned by
# getClientsIPs() is going to connect to middleware[b] where b is
# returned by middlewareIPs
mappings = [(0, 0)]

instancesRetriever = EC2InstancesRetriever()
clientIPs = instancesRetriever.getClientsIPs(conf["numberOfClientInstances"])
middlewareIPs = instancesRetriever.getMiddlewaresIPs(conf["numberOfMiddlewareInstances"])

numberOfClients = [25]
for variableValue in numberOfClients:

    numberOfThreads = variableValue

    print "Doing it for: " + str(variableValue)

    # clean the directory with ant
    call(["ant", "-buildfile", "../..", "clean"])

    # create the jar
    call(["ant", "-buildfile", "../..", "jar"])
    if isfile("../mepas.jar"):
        print ">> executable JAR was created"

    # transfer the jar to the clients & middlewares
    print ">>> transferring executable JAR to clients & middlewares"
    scpTo(jarFile, "", username, client[0])
    scpTo(jarFile, "", username, middleware[0])
    print ">>> executable JAR was transferred to the clients & middlewares"

    middlewareInstances = []
    for middlewareIP in middlewareIPs:
        middleware = Middleware(username, middlewareIP[0], databaseHost, databasePortNumber, databaseUsername,
                                databasePassword,
                                databaseName, str(numberOfThreads), str(numberOfConnectionsToDb),
                                str(middlewarePortNumber))
        middlewareInstances.append(middleware)

    clientInstances = []
    i = 0
    for mapping in mappings:
        privateIPOfCorrespondingMiddleware = middlewareIPs[mapping[1]][1]
        client = Client(username, clientIPs[mapping[0]][0], privateIPOfCorrespondingMiddleware,
                        str(middlewarePortNumber), str(clientsData[i][0]),
                        str(numberOfClients),
                        str(clientsData[i][1]),
                        str(runningTimeInSeconds))
        clientInstances.append(client)

        i += 1

    # clean the clients & MW from possible logs (?)
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

    # create a directory for the experiment
    # TODO ... inform if the directory already exists and if so put
    # it somewhere else
    experimentName = conf["nameOfTheExperiment"] + "/" + str(variableValue)
    if isdir(experimentName):
        print "This directory already exists: " + experimentName
        exit(1)
    os.mkdir(experimentName)

    # gather results and put them back somewhere locally
    print ">>> getting log files from middlewares ..."
    instanceCounter = 1
    for middleware in middlewareIPs:
        localDirectoryResults = experimentName + "/middlewareInstance" + str(instanceCounter)
        os.mkdir(localDirectoryResults)
        scpFrom("logs/*", localDirectoryResults, username, middleware[0])
        instanceCounter += 1

    print ">>> getting log files from clients ..."
    instanceCounter = 1
    for client in clientIPs:
        localDirectoryResults = experimentName + "/clientInstance" + str(instanceCounter)
        os.mkdir(localDirectoryResults)
        scpFrom("logs/*", localDirectoryResults, username, client[0])
        instanceCounter += 1
    print ">>> log files from clients were received"
    # clean clients and MW and verify it's cleaned

    # profit!
    # get_data(possibleValues)  # will create a file
    # plot_data(nameOfTheExperiment + "/plot_data.csv", 210, "someExperiment.png", "1:2:3")
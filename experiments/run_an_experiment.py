import os
from os.path import isfile
from time import sleep

from plot_data import plot_data
from retrieve_ec2_instances import *
from clean_database import *
from utilities import *
from read_experimental_results import get_data

# FIXME ... sto client.out exo >> eno sto server.out exo >

nameOfTheExperiment = "someExperiment"
# verify this experiment has not yet been created

os.mkdir(nameOfTheExperiment)

# TODO : experiments names .. config file for running shitt ...
# TODO For java also use configuration file, probably better than arguments

possibleValues = [10]
for totalClients in possibleValues:

    print "Doing it for totalClients: " + str(totalClients)
    # clean database
    databases = get_databases()
    dbHost = databases[0][0]
    dbUsername = "ubuntu"
    dbPassword = "mepas$1$2$3$"
    dbName = "mepas"

    print dbHost
    numberOfQueues = 1

    recreate_database(dbHost, dbName, dbUsername, dbPassword)

    auxiliaryFunctionsFilePath = "../src/main/resources/auxiliary_functions.sql"
    basicFunctionsFilePath = "../src/main/resources/read_committed_basic_functions.sql"
    initialize_database(dbHost, dbName, dbUsername, dbPassword, totalClients, numberOfQueues,
                        auxiliaryFunctionsFilePath,
                        basicFunctionsFilePath)
    print ">>> Database was cleaned and initialized!"


    # clean the directory with ant
    call(["ant", "-buildfile", "..", "clean"])

    # create the jar
    call(["ant", "-buildfile", "..", "jar"])
    if isfile("../mepas.jar"):
        print ">> executable JAR was created"

    privateKeyFile = "/Users/bandwitch/Desktop/mepas.pem"
    file = "../mepas.jar"
    username = "ubuntu"

    # transfer the jar to the clients & middlewares
    for client in get_clients():
        scp_to(file, username, client[0], privateKeyFile)

    for middleware in get_middlewares():
        scp_to(file, username, middleware[0], privateKeyFile)
    print ">>> executable JAR was moved to the clients & middlewares"


    # clean the clients & MW from possible logs (?)
    # assumes file exists "~/logs" in the corresponding machines
    for client in get_clients():
        clean_machine(username, client[0], privateKeyFile)
        execute_command(username, client[0], privateKeyFile, "mkdir logs")

    for middleware in get_middlewares():
        clean_machine(username, middleware[0], privateKeyFile)
        execute_command(username, middleware[0], privateKeyFile, "mkdir logs")

    print ">>> clients & middlewares were cleaned from previous experiments"

    # FIXME ... ERROR MESSAGES ARE SAVED in the local computer
    # start the MW
    numberOfThreads = 50
    numberOfConnectionsToDb = 50
    for middleware in get_middlewares():
        start_machine(username, middleware[0], privateKeyFile, dbHost, dbUsername, dbPassword,
                      dbName, str(6789), str(numberOfThreads), str(numberOfConnectionsToDb))
        sleep(10)  # FIXME ... wait to make sure the middleware started working
    print ">>> middlewares were started"

    # verify they started

    # start the clients
    mwHost = get_middlewares()[0][0]
    mwPort = 6789
    startingId = 1
    runningTime = 1

    for client in get_clients():
        start_client(username, client[0], privateKeyFile, mwHost, str(mwPort), str(totalClients), str(startingId),
                     str(runningTime))
    print ">>> clients were started"

    # verify they started

    # wait until clients finish fIXME
    sleep(60 + 30)  # for 1 minutes + 20seconds more to be sure FIXME
    for client in get_clients():
        execute_command(username, client[0], privateKeyFile, "killall java")

    print ">>> clients have finished"

    # gracefully close MW
    # send a message to the MW ... for starters just kill it FIXME
    for middleware in get_middlewares():
        execute_command(username, middleware[0], privateKeyFile, "killall java")

    print ">> middlewares were closed"

    # make sure machine stopped ...
    sleep(10)

    # create a directory for the experiment
    # TODO ... inform if the directory already existsa and if so put
    # it somewhere else
    experimentName = nameOfTheExperiment + "/experiment_10dbconnections_10threads_" + str(totalClients) + "clients"
    os.mkdir(experimentName)

    # gather results and put them back somewhere locally
    # for middleware in get_middlewares()
    # scp_from("logs/*", experimentName + "/", username, middleware[0], privateKeyFile)

    for client in get_clients():
        scp_from("logs/*", experimentName + "/", username, client[0], privateKeyFile)


        # clean clients and MW and verify it's cleaned

# profit!
get_data(possibleValues)  # will create a file
plot_data(nameOfTheExperiment + "/plot_data.csv", 210, "someExperiment.png", "1:2:3")


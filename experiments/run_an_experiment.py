from retrieve_ec2_instances import *
from clean_database import *
from subprocess import call
from os.path import isfile
from os import system
from utilities import clean_machine, scp_to, scp_from, start_machine, start_client, execute_command
from time import sleep

# clean database
databases = get_databases()
host = databases[0][0]
username = "ubuntu"
password = "mepas$1$2$3$"
dbname = "mepas"

#recreate_database(host, dbname, username, password)
#initialize_database(host, dbname, username, password, 5, 5, "../src/main/resources/auxiliary_functions.sql")

# clean the directory with ant
#call(["ant", "-buildfile", "..", "clean"])

# create the jar
#call(["ant", "-buildfile", "..", "jar"])
#if isfile("../mepas.jar"):
#    print "It was created"


privateKeyFile = "/Users/bandwitch/Desktop/mepas.pem"
file = "../mepas.jar"

# transfer the jar to the clients & middlewares
for client in get_clients():
    scp_to(file, username, client[0], privateKeyFile)

for middleware in get_middlewares():
    scp_to(file, username, middleware[0], privateKeyFile)


# clean the clients & MW from possible logs (?)
# assumes file exists "~/logs" in the corresponding machines
for client in get_clients():
    clean_machine(username, client[0], privateKeyFile)

for middleware in get_middlewares():
    clean_machine(username, middleware[0], privateKeyFile)


# start the MW
for middleware in get_middlewares():
    start_machine(username, middleware[0], privateKeyFile)
    sleep(10) # FIXME ... wait to make sure the middleware started working

# verify they started

# start the clients
for client in get_clients():
    start_client(username, clients[0], privateKeyFile)

# verify they started

# wait until clients finish fIXME
sleep(600)

# gracefully close MW
# send a message to the MW ... for starters just kill it FIXME
for middleware in get_middlewares():
    execute_command(username, middleware[0], privateKeyFile, "killall java")

# make sure machine stopped ...
sleep(10)

# create a directory for the experiment
experimentName = "responseTimeWithUsers"
system("mkdir " + experimentName)

# gather results and put them back somewhere locally
for middleware in get_middlewares():
    scp_from("logs/*", experimentName + "/", username, host, privateKeyFile)

for client in get_clients():
    scp_from("logs/*", experimentName + "/", username, host, privateKeyFile)


# clean clients and MW and verify it's cleaned

# profit!


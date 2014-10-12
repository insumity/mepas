#!/bin/bash

######################################################
# PARAMETERS THA NEED TO BE GIVEN FOR THE EXPERIMENT #
# given by the "total" configuration file            #
######################################################

#
# >>> Automation script for experiments <<<
#
# 1. Checks that login to server, client and db machine is working
# 2. Copies jar file to server, client and db machines
# 3. Runs db to make sure database is a needed state before starting the experiment
# 4. Runs server and waits for it to start listening to connections
# 5. Starts clients on client machines
# 6. Waits for clients to finish
# 7. Copies log files from server and client machines
# 8. Deletes log files from client and server machines 
# 9. Cleans database

function usage() {
    local programName=$1
    echo "Usage: $programName --configurationFile=<file>"
    exit -1
}


# Extract command line arguments
TEMP=`getopt -o b: --long configurationFile: \
     -n 'example.bash' -- "$@"`

if [ $? != 0 ] ; then echo "Terminating ... " >&2 ; exit 1 ; fi

# Note the quotes around `$TEMP': they are essential!
eval set -- "$TEMP"

while true ; do
    case "$1" in
        --configurationFile) configurationFile="$2" ; shift 2 ;;
        --) shift ; break ;;
        *) echo "Internal error!" ; exit 1 ;;
    esac
done

# Check for correctness of the commandline arguments
if [[ $configurationFile == "" ]]
then
    usage $1
    exit -1
fi

source $configurationFile

#######################################
# Test that all instances are running #
#######################################

# first argument corresponds to the type (server, client, database)
# second to the machine address
function testConnection()
{
    echo -ne "Testing connection to the $1 machine ... "
    # Check if command can be run on client
    success=$( ssh -i $privateKey $remoteUserName@$2 echo ok 2>&1 )
    if [[ $success != "ok" ]]
    then
	    echo -e "\n>> Login not successful for $remoteUserName on $2. Exiting ..."
	    exit -1
    fi

    echo "SUCCESS"
}

if [[ $testConnection == "yes" ]]
then
    testConnection "server" $serverMachine
    testConnection "client" $clientMachine
    testConnection "database" $dbMachine
    echo "=========="
fi


# in case database needs to be initialized (users & queues to be added)
if [[ $initDatabase == "yes" ]]
then
    # in order for the following command to run .pgpass should be added, look at the SQL .txt file for more info
    psql -U postgres -d message -h $dbMachine -v initNumberOfClients=$initDBClients -v initNumberOfQueues=$initDBQueues -f clear_and_restore_database.sql 1>/dev/null 2>&1

    echo ">> Database was initialized!"
fi



# TODO possibly clean database with a simple python script

function copyJarFile()
{
    echo -ne "Copying $jarName to $1 machine: $2 ... "
    scp -i $privateKey $jarPath $remoteUserName@$2:/tmp 1>/dev/null
    echo "SUCCESS"
}

if [[ $copyJar == "yes" ]]
then
    copyJarFile "server" $serverMachine
    copyJarFile "client" $clientMachine
    echo "=========="
fi



# Run server
# e.g. java -jar message.jar server 4444 10 10 message localhost monra monra123
ssh -i $privateKey $remoteUserName@$serverMachine "java -jar /tmp/$jarName server $port" \
    "$numberOfThreads $numberOfDbConnections $dbName $dbPrivateIP $dbUsername $dbPassword"\
    " 1>/tmp/server.out 2>>/tmp/server_errors.out" &

# Wait for the server to start up
echo -ne "Starting the server ... "
sleep 1
while [ `ssh -i $privateKey $remoteUserName@$serverMachine "cat /tmp/server.out | grep 'Server listening' | wc -l"` != 1 ]
do
    sleep 1
done

echo "SUCCESS"
echo "=========="

if [[ $mode == "oneWay" ]]
then
    str=""
    echo ">> Starting clients ... (wait till they finish) "
    clientIds=`seq $numberOfClients`
    for clientId in $clientIds
    do
	# running ssh here for clientIds time was creating connectivity issues
	# send all the commands at once to the machine and execute them there one by one
	tmp="java -jar /tmp/${jarName} client oneWay ${serverPrivateIP} ${port} ${clientId} ${numberOfClients} ${requestsPerSecond} ${runningTimeInSeconds} ${messageSize} 1>>/tmp/clients.out 2>>/tmp/clients_errors.out & "
	str=$str$tmp
    done

    ending="wait"
    str=$str$ending

    date
    ssh -i $privateKey $remoteUserName@$clientMachine $str
    date

    echo ">> $numberOfClients clients finished"
fi

echo "=========="


echo "Sending shut down signal to server ..."
# Send a shut down signal to the server
# Note: server.jar catches SIGHUP signals and terminates gracefully
ssh -i $privateKey $remoteUserName@$serverMachine "killall java"

echo -ne "Waiting for the server to shut down ... "
# Wait for the server to gracefully shut down
while [ `ssh -i $privateKey $remoteUserName@$serverMachine "cat /tmp/server.out | grep 'Server shutting down' | wc -l"` != 1 ]
do
    sleep 1
done

echo "SUCCESS"

echo "=========="

# Copy log files from the clients
mkdir -p $experimentId
echo -ne "Copying log files from client machine ... "

# first put the files in a tar so you don't issue a scp command
# for every individual file
ssh -i $privateKey $remoteUserName@$clientMachine "tar -cvf /tmp/client_output.tar /tmp/client*" 1>/dev/null 2>&1
scp -i $privateKey $remoteUserName@$clientMachine:/tmp/client_output.tar ./$experimentId/ 1>/dev/null 
scp -i $privateKey $remoteUserName@$serverMachine:/tmp/server* ./$experimentId/ 1>/dev/null 
scp -i $privateKey $remoteUserName@$clientMachine:/tmp/clients* ./$experimentId/ 1>/dev/null


echo "SUCCESS"

# Cleanup
echo -ne "Cleaning up files on client and server machines ... "
ssh -i $privateKey $remoteUserName@$clientMachine "rm /tmp/client*" 1>/dev/null
ssh -i $privateKey $remoteUserName@$serverMachine "rm /tmp/server*" 1>/dev/null
echo "SUCCESS"

if [[ $cleanDatabase == "yes" ]]
then
    echo -ne "Cleaning up the database ..."
    psql -U postgres -d message -h $dbMachine -f clean_database.sql 1>/dev/null 2>&1
    echo "SUCCESS"
fi


# Process the log files from the clients
echo -ne "Processing log files ... "
# untar client files
tar -xvf $experimentId/client_output.tar 1>/dev/null 2>&1
mv tmp/client* $experimentId 1>/dev/null
rmdir tmp 1>/dev/null
rm -f $experimentId/client_output.tar 1>/dev/null
echo "SUCCESS"


# remove error files if they are empty
if ! [[ -s ./$experimentId/clients_errors.out ]]
then
    rm ./$experimentId/clients_errors.out
fi

if ! [[ -s ./$experimentId/server_errors.out ]]
then
    rm ./$experimentId/server_errors.out
fi


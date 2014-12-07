import commands
from os import listdir, system
import os
from os.path import isfile, join
import math
import shutil
import tempfile

# calulates results tha are used by the trace
import sys


def getTrace(experimentName, clientInstances, totalTimeInSeconds, typeOfMessage):
    # merge all files in a big one and sort them by time
    # merge all files in one instance
    for i in range(1, clientInstances + 1):
        directory = experimentName + "/clientInstance" + str(i)

        # found in http://stackoverflow.com/questions/10103619/unix-merge-many-files-while-deleting-first-line-of-all-files
        # remove the first line of every file before merging them
        mergeFilesAndRemoveFirstLineOfEach = "find " + directory + " -name \"*.csv\" | xargs -n 1 tail -n +2 " \
                                                                   " | " + "grep -E '(" + typeOfMessage + ")'"

        command = mergeFilesAndRemoveFirstLineOfEach + " > " + directory + "/all_clients_of_this_instance.csv"
        system(command)

    # merge all_clients_of_this_instance.csv files from all the instances
    directory = experimentName + "/*"
    command = "cat " + directory + "/all_clients_of_this_instance.csv > " + experimentName + "/all_clients.csv"
    system(command)

    # sort generated file by time
    system("sort -k" + str(1) + " -n " + experimentName + "/all_clients.csv -o " + experimentName + "/all_clients.csv")

    intervalWindowInMilliseconds = 60 * 1000
    totalTimeInMilliseconds = totalTimeInSeconds * 1000

    minutes = 1
    for i in range(0, totalTimeInMilliseconds, intervalWindowInMilliseconds + 1):
        fileName = experimentName + "/all_clients.csv"

        command = "awk -F'\t' '$1 >=" + str(i) + " && $1 < " + str(
            (i + intervalWindowInMilliseconds)) + " { print; }' " + fileName
        getAverageAndStd = "awk -F'\t' '{ sum += $2; n++; sumsq += $2 * $2 } END { if (n > 0) printf \"%d %f %f\", " \
                           + str(minutes) + ", (sum / n), sqrt(sumsq/n - (sum/n)**2); }'"
        system(command + " | " + getAverageAndStd)


        # average throughput every second in order to get standard deviation
        times = 0
        sum = 0
        sdeviation = 0
        for j in range(i, i + 60 * 1000, 1000):
            command = "awk -F'\t' '$1 >=" + str(j) + " && $1 < " + str(
            (j + 1000)) + " { print; }' " + fileName

            output = commands.getstatusoutput(command + " | " + "wc")

            # extract result from wc command
            numberOfRequests = int(" ".join(output[1].split()).split(' ', 1)[0])
            sum += numberOfRequests
            sdeviation += (numberOfRequests * numberOfRequests)
            times += 1


        sdeviation = math.sqrt(sdeviation / times - ((sum / times) * (sum / times)))
        sys.stdout.write(" " + str(sum / times) + " " + str(sdeviation) + "\n")
        sys.stdout.flush()

        minutes += 1

# calculates and returns response time
def getResponseTime(experimentDir, clientInstances, timeInSeconds, warmUpInSeconds, coolDownInSeconds,
                    typeOfMessage):
    # files could be quite big, use wc instead of reading them
    # every line in a client file contains a successful request

    directoryForTempResults = tempfile.mkdtemp()
    # print directoryForTempResults

    cleanedUpFiles = []  # files with removed warm up and cool down phases
    for i in range(1, clientInstances + 1):
        files = []
        files.extend([f for f in listdir(experimentDir + "/clientInstance" + str(i))])

        for f in files:

            if f == "cpu_usage" or f == "client_errors.out" or f == "all_clients_of_this_instance.csv":
                continue

            specificFile = experimentDir + "/clientInstance" + str(i) + "/" + f

            lastTimeInMilliseconds = timeInSeconds * 1000
            command = "awk -F'\t' '$1 >= " + str(warmUpInSeconds * 1000) + " && $1 <= " + \
                      str(lastTimeInMilliseconds - coolDownInSeconds * 1000) + " { print; }' " + specificFile

            system(command + " > " + directoryForTempResults + "/" + f)
            cleanedUpFiles.append(directoryForTempResults + "/" + f)

    tmpAllFiles = directoryForTempResults + "/tmp_all_files.csv"
    for f in cleanedUpFiles:
        system("cat " + f + " >> " + tmpAllFiles)

    allFiles = directoryForTempResults + "/all_files.csv"
    system("grep -E '(" + typeOfMessage + ")' " + tmpAllFiles + ">" + allFiles)

    result = os.popen("cat " + allFiles + " | " + "awk -F'\\t' '{ sum += $2; sumsq += $2 * $2; n++ } END { if (n > 0) "
                                                  "print sum / n, sqrt(sumsq/n- (sum/n)**2); }' ").read()
    shutil.rmtree(directoryForTempResults)
    return (result.split()[0], result.split()[1])

# calculates and returns throughput
def getThroughput(experimentDir, clientInstances, timeInSeconds, warmUpInSeconds, coolDownInSeconds, typeOfMessage):
    # files could be quite big, use wc instead of reading them
    # every line in a client file contains a successful request

    directoryForTempResults = tempfile.mkdtemp()
    # print directoryForTempResults

    cleanedUpFiles = []  # files with removed warm up and cool down phases
    for i in range(1, clientInstances + 1):
        files = []
        files.extend([f for f in listdir(experimentDir + "/clientInstance" + str(i))])

        for f in files:

            if f == "cpu_usage" or f == "client_errors.out" or f == "all_clients_of_this_instance.csv":
                continue

            specificFile = experimentDir + "/clientInstance" + str(i) + "/" + f

            lastTimeInMilliseconds = timeInSeconds * 1000
            command = "awk -F'\t' '$1 >= " + str(warmUpInSeconds * 1000) + " && $1 <= " + \
                      str(lastTimeInMilliseconds - coolDownInSeconds * 1000) + " { print; }' " + specificFile

            system(command + " | grep -E '(" + typeOfMessage + ")' " + " > " + directoryForTempResults + "/" + f)
            cleanedUpFiles.append(directoryForTempResults + "/" + f)

    # break a file into 60seconds chunks and get the average througput in that chunk
    windowInSeconds = 20
    intervalWindowInMilliseconds = windowInSeconds * 1000
    totalTimeInMilliseconds = timeInSeconds * 1000

    requestsPerWindow = {}
    for f in cleanedUpFiles:
        # print f
        for i in range(warmUpInSeconds * 1000, totalTimeInMilliseconds - coolDownInSeconds * 1000,
                       intervalWindowInMilliseconds + 1):

            command = "awk -F'\t' '$1 >=" + str(i) + " && $1 < " + str(
                (i + intervalWindowInMilliseconds)) + " { print; }' " + f

            # lock.acquire()
            output = commands.getstatusoutput(command + " | " + "wc")  # getAverageThroughput)
            # lock.release()
            numberOfRequests = (int(" ".join(output[1].split()).split(' ', 1)[0])) / float(windowInSeconds)
            if not i in requestsPerWindow:
                requestsPerWindow[i] = []

            requestsPerWindow[i].append(numberOfRequests)

    for window in requestsPerWindow:
        iSum = 0
        for i in requestsPerWindow[window]:
            iSum += i
        requestsPerWindow[window] = iSum

    sum = 0
    sumq = 0.0
    for window in requestsPerWindow:
        sum += requestsPerWindow[window]
        sumq += (requestsPerWindow[window] ** 2)

    size = len(requestsPerWindow)
    average = str(sum / float(size))
    # print average, sumq, sum
    std = str(math.sqrt((sumq / size) - (sum / size) ** 2))

    shutil.rmtree(directoryForTempResults)
    return average, std

# returns average cpu usage of the system and standard deviation
# startTime and endTime are string of this format "08-11 21:02:23"
def getCPUUsage(filePathOfUsage, startTime, endTime):
    result = os.popen(
        "awk -F'|' '{ if ($1 > \"" + startTime + "\" && $1 < \"" + endTime + "\") print $3 }' " + filePathOfUsage +
        " | awk -F' ' '{ sum += ($1 + $2); sumq += ($1 + $2) * ($1 + $2); n++; } END { print sum /n, sqrt(sumq/n- (sum/n)**2); }'").read();

    return result.split()[0], result.split()[1]

# returns average and standard deviation
def getAverageAndSd(middlewareInstanceDir, what, column):
    avg = os.popen("grep -h '" + what + "' " + middlewareInstanceDir + "/*" + " | " +
                   "awk -F'\\t' '{ sum += $" + column + "; n++ } END { if (n > 0) print sum / n; }' ").read()
    sd = os.popen("grep -h '" + what + "' " + middlewareInstanceDir + "/*" + " | " +
                  "awk -F'\\t' '{sum+=$" + column + "; n++; sumsq+=$" + column + "*$" + column + "} END {print sqrt(sumsq/n- (sum/n)**2)}' ").read()

    return avg.rstrip(), sd.rstrip()

# calculates time middleware spent on each component
def getTimeSpentOnEachComponent(middlewareInstanceDir, percentageToRemove):
    files = [f for f in listdir(middlewareInstanceDir)]

    directoryForTempResults = tempfile.mkdtemp()
    print directoryForTempResults

    for f in files:

        if f == "cpu_usage" or f == "middleware_errors.out":
            continue

        specificFile = middlewareInstanceDir + "/" + f

        # delete 10% of top and 10% bottom of the file
        num_lines = sum(1 for line in open(specificFile))

        percentageInLines = int(math.floor(num_lines * percentageToRemove))

        # delete first 10% of the lines
        #
        # lastTimeInMilliseconds = timeInSeconds * 1000
        # command = "awk -F'\t' '$1 >= " + str(warmUpInSeconds * 1000) + " && $1 <= " + \
        # str(lastTimeInMilliseconds - coolDownInSeconds * 1000) + " { print; }' " + specificFile

        # system(command + " > " + directoryForTempResults + "/" + f)

        system("sed -e '1," + str(
            percentageInLines) + "d' " + specificFile + " > " + directoryForTempResults + "/" + f)
        num_lines = sum(1 for line in open(directoryForTempResults + "/" + f))

        # delete last 10% of the lines
        system("sed -i '' -e '" + str(num_lines - percentageInLines) + "," + str(
            num_lines) + "d' " + directoryForTempResults + "/" + f)

    (avg, sd) = getAverageAndSd(directoryForTempResults, "GOT CONNECTION", "2")
    print "CONNECTION: " + avg + ", " + sd

    (avg, sd) = getAverageAndSd(directoryForTempResults, "DB REQUEST", "2")
    print "REQUEST: " + avg + ", " + sd

    (avg, sd) = getAverageAndSd(directoryForTempResults, "WAITING THREAD", "2")
    print "IN WORKER THREAD QUEUE: " + avg + ", " + sd

    (avg, sd) = getAverageAndSd(directoryForTempResults, "# TO READ", "2")
    print "TIMES A SOCKET IS WORKED for a REQUEST TO BE READ: " + avg + ", " + sd

    (avg, sd) = getAverageAndSd(directoryForTempResults, "# OF ENTERS", "2")
    print "TIMES TO ENTER: " + avg + ", " + sd

    (avg, sd) = getAverageAndSd(directoryForTempResults, "READING INSIDE", "2")
    print "TIMES (DOING) INSIDE: " + avg + ", " + sd

    shutil.rmtree(directoryForTempResults)


getTimeSpentOnEachComponent("../10_and_30_clients_experiment_1_queue/30/middlewareInstance1", 0.1)
# print getThroughput("../2_clients_experiment/2", 1, 600, 120, 60, "LIST_QUEUES|SEND_MESSAGE|RECEIVE_MESSAGE")
# print getThroughput("../2_k_experiment/20_threads_40_connections_m3_xlarge_db", 1, 600, 120, 60, "LIST_QUEUES|SEND_MESSAGE|RECEIVE_MESSAGE")
# print getThroughput("../10_and_30_clients_experiment_1_queue/30", 1, 600, 120, 60, "LIST_QUEUES|SEND_MESSAGE|RECEIVE_MESSAGE")
# print getResponseTime("../10_and_30_clients_experiment_1_queue/30", 1, 600, 120, 60, "LIST_QUEUES|SEND_MESSAGE|RECEIVE_MESSAGE")
# print getThroughput("../2_k_experiment/40_threads_40_connections_m3_xlarge_db", 1, 600, 120, 60, "LIST_QUEUES|SEND_MESSAGE|RECEIVE_MESSAGE")



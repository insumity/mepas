import commands
from os import listdir, system
import os
from os.path import isfile, join
import math
import shutil
import tempfile


# prints one line for every minute containing average, standard deviation, throughput per second
# to be used to plot the trace
import sys
import threading


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

    # TODO the seperator is a tab
    minutes = 1
    for i in range(0, totalTimeInMilliseconds, intervalWindowInMilliseconds + 1):
        fileName = experimentName + "/all_clients.csv"

        command = "awk -F'\t' '$1 >=" + str(i) + " && $1 < " + str(
            (i + intervalWindowInMilliseconds)) + " { print; }' " + fileName
        getAverageAndStd = "awk -F'\t' '{ sum += $2; n++; sumsq += $2 * $2 } END { if (n > 0) printf \"%d %f %f\", " \
                           + str(minutes) + ", (sum / n), sqrt(sumsq/n - (sum/n)**2); }'"
        system(command + " | " + getAverageAndStd)


        # average throughput every second in order to get standard deviation
        # times = 0
        # sum = 0
        # sdeviation = 0
        # for j in range(i, i + 60 * 1000, 1000):
        # command = "awk -F'\t' '$1 >=" + str(j) + " && $1 < " + str(
        # (j + 1000)) + " { print; }' " + fileName
        #
        # output = commands.getstatusoutput(command + " | " + "wc")
        #
        # # extract result from wc command
        #     numberOfRequests = int(" ".join(output[1].split()).split(' ', 1)[0])
        #     sum += numberOfRequests
        #     sdeviation += (numberOfRequests * numberOfRequests)
        #     times += 1
        #
        #
        # sdeviation = math.sqrt(sdeviation / times - ((sum / times) * (sum / times)))
        # sys.stdout.write(" " + str(sum / times) + " " + str(sdeviation) + "\n")
        # sys.stdout.flush()
        print

        minutes += 1


# getTrace("../AGAIN2k10MWThreads10Connections1MWSmallDBxLarge/1", 1, 600, "LIST_QUEUES|SEND_MESSAGE|RECEIVE_MESSAGE")


def getAverageAndSd(middlewareInstanceDir, what, column):
    avg = os.popen("grep -h '" + what + "' " + middlewareInstanceDir + "/*" + " | " +
                   "awk -F'\\t' '{ sum += $" + column + "; n++ } END { if (n > 0) print sum / n; }' ").read()
    sd = os.popen("grep -h '" + what + "' " + middlewareInstanceDir + "/*" + " | " +
                  "awk -F'\\t' '{sum+=$" + column + "; n++; sumsq+=$" + column + "*$" + column + "} END {print sqrt(sumsq/n- (sum/n)**2)}' ").read()

    return avg.rstrip(), sd.rstrip()


def getData(experimentName, possibleValues, numberOfClientInstances, percentageToRemove):
    # resultFile = experimentName + "/plot_data.csv"
    # f = open(resultFile, 'w')
    # f.close()

    for variableValue in possibleValues:
        print variableValue
        average = 0.0
        deviation = 0.0
        # for removing warm up and cool down
        newExperimentName = experimentName + "/" + str(variableValue)

        # TODO .. delete temp directory
        directoryForTempResults = tempfile.mkdtemp()
        print directoryForTempResults

        onlyfiles = []
        for i in range(1, numberOfClientInstances + 1):
            newNewExperimentName = newExperimentName + "/" + "clientInstance" + str(i)
            os.mkdir(directoryForTempResults + "/" + "clientInstance" + str(i))

            print newNewExperimentName
            files = [f for f in listdir(newNewExperimentName) if isfile(join(newNewExperimentName, f))]
            print files
            for f in files:
                system("cp " + newExperimentName + "/" + "clientInstance" + str(i) + "/" + f + " " +
                       directoryForTempResults + "/" + "clientInstance" + str(i))
                onlyfiles.append(directoryForTempResults + "/" + "clientInstance" + str(i) + "/" + f)

    for f in onlyfiles:
        # delete 10% of top and 10% bottom of the file
        num_lines = sum(1 for line in open(f))

        percentageInLines = int(math.floor(num_lines * percentageToRemove))

        # delete first 10% of the lines
        system("sed -i '' -e '1," + str(percentageInLines) + "d' " + f)

        num_lines = sum(1 for line in open(f))

        # delete last 10% of the lines
        system("sed -i '' -e '" + str(num_lines - percentageInLines) + "," + str(
            num_lines) + "d' " + f)

    average = 0
    averageSend = 0
    averageList = 0
    sd = 0
    sdSend = 0
    sdList = 0
    for value in range(1, numberOfClientInstances + 1):
        (avgSend, stdSend) = getAverageAndSd(directoryForTempResults + "/clientInstance" + str(value), "SEND_MESSAGE",
                                             "2")
        (avg, std) = getAverageAndSd(directoryForTempResults + "/clientInstance" + str(value), "RECEIVE_MESSAGE", "2")
        (avgList, stdList) = getAverageAndSd(directoryForTempResults + "/clientInstance" + str(value), "LIST_QUEUES",
                                             "2")
        average += float(avg)
        sd += float(std)
        averageSend += float(avgSend)
        sdSend += float(stdSend)
        averageList += float(avgList)
        sdList += float(stdList)

    average /= numberOfClientInstances
    sd /= numberOfClientInstances
    averageSend /= numberOfClientInstances
    sdSend /= numberOfClientInstances
    averageList /= numberOfClientInstances
    sdList /= numberOfClientInstances

    print "RECEIVE: " + str(variableValue) + "\t" + str(average) + "\t" + str(sd)
    print "SEND: " + str(variableValue) + "\t" + str(averageSend) + "\t" + str(sdSend)
    print "LIST: " + str(variableValue) + "\t" + str(averageList) + "\t" + str(sdList)
    # f = open(resultFile, 'a')
    # f.write(str(variableValue) + "\t" + str(average) + "\t" + str(deviation) + "\t" + str(total_lines) + "\n")
    # f.close()

    # TODO .. remove temporary files - directories


# getData("../traceFor1Hour/", [1, 5, 10])

def getResponseTime(experimentDir, what):
    result = os.popen("grep -h '" + what + "' " + experimentDir + "/*" + " | " +
                      "awk -F'\\t' '{ sum += $2; sumsq += $2 * $2; n++ } END { if (n > 0) print sum / n, sqrt(sumsq/n- (sum/n)**2); }' ").read()
    return result


def getResponseTimeCOOL(experimentDir, clientInstances, timeInSeconds, warmUpInSeconds, coolDownInSeconds,
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

    # TODO FIXME
    # responseAndStd = getResponseTime(directoryForTempResults, "LIST")

    shutil.rmtree(directoryForTempResults)
    return (average, std)



# def findThroughput(i):
(avg, std) = getThroughput("../NEW_increasing_both/" + "25", 1, 600, 120, 60, "LIST_QUEUES|SEND_MESSAGE|RECEIVE_MESSAGE")
print str(25) + " " + avg + " " + std
exit(1)
for i in [120, 140, 160, 180]:
    (avgAll, stdAll) = getThroughput("../NEW_increasing_number_of_clients/" + str(i), 1, 600, 120, 60,
                                           "LIST_QUEUES|SEND_MESSAGE|RECEIVE_MESSAGE")
  #  (avgList, stdList) = getResponseTimeCOOL("../NEW_increasing_number_of_clients/" + str(i), 1, 600, 120, 60,
   #                                          "LIST_QUEUES")
    #(avgSend, stdSend) = getResponseTimeCOOL("../NEW_increasing_number_of_clients/" + str(i), 1, 600, 120, 60,
     #                                        "SEND_MESSAGE")
    #(avgReceive, stdReceive) = getResponseTimeCOOL("../NEW_increasing_number_of_clients/" + str(i), 1, 600, 120, 60,
       #                                            "RECEIVE_MESSAGE")
    print str(
        i) + " " + avgAll + " " + stdAll# + " " + avgList + " " + stdList + " " + avgSend + " " + stdSend + " " + avgReceive + " " + stdReceive
exit(1)

for i in [1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70]:
    (avgAll, stdAll) = getResponseTimeCOOL("../NEW_increasing_both/" + str(i), 1, 600, 120, 60,
                                           "LIST_QUEUES|SEND_MESSAGE|RECEIVE_MESSAGE")
    (avgList, stdList) = getResponseTimeCOOL("../NEW_increasing_both/" + str(i), 1, 600, 120, 60,
                                       "LIST_QUEUES")
    (avgSend, stdSend) = getResponseTimeCOOL("../NEW_increasing_both/" + str(i), 1, 600, 120, 60,
                                             "SEND_MESSAGE")
    (avgReceive, stdReceive) = getResponseTimeCOOL("../NEW_increasing_both/" + str(i), 1, 600, 120, 60,
                                                   "RECEIVE_MESSAGE")
    print str(
        i) + " " + avgAll + " " + stdAll + " " + avgList + " " + stdList + " " + avgSend + " " + stdSend + " " + avgReceive + " " + stdReceive

    # thread = threading.Thread(name=str(i), target=findThroughput, args=([i]))
    # thread.start()
    # # (avg, std) = getThroughput("../NEW_increasing_message_size/" + str(i), 1, 600, 120, 60)
exit(1)

for i in [1, 500, 1000, 5000, 10000, 20000, 30000, 40000, 50000, 100000, 150000, 200000, 500000, 1000000]:
    (avgAll, stdAll) = getThroughput("../NEW_increasing_message_size/" + str(i), 1, 600, 120, 60,
                                     "LIST_QUEUES|SEND_MESSAGE|RECEIVE_MESSAGE")
    (avgList, stdList) = getThroughput("../NEW_increasing_message_size/" + str(i), 1, 600, 120, 60, "LIST_QUEUES")
    (avgSend, stdSend) = getThroughput("../NEW_increasing_message_size/" + str(i), 1, 600, 120, 60,
                                       "SEND_MESSAGE")
    (avgReceive, stdReceive) = getThroughput("../NEW_increasing_message_size/" + str(i), 1, 600, 120, 60,
                                             "RECEIVE_MESSAGE")

    # (avgAll, stdAll) = getResponseTimeCOOL("../NEW_increasing_message_size/" + str(i), 1, 600, 120, 60,
    # "LIST_QUEUES|SEND_MESSAGE|RECEIVE_MESSAGE")
    # (avgList, stdList) = getResponseTimeCOOL("../NEW_increasing_message_size/" + str(i), 1, 600, 120, 60, "LIST_QUEUES")
    # (avgSend, stdSend) = getResponseTimeCOOL("../NEW_increasing_message_size/" + str(i), 1, 600, 120, 60,
    #                                          "SEND_MESSAGE")
    # (avgReceive, stdReceive) = getResponseTimeCOOL("../NEW_increasing_message_size/" + str(i), 1, 600, 120, 60,
    #                                                "RECEIVE_MESSAGE")
    print str(
        i) + " " + avgAll + " " + stdAll + " " + avgList + " " + stdList + " " + avgSend + " " + stdSend + " " + avgReceive + " " + stdReceive

    # thread = threading.Thread(name=str(i), target=findThroughput, args=([i]))
    # thread.start()
    # # (avg, std) = getThroughput("../NEW_increasing_message_size/" + str(i), 1, 600, 120, 60)

exit(1)

for i in [2, 5, 10, 15, 20, 25, 30, 35, 40, 45]:
    getThroughput("../NEW_increasing_number_of_Clients/" + str(i), 1, 600, 120, 60)

exit(1)

# getThroughput("../2k10MWThreads10Connections1MWSmallDBxLarge/1", 1, 600, 120, 60)
# getThroughput("../2k10MWThreads20Connections1MWSmallDBxLarge/1", 1, 600, 120, 60)
# getThroughput("../2k20MWThreads10Connections1MWSmallDBxLarge/1", 1, 600, 120, 60)
# getThroughput("../2k20MWThreads20Connections1MWSmallDBxLarge/1", 1, 600, 120, 60)
#
# getThroughput("../2k10MWThreads10Connections1MWMediumDBxLarge/1", 1, 600, 120, 60)
# getThroughput("../2k10MWThreads20Connections1MWMediumDBxLarge/1", 1, 600, 120, 60)
# getThroughput("../2k20MWThreads10Connections1MWMediumDBxLarge/1", 1, 600, 120, 60)
# getThroughput("../2k20MWThreads20Connections1MWMediumDBxLarge/1", 1, 600, 120, 60)
#
# getThroughput("../2k10MWThreads10Connections1MWSmall/1", 1, 600, 120, 60)
# getThroughput("../2k10MWThreads20Connections1MWSmall/1", 1, 600, 120, 60)
# getThroughput("../2k20MWThreads10Connections1MWSmall/1", 1, 600, 120, 60)
# getThroughput("../2k20MWThreads20Connections1MWSmall/1", 1, 600, 120, 60)
#
# getThroughput("../2k10MWThreads10Connections1MWMedium/1", 1, 600, 120, 60)
# getThroughput("../2k10MWThreads20Connections1MWMedium/1", 1, 600, 120, 60)
# getThroughput("../2k20MWThreads10Connections1MWMedium/1", 1, 600, 120, 60)
# getThroughput("../2k20MWThreads20Connections1MWMedium/1", 1, 600, 120, 60)
# #
# #
# if True:
# exit(1)

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

    # (avg, sd) = getAverageAndSd(directoryForTempResults, "GOT CONNECTION", "2")
    # print "CONNECTION: " + avg + ", " + sd

    (avg, sd) = getAverageAndSd(directoryForTempResults, "DB REQUEST", "2")
    print "REQUEST: " + avg + ", " + sd

    (avg, sd) = getAverageAndSd(directoryForTempResults, "WAITING THREAD", "2")
    print "IN WORKER THREAD QUEUE: " + avg + ", " + sd

    (avg, sd) = getAverageAndSd(directoryForTempResults, "# TO READ", "2")
    print "TIMES A SOCKET IS WORKED for a REQUEST TO BE READ: " + avg + ", " + sd

    (avg, sd) = getAverageAndSd(directoryForTempResults, "# OF ENTERS", "2")
    print "TIMES TO ENTER: " + avg + ", " + sd

    (avg, sd) = getAverageAndSd(directoryForTempResults, "NOTHING INSIDE", "2")
    print "TIMES (NOTHING) INSIDE: " + avg + ", " + sd

    (avg, sd) = getAverageAndSd(directoryForTempResults, "READING INSIDE", "2")
    print "TIMES (DOING) INSIDE: " + avg + ", " + sd

    shutil.rmtree(directoryForTempResults)


getTimeSpentOnEachComponent("../connectionsIncreasingFor30_35/30/middlewareInstance1", 0.01)
# getTimeSpentOnEachComponent("../traceFor20Minutes/25/middlewareInstance2", 0.2)
# #
# getData("../AGAIN2k10MWThreads10Connections1MWSmall", [1], 1, 0.0)
# getResponseTimeAndThroughput("../trace100clientsWithMoreLogging10ClientsBARZ/10")

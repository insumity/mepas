from os import listdir, system
import os
from os.path import isfile, join
import math
import tempfile


def getTrace(experimentName, clientInstances, intervalWindowInSeconds, totalTimeInSeconds):
    # merge all files in a big one and sort them by time
    # merge all files in one instance
    for i in range(1, clientInstances + 1):
        directory = experimentName + "/clientInstance" + str(i)
        command = "cat " + directory + "/*.csv > " + directory + "/all_clients_of_this_instance.csv"
        print command
        system(command)

    # merge all_clients_of_this_instance.csv files from all the instances
    for i in range(1, clientInstances + 1):
        directory = experimentName + "/*"
        command = "cat " + directory + "/all_clients_of_this_instance.csv > " + experimentName + "/all_clients.csv"
        print command
        system(command)

    # sort generated file by time
    system("sort -k" + str(1) + " -n " + experimentName + "/all_clients.csv -o " + experimentName + "/all_clients.csv")

    intervalWindowInMilliseconds = intervalWindowInSeconds * 1000

    totalTimeInMilliseconds = totalTimeInSeconds * 1000

    # TODO the seperator is a tab
    for i in range(0, totalTimeInMilliseconds, intervalWindowInMilliseconds + 1):
        fileName = experimentName + "/all_clients.csv"
        command = "awk -F \"\t\" '$1 >=" + str(i) + " && $1 < " + str(
            (i + intervalWindowInMilliseconds)) + " { print; }' " + fileName
        getAverageAndStd = "awk \"\t\" '{ sum += $2; n++; sumsq += $2 * $2 } END { if (n > 0) printf \"%d %f %f %d\\n\", " \
                           "$1, (sum / n), sqrt(sumsq/NR - (sum/NR)**2), n; }'"
        system(command + "|" + getAverageAndStd)


# getTrace("../trace100clients2/100", 2, 1, 600)

def getAverageAndSd(middlewareInstanceDir, what, column):
    avg = os.popen("grep -h '" + what + "' " + middlewareInstanceDir + "/*" + " | " +
                   "awk -F'\\t' '{ sum += $" + column + "; n++ } END { if (n > 0) print sum / n; }' ").read()
    sd = os.popen("grep -h '" + what + "' " + middlewareInstanceDir + "/*" + " | " +
                  "awk -F'\\t' '{sum+=$" + column + "; sumsq+=$" + column + "*$" + column + "}END{print sqrt(sumsq/NR - (sum/NR)**2)}' ").read()

    return avg.rstrip(), sd.rstrip()

def getData(experimentName, possibleValues):
    # resultFile = experimentName + "/plot_data.csv"
    # f = open(resultFile, 'w')
    # f.close()

    for variableValue in possibleValues:
        print variableValue
        average = 0.0
        deviation = 0.0
        # for removing warm up and cool down
        newExperimentName = experimentName + "/" + str(variableValue)

        directoryForTempResults = tempfile.mkdtemp()
        print directoryForTempResults

        onlyfiles = []
        for i in range(1, 5):
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

        percentageToRemove = 0.45
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
    sd = 0
    sdSend = 0
    for value in range(1, 5):
        (avgSend, stdSend) = getAverageAndSd(directoryForTempResults + "/clientInstance" + str(value), "SEND_MESSAGE", "2")
        (avg, std) = getAverageAndSd(directoryForTempResults + "/clientInstance" + str(value), "RECEIVE_MESSAGE", "2")
        average += float(avg)
        sd += float(std)
        averageSend += float(avgSend)
        sdSend += float(stdSend)

    average /= 4
    sd /= 4
    averageSend /= 4
    sdSend /= 4

    print str(variableValue) + "\t" + str(average) + "\t" + str(sd)
    print str(variableValue) + "\t" + str(averageSend) + "\t" + str(sdSend)
        # f = open(resultFile, 'a')
        # f.write(str(variableValue) + "\t" + str(average) + "\t" + str(deviation) + "\t" + str(total_lines) + "\n")
        # f.close()


# getData("../someExperiment/", [1, 5, 10])


def getTimeSpentOnEachComponent(middlewareInstanceDir):
    files = [f for f in listdir(middlewareInstanceDir)]

    directoryForTempResults = tempfile.mkdtemp()
    print directoryForTempResults

    for f in files:

        if f == "middlewareSockets.csv":
            continue

        specificFile = middlewareInstanceDir + "/" + f

        percentageToRemove = 0.45
        # delete 10% of top and 10% bottom of the file
        num_lines = sum(1 for line in open(specificFile))

        percentageInLines = int(math.floor(num_lines * percentageToRemove))

        # delete first 10% of the lines
        system("sed -e '1," + str(
            percentageInLines) + "d' " + specificFile + " > " + directoryForTempResults + "/" + f)
        num_lines = sum(1 for line in open(directoryForTempResults + "/" + f))

        # delete last 10% of the lines
        system("sed -i '' -e '" + str(num_lines - percentageInLines) + "," + str(
            num_lines) + "d' " + directoryForTempResults + "/" + f)

    (avg, sd) = getAverageAndSd(directoryForTempResults, "CONNECTION", "2")
    print "CONNECTION: " + avg + ", " + sd

    (avg, sd) = getAverageAndSd(directoryForTempResults, "REQUEST", "2")
    print "REQUEST: " + avg + ", " + sd

    (avg, sd) = getAverageAndSd(directoryForTempResults, "IN WORKER THREAD", "4")
    print "IN WORKER THREAD QUEUE: " + avg + ", " + sd

    (avg, sd) = getAverageAndSd(directoryForTempResults, "TIMES TO READ", "3")
    print "TIMES A SOCKET IS WORKED for a REQUEST TO BE READ: " + avg + ", " + sd

    (avg, sd) = getAverageAndSd(directoryForTempResults, "TIMES TO ENTER", "3")
    print "TIMES TO ENTER: " + avg + ", " + sd

    (avg, sd) = getAverageAndSd(directoryForTempResults, "(NOTHING) INSIDE", "3")
    print "TIMES (NOTHING) INSIDE: " + avg + ", " + sd

    (avg, sd) = getAverageAndSd(directoryForTempResults, "(DOING) INSIDE", "3")
    print "TIMES (DOING) INSIDE: " + avg + ", " + sd

    # os.rmdir(directoryForTempResults)

getTimeSpentOnEachComponent("../trace100clientsWithMoreLogging10ClientsBARZ/25/middlewareInstance1")
getData("../trace100clientsWithMoreLogging10ClientsBARZ", [25])
# getResponseTimeAndThroughput("../trace100clientsWithMoreLogging10ClientsBARZ/10")

from os import listdir, system
import os
from os.path import isfile, join
import math
import numpy


def getTrace(experimentName, clientInstances, intervalWindowInSeconds, totalTimeInSeconds):
    # merge all files in a big one and sort them by time
    # merge all files in one instance
    for i in range(1, clientInstances + 1):
        directory = experimentName + "/clientInstance" + str(i)
        command = "cat " + directory + "/*.csv > " + directory + "/all_clients_of_this_instance.csv"
        print command
        system(command)

    # merge all_clients_of_this_instance.csv files from all the instaces
    for i in range(1, clientInstances + 1):
        directory = experimentName + "/*"
        command = "cat " + directory + "/all_clients_of_this_instance.csv > " + experimentName + "/all_clients.csv"
        print command
        system(command)

    # sort generated file by time
    system("sort -k" + str(1) + " -n " + experimentName + "/all_clients.csv -o " + experimentName + "/all_clients.csv")

    intervalWindowInMilliseconds = intervalWindowInSeconds * 1000

    totalTimeInMilliseconds = totalTimeInSeconds * 1000

    for i in range(0, totalTimeInMilliseconds, intervalWindowInMilliseconds + 1):
        fileName = experimentName + "/all_clients.csv"
        command = "awk -F \"\t\" '$1 >=" + str(i) + " && $1 < " + str(
            (i + intervalWindowInMilliseconds)) + " { print; }' " + fileName
        getAverageAndStd = "awk '{ sum += $2; n++; sumsq += $2 * $2 } END { if (n > 0) printf \"%d %f %f %d\\n\", $1, (sum / n), sqrt(sumsq/NR - (sum/NR)**2), n; }'"
        system(command + "|" + getAverageAndStd)


getTrace("../trace100clients/100", 2, 10, 600)

def getData(experimentName, possibleValues):
    resultFile = experimentName + "/plot_data.csv"
    f = open(resultFile, 'w')
    f.close()

    for variableValue in possibleValues:
        print variableValue
        average = 0.0
        deviation = 0.0
        # for removing warm up and cool down
        newExperimentName = experimentName + str(variableValue)

        onlyfiles = []
        for i in range(1, 5):
            newNewExperimentName = newExperimentName + "/" + "clientInstance" + str(i)
            files = [f for f in listdir(newNewExperimentName) if isfile(join(newNewExperimentName, f))]
            print files
            for file in files:
                onlyfiles.append("clientInstance" + str(i) + "/" + file)

        total_lines = 0
        for f in onlyfiles:
            print f
            # # delete 5% of top and 5% bottom of the file
            # num_lines = sum(1 for line in open(experimentName + "/" + f))
            #
            # percentageInLines = int(math.floor(num_lines * 0.05))
            #
            # # delete first 5% of the lines
            # system("sed -i '' -e '1," + str(percentageInLines) + "d' " + experimentName + "/" + f)
            # num_lines = sum(1 for line in open(experimentName + "/" + f))
            #
            # # delete last 5% of the lines
            # system("sed -i '' -e '" + str(num_lines - percentageInLines) + "," + str(
            # num_lines) + "d' " + experimentName + "/" + f)
            # num_lines = sum(1 for line in open(experimentName + "/" + f))

            # find average
            avg = os.popen(
                "awk '{ sum += $2; n++ } END { if (n > 0) print sum / n; }' " + newExperimentName + "/" + f).read()
            std = os.popen(
                "awk '{sum+=$2; sumsq+=$2*$2}END{print sqrt(sumsq/NR - (sum/NR)**2)}' " + newExperimentName + "/" + f).read()

            average += float(avg)
            deviation += float(std)
            # total_lines += num_lines

        average /= variableValue
        deviation /= variableValue

        f = open(resultFile, 'a')
        f.write(str(variableValue) + "\t" + str(average) + "\t" + str(deviation) + "\t" + str(total_lines) + "\n")
        f.close()


# getData("../someExperiment/", [1, 5, 10])

from os import listdir, system
import os
from os.path import isfile, join
import math
import numpy


def get_trace(experimentName, clientInstances, intervalWindowInSeconds, totalTimeInSeconds):
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
        getAverage = "awk '{ sum += $2; n++ } END { if (n > 0) print $1, (sum / n); }'"
        system(command + "|" + getAverage)


get_trace("../someExperiment/100", 2, 5, 120)

# with the following command you can read all the rows that in the first column have their
# value going in this range
# awk -F "\t" '$1 >= 110000 && $1 < 115000 { print; }' client001.csv
#
#
# for 0 in (0, totalTimeInSeconds, timesToSplit)
# average = 0.0
#     deviation = 0.0
#     # for removing warm up and cool down
#     onlyfiles = [f for f in listdir(experimentName) if isfile(join(experimentName, f))]
#
#     total_lines = 0
#     for f in onlyfiles:
#
#         # find average
#         avg = os.popen("awk '{ sum += $1; n++ } END { if (n > 0) print sum / n; }' " + experimentName + "/" + f).read()
#         std = os.popen(
#             "awk '{sum+=$1; sumsq+=$1*$1}END{print sqrt(sumsq/NR - (sum/NR)**2)}' " + experimentName + "/" + f).read()
#
#         average += float(avg)
#         deviation += float(std)
#         # total_lines += num_lines
#
#     average /= totalClients
#     deviation /= totalClients
#
#     f = open(resultFile, 'a')
#     f.write(str(totalClients) + "\t" + str(average) + "\t" + str(deviation) + "\t" + str(total_lines) + "\n")
#     f.close()



def get_data(experimentName, possibleValues):
    # resultFile = "increasingNumberOfClients/plot_data.csv"

    for totalClients in possibleValues:
        average = 0.0
        deviation = 0.0
        # for removing warm up and cool down
        onlyfiles = [f for f in listdir(experimentName) if isfile(join(experimentName, f))]

        total_lines = 0
        for f in onlyfiles:
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
            #     num_lines) + "d' " + experimentName + "/" + f)
            # num_lines = sum(1 for line in open(experimentName + "/" + f))

            # find average
            avg = os.popen(
                "awk '{ sum += $1; n++ } END { if (n > 0) print sum / n; }' " + experimentName + "/" + f).read()
            std = os.popen(
                "awk '{sum+=$1; sumsq+=$1*$1}END{print sqrt(sumsq/NR - (sum/NR)**2)}' " + experimentName + "/" + f).read()

            average += float(avg)
            deviation += float(std)
            # total_lines += num_lines

        average /= totalClients
        deviation /= totalClients

        f = open(resultFile, 'a')
        f.write(str(totalClients) + "\t" + str(average) + "\t" + str(deviation) + "\t" + str(total_lines) + "\n")
        f.close()
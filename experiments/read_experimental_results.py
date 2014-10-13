from os import listdir, system
import os
from os.path import isfile, join
import math

def get_data(possibleValues):
    resultFile = "increasingNumberOfClients/plot_data.csv"

    for totalClients in possibleValues:
        average = 0.0
        deviation = 0.0
        # for removing warm up and cool down
        experimentName = "increasingNumberOfClients/experiment_10dbconnections_10threads_" + str(totalClients) + "clients"
        onlyfiles = [f for f in listdir(experimentName) if isfile(join(experimentName, f))]
        for f in onlyfiles:
            # delete 5% of top and 5% bottom of the file
            num_lines = sum(1 for line in open(experimentName + "/" + f))

            percentageInLines = int(math.floor(num_lines * 0.05))

            # delete first 5% of the lines
            system("sed -i '' -e '1," + str(percentageInLines) + "d' " + experimentName + "/" + f)
            num_lines = sum(1 for line in open(experimentName + "/" + f))

            # delete last 5% of the lines
            system("sed -i '' -e '" + str(num_lines - percentageInLines) + "," + str(
                num_lines) + "d' " + experimentName + "/" + f)
            num_lines = sum(1 for line in open(experimentName + "/" + f))

            # find average
            avg = os.popen("awk '{ sum += $1; n++ } END { if (n > 0) print sum / n; }' " + experimentName + "/" + f).read()
            std = os.popen(
                "awk '{sum+=$1; sumsq+=$1*$1}END{print sqrt(sumsq/NR - (sum/NR)**2)}' " + experimentName + "/" + f).read()

            average += float(avg)
            deviation += float(std)

        average /= totalClients
        deviation /= totalClients

        f = open(resultFile, 'a')
        f.write(str(average) + "\t" + str(deviation) + "\n")
        f.close()


for line in open("responseTimeAllRequests.csv"):
	point = int(line.split()[0])
	responseTime = float(line.split()[1])
	if (point == 0):
		continue

	calculatedThroughput = (50.0 / (responseTime)) * 1000
	print str(point) + " " + str(responseTime) + " " + str(calculatedThroughput)


set terminal postscript eps enhanced color font 'Times-NewRoman,14' linewidth 1.5
set output 'throughput.eps'

set xlabel "Time (min)"
set ylabel "Throughput (requests/second)"
set title "Trace for 1 hour: 2 Client Instances (50 clients/instance), 1 MW (20 threads, 20 connections)" 

set key bottom right

set xrange [0:61]
set yrange [0:7500]


# Line width of the axes
set border linewidth 0.5
# Line styles
set style line 1 lt 1 lc rgb "red" lw 1
set style line 2 lt 2 lc rgb "blue" lw 1
set style line 3 lt 3 lc rgb "yellow" lw 1
set style line 4 lt 4 lc rgb "green" lw 1

set xtics 0, 5
plot "throughputAllRequests.csv" using 1:2:3 title "Troughput for LIST\\_QUEUES, SEND\\_MESSAGE and RECEIVE\\_MESSAGE" with errorlines ls 1, "throughputListQueues.csv" using 1:2:3 title "Throughput for LIST\\_QUEUES" with lines ls 2, "throughputReceiveMessage.csv" using 1:2:3 title "Throughput for RECEIVE\\_MESSAGE" with errorlines ls 3, "throughputSendMessage.csv" using 1:2:3 title "Throughput for SEND\\_MESSAGE" with errorlines ls 4

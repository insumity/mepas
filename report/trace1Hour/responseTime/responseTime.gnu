set terminal postscript eps enhanced color font 'Times-NewRoman,14' linewidth 1.5
set output 'responseTime.eps'

set xlabel "Time (min)"
set ylabel "Response time (ms)"
set title "Trace for 1 hour: 2 Client Instances (50 clients/instance), 1 MW (20 threads, 20 connections)" 

# set key bottom right
set xrange [0:60.5]
set yrange [0:50]


# Line width of the axes
set border linewidth 0.5
# Line styles
set style line 1 lt 1 lc rgb "red" lw 1
set style line 2 lt 2 lc rgb "blue" lw 1
set style line 3 lt 3 lc rgb "yellow" lw 1
set style line 4 lt 4 lc rgb "green" lw 1

set xtics 0, 5
plot "responseTimeAllRequests.csv" using 1:2:3 title "Response time for LIST\\_QUEUES, SEND\\_MESSAGE and RECEIVE\\_MESSAGE" with errorlines ls 1, "responseTimeListQueues.csv" using 1:2:3 title "Response time for LIST\\_QUEUES" with errorlines ls 2, "responseTimeReceiveMessage.csv" using 1:2:3 title "Response time for RECEIVE\\_MESSAGE" with errorlines ls 3, "responseTimeSendMessage.csv" using 1:2:3 title "Response time for SEND\\_MESSAGE" with errorlines ls 4

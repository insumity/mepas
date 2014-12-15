set terminal postscript eps enhanced color font 'Times-NewRoman,14' linewidth 1.5
set output 'responseTime.eps'

set xlabel "Clients"
set ylabel "Response time (ms)"
set title "Increasing Number of Clients: 1 Client Instance, 1 MW (20 connections, 20 threads)" 

#set key bottom right
set xrange [0:200.5]
set yrange [0:50]


# Line width of the axes
set border linewidth 0.5
# Line styles
set style line 1 lt 1 lc rgb "red" lw 1
set style line 2 lt 2 lc rgb "blue" lw 1
set style line 3 lt 3 lc rgb "yellow" lw 1
set style line 4 lt 4 lc rgb "green" lw 1

set xtics 0, 20
plot "responseTimeAllRequests.csv" using 1:2:3 title "Response time for LIST\\_QUEUES, SEND\\_MESSAGE and RECEIVE\\_MESSAGE" with errorlines ls 1, "responseTimeAllRequests.csv" using 1:4 title "Model Predicted Response time for LIST\\_QUEUES, SEND\\_MESSAGE, RECEIVE\\_MESSAGE" with errorlines ls 2

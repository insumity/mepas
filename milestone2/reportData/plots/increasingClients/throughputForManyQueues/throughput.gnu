#set terminal png font arial 14 size 800,600
set terminal postscript eps enhanced color font 'Times-NewRoman,14' linewidth 1.5
set output 'throughput.eps'

set xlabel "Clients"
set ylabel "Throughput (requests/second)"
set title "Increasing Number of Clients: 1 Client Instance, 1 MW (20 connections, 20 threads)" 

set key top right

set xrange [0:200.5]
set yrange [0:6500]


# Line width of the axes
set border linewidth 0.5
# Line styles
set style line 1 lt 1 lc rgb "red" lw 1
set style line 2 lt 2 lc rgb "blue" lw 1
set style line 3 lt 3 lc rgb "green" lw 1
#set style line 4 lt 4 lc rgb "green" lw 1

set xtics 0, 20
plot "throughputAllRequests.csv" using 1:2:3 title "Throughput for LIST\\_QUEUES, SEND\\_MESSAGE, RECEIVE\\_MESSAGE" with errorlines ls 1, "throughputAllRequests.csv" using 1:5 title "Calculated Throughput for LIST\\_QUEUES, SEND\\_MESSAGE, RECEIVE\\_MESSAGE" with errorlines ls 2

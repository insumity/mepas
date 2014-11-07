#set terminal png font arial 14 size 800,600
set terminal postscript eps enhanced color font 'Times-NewRoman,14' linewidth 1.5
set output 'throughput.eps'

set xlabel "Message size in characters"
set ylabel "Throughput (requests/second)"
set title "Increasing Size of the Messages: 1 Client Instance (50 clients/instance), 1 MW (20 threads, 20 connections)" 

#set key bottom right

set xrange [0:1000005]
set yrange [0:5000]


# Line width of the axes
set border linewidth 0.5
# Line styles
set style line 1 lt 1 lc rgb "red" lw 1
set style line 2 lt 2 lc rgb "blue" lw 1
set style line 3 lt 3 lc rgb "yellow" lw 1
set style line 4 lt 4 lc rgb "green" lw 1

#set xtics 0, 100000
plot "throughputAllRequests.csv" using 1:2:3 title "Throughput for LIST\\_QUEUES, SEND\\_MESSAGE and RECEIVE\\_MESSAGE" with errorlines ls 1
# "throughputAllRequests.csv" using 1:4:5 title "Throughput for LIST\\_QUEUES" with errorlines ls 2, "throughputAllRequests.csv" using 1:6:7 title "Throughput for RECEIVE\\_MESSAGE" with errorlines ls 3, "throughputAllRequests.csv" using 1:8:9 title "Throughput for SEND\\_MESSAGE" with errorlines ls 4

#set terminal png font arial 14 size 800,600
set terminal postscript eps enhanced color font 'Times-NewRoman,14' linewidth 1.5
set output 'throughput.eps'

set xlabel "Number of Client Instances"
set ylabel "Throughput (requests/second)"
set title "Increasing Number of Client Instances, 1 MW (20 threads, 20 connections)" 

set key bottom right

set xrange [0:10.1]
set yrange [0:5500]


# Line width of the axes
set border linewidth 0.5
# Line styles
set style line 1 lt 1 lc rgb "red" lw 1
#set style line 2 lt 2 lc rgb "blue" lw 1
#set style line 3 lt 3 lc rgb "yellow" lw 1
#set style line 4 lt 4 lc rgb "green" lw 1

set xtics 0, 1
plot "throughputAllRequests.csv" using 1:2:3 title "Throughput for LIST\\_QUEUES, SEND\\_MESSAGE and RECEIVE\\_MESSAGE" with errorlines ls 1

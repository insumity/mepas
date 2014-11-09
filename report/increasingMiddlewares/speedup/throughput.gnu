#set terminal png font arial 14 size 800,600
set terminal postscript eps enhanced color font 'Times-NewRoman,14' linewidth 1.5
set output 'throughput.eps'

set xlabel "Number of Middlewares, (20 connections and 20 threads per Middleware)"
set ylabel "Speedup"
set title "Increasing Number of Middlewares: 10 Client Instances (100 clients/instance) uniformly distributed per Middleware" 

set key bottom right

set xrange [0:5]
set yrange [0:5]


# Line width of the axes
set border linewidth 0.5
# Line styles
set style line 1 lt 1 lc rgb "red" lw 1
set style line 2 lt 2 lc rgb "blue" lw 1
#set style line 3 lt 3 lc rgb "yellow" lw 1
#set style line 4 lt 4 lc rgb "green" lw 1

set xtics 0, 1
set ytics 0, 1
plot "throughputAllRequests.csv" using 1:5 title "Real Speedup" with errorlines ls 1, "throughputAllRequests.csv" using 1:2 title "Ideal Speedup" with lines ls 2


#set terminal png font arial 14 size 800,600
set terminal postscript eps enhanced color font 'Times-NewRoman,14' linewidth 1.5
set output 'throughput.eps'

set xlabel "Number of Middleware (20 connections and 20 threads per Middleware) & Client Instances (100 clients per Instance)"
set ylabel "Scale-out"
set title "Increasing both Number of Middlewares and Client Instances" 

set key bottom right

set xrange [0:9]
set yrange [0:2]


# Line width of the axes
set border linewidth 0.5
# Line styles
set style line 1 lt 1 lc rgb "red" lw 1
set style line 2 lt 2 lc rgb "blue" lw 1

set xtics 0, 1
set ytics 0, 1
plot "throughput.csv" using 1:2 title "Real Scale-out" with errorlines ls 1, "throughput.csv" using 1:3 title "Ideal Scale-out" with errorlines ls 2


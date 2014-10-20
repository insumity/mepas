
set terminal pngcairo size 1000,800 enhanced font 'Verdana,12'
set output "througput.png"

set xlabel "Number of Clients"
set ylabel "Response time (ms)"
set title "Response time while increasing number of clients" # TODO whtat's the differece

set key bottom right

set xrange [0:230]
set yrange [0:150000]

# Line width of the axes
set border linewidth 1
# Line styles
set style line 1 linecolor rgb '#0060ad' linetype 1 linewidth 3.5
set style line 2 linecolor rgb '#dd181f' linetype 1 linewidth 3.5
#set style line 3 linecolor rgb '#cc181f' linetype 1 linewidth 3.5

plot "increasingNumberOfClients/plot_data.csv" using 1:4 title "Response time while increasing number of clients" with lines, "increasingNumberOfClients/plot_data.csv" with yerrorbars

set terminal pngcairo size 700,500 enhanced font 'Verdana,12'
set output "foo.png"

set xlabel "x axis label"
set ylabel "y axis label"
set title "aN mou tilefoNouse!s"

set key bottom right

set xrange [0:10]
set yrange [0:10]

# Line width of the axes
set border linewidth 1
# Line styles
set style line 1 linecolor rgb '#0060ad' linetype 1 linewidth 3.5
set style line 2 linecolor rgb '#dd181f' linetype 1 linewidth 3.5
set style line 3 linecolor rgb '#cc181f' linetype 1 linewidth 3.5

plot "foo.cvs" using 1:2 title "Example line" w lines, \
"foo.cvs" using 2:3 title "Another example" w lp, \
"foo.cvs" using 1:3 title "AnotherCOOl example" w lp


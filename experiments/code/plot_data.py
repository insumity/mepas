
#TODO inside the string
from os import system

plot_code_template = """
set terminal pngcairo size 1000,800 enhanced font 'Verdana,12'
set output {plot_result_filename}

set xlabel {xlabel}
set ylabel {ylabel}
set title {title} # TODO whtat's the differece

set key bottom right

set xrange [{xrangemin}:{xrangemax}]
set yrange [{yrangemin}:{yrangemax}]

# Line width of the axes
set border linewidth 1
# Line styles
set style line 1 linecolor rgb '#0060ad' linetype 1 linewidth 3.5
set style line 2 linecolor rgb '#dd181f' linetype 1 linewidth 3.5
#set style line 3 linecolor rgb '#cc181f' linetype 1 linewidth 3.5

plot {data_file} using {columns} title {title} with lines, {data_file} with yerrorbars
"""


def plot_data(file, totalClients, resultFile, columns):
    context = {
        "plot_result_filename": "\"" + resultFile + "\"",
        "xlabel": "\"Number of Clients\"",
        "ylabel": "\"Response time (ms)\"",
        "title": "\"Response time while increasing number of clients\"",
        "xrangemin": 0,
        "xrangemax": totalClients,
        "yrangemin": 0,
        "yrangemax": 150000,
        "data_file": "\"" + file + "\"",
        "columns": columns
    }

    with open('plot_code.gnu','w') as myfile:
        myfile.write(plot_code_template.format(**context))

    system("gnuplot plot_code.gnu")

# get_data([5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100, 150, 200])
plot_data("increasingNumberOfClients/plot_data.csv", 230, "result.png", "1:2:3")
plot_data("increasingNumberOfClients/plot_data.csv", 230, "througput.png", "1:4")
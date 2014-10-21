from os import system


class Plotter:
    def __init__(self):
        self.plot_code_template = """
            set terminal pngcairo size 1000,800 enhanced font 'Verdana,12'
            set output {plot_result_filename}

            set xlabel {xlabel}
            set ylabel {ylabel}

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

    def __putQuotesToString(string):
        return "\"" + str(string) + "\""

    def plotData(self, plotResultFilename, xlabel, ylabel, xrange, yrange, data_file, columns, title):
        context = {
            "plot_result_filename": self.__putQuotesToString(plotResultFilename),
            "xlabel": self.__putQuotesToString(xlabel),
            "ylabel": self.__putQuotesToString(ylabel),
            "title": self.__putQuotesToString(title),
            "xrangemin": xrange[0],
            "xrangemax": xrange[1],
            "yrangemin": yrange[0],
            "yrangemax": yrange[1],
            "data_file": self.__putQuotesToString(data_file),
            "columns": columns
        }

        with open('plot_code.gnu', 'w') as myfile:
            myfile.write(self.plotCodeTemplate.format(**context))

        system("gnuplot plot_code.gnu")

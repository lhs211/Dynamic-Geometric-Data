# -*- coding: utf-8 -*-
"""
Created on Sat Jun 22 11:36:50 2019

@author: Luke
"""
import matplotlib.pyplot as plt
import os
import statistics
import sys
from os import listdir
from os.path import isfile, join
from statistics import mean
import math as m

TIME_UNIT = 1e6  # milli seconds
DATA_INTERVAL = 1


def find_max(file):

    max_comparisons = []
    max_times = []

    for filename in filenames:
        path = os.path.join(os.getcwd(), filename)
        file = open(path, "r")
        lines = file.readlines()
        file.close()

        max_comparisons.append(max(
            int(line.split(' ')[1]) for line in open(filename).readlines()[:-1]))

        max_times.append(max(
            int(line.split(' ')[4]) for line in open(filename).readlines()[:-1]))

    return mean(max_comparisons), max(max_comparisons), mean(max_times)/TIME_UNIT, max(max_times)/TIME_UNIT


def fill_experiment_data(s, filename):
    path = os.path.join(os.getcwd(), filename)
    file = open(path, "r")
    lines = file.readlines()
    file.close()

    timesteps = [[] for i in range(s)]
    comparisons = [[] for i in range(s)]
    population_size = [[] for i in range(s)]
    elite_archive_size = [[] for i in range(s)]
    times = [[] for i in range(s)]
    timing = []

    simulationIndex = 0

    for i in range(len(lines)):
        line = lines[i]
        data = line.split()
        if len(data) == 5:
            comparisons[simulationIndex].append(int(data[1]))
            population_size[simulationIndex].append(int(data[2]))
            elite_archive_size[simulationIndex].append(int(data[3]))
            times[simulationIndex].append(int(data[4])/TIME_UNIT)
        else:
            if len(data) == 6:
                timing.append(int(data[0]))  # in milliseconds already
                simulationIndex += 1

    return comparisons, population_size, elite_archive_size, times, timing


def plot_population_comparisons(population_size, comparisons, filename, max_comparisons):
    filename = filename.replace("results", "population_comparisons")
    filename = filename.replace(".dat", ".png")
    plot(population_size, comparisons, "Population Size",
         "Comparisons", filename, min_y=1e5, max_y=max_comparisons, yscale="log")


def plot_population_times(population_size, times, filename, max_time):
    filename = filename.replace("results", "population_timings")
    filename = filename.replace(".dat", ".png")

    plot(population_size, times, "Population Size",
         "Timing (s)", filename, min_y=1, max_y=max_time, yscale="log")


def find_comparison_change(comparisons):
    change = [[] for i in range(len(comparisons))]
    for i in range(len(comparisons)):
        for j in range(1, len(comparisons[i])):
            change[i].append(
                (comparisons[i][j]-comparisons[i][j-1])/DATA_INTERVAL)

    return change


def plot_population_eliteComparisons(filename, population_size, elite_archive_size, comparisons):
    filename = filename.replace("results", "population_eliteComparisons")
    filename = filename.replace(".dat", ".png")

    eliteComparisons, timesteps = calculate_eliteComparisons(
        elite_archive_size, comparisons, population_size)

    plot(timesteps, eliteComparisons, "Population Size",
         "Ratio", filename, min_x=None, max_x=None, min_y=1, max_y=1e8, yscale="log", marker_interval=50)


def calculate_eliteComparisons(elite_archive_size, comparisons, population_size):

    elite_comparisons = [[] for i in range(len(elite_archive_size))]
    timesteps = [[] for i in range(len(elite_archive_size))]

    #comparison_change = find_comparison_change(comparisons)

    total = 0

    for i in range(len(elite_archive_size)):
        # last two bits of data isnt there
        for j in range(1, len(elite_archive_size[i])-2, 2):
            if elite_archive_size[i][j] - elite_archive_size[i][j-1] != 0:
                comparison_change = comparisons[i][j] - comparisons[i][j-1]
                total += comparison_change
                elite_comparisons[i].append(total)
                timesteps[i].append(j)

    return elite_comparisons, timesteps


def plot(x_data, y_data, xAxis, yAxis, filename, min_x=None, max_x=50000, min_y=0, max_y=None, yscale="linear", marker_interval=5000):
    fig, ax = plt.subplots()

    markers = ['o', '^', 'x', '*']

    for i, (x, y) in enumerate(zip(x_data, y_data)):
        ax.plot(x, y, marker=markers[i], markersize=10, markevery=marker_interval, label="Simulation " +
                str(i+1))

    ax.set_xlabel(None)
    ax.set_ylabel(None)

    plt.xticks(fontsize=18)
    plt.yticks(fontsize=20)

    ax.ticklabel_format(style='sci', axis='both',
                        scilimits=(0, 0))

    ax.set_yscale(yscale)

    ax.set_xlim([0, max_x])
    # ax.set_ylim([1e-1, max_y])
    ax.set_ylim([min_y, max_y])

    handles, labels = ax.get_legend_handles_labels()
    # fig.legend(handles, labels, loc='upper center', ncol=4)

    # plt.tick_params(axis='x', which='both', bottom=False,
    # top=False, labelbottom=False)
    # plt.tick_params(axis='y', which='both', bottom=False,
    # top=False, labelbottom=False)

    # ax.spines['top'].set_visible(False)
    # ax.spines['right'].set_visible(False)
    # ax.legend()
    plt.savefig(os.path.join(os.getcwd() + "//..//report//plots//" + filename))
    plt.cla()
    plt.clf()
    plt.close()


if __name__ == "__main__":
    ids = []
    vector_x = []
    vector_y = []
    dominates = []
    # ids, vector_x, vector_y, dominates = fill_dominance_data()
    # plot_all_dominance(vector_x, vector_y, ids, dominates)

    # filenames = ["results_c1.txt","results_c2.txt","results_c3.txt","results_c4.txt","results_c5.txt","results_c6.txt"]

    mypath = os.path.join(os.getcwd())
    filenames = [f for f in listdir(mypath) if (
        isfile(join(mypath, f)) and f[-3:] == "dat")]

    # s, n = int(sys.argv[1]), int(sys.argv[2])
    s = 4

    # find max comparisons and max time
    max_comparisons_linear, max_comparisons_log, max_time_linear, max_time_log = find_max(
        filenames)

    for file in filenames:
        comparisons, population_size, elite_archive_size, times, timing = fill_experiment_data(
            s, file)
        plot_population_comparisons(
            population_size, comparisons, file, max_comparisons_log)

        plot_population_times(
            population_size, times, file, max_time_log)

        # find only elite member comparisons
        plot_population_eliteComparisons(
            file, population_size, elite_archive_size, comparisons)

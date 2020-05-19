# -*- coding: utf-8 -*-
"""
Created on Wed Apr 29 14:47:51 2020

@author: Luke
"""
import statistics
import matplotlib.pyplot as plt
import os

def fill_dominance_data():
    path = os.path.join( os.getcwd(), '..\\..\\..', 'domination-links.txt' )
    file=open(path, "r")
    lines = file.readlines()
    file.close()
    
    ids = {}
    vector_x = []
    vector_y = []
    dominates = [[]]
    
    for i, line in enumerate(lines):
        line = line.translate({ord(i): None for i in '[] '})
        splitline = line.split(",")
        
        
        # use object reference as ID
        #ids.append(splitline[0])
        ids[splitline[0]] = i
        
        # get objective values of solutions
        vector_x.append(float(splitline[1]))
        vector_y.append(float(splitline[2]))
        
        for j in range(3, len(splitline)):
            dominates[i].append(splitline[j].translate({ord(k): None for k in '[] '}).replace('\n', ''))
        dominates.append([])
    
    x_mean = statistics.mean(vector_x)
    x_std = statistics.stdev(vector_x)
    y_mean = statistics.mean(vector_y)
    y_std = statistics.stdev(vector_y)
    
    
    vector_xN = [(float(i) - x_mean)/x_std for i in vector_x]
    vector_yN = [(float(i) - y_mean)/y_std for i in vector_y]
    
    #vector_xN = [(float(i) - min_valueX)/(max_valueX-min_valueX) for i in vector_x]
    #vector_yN = [(float(i) - min_valueY)/(max_valueY-min_valueY) for i in vector_y]
    
    # standardise data

    
    return ids, vector_x, vector_y, dominates

def plot_all_dominance(vector_x, vector_y, ids, dominates):
    fig, ax = plt.subplots()
    ax.scatter(vector_x, vector_y)
    
    #for i, txt in enumerate(ids):
        #ax.annotate(txt, (vector_x[i], vector_y[i]))
    
    # need four coordinates
    
    for i in range(0, len(dominates)-1):
        x1 = vector_x[i]
        y1 = vector_y[i]
        for j in range(0, len(dominates[i])):
            if dominates[i][0] != '':
                x2 = vector_x[ids.get(dominates[i][j], 0)]
                y2 = vector_y[ids.get(dominates[i][j], 0)]
                ax.plot([x1, x2], [y1, y2])
    
    # set axes limit?
    #ax.set_xlim(min(vector_x),0.2)
    #ax.set_ylim(min(vector_y), 0.2)
    
    # set axes labels
    ax.set_ylabel('Objective 1')
    ax.set_xlabel('Objective 2')
    
    # turn off tick labels
    #ax.set_yticklabels([])
    #ax.set_xticklabels([])
    
    # remove border
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    
if __name__ == "__main__":
    ids = []
    vector_x = []
    vector_y = []
    dominates = []
    
    ids, vector_x, vector_y, dominates = fill_dominance_data()
    plot_all_dominance(vector_x, vector_y, ids, dominates)
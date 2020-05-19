package projectimplementation.empiralAnalysis;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class ExperimentResults {
    private int[] timesteps;
    private long[] dominationComparisons;
    private int[] populationSize;
    private int[] eliteArchiveSize;
    private long[] timing;
    String filename;
    int numSamples;
    int numTimesteps;
    int interval;
    int N;

    public ExperimentResults(String filename, int N, int numTimeSteps, int interval) {
        this.filename = filename;
        this.numSamples = 0;
        this.numTimesteps = numTimeSteps;
        this.interval = interval;
        this.dominationComparisons = new long[this.numTimesteps+1];
        this.eliteArchiveSize = new int[this.numTimesteps+1];
        this.timing = new long[this.numTimesteps+1];
        this.N = N;

        try {
            FileWriter fileWriter = new FileWriter(System.getProperty("user.dir") + "/src/test/resources/" + filename, false);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print("");
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addData(int timestep, long numComparisons, int populationSize, int eliteArchiveSize, long time) {

        this.dominationComparisons[timestep/interval] += numComparisons;

        this.eliteArchiveSize[timestep/interval] += eliteArchiveSize;

        this.timing[timestep/interval] += time;
    }

    public void addSample(){
        this.numSamples += 1;
    }

    public void clearData() {
        timesteps = new int[numTimesteps+1];
        dominationComparisons = new long[numTimesteps+1];
        populationSize = new int[numTimesteps+1];
        eliteArchiveSize = new int[numTimesteps+1];
        timing = new long[numTimesteps+1];
        numSamples = 0;
    }

    public void writeSimulation(long time) {
        try {
            FileWriter fileWriter = new FileWriter(System.getProperty("user.dir") + "/src/test/resources/" + filename, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            for (int i = 0; i <= numTimesteps; i++) {
                printWriter.println((i*interval) + " " + (dominationComparisons[i]/N) + " " + (i*interval) + " " + (eliteArchiveSize[i]/N) + " " + (timing[i]/N));
            }
            printWriter.println(time + " " + 0 + " "  + 0 + " " + 0 + " " + 0 + " " + 0);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

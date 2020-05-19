package projectimplementation.empiralAnalysis;

import Jama.Matrix;
import org.moeaframework.core.NondominatedPopulation;
import projectimplementation.guardianArchive.*;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static projectimplementation.guardianArchive.GuardianAssignment.*;

public class Experiment {

    private static final ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    private static final GuardianAssignment[] c1 = new GuardianAssignment[]{FIRST, OPTIMAL, FIRST, OPTIMAL, OPTIMAL, OPTIMAL};
    private static final GuardianAssignment[] c2 = new GuardianAssignment[]{CLOSEST, OPTIMAL, FIRST, OPTIMAL, OPTIMAL, OPTIMAL};
    private static final GuardianAssignment[] c3 = new GuardianAssignment[]{FEWEST, OPTIMAL, FIRST, OPTIMAL, OPTIMAL, OPTIMAL};
    private static final GuardianAssignment[] c4 = new GuardianAssignment[]{FIRST, FIRST, FIRST, FIRST, FIRST, FIRST};
    private static final GuardianAssignment[] c5 = new GuardianAssignment[]{CLOSEST, CLOSEST, CLOSEST, CLOSEST, CLOSEST, CLOSEST};
    private static final GuardianAssignment[] c6 = new GuardianAssignment[]{FEWEST, FEWEST, FEWEST, FEWEST, FEWEST, FEWEST};
    private static final GuardianAssignment[] c7 = new GuardianAssignment[]{FEWEST, OPTIMAL, FEWEST, OPTIMAL, OPTIMAL, OPTIMAL};


    private static final List<GuardianAssignment[]> combinations = new ArrayList<>(Arrays.asList(c1, c2, c3, c4, c5, c6));
    private static final ParetoDominanceCounter comparator = new ParetoDominanceCounter();
    private static final List<SimulationRunner> simulations = new ArrayList<>(Arrays.asList(Experiment::testSimulation1, Experiment::testSimulation2, Experiment::testSimulation3, Experiment::testSimulation4));

    private static AnalyticalDistribution distribution;
    private static final int N = 10;
    private static final int DATA_INTERVAL = 1;

    private ExperimentResults results;
    private int dominated;
    private int nonDominated;
    private double c;
    private int dimensions;
    private int combinationIndex;


    public Experiment(int dimensions, int combinationIndex, int dominated, int nonDominated, double c, String filename) {
        this.results = new ExperimentResults(filename, N, 2*(dominated + nonDominated) / DATA_INTERVAL, DATA_INTERVAL);
        this.dominated = dominated;
        this.nonDominated = nonDominated;
        this.c = c;
        this.dimensions = dimensions;
        this.combinationIndex = combinationIndex;
    }

    public static void main(String... args) throws InterruptedException {
        runAllExperiments();
        // runOneExperiment();
       //testC();
        //testLinearList();
    }

    private static void testLinearList() throws InterruptedException {
        int DOMINATED = 48976;
        int NON_DOMINATED = 1024;
        double c = 1.0;
        distribution = new AnalyticalDistribution(2);
        Experiment experiment = new Experiment(2, 0, DOMINATED, NON_DOMINATED, c, "linear.dat");

        experiment.runSimulations();
    }

    private static void runOneExperiment() throws InterruptedException {
        int DOMINATED = 48976;
        int NON_DOMINATED = 1024;
        double c = 1.0;
        distribution = new AnalyticalDistribution(2);
        List<AnalyticalSolution> vectorSequence = distribution.calculateSequence(DOMINATED, NON_DOMINATED, c);
        DynamicArchive<GuardianSolution> population = new GuardianArchive(2, comparator, NondominatedPopulation.DuplicateMode.ALLOW_DUPLICATES, new ManhattanDistance(), c6);
        Experiment experiment = new Experiment(2, 0, DOMINATED, NON_DOMINATED, c, "results.dat");

        experiment.runSimulations();
    }

    private static void testC() {
        double probability;
        double d;
        int nonDominatedPointsSoFar = 0;
        int dominated = 49488;
        int nonDominated = 512;
        Random random = new Random();


        for (double c = 0.1; c < 2.0; c += 0.1) {
            int overAllIndex = 0;
            for (int i = 0; i < 10000; i++) {
                int meanIndex = 0;
                nonDominatedPointsSoFar = 0;
                for (int t = 1; t <= nonDominated + dominated; t++) {

                    probability = c * (((double) (nonDominated - nonDominatedPointsSoFar))) / (((double) (nonDominated + dominated) - t));

                    if (random.nextDouble() < probability) {
                        d = 0; // non dominated, c=1.1, later in sequence
                        nonDominatedPointsSoFar += 1;
                        meanIndex += t;
                    } else {
                        d = 1; /// ((double) (nonDominated + dominated));
                    }
                }
                overAllIndex += meanIndex / nonDominatedPointsSoFar;
            }
            System.out.println("c=" + c + ", index=" + overAllIndex/10000);
        }
    }

    private static void runAllExperiments() throws InterruptedException {
        /*
        int[] DIMENSIONS = new int[]{2};
        double[] c = new double[]{0.5, 1.0, 1.5};
        int[] DOMINATED = new int[]{49872, 49488, 47952};
        int[] NON_DOMINATED = new int[]{128, 512, 2048};
         */

        int[] DIMENSIONS = new int[]{2};
        double[] c = new double[]{1.0};
        int[] DOMINATED = new int[]{49872, 49488, 47952};
        int[] NON_DOMINATED = new int[]{128, 512, 2048};

        //pullOverleaf();

        for (int i = 0; i < DIMENSIONS.length; i++) {
            distribution = new AnalyticalDistribution(DIMENSIONS[i]);
            for (int j = 0; j < c.length; j++) {
                for (int k = 0; k < DOMINATED.length; k++) {

                    for (int l = 0; l < combinations.size()-5; l++) {
                        String filename = "linear_results_c" + (l + 1) + "_D=" + DIMENSIONS[i] + "_NON_DOM=" + NON_DOMINATED[k] + "_c=" + c[j] + ".dat";
                        runExperiment(DIMENSIONS[i], l, DOMINATED[k], NON_DOMINATED[k], c[j], filename);
                    }
                }
            }
        }

        //plotGraphs(simulations.size());
        //updateOverleaf();
    }

    private static void runExperiment(int DIMENSIONS, int combinationIndex, int dominated, int nonDominated, double c, String filename) throws InterruptedException {
        Experiment experiment = new Experiment(DIMENSIONS, combinationIndex, dominated, nonDominated, c, filename);
        experiment.runSimulations();
    }

    private long timeEdit(AnalyticalSolution solution, double[] newObjectives) {
        long cpuTime = bean.getCurrentThreadCpuTime();
        solution.setObjectives(newObjectives);
        return bean.getCurrentThreadCpuTime() - cpuTime;
    }

    private long timeAdd(DynamicArchive<GuardianSolution> population, AnalyticalSolution solution) {
        long cpuTime = bean.getCurrentThreadCpuTime();
        population.add(solution);
        return bean.getCurrentThreadCpuTime() - cpuTime;
    }

    private long testSimulation1(List<AnalyticalSolution> vectorSequence, DynamicArchive<GuardianSolution> population) {
        long time = 0;
        int vectorSequenceSize = vectorSequence.size();
        Random random = new Random();
        int timestep = 1;


        for (int i = 0; i < vectorSequenceSize-1; i++) {
            // add vector with one sample to population
            time += timeAdd(population, vectorSequence.get(i));
            if ((i + 1) % DATA_INTERVAL == 0) {
                results.addData(timestep, comparator.getCounter(), population.size(), population.paretoSize(), time);
            }
            timestep += 1;

            // edit any random vector in population
            AnalyticalSolution vector = vectorSequence.get(random.nextInt(i + 1));

            // get mean of random vector and set it as that vectors sample
            vector.setSample(vector.getMeans().getColumnPackedCopy());
            double[] newObjectives = vector.getSample();

            time += timeEdit(vector, newObjectives);

            if ((i + 1) % DATA_INTERVAL == 0) {
                results.addData(timestep, comparator.getCounter(), population.size(), population.paretoSize(), time);
            }

            timestep += 1;

        }

        return TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS);
    }

    private long testSimulation2(List<AnalyticalSolution> vectorSequence, DynamicArchive<GuardianSolution> population) {
        long time = 0;
        int vectorSequenceSize = vectorSequence.size();
        Random random = new Random();
        int timestep = 1;

        for (int i = 0; i < vectorSequenceSize; i++) {
            // add vector with one sample to population
            time += timeAdd(population, vectorSequence.get(i));
            if ((i + 1) % DATA_INTERVAL == 0) {
                results.addData(timestep, comparator.getCounter(), population.size(), population.paretoSize(), time);
            }
            timestep += 1;

            // edit a pareto vector in population
            AnalyticalSolution vector = (AnalyticalSolution) population.paretoSet().get(random.nextInt(population.paretoSize()));

            // get mean of random vector and set it as that vectors sample
            vector.setSample(vector.getMeans().getColumnPackedCopy());
            double[] newObjectives = vector.getSample();

            time += timeEdit(vector, newObjectives);

            // record data every 100 time steps
            if ((i + 1) % DATA_INTERVAL == 0) {
                results.addData(timestep, comparator.getCounter(), population.size(), population.paretoSize(), time);
            }

            timestep += 1;
        }

        return TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS);
    }

    private long testSimulation3(List<AnalyticalSolution> vectorSequence, DynamicArchive<GuardianSolution> population) {
        long time = 0;
        int vectorSequenceSize = vectorSequence.size();
        Random random = new Random();
        int timestep = 1;

        for (int i = 0; i < vectorSequenceSize; i++) {
            // add vector with one sample to population
            time += timeAdd(population, vectorSequence.get(i));
            if ((i + 1) % DATA_INTERVAL == 0) {
                results.addData(timestep, comparator.getCounter(), population.size(), population.paretoSize(), time);
            }
            timestep += 1;

            // find a random member of population
            AnalyticalSolution vector = vectorSequence.get(random.nextInt(i + 1));

            // sample random vector once
            vector.addSample(distribution.sample(vector.getMeans()));
            double[] newObjectives = vector.getSample();

            time += timeEdit(vector, newObjectives);

            // record data every 100 time steps
            if ((i + 1) % DATA_INTERVAL == 0) {
                results.addData(timestep, comparator.getCounter(), population.size(), population.paretoSize(), time);
            }

            timestep += 1;
        }

        return TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS);
    }

    private long testSimulation4(List<AnalyticalSolution> vectorSequence, DynamicArchive<GuardianSolution> population) {
        long time = 0;
        int vectorSequenceSize = vectorSequence.size();
        Random random = new Random();
        int timestep = 1;

        for (int i = 0; i < vectorSequenceSize; i++) {
            // add vector with one sample to population
            time += timeAdd(population, vectorSequence.get(i));
            if ((i + 1) % DATA_INTERVAL == 0) {
                results.addData(timestep, comparator.getCounter(), population.size(), population.paretoSize(), time);
            }
            timestep += 1;

            // find a pareto vector in population
            AnalyticalSolution vector = (AnalyticalSolution) population.paretoSet().get(random.nextInt(population.paretoSize()));

            // sample pareto vector once
            vector.addSample(distribution.sample(vector.getMeans()));
            double[] newObjectives = vector.getSample();

            time += timeEdit(vector, newObjectives);

            // record data every 100 time steps
            if ((i + 1) % DATA_INTERVAL == 0) {
                results.addData(timestep, comparator.getCounter(), population.size(), population.paretoSize(), time);
            }

            timestep += 1;
        }

        return TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS);
    }

    private void runSimulations() throws InterruptedException {
        for (SimulationRunner simulation : simulations) {
            long total = 0;
            for (int i = 0; i < N; i++) {
                List<AnalyticalSolution> vectorSequence = this.distribution.calculateSequence(this.dominated, this.nonDominated, this.c);
                //DynamicArchive<GuardianSolution> population = new GuardianArchive(dimensions, comparator, NondominatedPopulation.DuplicateMode.ALLOW_DUPLICATES, new EuclideanDistance(), combinations.get(combinationIndex));
                DynamicArchive<GuardianSolution> population = new ListArchive(dimensions, comparator);
                total += simulation.runSimulation(this, vectorSequence, population);
                comparator.resetCount();
            }
            this.results.writeSimulation(total / N);
            this.results.clearData();
        }
    }

    private static void plotGraphs(int simulations) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "graphing.py", " " + simulations, " " + N);
            pb.directory(new File(System.getProperty("user.dir") + "/src/test/resources/"));
            Process p = pb.start();
            int exitCode = p.waitFor();
            assertEquals("No errors should be detected", 0, exitCode);
        } catch (IOException | InterruptedException e) {
            System.out.println(e);
        }
    }

    private static void pullOverleaf() {
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "pull");
            pb.directory(new File(System.getProperty("user.dir") + "/src/test/report/"));
            Process p = pb.start();
            int exitCode = p.waitFor();
            assertEquals("No errors should be detected", 0, exitCode);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateOverleaf() {
        try {
            ProcessBuilder pb;
            Process p;
            int exitCode;

            pb = new ProcessBuilder("git", "add", ".");
            pb.directory(new File(System.getProperty("user.dir") + "/src/test/report/plots"));
            p = pb.start();
            exitCode = p.waitFor();
            assertEquals("No errors should be detected", 0, exitCode);

            pb = new ProcessBuilder("git", "commit", "-m", "new plots");
            pb.directory(new File(System.getProperty("user.dir") + "/src/test/report/plots"));
            p = pb.start();
            exitCode = p.waitFor();
            assertEquals("No errors should be detected", 0, exitCode);

            pb = new ProcessBuilder("git", "push");
            pb.directory(new File(System.getProperty("user.dir") + "/src/test/report/plots"));
            p = pb.start();
            exitCode = p.waitFor();
            assertEquals("No errors should be detected", 0, exitCode);
        } catch (IOException | InterruptedException e) {
            System.out.println(e);
        }
    }

    @FunctionalInterface
    public interface SimulationRunner {
        long runSimulation(Experiment simulation, List<AnalyticalSolution> vectorSequence, DynamicArchive<GuardianSolution> population) throws InterruptedException;
    }
}

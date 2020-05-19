package projectimplementation.empiralAnalysis;

import Jama.Matrix;
import projectimplementation.guardianArchive.GuardianSolution;

public class AnalyticalSolution extends GuardianSolution {
    private final Matrix means;
    private double[] sample;
    private int numSamples;
    //private final GuardianSolution solution;

    AnalyticalSolution(double[] objectives, Matrix means, int dimensions){
        super(objectives);
        this.means = means;
        numSamples = 0;
        this.sample = new double[dimensions];
    }

    public void addSample(double[] sample){
        numSamples += 1;
        for(int i=0; i<sample.length; i++){
            this.sample[i] += (sample[i] - this.sample[i]) / numSamples;
        }
    }

    public Matrix getMeans(){
        return this.means;
    }

    public double[] getSample(){
        return this.sample;
    }

    public void setSample(double[] sample){ this.sample = sample;}
}

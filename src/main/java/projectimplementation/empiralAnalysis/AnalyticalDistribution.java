package projectimplementation.empiralAnalysis;

import Jama.CholeskyDecomposition;
import Jama.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class AnalyticalDistribution {

    private final Random random = new Random();

    private final Matrix onesMatrix;
    private final Matrix covariances;
    private final CholeskyDecomposition l;
    private final int DIMENSIONS;

    public AnalyticalDistribution(int DIMENSIONS) {
        this.DIMENSIONS = DIMENSIONS;

        this.onesMatrix = new Matrix(DIMENSIONS, 1);
        for (int i = 0; i < DIMENSIONS; i++) {
            onesMatrix.set(i, 0, 1.0);
        }
        Matrix identityMatrix = Matrix.identity(DIMENSIONS, DIMENSIONS);

        this.covariances = identityMatrix.minus(onesMatrix.times(onesMatrix.transpose()).times(1 / (double) DIMENSIONS));
        this.l = covariances.chol(); // singular and positive definite
    }

    public List<AnalyticalSolution> calculateSequence(int dominated, int nonDominated, double c) {

        double probability;
        double d;
        int nonDominatedPointsSoFar = 0;

        List<AnalyticalSolution> sequence = new ArrayList<>(nonDominated + dominated + 1);

        for (int t = 1; t <= nonDominated + dominated; t++) {

            probability = c * (((double) (nonDominated - nonDominatedPointsSoFar))) / (((double) (nonDominated + dominated) - t));

            if(random.nextDouble() < probability){
                d = 0; // non dominated, c=1.1, later in sequence
                nonDominatedPointsSoFar += 1;
            } else {
                d = 1; /// ((double) (nonDominated + dominated));
            }

            Matrix means = this.onesMatrix.times((d * (double) (nonDominated + dominated)) / (double) t);
            sequence.add(new AnalyticalSolution(sample(means), means, DIMENSIONS));
        }
        return sequence;
    }

    public void sampleAll(List<AnalyticalSolution> vectors) {
        for (AnalyticalSolution vector : vectors) {
            vector.addSample(this.sample(vector.getMeans()));
        }
    }

    public double[] sample(Matrix means) {
        Matrix z = computeZ();
        Matrix randomMatrixArr = l.getL().times(z).plus(means);
        return randomMatrixArr.getColumnPackedCopy();
    }

    private Matrix computeZ() {
        Matrix z = new Matrix(DIMENSIONS, 1);
        double noise = random.nextGaussian();
        for (int i = 0; i < DIMENSIONS; i++) {
            z.set(i, 0, noise);
        }
        return z;
    }
}

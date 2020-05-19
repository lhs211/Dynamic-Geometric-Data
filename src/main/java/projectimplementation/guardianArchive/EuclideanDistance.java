package projectimplementation.guardianArchive;

/**
 * The Euclidean distance function.
 */
public class EuclideanDistance implements DistanceMetric {

    @Override
    public double distance(DynamicSolution s1, DynamicSolution s2) {
        double distance = 0.0;
        double[] s1Objectives = s1.getObjectives();
        double[] s2Objectives = s2.getObjectives();

        for (int i = 0; i < s1Objectives.length; i++) {
            distance += Math.pow(s1Objectives[i] - s2Objectives[i], 2.0);
        }

        return Math.sqrt(distance);
    }
}

package projectimplementation.guardianArchive;


/**
 * The Manhattan distance function.
 */
public class ManhattanDistance implements DistanceMetric {

    @Override
    public double distance(DynamicSolution s1, DynamicSolution s2) {
        double distance = 0.0;
        double[] s1Objectives = s1.getObjectives();
        double[] s2Objectives = s2.getObjectives();

        for (int i = 0; i < s1Objectives.length; i++) {
            distance += Math.abs(s1Objectives[i] - s2Objectives[i]);
        }

        return Math.round(distance);
    }
}
package projectimplementation.guardianArchive;

/**
 * Interface for provided distance functions.
 */
public interface DistanceMetric {
    /**
     * Computes and returns the distance between between the two solutions objective values
     *
     * @param s1 the first solution containing a location
     * @param s2 the second solution containing a location
     * @return the distance between the two locations in the solutions
     */
    double distance(DynamicSolution s1, DynamicSolution s2);
}

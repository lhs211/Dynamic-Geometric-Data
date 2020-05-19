package projectimplementation.guardianArchive;

/**
 * Options for selecting guardian dominators.
 */
public enum GuardianAssignment {

    /**
     * Choose the first element which we know will dominate the solution.
     */
    OPTIMAL,

    /**
     * Choose the first dominating element from the set.
     */
    FIRST,

    /**
     * Choose the dominating element with the least distance from the set.
     */
    CLOSEST,

    /**
     * Choose the dominating element with the fewest guards from the set.
     */
    FEWEST
}

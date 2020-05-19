package projectimplementation.guardianArchive;

import org.moeaframework.core.NondominatedPopulation.DuplicateMode;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import static org.moeaframework.core.Settings.EPS;

/**
 * Provides the methods required for a non elitist unbounded dynamic archive.
 * @param <T> Only store solutions which extend DynamicSolution.
 */
public abstract class DynamicArchive<T extends DynamicSolution> implements PropertyChangeListener {

    /**
     * The dominance comparator used by this archive.
     */
    protected final DominanceComparator comparator;

    /**
     * The distance metric used by this archive.
     */
    protected final DistanceMetric distanceMetric;

    /**
     * Specifies how duplicate solutions are handled.
     */
    protected final DuplicateMode duplicateMode;

    /**
     * The number of objectives in each solution of this archive.
     */
    public final int NUMBER_OF_OBJECTIVES;

    public DynamicArchive(int numberOfObjectives) {
        this(numberOfObjectives, new ParetoDominanceComparator());
    }

    public DynamicArchive(int numberOfObjectives, DominanceComparator comparator) {
        this(numberOfObjectives, comparator, DuplicateMode.ALLOW_DUPLICATES, new EuclideanDistance());
    }

    public DynamicArchive(int numberOfObjectives, DuplicateMode duplicateMode) {
        this(numberOfObjectives, new ParetoDominanceComparator(), duplicateMode, new EuclideanDistance());
    }

    public DynamicArchive(int numberOfObjectives, DistanceMetric distanceMetric) {
        this(numberOfObjectives, new ParetoDominanceComparator(), DuplicateMode.ALLOW_DUPLICATES, distanceMetric);
    }

    /**
     * Constructs an empty non-dominated population using the specified dominance
     * relation, duplicate mode, and options
     *
     * @param comparator    the dominance relation used by this non-dominated
     *                      population
     * @param duplicateMode specifies how duplicate solutions are handled
     */
    public DynamicArchive(int numberOfObjectives, DominanceComparator comparator, DuplicateMode duplicateMode,
                          DistanceMetric distanceMetric) {
        if (numberOfObjectives > 0) {
            this.NUMBER_OF_OBJECTIVES = numberOfObjectives;
        } else {
            throw new IllegalArgumentException("Number of objectives must be positive");
        }
        this.duplicateMode = duplicateMode;
        this.comparator = comparator;
        this.distanceMetric = distanceMetric;
    }

    /**
     * Constructs a population initialized with a collection of solutions.
     *
     * @param iterable the collection of solutions for initializing this population
     */
    public DynamicArchive(Iterable<? extends T> iterable) {
        this(iterable.iterator().next().getNumberOfObjectives());
        addAll(iterable);
    }

    /**
     * Constructs a population initialized with an array of solutions.
     *
     * @param solutions the array of solutions for initializing this population
     */
    public <K extends T> DynamicArchive(K[] solutions) {
        this(Arrays.asList(solutions));
    }

    public void propertyChange(PropertyChangeEvent evt) {
        objectivesChanged((T) evt.getNewValue());
    }

    /**
     * Adds a collection of solutions to this population.
     *
     * @param solutions the collection of solutions to be added
     * @return {@code true} if the population was modified as a result of this
     * method;
     * {@code false} otherwise
     */
    public boolean addAll(Iterable<? extends T> solutions) {
        for (T solution : solutions) {
            if (!this.add(solution)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds an array of solutions to this population.
     *
     * @param solutions the solutions to be added
     * @return {@code true} if the population was modified as a result of this
     * method; {@code false} otherwise
     */
    public <K extends T> boolean addAll(K[] solutions) {
        for (T solution : solutions) {
            if (!this.add(solution)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns every member of the population.
     *
     * @return an iterable of all the solutions in the population
     */
    public abstract List<T> getPopulation();


    /**
     * Gives the current approximation of the Pareto-optimal front
     *
     * @return An iterable of solutions that make up the Pareto front
     */
    public abstract List<T> paretoSet();

    /**
     * Determines whether a given solution is part of the Pareto-optimal front
     *
     * @param solution the solution to investigate
     * @return {@code true} if the solution is non-dominated
     * {@code false} if the solution is dominated
     */
    public abstract boolean pareto(T solution);

    /**
     * Finds the size of the current approximation of the Pareto-optimal front
     *
     * @return the number of solutions in the current Pareto-optimal front
     */
    public abstract int paretoSize();

    /**
     * Removes all solutions from this population.
     */
    public abstract void clear();

    /**
     * Returns the dominance comparator used by the population.
     *
     * @return the dominance comparator used by the population
     */
    public DominanceComparator getComparator() {
        return comparator;
    }

    /**
     * Returns the distance metric being used by this non-dominated population.
     *
     * @return the distance metric used by this non-dominated population
     */
    public DistanceMetric getDistanceMetric() {
        return distanceMetric;
    }

    /**
     * Checks if the population contains the given solution.
     *
     * @param solution the solution whose presence is tested
     * @return {@code true} if this population contains the specified
     * solution;
     * {@code false} otherwise
     */
    public abstract boolean contains(T solution);

    /**
     * Checks if the population contains all the solutions given.
     *
     * @param solutions the collection whose presence is tested
     * @return {@code true} if this population contains all the solutions in the
     * specified collection;
     * {@code false} otherwise
     */
    public boolean containsAll(Iterable<? extends T> solutions) {
        for (T solution : solutions) {
            if (!this.contains(solution)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if this population contains all the solutions in the
     * specified array; {@code false} otherwise.
     *
     * @param solutions the array whose presence is tested
     * @return {@code true} if this population contains all the solutions in the
     * specified array; {@code false} otherwise
     */
    public <K extends T> boolean containsAll(K[] solutions) {
        for (T solution : solutions) {
            if (!this.contains(solution))
                return false;
        }
        return true;
    }

    /**
     * Returns {@code true} if this population contains no solutions;
     * {@code false} otherwise.
     *
     * @return {@code true} if this population contains no solutions;
     * {@code false} otherwise.
     */
    public abstract boolean isEmpty();

    /**
     * Returns the number of solutions in this population.
     *
     * @return the number of solutions in this population
     */
    public abstract int size();

    /**
     * Edit the specified objective value of the specified solution.
     *
     * @param solution       the solution to edit
     * @param objectiveIndex the index of the objective value to edit
     * @param newObjective   the value of the new objective
     */
    public void editObjective(T solution, int objectiveIndex, double newObjective) {
        solution.setObjective(objectiveIndex, newObjective);
    }

    /**
     * Edit all the objective values of a solution.
     *
     * @param solution      the solution to edit
     * @param newObjectives the values of the new objectives
     */
    public void editObjectives(T solution, double[] newObjectives) {
        solution.setObjectives(newObjectives);
    }

    /**
     * Adds the specified solution to this population.
     *
     * @param solution the solution to be added
     * @return {@code true} if the population was modified as a result of this
     * method;
     * {@code false} otherwise.
     */
    public abstract boolean add(T solution);

    abstract void objectivesChanged(T solution);

    /**
     * Returns {@code true} if the two solutions are duplicates and one should be
     * ignored based on the duplicate mode. Checks for equality of the decision
     * variables.
     *
     * @param s1 the first solution
     * @param s2 the second solution
     * @return {@code true} if the solutions are duplicates; {@code false} otherwise
     */
    protected boolean isDuplicate(T s1, T s2) {
        switch (duplicateMode) {
            case NO_DUPLICATE_OBJECTIVES:
                return new EuclideanDistance().distance(s1, s2) < EPS;
            case ALLOW_DUPLICATE_OBJECTIVES:
                if (s1.getNumberOfVariables() != s2.getNumberOfVariables()) {
                    return false;
                }
                for (int i = 0; i < s1.getNumberOfVariables(); i++) {
                    if (!s1.getVariable(i).equals(s2.getVariable(i))) {
                        return false;
                    }
                }
                return true;
            default:
                return false;
        }
    }
}

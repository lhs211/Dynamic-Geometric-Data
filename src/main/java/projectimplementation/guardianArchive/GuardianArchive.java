package projectimplementation.guardianArchive;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;

import java.util.*;

import static projectimplementation.guardianArchive.GuardianAssignment.*;

/**
 * Concrete Implmentation of a DynamicArchive, which stores GuardianSolutions.
 */
public class GuardianArchive extends DynamicArchive<GuardianSolution> {

    /**
     * Specifies the methodology used to assign guardians
     */
    final GuardianAssignment[] options;

    /**
     * Stores the pareto solutions of this population
     */
    private List<GuardianSolution> paretoSet;

    /**
     * Stores how many solutions are in the archive
     */
    private int size;

    public GuardianArchive(int numberOfObjectives) {
        this(numberOfObjectives, new ParetoDominanceComparator());
    }

    public GuardianArchive(int numberOfObjectives, DominanceComparator comparator) {
        this(numberOfObjectives, comparator, NondominatedPopulation.DuplicateMode.ALLOW_DUPLICATES, new ManhattanDistance(), new GuardianAssignment[]{GuardianAssignment.FIRST,
            GuardianAssignment.FIRST, GuardianAssignment.FIRST, GuardianAssignment.FIRST, GuardianAssignment.FIRST, GuardianAssignment.FIRST});
    }

    public GuardianArchive(int numberOfObjectives, NondominatedPopulation.DuplicateMode duplicateMode) {
        this(numberOfObjectives, new ParetoDominanceComparator(), duplicateMode, new ManhattanDistance(), new GuardianAssignment[]{GuardianAssignment.FIRST,
            GuardianAssignment.FIRST, GuardianAssignment.FIRST, GuardianAssignment.FIRST, GuardianAssignment.FIRST, GuardianAssignment.FIRST});
    }

    public GuardianArchive(int numberOfObjectives, DistanceMetric distanceMetric) {
        this(numberOfObjectives, new ParetoDominanceComparator(), NondominatedPopulation.DuplicateMode.ALLOW_DUPLICATES, distanceMetric, new GuardianAssignment[]{
            GuardianAssignment.FIRST, GuardianAssignment.FIRST, GuardianAssignment.FIRST, GuardianAssignment.FIRST, GuardianAssignment.FIRST, GuardianAssignment.FIRST});
    }

    public GuardianArchive(int numberOfObjectives, GuardianAssignment[] options) {
        this(numberOfObjectives, new ParetoDominanceComparator(), NondominatedPopulation.DuplicateMode.ALLOW_DUPLICATES, new ManhattanDistance(), options);
    }

    public GuardianArchive(int numberOfObjectives, DominanceComparator comparator, NondominatedPopulation.DuplicateMode duplicateMode,
                           DistanceMetric distanceMetric, GuardianAssignment[] options) {
        super(numberOfObjectives, comparator, duplicateMode, distanceMetric);
        if (options[0] == OPTIMAL || options[2] == OPTIMAL) {
            throw new IllegalArgumentException("Option 1 and 3 cannot be optimal");
        }
        this.options = options;
        this.paretoSet = new ArrayList<>();
        this.size = 0;
    }

    /**
     * Constructs a population initialized with a collection of solutions.
     *
     * @param iterable the collection of solutions for initializing this population
     */
    public GuardianArchive(Iterable<? extends GuardianSolution> iterable) {
        this(iterable.iterator().next().getNumberOfObjectives());
        addAll(iterable);
    }

    /**
     * Constructs a population initialized with an array of solutions.
     *
     * @param solutions the array of solutions for initializing this population
     */
    public <T extends GuardianSolution> GuardianArchive(T[] solutions) {
        this(Arrays.asList(solutions));
    }

    /**
     * Returns the set of options being used to assign guardians
     *
     * @return the choices for how to assign guardians
     */
    public GuardianAssignment[] getOptions() {
        return options;
    }

    @Override
    public List<GuardianSolution> getPopulation() {
        List<GuardianSolution> all = new ArrayList<>(paretoSet);
        for (GuardianSolution pareto : paretoSet) {
            Queue<GuardianSolution> queue = new ArrayDeque<>(pareto.getChildren());
            while (!queue.isEmpty()) {
                GuardianSolution child = queue.poll();
                all.add(child);
                queue.addAll(child.getChildren());
            }
        }
        return all;
    }

    @Override
    public boolean contains(GuardianSolution solution) {
        if (paretoSet.contains(solution)) {
            return true;
        } else {
            for (GuardianSolution pareto : paretoSet) {
                Queue<GuardianSolution> queue = new ArrayDeque<>(pareto.getChildren());
                while (!queue.isEmpty()) {
                    GuardianSolution child = queue.poll();
                    if (child == solution) {
                        return true;
                    }
                    queue.addAll(child.getChildren());
                }
            }
        }
        return false;
    }

    @Override
    public List<GuardianSolution> paretoSet() {
        return new ArrayList<>(this.paretoSet);
    }

    @Override
    public boolean pareto(GuardianSolution solution) {
        return !solution.hasParent();
    }

    @Override
    public int paretoSize() {
        return paretoSet.size();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return paretoSet.size() == 0;
    }

    @Override
    public void clear() {
        for (GuardianSolution solution : paretoSet) {
            Queue<GuardianSolution> queue = new ArrayDeque<>(solution.getChildren());
            while (!queue.isEmpty()) {
                GuardianSolution currentNode = queue.poll();
                currentNode.setParent(null);
                queue.addAll(currentNode.getChildren());
                currentNode.removeChildren();
            }
        }
        paretoSet = new ArrayList<>();
        size = 0;
    }

    @Override
    public boolean add(GuardianSolution solution) {
        if (!Objects.nonNull(solution) || solution.getNumberOfObjectives() != this.NUMBER_OF_OBJECTIVES) {
            throw new IllegalArgumentException();
        }

        if (isEmpty()) {
            solution.addPropertyChangeListener(this);
            paretoSet.add(solution);
            size += 1;
            return true;
        }

        if (duplicateMode != NondominatedPopulation.DuplicateMode.ALLOW_DUPLICATES) {
            List<GuardianSolution> solutions = getPopulation();
            for (GuardianSolution duplicateCheck : solutions) {
                if (isDuplicate(solution, duplicateCheck)) {
                    return false;
                }
            }
        }

        solution.addPropertyChangeListener(this);

        int eIndex = checkParetoSet(solution);
        if (eIndex != -1) { // paretoGuardianFound
            GuardianSolution guardian = assignNewSolutionAGuardian(options[1], solution, eIndex);
            guardian.addChild(solution);
            solution.setParent(guardian); // solution is dominated
        } else {
            paretoSet.add(solution);
        }
        size += 1;
        return true;
    }

    /**
     * Determines which course of action to take when an objective's value's change.
     * @param solution the solution whose objective values have changed.
     */
    protected void objectivesChanged(GuardianSolution solution) {
        if (pareto(solution)) {
            paretoSolutionChanged(solution);
        } else {
            dominatedSolutionChanged(solution);
        }
    }

    /**
     * Handles when a pareto solutions objective values have changed.
     * @param solution the solution whose objectives have changed.
     */
    protected void paretoSolutionChanged(GuardianSolution solution) {
        List<GuardianSolution> oldChildren = new ArrayList<>(solution.getChildren());
        solution.removeChildren();
        GuardianSolution newGuardian = editNonDominated(options[2], solution, oldChildren);
        if (newGuardian != null) {
            // it became dominated
            paretoSet.remove(solution);
            solution.setParent(newGuardian);
            newGuardian.addChild(solution);
        }
        updateChildrenOfNonDominatedSolution(options[3], solution, new ArrayList<>(oldChildren));
    }

    /**
     * Handles when a dominated solutions objective values have changed.
     * @param solution the solution whose objectives have changed.
     */
    protected void dominatedSolutionChanged(GuardianSolution solution) {
        List<GuardianSolution> oldChildren = new ArrayList<>(solution.getChildren());
        solution.removeChildren();
        GuardianSolution oldGuardian = solution.getParent();

        GuardianSolution newGuardian = editDominated(options[4], solution, new ArrayDeque<>(oldChildren));

        oldGuardian.removeChild(solution);

        if (newGuardian != null) {
            solution.setParent(newGuardian); // remains dominated
            newGuardian.addChild(solution);
        } else {
            solution.setParent(null); // became non dominated
            paretoSet.add(solution);
        }

        updateChildrenOfDominatedSolution(options[5], solution, new ArrayList<>(oldChildren), oldGuardian);
    }

    /**
     * Checks the pareto set to remove any pareto solutions if required.
     * @param solution the solution to check agains the pareto set.
     * @return the index of the first pareto member which dominates solution.
     * Return -1 otherwise.
     */
    protected int checkParetoSet(GuardianSolution solution) {
        int paretoSize = paretoSize();

        for (int i = 0; i < paretoSize; i++) {
            GuardianSolution currentPareto = paretoSet.get(i);
            int dominance = comparator.compare(solution, currentPareto);

            if (dominance == -1) {
                solution.addChild(currentPareto);
                currentPareto.setParent(solution);

                paretoSet.remove(i);
                i--;
                paretoSize--;
            } else {
                if (dominance == 1) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Finds a guardian, if required, for a non dominated solution that has had its objectives changed.
     * @param option Guardian Assigment method to use.
     * @param solution the solution whose objective values have changed.
     * @param oldChildren the edited solutions old children.
     * @return a guardian for solution, return null otherwise.
     */
    protected GuardianSolution editNonDominated(GuardianAssignment option, GuardianSolution solution, List<GuardianSolution> oldChildren) {
        // search rest of tree, not just other children
        GuardianSolution guardianSibling = searchOtherChildren(option, solution, oldChildren); // returns null if no children
        if (guardianSibling == null) { // other children did not dominate it, will a pareto solution?
            int paretoIndex = checkParetoSet(solution);
            if (paretoIndex == -1) {
                // no pareto element dominated it, stays in pareto set
                return null;
            } else {
                return continueParetoSearch(option, solution, paretoIndex); // find guardian for now dominated solution
            }
        } else {
            paretoSet.remove(solution);
            return guardianSibling;
        }
    }

    /**
     * Finds a guardian, if required, for a dominated solution that has had its objectives changed.
     * @param option GuardianAssigment method to use.
     * @param solution the solution whose objective values have changed.
     * @param oldChildren the edited solutions old children.
     * @return a guardian for solution, return null otherwise.
     */
    protected GuardianSolution editDominated(GuardianAssignment option, GuardianSolution solution, Queue<GuardianSolution> oldChildren) {

        int paretoIndex = checkParetoSet(solution);
        if (paretoIndex == -1) {
            // no pareto element dominated it, add to pareto set
            return null;
        } else {
            // option 5 here
            return findGuardianForEditedDominatedSolution(option, solution, paretoIndex, oldChildren);
            //return continueParetoSearch(option, solution, paretoIndex); // find guardian for now dominated solution
        }

    }

    /**
     * Assign a parent for an edited solutions old children.
     * @param option GuardianAssigment method to use.
     * @param solution the solution whose objective values have changed.
     * @param oldChildren the edited solutions old children.
     */
    protected void updateChildrenOfNonDominatedSolution(GuardianAssignment option, GuardianSolution solution, List<GuardianSolution> oldChildren) {
        int numChildren = oldChildren.size();

        for (int i = 0; i < numChildren; i++) {
            GuardianSolution oldChild = oldChildren.get(i);
            GuardianSolution newGuardian = findGuardianForNonDominatedChild(option, solution, oldChild, oldChildren);

            oldChild.setParent(newGuardian);
            if (newGuardian == null) {
                paretoSet.add(oldChild); // child becomes non dominated
            } else {
                newGuardian.addChild(oldChild); // child remains dominated
            }
        }
    }

    /**
     * Finds a guardian for an individual child of an edited non dominated solution.
     * @param option GuardianAssigment method to use.
     * @param solution the solution who is the old parent of the child.
     * @param oldChild the old child of solution requiring a guardian.
     * @param oldChildren the other old children of the solution.
     * @return a guardian for the old child if required.
     */
    private GuardianSolution findGuardianForNonDominatedChild(GuardianAssignment option, GuardianSolution solution, GuardianSolution oldChild, List<GuardianSolution> oldChildren) {
        GuardianSolution siblingGuardian = searchOtherChildren(option, oldChild, oldChildren);

        if (siblingGuardian == null) { // other child does not dominate it, only check pareto if no other child dominates it

            // find dominating pareto element
            int paretoIndex = findParetoGuardianOfOldNonDominatedChild(oldChild);

            if (paretoIndex == -1) { // pareto set did not dominate it
                return null;
            } else { // pareto set dominated old child
                if (comparator.compare(solution, oldChild) == -1) {
                    return solution;
                } else {
                    return continueParetoSearch(option, oldChild, paretoIndex);
                }
            }
        } else { // another child dominated old child
            return siblingGuardian;
        }
    }

    /**
     * Find the index of the first member of the pareto set to dominate oldChild.
     * @param oldChild to compare against the pareto set.
     * @return the index of the pareto element which dominates old child, Return -1 otherwise.
     */
    private int findParetoGuardianOfOldNonDominatedChild(GuardianSolution oldChild) {

        int paretoSize = paretoSet.size();
        for (int j = 0; j < paretoSize; j++) {
            GuardianSolution currentPareto = paretoSet.get(j);

            int dominance = comparator.compare(oldChild, currentPareto);
            if (dominance == 1) {
                return j;
            }
        }
        return -1;
    }

    /**
     * Search the old child's siblings to see if they can become the guardian dominator.
     * @param oldChild the old child to compare against it's siblings.
     * @param children the siblings of old child.
     * @param option GuardianAssigment method to use.
     * @return a guardian solution for old child.
     */
    private GuardianSolution searchOtherChildren(GuardianAssignment option, GuardianSolution oldChild, List<GuardianSolution> children) {
        double closestDistance = Double.MAX_VALUE;
        int fewestGuards = Integer.MAX_VALUE;
        GuardianSolution siblingGuardian = null;
        for (int j = 0; j < children.size(); j++) {
            GuardianSolution otherChild = children.get(j);
            int dominance = comparator.compare(otherChild, oldChild);
            if (dominance == -1) {
                switch (option) {
                    case OPTIMAL:
                    case FIRST:
                        return otherChild;
                    case CLOSEST:
                        double currDistance = distanceMetric.distance(oldChild, otherChild);
                        if (currDistance < closestDistance) {
                            closestDistance = currDistance;
                            siblingGuardian = otherChild;
                        }
                        break;
                    case FEWEST:
                        int numChildren = oldChild.getNumberOfChildren();
                        if (numChildren < fewestGuards) {
                            if (numChildren == 0) {
                                return otherChild;
                            }
                            fewestGuards = otherChild.getNumberOfChildren();
                            siblingGuardian = otherChild;
                        }
                        break;
                }
            }
        }
        return siblingGuardian;
    }

    /**
     * Find guardians for the old children of a dominated solution.
     * @param option GuardianAssigment method to use.
     * @param solution the solution whose objective values have changed.
     * @param oldChildren the children of solution before its objective values changed.
     * @param oldGuardian the guardian of solution before the edit operation.
     */
    protected void updateChildrenOfDominatedSolution(GuardianAssignment option, GuardianSolution solution, List<GuardianSolution> oldChildren, GuardianSolution oldGuardian) {
        int numChildren = oldChildren.size();

        for (int i = 0; i < numChildren; i++) {
            GuardianSolution oldChild = oldChildren.get(i);

            GuardianSolution newGuardian = chooseGuardianForChild(option, solution, oldGuardian, oldChild, oldChildren);

            oldChild.setParent(newGuardian); // child can only remain dominated
            newGuardian.addChild(oldChild);
        }
    }

    /**
     * Choose a guardian for a child from the available options.
     * @param option GuardianAssigment method.
     * @param solution the solutions whose objective values have changed.
     * @param oldGuardian the guardian of solution before its objective values changed.
     * @param oldChild an old child of solution before its objective values changed.
     * @param oldChildren the children of solution before its objective values changed.
     * @return a guardian for old child.
     */
    protected GuardianSolution chooseGuardianForChild(GuardianAssignment option, GuardianSolution solution, GuardianSolution oldGuardian, GuardianSolution oldChild, List<GuardianSolution> oldChildren) {
        GuardianSolution siblingGuardian;
        switch (option) {
            case OPTIMAL:
                if (comparator.compare(solution, oldChild) == -1) {
                    return solution;
                } else {
                    return oldGuardian;
                }
            case FIRST:
                siblingGuardian = searchOtherChildren(option, oldChild, oldChildren);
                if (siblingGuardian == null) {
                    if (comparator.compare(solution, oldChild) == -1) {
                        return solution;
                    } else {
                        return oldGuardian;
                    }
                } else {
                    return siblingGuardian;
                }
            case CLOSEST:
            case FEWEST:
                siblingGuardian = searchOtherChildren(option, oldChild, oldChildren);
                if (siblingGuardian == null) {
                    if (comparator.compare(solution, oldChild) == -1) {
                        return solution;
                    } else {
                        if (comparator.compare(oldGuardian, oldChild) == -1) {
                            return oldGuardian;
                        } else {
                            return findParetoGuardianOfOldDominatedChild(oldChild, option);
                        }
                    }
                } else {
                    return siblingGuardian;
                }
            default:
                return null;
        }
    }

    /**
     * Find a guardian from the pareto set.
     * @param oldChild child of a changed solution.
     * @param option GuardianAssigment method.
     * @return a guardian for oldChild
     */
    protected GuardianSolution findParetoGuardianOfOldDominatedChild(GuardianSolution oldChild, GuardianAssignment option) {
        GuardianSolution paretoGuardian = null;
        double closestDistance = Double.MAX_VALUE;
        int fewestGuards = Integer.MAX_VALUE;

        int paretoSize = paretoSet.size();
        for (int j = 0; j < paretoSize; j++) {
            GuardianSolution currentPareto = paretoSet.get(j);

            int dominance = comparator.compare(oldChild, currentPareto);

            if (dominance == 1) {
                switch (option) {
                    case FIRST:
                        return currentPareto;
                    case CLOSEST:
                        double currDistance = distanceMetric.distance(oldChild, currentPareto);
                        if (currDistance < closestDistance) {
                            closestDistance = currDistance;
                            paretoGuardian = currentPareto;
                        }
                        break;
                    case FEWEST:
                        int numGuarded = currentPareto.getNumberOfChildren();
                        if (numGuarded < fewestGuards) {
                            fewestGuards = numGuarded;
                            paretoGuardian = currentPareto;
                            if (fewestGuards == 0) {
                                return paretoGuardian;
                            }
                        }
                        break;
                }
            }
        }
        return paretoGuardian;
    }



    /**
     * Searches the entire subtree for a solution which dominates.
     * @param option GuardianAssignment method.
     * @param solution the solution to compare against the tree.
     * @param children the children of the root of the tree.
     * @return a member of the tree which dominates solution.
     */
    protected GuardianSolution searchWholeTree(GuardianAssignment option, GuardianSolution solution, Queue<GuardianSolution> children) {
        if (children.size() == 0) {
            return null;
        }

        double closest = Double.MAX_VALUE;
        int fewestGuards = Integer.MAX_VALUE;
        GuardianSolution treeGuardian = null;

        GuardianSolution currentNode;
        while (!children.isEmpty()) {
            currentNode = children.poll();
            if (comparator.compare(currentNode, solution) == -1) {
                switch (option) {
                    case FIRST:
                        treeGuardian = currentNode;
                    case CLOSEST:
                        double currDistance = distanceMetric.distance(currentNode, solution);
                        if (currDistance < closest) {
                            closest = currDistance;
                            treeGuardian = currentNode;
                        }
                    case FEWEST:
                        int numChildren = currentNode.getNumberOfChildren();
                        if (numChildren < fewestGuards) {
                            fewestGuards = numChildren;
                            treeGuardian = currentNode;
                            if (fewestGuards == 0) {
                                return treeGuardian;
                            }
                        }
                }
                children.addAll(currentNode.getChildren());
            }
        }
        return treeGuardian;
    }

    /**
     * Chooses a guardian for a new solution.
     * @param option GuardianAssigment method.
     * @param solution the solution requiring a guardian.
     * @param rootIndex the index of the first pareto member to dominate solution.
     * @return a guardian for solution.
     */
    protected GuardianSolution assignNewSolutionAGuardian(GuardianAssignment option, GuardianSolution solution, int rootIndex) {
        switch (option) {
            case OPTIMAL:
                return paretoSet.get(rootIndex);
            case FIRST:
                GuardianSolution first = searchWholeTree(option, solution, new ArrayDeque<>(paretoSet.get(rootIndex).getChildren()));
                return Objects.requireNonNullElse(first, paretoSet.get(rootIndex));
            case CLOSEST:
            case FEWEST:
                GuardianSolution closest = searchWholeTree(option, solution, new ArrayDeque<>(paretoSet.get(rootIndex).getChildren()));
                return Objects.requireNonNullElse(closest, continueParetoSearch(option, solution, rootIndex));
            default:
                return null;
        }
    }

    /**
     * Continue searching the pareto set from the given index.
     * @param option GuardianAssigment method.
     * @param solution the solution requiring a guardian.
     * @param rootIndex the index of the pareto set to continue the search from.
     * @return a guardian from the Pareto set for solution.
     */
    private GuardianSolution continueParetoSearch(GuardianAssignment option, GuardianSolution solution, int rootIndex) {
        double closestDistance = Double.MAX_VALUE;
        int fewestGuards = Integer.MAX_VALUE;
        GuardianSolution paretoGuardian = null;

        int paretoSize = paretoSet.size();
        for (int i = rootIndex; i < paretoSize; i++) {
            GuardianSolution currentPareto = paretoSet.get(i);
            int dominance = comparator.compare(currentPareto, solution);
            if (dominance == -1) {
                switch (option) {
                    case OPTIMAL:
                    case FIRST:
                        return currentPareto;
                    case CLOSEST:
                        double currDistance = distanceMetric.distance(solution, currentPareto);
                        if (currDistance < closestDistance) {
                            closestDistance = currDistance;
                            paretoGuardian = currentPareto;
                        }
                    case FEWEST:
                        int numChildren = currentPareto.getNumberOfChildren();
                        if (numChildren < fewestGuards) {
                            fewestGuards = numChildren;
                            paretoGuardian = currentPareto;
                            if (fewestGuards == 0) {
                                return currentPareto;
                            }
                        }
                }
            }
        }
        return paretoGuardian;
    }

    /**
     * Chooses a guardian for an edited dominated solution.
     * @param option GuardianAssigment method.
     * @param solution the solution to dominate
     * @param paretoIndex the index of the first pareto element to dominate solution.
     * @param oldChildren the children of solution before the edit operation.
     * @return a guardian for solution.
     */
    protected GuardianSolution findGuardianForEditedDominatedSolution(GuardianAssignment option, GuardianSolution solution, int paretoIndex, Queue<GuardianSolution> oldChildren) {
        if (option == OPTIMAL) {
            if (comparator.compare(solution.getParent(), solution) == -1) {
                return solution.getParent();
            } else {
                return continueParetoSearch(option, solution, paretoIndex);
            }
        } else {
            GuardianSolution treeGuardian = searchWholeTree(option, solution, new ArrayDeque<>(oldChildren));
            GuardianSolution oldGuardian = solution.getParent();
            if (treeGuardian != null) {
                return treeGuardian;
            } else {
                if (comparator.compare(oldGuardian, solution) == -1) {
                    return oldGuardian;
                } else {
                    return continueParetoSearch(option, solution, paretoIndex);
                }
            }
        }
    }
}

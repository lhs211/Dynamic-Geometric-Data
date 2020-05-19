package projectimplementation.guardianArchive;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ListArchive extends DynamicArchive<GuardianSolution> {

    private List<GuardianSolution> paretoSet;
    private List<GuardianSolution> dominatedSet;

    public ListArchive(int numberOfObjectives) {
        this(numberOfObjectives, new ParetoDominanceComparator());
    }

    public ListArchive(int numberOfObjectives, DominanceComparator comparator) {
        this(numberOfObjectives, comparator, NondominatedPopulation.DuplicateMode.ALLOW_DUPLICATES, new ManhattanDistance());
    }

    public ListArchive(int numberOfObjectives, NondominatedPopulation.DuplicateMode duplicateMode) {
        this(numberOfObjectives, new ParetoDominanceComparator(), duplicateMode, new ManhattanDistance());
    }

    public ListArchive(int numberOfObjectives, DistanceMetric distanceMetric) {
        this(numberOfObjectives, new ParetoDominanceComparator(), NondominatedPopulation.DuplicateMode.ALLOW_DUPLICATES, distanceMetric);
    }

    public ListArchive(int numberOfObjectives, DominanceComparator comparator, NondominatedPopulation.DuplicateMode duplicateMode,
                           DistanceMetric distanceMetric) {
        super(numberOfObjectives, comparator, duplicateMode, distanceMetric);
        this.paretoSet = new ArrayList<>();
        this.dominatedSet = new ArrayList<>();
    }

    @Override
    public List<GuardianSolution> getPopulation() {
        List<GuardianSolution> population = new ArrayList<>(dominatedSet);
        population.addAll(paretoSet);
        return population;
    }

    @Override
    public List<GuardianSolution> paretoSet() {
        return paretoSet;
    }

    @Override
    public boolean pareto(GuardianSolution solution) {
        return paretoSet.contains(solution);
    }

    @Override
    public int paretoSize() {
        return paretoSet.size();
    }

    @Override
    public void clear() {
        paretoSet = new ArrayList<>();
        dominatedSet = new ArrayList<>();
    }

    @Override
    public boolean contains(GuardianSolution solution) {
        return paretoSet.contains(solution) || dominatedSet.contains(solution);
    }

    @Override
    public boolean isEmpty() {
        return paretoSet.size() == 0 && dominatedSet.size() == 0;
    }

    @Override
    public int size() {
        return paretoSet.size() + dominatedSet.size();
    }

    @Override
    public boolean add(GuardianSolution solution) {
        if (!Objects.nonNull(solution) || solution.getNumberOfObjectives() != this.NUMBER_OF_OBJECTIVES) {
            throw new IllegalArgumentException();
        }

        if (isEmpty()) {
            solution.addPropertyChangeListener(this);
            paretoSet.add(solution);
            return true;
        }

        solution.addPropertyChangeListener(this);

        boolean pareto = becomesPareto(solution);
        if(!pareto){
            dominatedSet.add(solution);
        } else {
            paretoSet.add(solution);
        }
        return true;
    }

    private boolean becomesPareto(GuardianSolution solution){
        int paretoSize = paretoSize();

        for (int i = 0; i < paretoSize; i++) {
            GuardianSolution currentPareto = paretoSet.get(i);
            int dominance = comparator.compare(solution, currentPareto);

            if (dominance == -1) {
                dominatedSet.add(paretoSet.get(i));
                paretoSet.remove(i);
                i--;
                paretoSize--;
            } else {
                if (dominance == 1) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    void objectivesChanged(GuardianSolution solution) {
        if (pareto(solution)) {
            paretoSolutionChanged(solution);
        } else {
            dominatedSolutionChanged(solution);
        }
    }

    private void paretoSolutionChanged(GuardianSolution solution){
        // does it remain pareto

        // check if dominated by pareto set
        boolean paretoDominates = becomesPareto(solution);

        // check if dominated by dominated solution
        boolean dominatedDominates = nonDominatedBecomesDominated(solution);

        if(dominatedDominates || !paretoDominates){
            paretoSet.remove(solution);
            dominatedSet.add(solution);
        }
    }

    private boolean nonDominatedBecomesDominated(GuardianSolution solution){
        int dominatedSize = dominatedSet.size();
        boolean dominatedBy = false;

        for (int i = 0; i < dominatedSize; i++) {
            GuardianSolution dominated = dominatedSet.get(i);
            int dominance = comparator.compare(solution, dominated);

            if (dominance == 1) {
                dominatedBy = true;

                // does dominate become pareto?
                if(becomesPareto(dominated)){
                    paretoSet.add(dominated);
                    dominatedSet.remove(dominated);
                    i--;
                    dominatedSize--;
                }
            } else {
                if (dominance == 0){
                    // does dominated, mutually non dominate every pareto?
                    boolean mutual = true;
                    for(GuardianSolution pareto : paretoSet){
                        if(comparator.compare(dominated, pareto) != 0){
                            mutual = false;
                        }
                    }
                    if(mutual){
                        paretoSet.add(dominated);
                        dominatedSet.remove(dominated);
                        i--;
                        dominatedSize--;
                    }
                }
            }
        }
        return dominatedBy;
    }

    /**
     * 
     * @param solution
     */
    private void dominatedSolutionChanged(GuardianSolution solution){
        // check if dominated by pareto set
        boolean solutionPareto = becomesPareto(solution);

        if(solutionPareto){
            paretoSet.add(solution);
            dominatedSet.remove(solution);
        }
    }
}

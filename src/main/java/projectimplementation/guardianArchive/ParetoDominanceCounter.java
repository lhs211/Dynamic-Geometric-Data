package projectimplementation.guardianArchive;

import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;

public class ParetoDominanceCounter implements DominanceComparator {

    private final ParetoDominanceComparator comparator;
    private long counter;

    public ParetoDominanceCounter() {
        this.comparator = new ParetoDominanceComparator();
        this.counter = 0;
    }

    @Override
    public int compare(Solution solution, Solution solution1) {
        counter+=1;
        return comparator.compare(solution, solution1);
    }

    public long getCounter() {
        return counter;
    }

    public void resetCount() {
        this.counter = 0;
    }
}

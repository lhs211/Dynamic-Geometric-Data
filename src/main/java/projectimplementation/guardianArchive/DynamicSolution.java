package projectimplementation.guardianArchive;

import org.moeaframework.core.Solution;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * A dynamic solution to an optimization problem which allows
 * the objective values to change throughout the solutions lifespan
 */
public abstract class DynamicSolution extends Solution {

    /**
     * The property change listener for objectives.
     */
    protected PropertyChangeSupport changes;

    /**
     * Constructs a dynamic solution with the specified number of variables and
     * objectives with no constraints.
     *
     * @param numberOfVariables the number of variables defined by this solution
     * @param numberOfObjectives the number of objectives defined by this
     *        solution
     */
    public DynamicSolution(int numberOfVariables, int numberOfObjectives) {
        super(numberOfVariables, numberOfObjectives, 0);
        this.changes = new PropertyChangeSupport(this);
    }

    /**
     * Constructs a dynamic solution with the specified number of variables, objectives
     * and constraints.
     *
     * @param numberOfVariables the number of variables defined by this solution
     * @param numberOfObjectives the number of objectives defined by this
     *        solution
     * @param numberOfConstraints the number of constraints defined by this
     *        solution
     */
    public DynamicSolution(int numberOfVariables, int numberOfObjectives, int numberOfConstraints) {
        super(numberOfVariables, numberOfObjectives, numberOfConstraints);
        this.changes = new PropertyChangeSupport(this);
    }

    /**
     * Constructs a dynamic solution with no variables and the specified objectives.
     * This is intended for creating reference set solutions.
     *
     * @param objectives the objectives to be stored in this solution
     */
    public DynamicSolution(double[] objectives) {
        super(objectives);
        this.changes = new PropertyChangeSupport(this);
    }

    /**
     * Constructs a dynamic solution from an existing solution.
     *
     * @param solution the solution being copied to the dynamic solution
     */
    public DynamicSolution(Solution solution) {
        super(solution);
        this.changes = new PropertyChangeSupport(this);
    }

    /**
     * Attaches the given property change listener to an existing solution to create a dynamic solution.
     *
     * @param solution an existing solution
     * @param listeners an array of property change listeners.
     */
    protected DynamicSolution(Solution solution, PropertyChangeListener[] listeners) {
        this(solution);
        for (PropertyChangeListener listener : listeners) {
            this.addPropertyChangeListener(listener);
        }
    }

    /**
     * Attaches a property change listener to an existing dynamic solution.
     * @param solution an existing DynamicSolution
     */
    protected DynamicSolution(DynamicSolution solution) {
        this(solution, solution.getPropertyChangeListener());
    }

    /**
     * Returns an independent copy of this solution. It is required that
     * {@code x.copy()} is completely independent from {@code x} . This means
     * any method invoked on {@code x.copy()} in no way alters the state of
     * {@code x} and vice versa. It is typically the case that
     * {@code x.copy().getClass() == x.getClass()} and
     * {@code x.copy().equals(x)}
     * <p>
     * Note that a solution's attributes are not copied, as the attributes are
     * generally specific to each instance.
     *
     * @return an independent copy of this solution
     */
    public abstract DynamicSolution copy();

    /**
     * Similar to {@link #copy()} except all attributes are also copied.  As a
     * result, this method tends to be significantly slower than {@code copy()}
     * if many large objects are stored as attributes.
     *
     * @return an independent copy of this solution
     */
    public abstract DynamicSolution deepCopy();

    /**
     * Adds a listener to detect when a change occurs to the objectives attribute.
     *
     * @param l the listener observing the attribute
     */
    protected void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    /**
     * Gets the property change listeners associated with this class
     *
     * @return an array of property change listeners
     */
    protected PropertyChangeListener[] getPropertyChangeListener() {
        return changes.getPropertyChangeListeners();
    }

    /**
     * Set the objective and fire property change provided this class contains a listener
     *
     * @param index     the index of the objective to change
     * @param objective the objective value
     */
    @Override
    public void setObjective(int index, double objective) {
        double oldObjective = this.getObjective(index);
        super.setObjective(index, objective);
        if (changes != null) {
            if (changes.getPropertyChangeListeners().length != 0) {
                changes.firePropertyChange("objectives", oldObjective, this);
            }
        }
    }

    /**
     * Sets the new objective values and fires property change provided this class contains a listener
     *
     * @param objectives the new objective values
     */
    @Override
    public void setObjectives(double[] objectives) {
        double[] oldObjectives = this.getObjectives();
        super.setObjectives(objectives);
        if (changes != null) {
            if (changes.getPropertyChangeListeners().length != 0) {
                changes.firePropertyChange("objectives", oldObjectives, this);
            }
        }
    }
}

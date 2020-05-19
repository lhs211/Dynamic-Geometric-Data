package projectimplementation.guardianArchive;

import org.apache.commons.lang3.SerializationUtils;
import org.moeaframework.core.Solution;

import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GuardianSolution extends DynamicSolution {

    private List<GuardianSolution> children;
    private GuardianSolution parent;

    /**
     * Constructs a guardian solution with the specified number of variables and
     * objectives with no constraints.
     *
     * @param numberOfVariables the number of variables defined by this solution
     * @param numberOfObjectives the number of objectives defined by this
     *        solution
     */
    public GuardianSolution(int numberOfVariables, int numberOfObjectives) {
        super(numberOfVariables, numberOfObjectives, 0);
        children = new LinkedList<>();
    }

    /**
     * Constructs a guardian solution with the specified number of variables, objectives
     * and constraints.
     *
     * @param numberOfVariables the number of variables defined by this solution
     * @param numberOfObjectives the number of objectives defined by this
     *        solution
     * @param numberOfConstraints the number of constraints defined by this
     *        solution
     */
    public GuardianSolution(int numberOfVariables, int numberOfObjectives, int numberOfConstraints) {
        super(numberOfVariables, numberOfObjectives, numberOfConstraints);
        children = new LinkedList<>();
    }

    /**
     * Constructs a guardian solution with no variables and the specified objectives.
     * This is intended for creating reference set solutions.
     *
     * @param objectives the objectives to be stored in this solution
     */
    public GuardianSolution(double[] objectives) {
        super(objectives);
        children = new LinkedList<>();
    }

    /**
     * Constructs a guardian solution from an existing solution.
     *
     * @param solution the solution being copied to the dynamic solution
     */
    public GuardianSolution(Solution solution) {
        super(solution);
        children = new LinkedList<>();
    }

    /**
     * Attaches the given property change listener to an existing solution to create a guardian solution.
     *
     * @param solution an existing solution
     * @param listeners an array of property change listeners.
     */
    private GuardianSolution(Solution solution, PropertyChangeListener[] listeners) {
        super(solution, listeners);
        children = new LinkedList<>();
    }

    /**
     * Attaches a property change listener to an existing guardian solution.
     * @param solution an existing DynamicSolution
     */
    public GuardianSolution(GuardianSolution solution) {
        this(solution, solution.getPropertyChangeListener());
    }

    @Override
    public GuardianSolution copy() {
        return new GuardianSolution(this);
    }

    @Override
    public GuardianSolution deepCopy() {
        GuardianSolution copy = copy();

        for (Map.Entry<String, Serializable> entry : getAttributes().entrySet()) {
            copy.setAttribute(
                entry.getKey(),
                SerializationUtils.clone(entry.getValue()));
        }

        return copy;
    }

    /**
     * Gets the parent solution of this node.
     *
     * @return this nodes parent node
     */
    protected GuardianSolution getParent() {
        return this.parent;
    }

    /**
     * Finds if this solution has a parent
     *
     * @return {@code true} if this solution has a parent
     * {@code false} otherwise
     */
    protected boolean hasParent() {
        return parent != null;
    }

    /**
     * Sets this solutions parent.
     *
     * @param newParent the solution to set as the parent
     */
    protected void setParent(GuardianSolution newParent) {
        this.parent = newParent;
    }

    /**
     * Finds the children of this solution.
     *
     * @return a list of child solutions
     */
    protected List<GuardianSolution> getChildren() {
        return this.children;
    }

    /**
     * Finds the number of children this solution has.
     *
     * @return this solutions number of children
     */
    protected int getNumberOfChildren() {
        return children.size();
    }

    /**
     * Adds a child solution to the current solution.
     *
     * @param child the solution to add as a child
     */
    protected void addChild(GuardianSolution child) {
        children.add(child);
    }

    /**
     * Removes a child solution from the set of children.
     *
     * @param child the child to remove
     */
    protected void removeChild(GuardianSolution child) {
        children.remove(child);
    }

    /**
     * Removes a child solution from the set of children.
     *
     * @param index the index of the child to remove
     */
    protected void removeChild(int index) {
        children.remove(index);
    }

    /**
     * Removes all child nodes.
     */
    protected void removeChildren() {
        children = new LinkedList<>();
    }

    protected boolean hasChild(GuardianSolution child){
        return children.contains(child);
    }
}

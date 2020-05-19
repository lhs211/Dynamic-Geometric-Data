//package projectimplementation.guardian_archive;
package guardianArchive;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.variable.RealVariable;
import projectimplementation.guardianArchive.DynamicArchive;
import projectimplementation.guardianArchive.DynamicSolution;
import projectimplementation.guardianArchive.GuardianSolution;
import projectimplementation.guardianArchive.GuardianArchive;

import static org.junit.jupiter.api.Assertions.*;
import static org.moeaframework.core.Settings.EPS;

/**
 * Tests the {@link DynamicSolution} class. Due to the central role of this class, many
 * obvious properties are tested to ensure complete correctness.
 */
public class DynamicSolutionImplTest {

    /**
     * The shared solution used by these tests.
     */
    private DynamicSolution solution;

    /**
     * Constructs the shared solution used by these tests.
     */
    @BeforeEach
    public void setUp() {
        solution = new GuardianSolution(1, 2, 2);

        solution.setVariable(0, new RealVariable(0.5, 0.0, 1.0));
        solution.setObjective(0, 1.0);
        solution.setObjective(1, 2.0);
        solution.setConstraint(0, 0.0);
        solution.setConstraint(1, 1.0);

        solution.setAttribute("foo", "bar");
    }

    /**
     * Removes references to shared objects so they can be garbage collected.
     */
    @AfterEach
    public void tearDown() {
        solution = null;
    }

    /**
     * Tests if the constructor, when given an array of objectives, correctly
     * initializes the solution.
     */
    @Test
    public void testObjectiveConstructor() {
        double[] objectives = new double[]{1.0, 2.0};
        DynamicSolution solution = new GuardianSolution(objectives);

        // correct internal state
        assertEquals(0, solution.getNumberOfVariables());
        assertEquals(2, solution.getNumberOfObjectives());
        assertEquals(0, solution.getNumberOfConstraints());
        assertEquals(0, solution.getAttributes().size());
        assertEquals(1.0, solution.getObjective(0), EPS);
        assertEquals(2.0, solution.getObjective(1), EPS);

        // check if objectives were defensively copied
        objectives[0] = 0.0;
        assertEquals(1.0, solution.getObjective(0), EPS);
    }

    /**
     * Tests if the copy constructor works correctly.
     */
    @Test
    public void testCopyConstructor() {
        DynamicSolution copy = new GuardianSolution(solution);

        // the equals method is based on object identity
        assertNotEquals(solution, copy);
        assertNotEquals(copy, solution);

        // copy has the same variables
        assertEquals(solution.getNumberOfVariables(), copy
            .getNumberOfVariables());
        for (int i = 0; i < copy.getNumberOfVariables(); i++) {
            assertEquals(solution.getVariable(i), copy.getVariable(i));
        }

        // copy has the same objectives
        assertEquals(solution.getNumberOfObjectives(), copy
            .getNumberOfObjectives());
        for (int i = 0; i < copy.getNumberOfObjectives(); i++) {
            assertEquals(solution.getObjective(i), copy.getObjective(i), EPS);
        }

        // copy has the same constraints
        assertEquals(solution.getNumberOfConstraints(), copy
            .getNumberOfConstraints());
        for (int i = 0; i < copy.getNumberOfConstraints(); i++) {
            assertEquals(solution.getConstraint(i), copy.getConstraint(i), EPS);
        }

        // the copy's variables are independent from the original
        ((RealVariable) copy.getVariable(0)).setValue(1.0);
        assertEquals(0.5, ((RealVariable) solution.getVariable(0))
            .getValue(), EPS);


        // the equals method works to detect the change
        assertNotEquals(solution, copy);
        assertNotEquals(copy, solution);
    }

    /**
     * Tests if the deep copy method works correctly, property cloning all
     * attributes.
     */
    @Test
    public void testDeepCopy() {
        double[] array = new double[]{1.0, 2.0};
        solution.setAttribute("key", array);

        DynamicSolution copy = solution.deepCopy();

        assertNotSame(array, copy.getAttribute("key"));
    }

    /**
     * Tests if the {@code setObjective} method sets the value correctly.
     */
    @Test
    public void testSetObjective() {
        solution.setObjective(1, 1.5);
        assertEquals(1.5, solution.getObjective(1), EPS);
    }

    /**
     * Tests if the {@code setObjective} method correctly detects invalid
     * indices.
     */
    @Test
    public void testSetObjectiveBoundsChecking1() {
        assertThrows(IndexOutOfBoundsException.class, () -> solution.setObjective(2, 1.0));
    }

    /**
     * Tests if the {@code setObjective} method correctly detects invalid
     * indices.
     */
    @Test
    public void testSetObjectiveBoundsChecking2() {
        assertThrows(IndexOutOfBoundsException.class, () -> solution.setObjective(-1, 1.0));
    }

    /**
     * Property change listener
     */
    @Test
    public void testSetObjectiveUpdateArchive() {
        DynamicArchive archive = new GuardianArchive(2, NondominatedPopulation.DuplicateMode.ALLOW_DUPLICATE_OBJECTIVES);
        DynamicSolution solution2 = new GuardianSolution(new double[]{2.0, 1.0});
        archive.add(solution);
        archive.add(solution2);
        assertEquals(1, archive.paretoSize());
        solution.setObjective(1, 0.5);
        assertEquals(0.5, solution.getObjective(1), EPS);
        assertEquals(1, archive.paretoSize());
    }

    /**
     * Tests if the {@code setObjectives} method sets the values correctly.
     */
    @Test
    public void testSetObjectives() {
        double[] objectives = new double[]{3.0, 4.0};
        solution.setObjectives(objectives);

        // stored array contains correct data
        assertEquals(2, solution.getNumberOfObjectives());
        assertEquals(3.0, solution.getObjective(0), EPS);
        assertEquals(4.0, solution.getObjective(1), EPS);

        // stored array is independent from external state
        objectives[0] = 0.0;
        assertEquals(3.0, solution.getObjective(0), EPS);
    }

    /**
     * Tests if the {@code setObjectives} method correctly detects invalid
     * indices.
     */
    @Test
    public void testSetObjectivesBoundsChecking() {
        assertThrows(IllegalArgumentException.class, () -> solution.setObjectives(new double[]{0.0, 1.0, 2.0}));
    }
}
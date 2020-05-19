package guardianArchive;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.moeaframework.core.NondominatedPopulation.DuplicateMode;
import org.moeaframework.core.variable.BinaryVariable;
import org.moeaframework.core.variable.EncodingUtils;
import projectimplementation.guardianArchive.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;
import static projectimplementation.guardianArchive.GuardianAssignment.*;

/**
 * Tests the {@link DynamicArchive} class.
 */
public class ListArchiveTest {
    private static final double EPS = 1e-10;
    private static final int numSolutions = 10000;
    private static final int numObjectives = 2;


    private static final List<GuardianAssignment[]> combinations = new ArrayList<>(Arrays.asList(new GuardianAssignment[]{FIRST, OPTIMAL, FIRST, OPTIMAL, OPTIMAL, OPTIMAL}, new GuardianAssignment[]{CLOSEST, OPTIMAL, FIRST, OPTIMAL, OPTIMAL, OPTIMAL}, new GuardianAssignment[]{FEWEST, OPTIMAL, FIRST, OPTIMAL, OPTIMAL, OPTIMAL}, new GuardianAssignment[]{FIRST, FIRST, FIRST, FIRST, FIRST, FIRST}, new GuardianAssignment[]{CLOSEST, CLOSEST, CLOSEST, CLOSEST, CLOSEST, CLOSEST}, new GuardianAssignment[]{FEWEST, FEWEST, FEWEST, FEWEST, FEWEST, FEWEST}));
    /**
     * The population being tested.
     */
    private ListArchive population;

    /**
     * Constructs any shared objects used by this class.
     */
    @BeforeEach
    public void setUp() {
        population = new ListArchive(3);

        population.add(new GuardianSolution(new double[]{3.0, 2.0, 3.0}));
        population.add(new GuardianSolution(new double[]{1.0, 2.0, 2.0}));
        population.add(new GuardianSolution(new double[]{2.0, 2.0, 3.0}));
        population.add(new GuardianSolution(new double[]{4.0, 3.0, 2.0}));
    }

    /**
     * Removes references to shared objects.
     */
    @AfterEach
    public void tearDown() {
        population = null;
    }

    /**
     * Tests the copy constructor to ensure the new population is an identical
     * copy of the original.
     */
    @Test
    public void testCopyConstructor() {
        DynamicArchive<GuardianSolution> copy = new GuardianArchive(population.getPopulation());

        assertEquals(population.size(), copy.size());
        assertTrue(copy.containsAll(population.getPopulation()));
        assertTrue(population.containsAll(copy.getPopulation()));
    }

    /**
     * Tests that pre-determined solutions can be added to the population, covering a range of scenarios.
     */
    @Test
    public void testAdd() {
        for (GuardianAssignment[] combination : combinations) {
            population = new ListArchive(numObjectives);
            addScenarios(population);
        }
    }

    /**
     * Tests that the archive can handle when all solutions added are non dominating.
     */
    @Test
    public void testAddAllNonDominating() {
        for (GuardianAssignment[] combination : combinations) {
            population = new ListArchive(numObjectives);
            addAllNonDominating(population);
        }
    }

    /**
     * Tests that the archive can handle when all solutions added are dominated.
     */
    @Test
    public void testAddAllDominating() {
        for (GuardianAssignment[] combination : combinations) {
            population = new ListArchive(numObjectives);
            addAllDominating(population);
        }
    }

    /**
     * Tests that the archive can handle when a new solution dominates every point in the archive.
     */
    @Test
    public void testDominatesAll() {
        for (GuardianAssignment[] combination : combinations) {
            population = new ListArchive(numObjectives);
            dominatesAll(population);
        }

    }

    /**
     * Tests that the archive can handle numerous randomly generated solutions being added to it
     * without any cycles being made.
     */
    @Test
    public void testAddNumerous() {
        for (GuardianAssignment[] combination : combinations) {
            population = new ListArchive(numObjectives);
            addNumerous(population);
        }
    }

    private void addScenarios(DynamicArchive<GuardianSolution> population) {
        List<GuardianSolution> solutions = new ArrayList<>();

        solutions.add(new GuardianSolution(new double[]{3.0, 3.0}));
        solutions.add(new GuardianSolution(new double[]{6.0, 6.0}));
        solutions.add(new GuardianSolution(new double[]{4.0, 5.0}));
        solutions.add(new GuardianSolution(new double[]{2.0, 2.0}));
        solutions.add(new GuardianSolution(new double[]{1.0, 3.0}));
        solutions.add(new GuardianSolution(new double[]{3.0, 1.0}));
        solutions.add(new GuardianSolution(new double[]{3.0, 4.0}));
        solutions.add(new GuardianSolution(new double[]{8.0, 8.0}));
        solutions.add(new GuardianSolution(new double[]{1.0, 1.0}));

        // non-dominated as first solution
        assertTrue(population.add(solutions.get(0)));
        assertEquals(1, population.size());
        assertEquals(1, population.paretoSize());
        assertTrue(population.contains(solutions.get(0)));
        assertTrue(population.pareto(solutions.get(0)));

        // dominated by first solution
        assertTrue(population.add(solutions.get(1)));
        assertEquals(2, population.size());
        assertEquals(1, population.paretoSize());
        assertTrue(population.contains(solutions.get(1)));
        assertFalse(population.pareto(solutions.get(1)));

        // dominated by first solution
        assertTrue(population.add(solutions.get(2)));
        assertEquals(3, population.size());
        assertEquals(1, population.paretoSize());
        assertTrue(population.contains(solutions.get(2)));
        assertFalse(population.pareto(solutions.get(2)));

        // non dominated, becomes only non-dominated solution
        assertTrue(population.add(solutions.get(3)));
        assertEquals(4, population.size());
        assertEquals(1, population.paretoSize());
        assertTrue(population.contains(solutions.get(3)));
        assertTrue(population.pareto(solutions.get(3)));
        assertFalse(population.pareto(solutions.get(0)));

        // non-dominated, mutually non dominates other pareto solution
        assertTrue(population.add(solutions.get(4)));
        assertEquals(5, population.size());
        assertEquals(2, population.paretoSize());
        assertTrue(population.contains(solutions.get(4)));
        assertTrue(population.pareto(solutions.get(4)));

        // non-dominated, mutually non dominates other pareto solutions
        assertTrue(population.add(solutions.get(5)));
        assertEquals(6, population.size());
        assertEquals(3, population.paretoSize());
        assertTrue(population.contains(solutions.get(5)));
        assertTrue(population.pareto(solutions.get(5)));

        // dominated
        assertTrue(population.add(solutions.get(6)));
        assertEquals(7, population.size());
        assertEquals(3, population.paretoSize());
        assertTrue(population.contains(solutions.get(6)));
        assertFalse(population.pareto(solutions.get(6)));

        // dominated by every solution
        assertTrue(population.add(solutions.get(7)));
        assertEquals(8, population.size());
        assertEquals(3, population.paretoSize());
        assertTrue(population.contains(solutions.get(7)));
        assertFalse(population.pareto(solutions.get(7)));

        // dominates every solutionn
        assertTrue(population.add(solutions.get(8)));
        assertEquals(9, population.size());
        assertEquals(1, population.paretoSize());
        assertTrue(population.contains(solutions.get(8)));
        assertTrue(population.pareto(solutions.get(8)));

    }

    private void addAllNonDominating(DynamicArchive<GuardianSolution> population) {
        int range = 10;

        for (int i = range; i > 0; i--) {
            GuardianSolution s = new GuardianSolution(new double[]{i, i});
            population.add(s);
            assertEquals((range - i) + 1, population.size());
            assertEquals(1, population.paretoSize());
            assertTrue(population.pareto(s));
        }
        assertEquals(range, population.size());
    }

    private void addAllDominating(DynamicArchive<GuardianSolution> population) {
        int range = 10;
        GuardianSolution s = new GuardianSolution(new double[]{0, 0});
        population.add(s);
        assertEquals(1, population.size());
        assertEquals(1, population.paretoSize());
        assertTrue(population.pareto(s));


        for (int i = 1; i < range; i++) {
            s = new GuardianSolution(new double[]{i, i});
            population.add(s);
            assertEquals(i + 1, population.size());
            assertEquals(1, population.paretoSize());
            assertFalse(population.pareto(s));
        }

        assertEquals(range, population.size());
    }

    private void dominatesAll(DynamicArchive<GuardianSolution> population) {
        final int numObjectives = 2;
        final int numSolutions = 10;

        double[] objectives;

        for (int i = 0; i < numSolutions; i++) {
            objectives = ThreadLocalRandom.current().doubles().limit(numObjectives).toArray();
            assertTrue(population.add(new GuardianSolution(objectives)));
        }
        assertEquals(numSolutions, population.size());

        assertTrue(population.add(new GuardianSolution(new double[]{0, 0})));
        assertEquals(1, population.paretoSize());
        assertEquals(numSolutions + 1, population.size());
    }

    private void addNumerous(DynamicArchive<GuardianSolution> population) {

        double[] objectives;

        for (int i = 0; i < numSolutions; i++) {
            objectives = ThreadLocalRandom.current().doubles().limit(numObjectives).toArray();
            assertTrue(population.add(new GuardianSolution(objectives)));
        }
        assertEquals(numSolutions, population.size());
    }

    /**
     * Tests that multiple solutions can be added at once.
     */
    @Test
    public void testAddAll() {
        population = new ListArchive(3, DuplicateMode.ALLOW_DUPLICATES);

        List<GuardianSolution> solutions = new ArrayList<>();
        solutions.add(new GuardianSolution(new double[]{1.0, 2.0, 3.0}));
        solutions.add(new GuardianSolution(new double[]{1.0, 3.0, 2.0}));
        solutions.add(new GuardianSolution(new double[]{2.0, 1.0, 3.0}));
        solutions.add(new GuardianSolution(new double[]{1.0, 1.0, 3.0}));
        solutions.add(new GuardianSolution(new double[]{1.0, 2.0, 3.0}));
        solutions.add(new GuardianSolution(new double[]{1.0, 1.0, 3.0}));

        assertTrue(population.addAll(solutions));
        assertEquals(6, population.size());
        assertTrue(population.contains(solutions.get(3)));
        assertTrue(population.contains(solutions.get(5)));

        solutions.get(0).setObjectives(new double[]{10, 20, 30});
        assertTrue(population.contains(solutions.get(0)));
    }

    /**
     * Add numerous random solutions and then change their objective values to random new values.
     * Will mostly consists of dominated points due to the random nature.
     */
    @Test
    public void editObjectivesC1() {
        population = new ListArchive(numObjectives);
        populateRandom(population);
        editObjectivesChanged(population);
    }

    /**
     * Add numerous random solutions and then change their objective values to random new values.
     * Will mostly consists of dominated points due to the random nature.
     */
    @Test
    public void editObjectivesC2() {
        population = new ListArchive(numObjectives);
        populateRandom(population);
        editObjectivesChanged(population);
    }

    /**
     * Add numerous random solutions and then change their objective values to random new values.
     * Will mostly consists of dominated points due to the random nature.
     */
    @Test
    public void editObjectivesC3() {
        population = new ListArchive(numObjectives);
        populateRandom(population);
        editObjectivesChanged(population);
    }

    /**
     * Add numerous random solutions and then change their objective values to random new values.
     * Will mostly consists of dominated points due to the random nature.
     */
    @Test
    public void editObjectivesC4() {
        population = new ListArchive(numObjectives);
        populateRandom(population);
        editObjectivesChanged(population);
    }

    /**
     * Add numerous random solutions and then change their objective values to random new values.
     * Will mostly consists of dominated points due to the random nature.
     */
    @Test
    public void editObjectivesC5() {
        population = new ListArchive(numObjectives);
        populateRandom(population);
        editObjectivesChanged(population);
    }

    /**
     * Add numerous random solutions and then change their objective values to random new values.
     * Will mostly consists of dominated points due to the random nature.
     */
    @Test
    public void editObjectivesC6() {
        population = new ListArchive(numObjectives);
        populateRandom(population);
        editObjectivesChanged(population);
    }

    private void populateRandom(DynamicArchive<GuardianSolution> population) {
        double[] objectives;

        for (int i = 0; i < numSolutions; i++) {
            objectives = ThreadLocalRandom.current().doubles().limit(numObjectives).toArray();
            population.add(new GuardianSolution(objectives));
        }
        assertEquals(numSolutions, population.size());
    }

    private void editObjectivesChanged(DynamicArchive<GuardianSolution> population) {
        double[] objectives;

        List<GuardianSolution> solutions = new ArrayList<>(population.getPopulation());

        for (GuardianSolution solution : solutions) {
            objectives = ThreadLocalRandom.current().doubles().limit(numObjectives).toArray();
            population.editObjectives(solution, objectives);
        }
    }

    /**
     * Add pre-determined solutions to the population,
     * and then sequentially change their objectives to pre-determined values to cover multiple scenarios
     */
    @Test
    public void editObjectivesScenariosC1() {
        population = new ListArchive(numObjectives);
        editObjectivesScenarios(population);
    }

    /**
     * Add pre-determined solutions to the population,
     * and then sequentially change their objectives to pre-determined values to cover multiple scenarios
     */
    @Test
    public void editObjectivesScenariosC2() {
        population = new ListArchive(numObjectives);
        editObjectivesScenarios(population);
    }

    /**
     * Add pre-determined solutions to the population,
     * and then sequentially change their objectives to pre-determined values to cover multiple scenarios
     */
    @Test
    public void editObjectivesScenariosC3() {
        population = new ListArchive(numObjectives);
        editObjectivesScenarios(population);
    }

    /**
     * Add pre-determined solutions to the population,
     * and then sequentially change their objectives to pre-determined values to cover multiple scenarios
     */
    @Test
    public void editObjectivesScenariosC4() {
        population = new ListArchive(numObjectives);
        editObjectivesScenarios(population);
    }

    /**
     * Add pre-determined solutions to the population,
     * and then sequentially change their objectives to pre-determined values to cover multiple scenarios
     */
    @Test
    public void editObjectivesScenariosC5() {
        population = new ListArchive(numObjectives);
        editObjectivesScenarios(population);
    }

    /**
     * Add pre-determined solutions to the population,
     * and then sequentially change their objectives to pre-determined values to cover multiple scenarios
     */
    @Test
    public void editObjectivesScenariosC6() {
        population = new ListArchive(numObjectives);
        editObjectivesScenarios(population);
    }

    private void editObjectivesScenarios(DynamicArchive<GuardianSolution> population) {
        GuardianSolution s0 = new GuardianSolution(new double[]{9, 12});
        GuardianSolution s1 = new GuardianSolution(new double[]{10, 9});
        GuardianSolution s2 = new GuardianSolution(new double[]{14, 10});
        GuardianSolution s3 = new GuardianSolution(new double[]{13, 11});
        GuardianSolution s4 = new GuardianSolution(new double[]{5, 13});
        GuardianSolution s5 = new GuardianSolution(new double[]{4, 8});
        GuardianSolution s6 = new GuardianSolution(new double[]{6, 7});
        GuardianSolution s7 = new GuardianSolution(new double[]{7, 6});
        GuardianSolution s8 = new GuardianSolution(new double[]{11, 3});
        GuardianSolution s9 = new GuardianSolution(new double[]{12, 2});
        GuardianSolution s10 = new GuardianSolution(new double[]{1, 8});
        GuardianSolution s11 = new GuardianSolution(new double[]{2, 5});
        GuardianSolution s12 = new GuardianSolution(new double[]{3, 4});
        GuardianSolution s13 = new GuardianSolution(new double[]{8, 1});

        population.add(s0);
        population.add(s1);
        population.add(s2);
        population.add(s3);
        population.add(s4);
        population.add(s5);
        population.add(s6);
        population.add(s7);
        population.add(s8);
        population.add(s9);
        population.add(s10);
        population.add(s11);
        population.add(s12);
        population.add(s13);
        assertEquals(4, population.paretoSize());

        // edit objectives, see what breaks what

        // non dominated being changed
        // stays non dominated
        // 2 children remain as children
        s13.setObjectives(new double[]{7, 1}); // loses s8 11,3 as child
        assertEquals(4, population.paretoSize());

        // non dominated being changed
        // becomes dominated
        // old child becomes its guardian
        s13.setObjectives(new double[]{13, 4});
        assertEquals(5, population.paretoSize());

        // non dominated being changed
        // becomes dominated
        // guardian is pareto member
        s12.setObjectives(new double[]{8, 8});
        assertEquals(4, population.paretoSize());

        // non dominated being changed
        // becomes dominated
        // guardian is member of its tree
        s10.setObjectives(new double[]{10, 10});
        assertEquals(3, population.paretoSize());

        // non dominated being changed
        // becomes dominated, its children become pareto
        // guardian is its old child
        s11.setObjectives(new double[]{9, 9});
        assertEquals(5, population.paretoSize());

        // non dominated being changed
        // becomes dominated
        // guardian is pareto, keeps child
        s9.setObjectives(new double[]{12, 4});
        assertEquals(4, population.paretoSize());

        // non dominated being changed
        // becomes dominated
        // guardian is child from its tree
        s8.setObjectives(new double[]{13, 6});
        assertEquals(4, population.paretoSize());

        // dominated being changed
        // stays dominated
        // guardian is different member of pareto set
        s0.setObjectives(new double[]{12, 5});
        assertEquals(4, population.paretoSize());

        // dominated being changed
        // becomes non dominated
        // guardian is parent of its old parent
        s8.setObjectives(new double[]{9, 3}); // breaks when it becomes guardian of its old guardian
        assertEquals(4, population.paretoSize());

        // dominated being changed
        // becomes non dominated
        // is guardian of old children and old pareto
        s1.setObjectives(new double[]{4, 4});
        assertEquals(2, population.paretoSize());

        // dominated being changed
        // becomes non dominated
        // guardian of old pareto member
        s2.setObjectives(new double[]{8, 2});
        assertEquals(2, population.paretoSize());

        // dominated being changed
        // stays dominated
        // guardian is pareto member
        s5.setObjectives(new double[]{9, 6});
        assertEquals(2, population.paretoSize());

        // one solution dominates all
        s3.setObjectives(new double[]{2, 2});
        assertEquals(1, population.paretoSize());

        // one solution that dominates moves back to form pareto front with its children
        s3.setObjectives(new double[]{6, 3});
        assertEquals(3, population.paretoSize());

        // pareto member is still pareto but does not dominate all of its children
        s1.setObjectives(new double[]{4, 7});
        assertEquals(3, population.paretoSize());

        s6.setObjectives(new double[]{8.5, 8});

        s6.setObjectives(new double[]{5, 5});
    }

    /**
     * Edit objectives so that the solution always becomes pareto
     */
    @Test
    public void editAlwaysBecomeParetoC1() {
        population = new ListArchive(numObjectives);
        editAlwaysBecomePareto(population);
    }

    /**
     * Edit objectives so that the solution always becomes dominated
     */
    @Test
    public void editAlwaysBecomeDominatedC1() {
        population = new ListArchive(numObjectives);
        editAlwaysBecomeDominated(population);
    }

    /**
     * Edit objectives so that the solution always becomes pareto
     */
    @Test
    public void editAlwaysBecomeParetoC2() {
        population = new ListArchive(numObjectives);
        editAlwaysBecomePareto(population);
    }

    /**
     * Edit objectives so that the solution always becomes dominated
     */
    @Test
    public void editAlwaysBecomeDominatedC2() {
        population = new ListArchive(numObjectives);
        editAlwaysBecomeDominated(population);
    }

    /**
     * Edit objectives so that the solution always becomes pareto
     */
    @Test
    public void editAlwaysBecomeParetoC3() {
        population = new ListArchive(numObjectives);
        editAlwaysBecomePareto(population);
    }

    /**
     * Edit objectives so that the solution always becomes dominated
     */
    @Test
    public void editAlwaysBecomeDominatedC3() {
        population = new ListArchive(numObjectives);
        editAlwaysBecomeDominated(population);
    }

    /**
     * Edit objectives so that the solution always becomes pareto
     */
    @Test
    public void editAlwaysBecomeParetoC4() {
        population = new ListArchive(numObjectives);
        editAlwaysBecomePareto(population);
    }

    /**
     * Edit objectives so that the solution always becomes dominated
     */
    @Test
    public void editAlwaysBecomeDominatedC4() {
        population = new ListArchive(numObjectives);
        editAlwaysBecomeDominated(population);
    }

    /**
     * Edit objectives so that the solution always becomes pareto
     */
    @Test
    public void editAlwaysBecomeParetoC5() {
        population = new ListArchive(numObjectives);
        editAlwaysBecomePareto(population);
    }

    /**
     * Edit objectives so that the solution always becomes dominated
     */
    @Test
    public void editAlwaysBecomeDominatedC5() {
        population = new ListArchive(numObjectives);
        editAlwaysBecomeDominated(population);
    }

    /**
     * Edit objectives so that the solution always becomes pareto
     */
    @Test
    public void editAlwaysBecomeParetoC6() {
        population = new ListArchive(numObjectives);
        editAlwaysBecomePareto(population);
    }

    /**
     * Edit objectives so that the solution always becomes dominated
     */
    @Test
    public void editAlwaysBecomeDominatedC6() {
        population = new ListArchive(numObjectives);
        editAlwaysBecomeDominated(population);
    }

    private void editAlwaysBecomePareto(DynamicArchive<GuardianSolution> population) {
        int numSolutions = 10;

        for (int i = 0; i < numSolutions; i++) {
            GuardianSolution s = new GuardianSolution(new double[]{numSolutions, numSolutions});
            population.add(s);
        }

        List<GuardianSolution> solutions = population.getPopulation();

        for (int i = numSolutions - 1; i > 0; i--) {
            GuardianSolution s = solutions.get(i - 1);
            s.setObjectives(new double[]{i, i});
            assertTrue(population.pareto(s));
        }

        assertEquals(numSolutions, population.size());
    }

    private void editAlwaysBecomeDominated(DynamicArchive<GuardianSolution> population) {
        int numSolutions = 10;

        for (int i = 0; i < numSolutions; i++) {
            GuardianSolution s = new GuardianSolution(new double[]{numSolutions, numSolutions});
            population.add(s);
        }

        List<GuardianSolution> solutions = population.getPopulation();

        for (int i = 0; i < numSolutions; i++) {
            GuardianSolution s = solutions.get(i);
            s.setObjectives(new double[]{i, i});
            assertTrue(population.pareto(solutions.get(0)));
        }
        assertEquals(numSolutions, population.size());
    }

    /**
     * Test that the archive can have its contents cleared.
     */
    @Test
    public void testClear() {
        assertEquals(4, population.size());
        population.clear();
        assertEquals(0, population.size());
    }

    /**
     * Test that the comparator for the archive is not null.
     */
    @Test
    public void testGetComparator() {
        assertNotNull(population.getComparator());
    }

    /**
     * Test that the distance metric for the archive is not null.
     */
    @Test
    public void testGetDistanceMetric() {
        assertNotNull(population.getDistanceMetric());
    }

    /**
     * Test whether a solution is a member of the archive or not.
     */
    @Test
    public void testContains() {
        GuardianSolution s0 = population.paretoSet().iterator().next();
        assertTrue(population.contains(s0));
        GuardianSolution s1 = new GuardianSolution(0, 2, 0);
        assertFalse(population.contains(s1));
    }

    /**
     * Test whether multiple soluitons are members of the archive or not.
     */
    @Test
    public void testContainsAll() {
        List<GuardianSolution> s = population.paretoSet();
        assertTrue(population.containsAll(s));
        s.add(new GuardianSolution(0, 2, 0));
        assertFalse(population.containsAll(s));
    }

    /**
     * Test whether the archive contains any solutions or not.
     */
    @Test
    public void testIsEmpty() {
        assertFalse(population.isEmpty());
        population.clear();
        assertEquals(0, population.size());
        assertTrue(population.isEmpty());
    }

    /**
     * Test that the size of the archive is accurate.
     */
    @Test
    public void testSize() {
        assertEquals(4, population.size());
    }

    @Test
    public void theFinalBug() {
        /*Random random = new Random();
        List<double[]> sequence = null;
        List<GuardianSolution> local;

        boolean errorFound = false;
        while (!errorFound) {
            sequence = new ArrayList<>();
            local = new ArrayList<>();
            population = new GuardianArchive(2);
            for (int i = 0; i < 10; i++) {
                double[] objectives = new double[]{random.nextInt(11), random.nextInt(11)};
                sequence.add(objectives);
                GuardianSolution toAdd = new GuardianSolution(objectives);
                population.add(toAdd);
                local.add(toAdd);
            }

            for (int j = 0; j < 10; j++) {
                double[] objectives = new double[]{random.nextInt(11), random.nextInt(11)};
                GuardianSolution toEdit = local.get(j);
                sequence.add(objectives);
                population.editObjectives(toEdit, objectives);
                if(population.size() != population.getPopulation().size()){
                    errorFound = true;
                    System.out.println(Arrays.deepToString(sequence.toArray()));
                    writeDominationStructure();
                    assertTrue(false);
                }
            }
        }*/
    }

    @Test
    public void bugScenario(){
        population = new ListArchive(2);
        GuardianSolution s1 = new GuardianSolution(new double[]{7,5});
        GuardianSolution s2 = new GuardianSolution(new double[]{2,9});
        GuardianSolution s3 = new GuardianSolution(new double[]{9,9});
        GuardianSolution s4 = new GuardianSolution(new double[]{7,8});
        GuardianSolution s5 = new GuardianSolution(new double[]{3,7});
        GuardianSolution s6 = new GuardianSolution(new double[]{5,6});
        GuardianSolution s7 = new GuardianSolution(new double[]{9,7});
        GuardianSolution s8 = new GuardianSolution(new double[]{5,2});
        GuardianSolution s9 = new GuardianSolution(new double[]{8,8});
        GuardianSolution s10 = new GuardianSolution(new double[]{4,0});

        population.add(s1);
        population.add(s2);
        population.add(s3);
        population.add(s4);
        population.add(s5);
        population.add(s6);
        population.add(s7);
        population.add(s8);
        population.add(s9);
        population.add(s10);

        s1.setObjectives(new double[]{3,5});
        s2.setObjectives(new double[]{8,2});
    }
}

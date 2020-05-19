package guardianArchive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import projectimplementation.guardianArchive.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DistanceMetricTest {

    private DistanceMetric d1;
    private DistanceMetric d2;

    /**
     * Construct DistanceMetric implementations.
     */
    @BeforeEach
    public void setUp() {
        d1 = new EuclideanDistance();
        d2 = new ManhattanDistance();
    }

    /**
     * Remove references to shared objects.
     */
    @AfterEach
    public void tearDown() {
        d1 = null;
        d2 = null;
    }

    /**
     * Test the distance functions between candidate solutions.
     */
    @Test
    public void testDistance() {
        DynamicSolution s1 = new GuardianSolution(new double[]{3, 4});
        DynamicSolution s2 = new GuardianSolution(new double[]{7, 7});

        // two positive values
        assertEquals(5.0, d1.distance(s1, s2));
        assertEquals(7.0, d2.distance(s1, s2));

        DynamicSolution s3 = new GuardianSolution(new double[]{-3, -4});
        DynamicSolution s4 = new GuardianSolution(new double[]{-7, -7});

        // two negative solutions
        assertEquals(5.0, d1.distance(s3, s4));
        assertEquals(7.0, d2.distance(s3, s4));

        // first solution positive, second solution negative
        assertEquals(10.0, d1.distance(s1, s3));
        assertEquals(14, d2.distance(s1, s3));

        // first solution negative, second solution positive
        assertEquals(10.0, d1.distance(s3, s1));
        assertEquals(14, d2.distance(s3, s1));
    }
}

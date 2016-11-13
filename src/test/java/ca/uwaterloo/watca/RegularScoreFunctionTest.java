package ca.uwaterloo.watca;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class RegularScoreFunctionTest {
    private static RegularScoreFunction sfn;

    @BeforeClass
    public static void beforeClass() {
        sfn = new RegularScoreFunction();
    }

    @Test
    public void testGetScoreSameCluster() {
        Cluster cluster = new Cluster("KEY", "VALUE");
        assertEquals(0, sfn.getScore(cluster, cluster));
    }

    @Test
    public void testGetScoreBackwardZones() {
        Cluster clusterBackwardA = new Cluster("KEY", "VALUE");
        clusterBackwardA.addOperation(new Operation("KEY", "VALUE", 1000, 2100, "R"));
        clusterBackwardA.addOperation(new Operation("KEY", "VALUE2", 1001, 4000, "W"));
        Cluster clusterBackwardB = new Cluster("KEY", "VALUE");
        clusterBackwardB.addOperation(new Operation("KEY", "VALUE", 2000, 3100, "R"));
        clusterBackwardB.addOperation(new Operation("KEY", "VALUE2", 2001, 5000, "W"));
        assertEquals(0, sfn.getScore(clusterBackwardA, clusterBackwardB));
    }

    @Test
    public void testGetScoreTwoForwardConflict() {
        Cluster clusterForwardA = new Cluster("KEY", "VALUE");
        clusterForwardA.addOperation(new Operation("KEY", "VALUE", 1000, 2000, "W"));
        clusterForwardA.addOperation(new Operation("KEY", "VALUE", 3000, 5000, "R"));
        Cluster clusterForwardB = new Cluster("KEY", "VALUE");
        clusterForwardB.addOperation(new Operation("KEY", "VALUE", 1000, 2000, "W"));
        clusterForwardB.addOperation(new Operation("KEY", "VALUE", 3000, 5000, "R"));
        assertEquals(1000, sfn.getScore(clusterForwardA, clusterForwardB));
    }

    @Test
    public void testGetScoreTwoForwardNoConflict() {
        Cluster clusterForwardA = new Cluster("KEY", "VALUE");
        clusterForwardA.addOperation(new Operation("KEY", "VALUE", 1000, 2000, "W"));
        clusterForwardA.addOperation(new Operation("KEY", "VALUE", 3000, 5000, "R"));
        Cluster clusterForwardB = new Cluster("KEY", "VALUE");
        clusterForwardB.addOperation(new Operation("KEY", "VALUE", 1000, 5000, "W"));
        clusterForwardB.addOperation(new Operation("KEY", "VALUE", 6000, 7000, "R"));
        assertEquals(0, sfn.getScore(clusterForwardA, clusterForwardB));
    }

    @Test
    public void testGetScoreAForwardNoConflict() {
        Cluster clusterForwardA = new Cluster("KEY", "VALUE");
        clusterForwardA.addOperation(new Operation("KEY", "VALUE", 1000, 2000, "W"));
        clusterForwardA.addOperation(new Operation("KEY", "VALUE", 3000, 5000, "R"));
        Cluster clusterForwardB = new Cluster("KEY", "VALUE");
        clusterForwardB.addOperation(new Operation("KEY", "VALUE", 1000, 2000, "R"));
        clusterForwardB.addOperation(new Operation("KEY", "VALUE", 3000, 5000, "W"));
        assertEquals(0, sfn.getScore(clusterForwardA, clusterForwardB));
    }

    @Test
    public void testGetScoreAForwardConflict() {
        Cluster clusterForwardA = new Cluster("KEY", "VALUE");
        clusterForwardA.addOperation(new Operation("KEY", "VALUE", 1000, 2000, "W"));
        clusterForwardA.addOperation(new Operation("KEY", "VALUE", 8000, 10000, "R"));
        Cluster clusterForwardB = new Cluster("KEY", "VALUE");
        clusterForwardB.addOperation(new Operation("KEY", "VALUE", 1000, 2000, "R"));
        clusterForwardB.addOperation(new Operation("KEY", "VALUE", 2500, 5000, "W"));
        assertEquals(500, sfn.getScore(clusterForwardA, clusterForwardB));
    }

    @Test
    public void testGetScoreBForwardConflict() {
        Cluster clusterForwardA = new Cluster("KEY", "VALUE");
        clusterForwardA.addOperation(new Operation("KEY", "VALUE", 1000, 2000, "R"));
        clusterForwardA.addOperation(new Operation("KEY", "VALUE", 2500, 5000, "W"));
        Cluster clusterForwardB = new Cluster("KEY", "VALUE");
        clusterForwardB.addOperation(new Operation("KEY", "VALUE", 1000, 2000, "W"));
        clusterForwardB.addOperation(new Operation("KEY", "VALUE", 8000, 10000, "R"));
        assertEquals(500, sfn.getScore(clusterForwardA, clusterForwardB));
    }

    @Test
    public void testGetScoreBForwardNoConflict() {
        Cluster clusterForwardA = new Cluster("KEY", "VALUE");
        clusterForwardA.addOperation(new Operation("KEY", "VALUE", 1000, 2000, "R"));
        clusterForwardA.addOperation(new Operation("KEY", "VALUE", 3000, 5000, "W"));
        Cluster clusterForwardB = new Cluster("KEY", "VALUE");
        clusterForwardB.addOperation(new Operation("KEY", "VALUE", 1000, 2000, "W"));
        clusterForwardB.addOperation(new Operation("KEY", "VALUE", 3000, 5000, "R"));
        assertEquals(0, sfn.getScore(clusterForwardA, clusterForwardB));
    }
}

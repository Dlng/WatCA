package ca.uwaterloo.watca;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class GKScoreFunctionTest {
    private static GKScoreFunction sfn;

    @BeforeClass
    public static void beforeClass() {
        sfn = new GKScoreFunction();
    }

    @Test
    public void testGetScores() {
        Cluster cluster = new Cluster("KEY", "VALUE");
        List<Long> result =  sfn.getScores(cluster, cluster);
        assertEquals(result.size(), 1);
        assertEquals(2, result.get(0).longValue());
    }

    @Test
    public void testGetScoreSameClusterNoWrite() {
        Cluster clusterNoOp = new Cluster("KEY", "VALUE");
        assertEquals(2, sfn.getScore(clusterNoOp, clusterNoOp));
    }

    @Test
    public void testGetScoreSameClusterWithWrite() {
        Cluster clusterOneWrite = new Cluster("KEY", "VALUE");
        clusterOneWrite.addOperation(new Operation("KEY", "VALUE", 100, 200, "W"));
        assertEquals(0, sfn.getScore(clusterOneWrite, clusterOneWrite));
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
    public void testGetScoreForwardConflict() {
        Cluster clusterForwardA = new Cluster("KEY", "VALUE");
        clusterForwardA.addOperation(new Operation("KEY", "VALUE", 100, 200, "R"));
        clusterForwardA.addOperation(new Operation("KEY", "VALUE2", 300, 400, "R"));
        Cluster clusterForwardB = new Cluster("KEY", "VALUE");
        clusterForwardB.addOperation(new Operation("KEY", "VALUE", 100, 150, "R"));
        clusterForwardB.addOperation(new Operation("KEY", "VALUE", 350, 400, "R"));
        assertEquals(1, sfn.getScore(clusterForwardA, clusterForwardB));
    }

    @Test
    public void testGetScoreForwardNoConflict() {
        Cluster clusterForwardA = new Cluster("KEY", "VALUE");
        clusterForwardA.addOperation(new Operation("KEY", "VALUE", 100, 200, "R"));
        clusterForwardA.addOperation(new Operation("KEY", "VALUE2", 300, 400, "R"));
        Cluster clusterForwardB = new Cluster("KEY", "VALUE");
        clusterForwardB.addOperation(new Operation("KEY", "VALUE", 150, 350, "R"));
        assertEquals(0, sfn.getScore(clusterForwardA, clusterForwardB));
    }
}

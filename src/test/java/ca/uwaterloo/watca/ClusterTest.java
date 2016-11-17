package ca.uwaterloo.watca;


import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by navinmahabir on 2016-11-13.
 */
public class ClusterTest {
    private static Cluster clst;

    @Before
    public void beforeEach() {
        clst = new Cluster("key", "value");
    }

    @Test
    public void testEqualsSameValues(){
        Cluster cluster = new Cluster("key", "value");
        boolean result = clst.equals(cluster);
        assertEquals (true, result);
    }

    @Test
    public void testEqualsDifferentValues(){
        Cluster cluster = new Cluster("key2", "value2");
        boolean result = clst.equals(cluster);
        assertEquals (false, result);
    }

    @Test
    public void testEqualsPartialValues(){
        Cluster cluster = new Cluster("key", "value2");
        boolean result = clst.equals(cluster);
        assertEquals (false, result);
    }

    @Test
    public void testAddOperationRead(){
        Operation op = new Operation("key", "value", 0, 10, "R");
        int readsCount = clst.getNumReads();
        clst.addOperation(op);
        assertEquals(readsCount+1, clst.getNumReads());
        assertEquals(0, clst.getMinStart());
        assertEquals(0, clst.getMaxStart());
        assertEquals(10, clst.getMinFinish());
        assertEquals(10, clst.getMaxFinish());
    }

    @Test
    public void testAddOperationWrite(){
        Operation op = new Operation("key", "value", 0, 10, "W");
        int readsCount = clst.getNumReads();
        clst.addOperation(op);
        assertEquals(readsCount, clst.getNumReads());
        assertEquals(0, clst.getMinStart());
        assertEquals(0, clst.getMaxStart());
        assertEquals(10, clst.getMinFinish());
        assertEquals(10, clst.getMaxFinish());
    }

    @Test
    public void testAddOperationMultiple(){
        Operation op1 = new Operation("key", "value", 0, 10, "W");
        Operation op2 = new Operation("key", "value", 4, 6, "R");
        int readsCount = clst.getNumReads();
        clst.addOperation(op1);
        clst.addOperation(op2);
        assertEquals(readsCount+1, clst.getNumReads());
        assertEquals(0, clst.getMinStart());
        assertEquals(4, clst.getMaxStart());
        assertEquals(6, clst.getMinFinish());
        assertEquals(10, clst.getMaxFinish());
    }

    @Test
    public void testOverlapsTrue(){
        Cluster cluster = new Cluster("key", "value2");
        Operation op1 = new Operation("key", "value", 0, 10, "W");
        Operation op2 = new Operation("key", "value", 4, 6, "R");
        clst.addOperation(op1);
        cluster.addOperation(op2);
        boolean result = clst.overlaps(cluster);
        assertEquals(true, result);
    }

    @Test
    public void testOverlapsFalse(){
        Cluster cluster = new Cluster("key", "value2");
        Operation op1 = new Operation("key", "value", 6, 10, "W");
        Operation op2 = new Operation("key", "value2", 0, 4, "R");
        clst.addOperation(op2);
        cluster.addOperation(op1);
        boolean result = clst.overlaps(cluster);
        assertEquals(false, result);
    }

    @Test
    public void testGetWriteStart(){
        Operation op1 = new Operation("key", "value", 6, 10, "W");
        Operation op2 = new Operation("key", "value2", 0, 4, "R");
        long result = clst.getWriteStart();
        assertEquals (Long.MIN_VALUE, result);
        clst.addOperation(op2);
        result = clst.getWriteStart();
        assertEquals (Long.MIN_VALUE, result);
        clst.addOperation(op1);
        result = clst.getWriteStart();
        assertEquals (6L , result);

    }

    @Test
    public void testGetWriteFinish(){
        Operation op1 = new Operation("key", "value", 6, 10, "W");
        Operation op2 = new Operation("key", "value2", 0, 4, "R");
        long result = clst.getWriteFinish();
        assertEquals (Long.MAX_VALUE, result);
        clst.addOperation(op2);
        result = clst.getWriteFinish();
        assertEquals (Long.MAX_VALUE, result);
        clst.addOperation(op1);
        result = clst.getWriteFinish();
        assertEquals (10L , result);

    }

    @Test
    public void testCompareToEquals(){
        Cluster cluster = new Cluster("key", "value");
        Operation op1 = new Operation("key", "value", 0, 4, "R");
        Operation op2 = new Operation("key", "value", 0, 4, "R");
        clst.addOperation(op1);
        cluster.addOperation(op2);
        int result = clst.compareTo(cluster);
        assertEquals(0, result);
    }

    @Test
    public void testCompareToEqualsDiffValue(){
        Cluster cluster = new Cluster("key", "value2");
        Operation op1 = new Operation("key", "value", 0, 4, "R");
        Operation op2 = new Operation("key", "value", 0, 4, "R");
        clst.addOperation(op1);
        cluster.addOperation(op2);
        int result = clst.compareTo(cluster);
        assertEquals(-1, result);
    }

    @Test
    public void testCompareToEqualsDiffKeys(){
        Cluster cluster = new Cluster("key2", "value");
        Operation op1 = new Operation("key", "value", 0, 4, "R");
        Operation op2 = new Operation("key", "value", 0, 4, "R");
        clst.addOperation(op1);
        cluster.addOperation(op2);
        int result = clst.compareTo(cluster);
        assertEquals(-1, result);
    }

    @Test
    public void testCompareToEqualsDiffRights(){
        Cluster cluster = new Cluster("key", "value2");
        Operation op1 = new Operation("key", "value", 0, 4, "R");
        Operation op2 = new Operation("key", "value", 0, 6, "R");
        clst.addOperation(op1);
        cluster.addOperation(op2);
        int result = clst.compareTo(cluster);
        assertEquals(-1, result);
    }

    @Test
    public void testCompareToEqualsDiffLefts(){
        Cluster cluster = new Cluster("key2", "value");
        Operation op1 = new Operation("key", "value", 0, 4, "R");
        Operation op2 = new Operation("key", "value", 2, 4, "R");
        clst.addOperation(op1);
        cluster.addOperation(op2);
        int result = clst.compareTo(cluster);
        assertEquals(-1, result);
    }

    @Test
    public void testGetNumOps(){
        Operation op1 = new Operation("key", "value", 0, 4, "R");
        Operation op2 = new Operation("key", "value", 2, 4, "R");
        Operation op3 = new Operation("key", "value", 4, 6, "W");
        int result = clst.getNumOperations();
        assertEquals(0, result);
        clst.addOperation(op1);
        result = clst.getNumOperations();
        assertEquals(1, result);
        clst.addOperation(op2);
        result = clst.getNumOperations();
        assertEquals(2, result);
        clst.addOperation(op3);
        result = clst.getNumOperations();
        assertEquals(3, result);

    }


}

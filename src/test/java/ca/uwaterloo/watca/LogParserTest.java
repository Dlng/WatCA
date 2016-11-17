package ca.uwaterloo.watca;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by navinmahabir on 2016-11-14.
 */
public class LogParserTest {
    private static EventType evnt;
    private static LogParser logPrsr;
    private static OperationType opr;

    @Before
    public void beforeEach() {
        logPrsr = new LogParser();

    }

    @Test
    public void testGetTypeEvent(){
        assertEquals (EventType.INVOKE, evnt.getType("INV"));
        assertEquals (EventType.RESPONSE, evnt.getType("RES"));
    }

    @Test
    public void testGetTypeOperation(){
        assertEquals (OperationType.READ, opr.getType("R"));
        assertEquals (OperationType.WRITE, opr.getType("W"));
    }

}

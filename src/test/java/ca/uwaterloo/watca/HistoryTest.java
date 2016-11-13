package ca.uwaterloo.watca;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.Assert.*;

public class HistoryTest {
    private History history;
    private static ScoreFunction sfn;
    private static final String KEY = "testing";

    @BeforeClass
    public static void beforeClass() {
        sfn = new GKScoreFunction();
    }

    @Before
    public void beforeEach() {
        history = new History(KEY);
    }

    @Test
    public void testGetKey() {
        assertEquals(history.getKey(), KEY);
    }

    @Test
    public void testLogScoresEmpty() {
        List<Long> result = history.logScores(sfn, null);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).longValue(), 0);
    }

    @Test
    public void testLogScoresSimple() {
        StringWriter writer = new StringWriter();
        history.addOperation(new Operation(KEY, "test1", 100, 1000, "R"));
        history.addOperation(new Operation(KEY, "test2", 150, 2002, "0"));
        List<Long> result1 = history.logScores(sfn, null);
        List<Long> result2 = history.logScores(sfn, new PrintWriter(writer));
        assertEquals(result1, result2);

        String[] lines = writer.toString().split("\n");
        assertEquals(lines.length, 2);
        assertTrue(lines[0].contains(KEY) && lines[0].contains("test1") && lines[0].contains(result1.get(1).toString()));
        assertTrue(lines[1].contains(KEY) && lines[1].contains("test2") && lines[1].contains(result1.get(2).toString()));
    }

    @Test
    public void testLogScoresInconsistent() {
        history.addOperation(new Operation(KEY, "old", 100, 110, "W"));
        history.addOperation(new Operation(KEY, "new", 200, 250, "W"));
        history.addOperation(new Operation(KEY, "old", 300, 400, "R"));
        List<Long> result = history.logScores(sfn, null);
        assertEquals(result.size(), 3);
        assertEquals(result.get(0).longValue(), 0);
        assertEquals(result.get(1).longValue(), 1);
        assertEquals(result.get(2).longValue(), 1);
    }

    @Test
    public void testLogScoresConsistent() {
        history.addOperation(new Operation(KEY, "old", 100, 110, "W"));
        history.addOperation(new Operation(KEY, "new", 200, 250, "W"));
        history.addOperation(new Operation(KEY, "new", 260, 270, "R"));
        history.addOperation(new Operation(KEY, "new2", 300, 400, "W"));
        history.addOperation(new Operation(KEY, "new2", 500, 600, "R"));
        List<Long> result = history.logScores(sfn, null);
        assertEquals(result.size(), 4);
        for (long score : result) {
            assertEquals(score, 0);
        }
    }
}

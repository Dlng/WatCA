package ca.uwaterloo.watca;

import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.Assert.*;

public class HistoryTest {
    private History history;
    private static final String KEY = "testing";

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
        List<Long> result = history.logScores(new GKScoreFunction(), null);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).longValue(), 0);
    }

    @Test
    public void testLogScoresSimple() {
        StringWriter writer = new StringWriter();
        history.addOperation(new Operation(KEY, "test1", 100, 1000, "R"));
        history.addOperation(new Operation(KEY, "test2", 150, 2002, "0"));
        List<Long> result1 = history.logScores(new GKScoreFunction(), null);
        List<Long> result2 = history.logScores(new GKScoreFunction(), new PrintWriter(writer));
        assertEquals(result1, result2);

        String[] lines = writer.toString().split("\n");
        assertEquals(lines.length, 2);
        assertEquals(lines[0], "Key = testing, Value = test1, Score = 2");
        assertEquals(lines[1], "Key = testing, Value = test2, Score = 2");
    }
}

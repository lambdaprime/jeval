package id.jeval;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import id.xfunction.XUtils;

public class PositionsHighlighterTests {

    static Stream<List> testDataProvider() {
        return Stream.of(
            // markers on same line
            List.of("PositionsHighlighter1", List.of(2,3,4)),
            // marker pointing zero
            List.of("PositionsHighlighter2", List.of(0,4)),
            // one marker
            List.of("PositionsHighlighter3", List.of(42)),
            // markers on multiple lines
            List.of("PositionsHighlighter4", List.of(7, 41)),
            // tabs and spaces
            List.of("PositionsHighlighter5", List.of(16, 39))
        );
    }
    
    @ParameterizedTest
    @MethodSource("testDataProvider")
    public void test(List data) {
        String text = XUtils.readResource(data.get(0) + ".in");
        String expected = XUtils.readResource(data.get(0) + ".out");
        assertEquals(expected, new PositionsHighlighter().highlight(text, (List<Integer>)data.get(1)));
    }
}

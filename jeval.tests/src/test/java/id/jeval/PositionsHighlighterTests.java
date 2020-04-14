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
            List.of("PositionsHighlighter1", List.of(2,3,4)),
            List.of("PositionsHighlighter2", List.of(0,4)),
            List.of("PositionsHighlighter3", List.of(42))
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

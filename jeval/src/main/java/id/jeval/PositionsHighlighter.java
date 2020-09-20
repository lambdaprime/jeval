package id.jeval;

import java.util.List;

public class PositionsHighlighter {

    public String highlight(String text, List<Integer> positions) {
        return new PositionsHighlighterImpl(text, positions).highlight();
    }
}

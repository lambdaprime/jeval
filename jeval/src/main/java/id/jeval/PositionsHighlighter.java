package id.jeval;

import static java.util.stream.Collectors.toCollection;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PositionsHighlighter {

    public String highlight(String text, List<Integer> positions) {
        StringBuilder newText = new StringBuilder();
        StringBuilder highlighter = new StringBuilder();
        boolean flushHighlighter = false;
        Queue<Integer> posQueue = new LinkedList<>(positions);
        for (char ch: text.toCharArray()) {
            newText.append(ch);
            if (ch == '\n') {
                if (flushHighlighter) {
                    highlighter.append("\n");
                    newText.append(highlighter);
                    flushHighlighter = false;
                    posQueue = posQueue.stream()
                        .map(pos -> pos + highlighter.length())
                        .collect(toCollection(LinkedList::new));
                }
                highlighter.setLength(0);
            } else {
                if (posQueue.isEmpty()) continue;
                if (posQueue.peek() != newText.length() - 1)
                    highlighter.append(' ');
                else {
                    flushHighlighter = true;
                    highlighter.append('^');
                    posQueue.remove();
                }
            }
        }
        if (flushHighlighter) {
            newText.append('\n');
            newText.append(highlighter);
        }
        return newText.toString();
    }
}

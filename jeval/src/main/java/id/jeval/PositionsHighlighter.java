package id.jeval;

import static java.util.stream.Collectors.toCollection;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PositionsHighlighter {
    
    private StringBuilder highlighter = new StringBuilder();
    private Queue<Integer> posQueue;
    private String text;
    
    public PositionsHighlighter(String text) {
        this.text = text;
    }
    
    public PositionsHighlighter withPositions(List<Integer> positions) {
        posQueue = new LinkedList<>(positions);
        return this;
    }

    public String highlight() {
        StringBuilder newText = new StringBuilder();
        boolean flushHighlighter = false;
        for (char ch: text.toCharArray()) {
            newText.append(ch);
            if (ch == '\n') {
                if (flushHighlighter) {
                    highlighter.append("\n");
                    newText.append(highlighter);
                    flushHighlighter = false;
                    increase();
                }
                highlighter.setLength(0);
            } else {
                if (posQueue.isEmpty()) continue;
                if (posQueue.peek() != newText.length() - 1)
                    incHighlighter(ch);
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

    private void incHighlighter(char currentChar) {
        if (currentChar == '\t')
            highlighter.append('\t');
        else
            highlighter.append(' ');
    }

    /*
     * Increase all values by k
     */
    private void increase() {
        posQueue = posQueue.stream()
                .map(pos -> pos + highlighter.length())
                .collect(toCollection(LinkedList::new));
    }
}

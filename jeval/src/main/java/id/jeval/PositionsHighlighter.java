package id.jeval;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PositionsHighlighter {

    public String highlight(String text, List<Integer> positions) {
        StringBuilder newText = new StringBuilder();
        StringBuilder highlighter = new StringBuilder();
        boolean flush = false;
        Queue<Integer> posQueue = new LinkedList<>(positions);
        for (char ch: text.toCharArray()) {
            newText.append(ch);
            if (ch == '\n') {
                if (flush) {
                    newText.append(highlighter + "\n");
                    flush = false;
                }
                highlighter.setLength(0);
            } else {
                if (posQueue.isEmpty()) continue;
                if (posQueue.peek() != newText.length() - 1)
                    highlighter.append(' ');
                else {
                    flush = true;
                    highlighter.append('^');
                    posQueue.remove();
                }
            }
        }
        if (flush) {
            newText.append('\n');
            newText.append(highlighter);
        }
        return newText.toString();
    }
}

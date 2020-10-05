package id.jeval.highlighter;

import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PositionsHighlighter {
    
    private Queue<Integer> posQueue;
    private String text;
    private boolean silent;
    private List<HLine> buffer;
    
    public PositionsHighlighter(String text) {
        this.text = text;
    }
    
    public PositionsHighlighter withPositions(List<Integer> positions) {
        posQueue = new LinkedList<>(positions);
        return this;
    }
    
    /**
     * Do not print all original text and print only lines which were
     * highlighted.
     */
    public PositionsHighlighter withSilentMode() {
        silent = true;
        return this;
    }
    
    public String highlight() {
        buffer = new ArrayList<>();
        int newTextLen = 0;
        HLine hline = new HLine();
        for (char ch: text.toCharArray()) {
            hline.append(ch);
            newTextLen++;
            if (ch == '\n') {
                hline.closeHighlighter();
                newTextLen += hline.highlighterLength();
                increase(hline.highlighterLength());
                addLine(hline);
                hline = new HLine();
            } else {
                if (posQueue.isEmpty()) continue;
                if (posQueue.peek() != newTextLen - 1)
                    hline.incHighlighter(ch);
                else {
                    hline.highlight();
                    posQueue.remove();
                }
            }
        }
        if (hline.hasHighlighter()) {
            hline.append('\n');
        }
        if (!hline.isEmpty()) {
            addLine(hline);
        }
        return asText();
    }

    private void addLine(HLine hline) {
        buffer.add(hline);
    }

    private String asText() {
        StringBuilder newText = new StringBuilder();
        for (HLine hline: buffer) {
            if (hline.hasHighlighter()) {
                newText.append(hline.toString());
            } else if (!silent) {
                newText.append(hline.toString());
            }
        }
        if (!text.endsWith("\n") && newText.lastIndexOf("\n") + 1 == newText.length()) {
            newText.setLength(newText.length() - 1);
        }
        return newText.toString();
    }

    /*
     * Increase all values by k
     */
    private void increase(int k) {
        posQueue = posQueue.stream()
                .map(pos -> pos + k)
                .collect(toCollection(LinkedList::new));
    }
}

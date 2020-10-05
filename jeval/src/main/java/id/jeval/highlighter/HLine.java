package id.jeval.highlighter;

class HLine {
    private StringBuilder line = new StringBuilder();
    private StringBuilder highlighter = new StringBuilder();

    public void incHighlighter(char currentChar) {
        if (currentChar == '\t')
            highlighter.append('\t');
        else
            highlighter.append(' ');
    }

    public int length() {
        return line.length() + highlighter.length();
    }

    public boolean hasHighlighter() {
        return highlighter.length() > 0;
    }

    public boolean isEmpty() {
        return length() == 0;
    }

    @Override
    public String toString() {
        return line.toString() + highlighter.toString();
    }

    public void append(char ch) {
        line.append(ch);
        if (ch == '\n')
            highlighter.append(ch);
    }

    public void highlight() {
        highlighter.append('^');
    }

    public int highlighterLength() {
        return highlighter.length();
    }

    public void closeHighlighter() {
        if (highlighter.toString().isBlank())
            highlighter.setLength(0);
    }
}
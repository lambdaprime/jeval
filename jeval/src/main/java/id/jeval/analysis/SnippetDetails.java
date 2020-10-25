package id.jeval.analysis;

import java.nio.file.Path;

import jdk.jshell.Snippet;

public class SnippetDetails {

    private Snippet snippet;
    private int endLineNumber;
    private Path file;

    public SnippetDetails(Snippet snippet,  Path file, int endLineNumber) {
        this.snippet = snippet;
        this.endLineNumber = endLineNumber;
        this.file = file;
    }

    public Snippet getSnippet() {
        return snippet;
    }

    public String getSource() {
        return snippet.source();
    }

    public int getEndLineNumber() {
        return endLineNumber;
    }

    public Path getFile() {
        return file;
    }

}

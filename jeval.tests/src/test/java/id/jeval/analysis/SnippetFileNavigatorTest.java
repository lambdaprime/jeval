package id.jeval.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import id.jeval.analysis.SnippetFileNavigator;
import id.xfunction.XUtils;

public class SnippetFileNavigatorTest {

    private SnippetFileNavigator navigator;

    @BeforeEach
    public void setup() {
        String snippet = XUtils.readResource("snippet");
        navigator = new SnippetFileNavigator(Paths.get("/tmp/r"), 6, snippet);
    }

    @Test
    public void test_toSnippetLineNum() {
        assertEquals(1, navigator.toSnippetLineNum(0));
        assertEquals(1, navigator.toSnippetLineNum(9));
        assertEquals(2, navigator.toSnippetLineNum(10));
        assertEquals(2, navigator.toSnippetLineNum(11));
        assertEquals(2, navigator.toSnippetLineNum(33));
        assertEquals(3, navigator.toSnippetLineNum(34));
    }

    @Test
    public void test_toSnippetLineNum_oneliner() {
        navigator = new SnippetFileNavigator(Paths.get("/tmp/r"), 6, "snippet");
        assertEquals(1, navigator.toSnippetLineNum(0));
    }

    @Test
    public void test_snippetLine() {
        assertEquals("class X {", navigator.snippetLine(1));
        assertEquals("    static X create() {", navigator.snippetLine(2));
    }
}

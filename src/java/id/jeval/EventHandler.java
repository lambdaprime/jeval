package id.jeval;

import static java.lang.System.err;
import static java.lang.System.out;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jdk.jshell.EvalException;
import jdk.jshell.JShell;
import jdk.jshell.JShellException;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;

public class EventHandler {

    private static Utils utils = new Utils();
    private boolean isScript;
    private JShell jshell;
    private boolean isError = false;
    private Map<String, List<Snippet>> unresolvedSnippets = new LinkedHashMap<>();

    public EventHandler(JShell jshell) {
        this.jshell = jshell;
    }

    public void setIsScript(boolean isScript) {
        this.isScript = isScript;
    }

    public boolean isError() {
        return isError;
    }

    public void onEvent(SnippetEvent ev) {
        JShellException ex = ev.exception();
        if (ex != null) {
            isError = true;
            printException(ex);
        }
        Snippet snippet = ev.snippet();
        String src = snippet.source();
        switch (ev.status()) {
        case VALID:
            if (ev.value() != null)
                if (!isScript || !ev.isSignatureChange())
                    out.print(ev.value());
            break;
        case REJECTED:
            isError = true;
            err.println("Rejected snippet: " + src);
            printLocation(snippet);
            for (Entry<String, List<Snippet>> e: unresolvedSnippets.entrySet()) {
                err.println("\nUnresolved snippet: ");
                err.println(e.getKey());
                e.getValue().forEach(this::printLocation);
            }
            break;
        case RECOVERABLE_DEFINED:
        case RECOVERABLE_NOT_DEFINED:
            unresolvedSnippets.putIfAbsent(src, new ArrayList<>());
            unresolvedSnippets.get(src).add(snippet);
            break;
        default:
            break;
        }
    }

    private void printLocation(Snippet snippet) {
        jshell.diagnostics(snippet)
            .map(d -> d.getMessage(null) + "\nat position: " + d.getStartPosition())
            .forEach(err::println);
    }

    private void printException(Throwable ex) {
        if (ex instanceof EvalException) {
            EvalException evx = (EvalException)ex;
            out.format("%s %s\n", evx.getExceptionClassName(), evx.getMessage());
        }
        utils.printExceptions(ex);
    }
}

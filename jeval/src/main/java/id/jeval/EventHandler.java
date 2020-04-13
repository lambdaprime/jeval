package id.jeval;

import static java.lang.System.err;
import static java.lang.System.out;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.LinkedList;
import java.util.List;

import jdk.jshell.Diag;
import jdk.jshell.EvalException;
import jdk.jshell.JShell;
import jdk.jshell.JShellException;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import jdk.jshell.UnresolvedReferenceException;

public class EventHandler {

    private static Utils utils = new Utils();
    private boolean isScript;
    private JShell jshell;
    private boolean isError = false;
    private LinkedList<Snippet> unresolvedSnippets = new LinkedList<>();

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
        switch (ev.status()) {
        case VALID:
            if (ev.value() != null)
                if (!isScript || !ev.isSignatureChange())
                    out.print(ev.value());
            break;
        case REJECTED:
            isError = true;
            unresolvedSnippets.push(snippet);
            unresolvedSnippets.stream()
                .forEach(this::printUnresolvedSnippet);
            break;
        case RECOVERABLE_DEFINED:
        case RECOVERABLE_NOT_DEFINED: {
            unresolvedSnippets.push(snippet);
            break;
        }
        default:
            break;
        }
    }

    private void printUnresolvedSnippet(Snippet snippet) {
        List<Diag> diag = jshell.diagnostics(snippet)
                .collect(toList());
        String src = snippet.source();
        err.println("\nUnresolved snippet:");
        err.println(src);
        diag.stream()
            .map(d -> d.getMessage(null) + "\nat position: " + d.getStartPosition())
            .forEach(err::println);
    }

    private void printException(Throwable ex) {
        if (ex instanceof UnresolvedReferenceException) {
            onUnresolvedReferenceException((UnresolvedReferenceException)ex);
        }
        if (ex instanceof EvalException) {
            onEvalException((EvalException)ex);
        }
        utils.printExceptions(ex);
    }
    
    private void onUnresolvedReferenceException(UnresolvedReferenceException e) {
        out.println("Unresolved references: " + jshell.unresolvedDependencies(e.getSnippet())
            .collect(joining(", ")));
    }

    private void onEvalException(EvalException e) {
        out.format("%s %s\n", e.getExceptionClassName(), e.getMessage());
    }
}

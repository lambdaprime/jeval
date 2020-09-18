package id.jeval;

import static java.lang.System.err;
import static java.lang.System.out;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import id.xfunction.XAsserts;
import id.xfunction.XUtils;
import jdk.jshell.DeclarationSnippet;
import jdk.jshell.Diag;
import jdk.jshell.EvalException;
import jdk.jshell.JShell;
import jdk.jshell.JShellException;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;

public class EventHandler implements AutoCloseable {

    private boolean isScript;
    private JShell jshell;
    private boolean isError;
    private boolean isClosed;
    private LinkedList<Snippet> unresolvedSnippets = new LinkedList<>();
    
    /*
     * When JShell rejects the snippet it means that it has at least one
     * unrecoverable error.
     * 
     * Because other errors may be recoverable we want to show to the
     * user only unrecoverable ones. That way we will not confuse and
     * prevent user from trying to fix errors which are not errors at all.
     * To do that in case of rejected snippet we:
     * - store it in the list below
     * - continue processing the source code further in hope to resolve
     * other recoverable errors in the snippet
     * 
     * When jeval terminates we evaluate snippet again and print the errors
     */
    private LinkedList<Snippet> rejectedSnippets = new LinkedList<>();
    
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
            if (isClosed) {
                printDiagnostics(snippet);
            } else {
                isError = true;
                rejectedSnippets.push(snippet);
            }
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

    private void printUnresolvedSnippets() {
        unresolvedSnippets.stream()
            .filter(s -> jshell.status(s) != Status.VALID)
            .forEach(this::printDiagnostics);
        unresolvedSnippets.clear();
    }

    private void printDiagnostics(Snippet snippet) {
        String src = snippet.source();
        List<Entry<Long, List<Diag>>> diags = jshell.diagnostics(snippet)
                .collect(groupingBy(Diag::getStartPosition))
                .entrySet()
                .stream()
                .sorted(Comparator.comparingLong(Entry::getKey))
                .collect(toList());
        err.println();
        err.println(new PositionsHighlighter().highlight(src, diags.stream()
            .map(e -> e.getKey().intValue())
            .collect(toList())));
        for (Entry<Long, List<Diag>> e: diags) {
            long pos = e.getKey();
            for (Diag d: e.getValue()) {
                err.println(d.getMessage(null) + "\nat position: " + pos);
            }
        }
        if (snippet instanceof DeclarationSnippet) {
            String s = jshell.unresolvedDependencies((DeclarationSnippet)snippet)
                    .collect(joining(", "));
            if (!s.isEmpty())
                err.println("Unresolved references: " + s);
        }
    }

    private void printException(Throwable ex) {
        if (ex instanceof EvalException) {
            onEvalException((EvalException)ex);
        }
        XUtils.printExceptions(ex);
    }
    
    private void onEvalException(EvalException e) {
        err.format("%s %s\n", e.getExceptionClassName(), e.getMessage());
    }

    @Override
    public void close() throws Exception {
        isClosed = true;
        if (isError)
            rerunRejectedSnippets();
        printUnresolvedSnippets();
    }

    /**
     * We rerun rejected snippets only when handler
     * was already closed. Doing so if snippet gets
     * rejected again we just print it diagnostics
     * immediately and not postpone for later.
     */
    private void rerunRejectedSnippets() {
        XAsserts.assertTrue(isClosed);
        for (Snippet snippet: rejectedSnippets) {
            jshell.eval(snippet.source());
        }
    }
}

/*
 * Copyright 2022 jeval project
 * 
 * Website: https://github.com/lambdaprime/jeval
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package id.jeval;

import static java.lang.System.err;
import static java.lang.System.out;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import id.jeval.analysis.LineCounter;
import id.jeval.analysis.SnippetAnalyzer;
import id.jeval.analysis.SnippetDetails;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jdk.jshell.DeclarationSnippet;
import jdk.jshell.Diag;
import jdk.jshell.EvalException;
import jdk.jshell.JShell;
import jdk.jshell.JShellException;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class EventHandler implements AutoCloseable {

    private boolean isScript;
    private JShell jshell;
    private boolean isError;
    private LineCounter lineCounter = new LineCounter();
    private LinkedList<SnippetDetails> unresolvedSnippets = new LinkedList<>();

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
        SnippetDetails details =
                new SnippetDetails(
                        snippet, lineCounter.getCurrentFile(), lineCounter.getCurrentLine());
        switch (ev.status()) {
            case VALID:
                if (ev.value() != null)
                    if (!isScript || !ev.isSignatureChange()) out.print(ev.value());
                break;
            case REJECTED:
                isError = true;
                printDiagnostics(details);
                break;
            case RECOVERABLE_DEFINED:
            case RECOVERABLE_NOT_DEFINED:
                {
                    unresolvedSnippets.push(details);
                    break;
                }
            default:
                break;
        }
    }

    private void printUnresolvedSnippets() {
        unresolvedSnippets.stream()
                .filter(s -> jshell.status(s.getSnippet()) != Status.VALID)
                .forEach(this::printDiagnostics);
        unresolvedSnippets.clear();
    }

    private void printDiagnostics(SnippetDetails snippetDetails) {
        Snippet snippet = snippetDetails.getSnippet();
        Map<Long, List<Diag>> diags =
                jshell.diagnostics(snippet).collect(groupingBy(Diag::getStartPosition));
        SnippetAnalyzer analyzer = new SnippetAnalyzer(snippetDetails);
        analyzer.printDiagnostics(diags);
        if (snippet instanceof DeclarationSnippet) {
            List<String> s =
                    jshell.unresolvedDependencies((DeclarationSnippet) snippet).collect(toList());
            analyzer.printUnresolvedDependencies(s);
        }
    }

    private void printException(Throwable ex) {
        if (ex instanceof EvalException) {
            onEvalException((EvalException) ex);
        }
        ex.printStackTrace();
    }

    private void onEvalException(EvalException e) {
        err.format("%s %s\n", e.getExceptionClassName(), e.getMessage());
    }

    @Override
    public void close() throws Exception {
        printUnresolvedSnippets();
    }

    public void onNextLine(Path file) {
        lineCounter.nextLine(file);
    }
}

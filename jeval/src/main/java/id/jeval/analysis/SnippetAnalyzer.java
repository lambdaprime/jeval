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
package id.jeval.analysis;

import static java.lang.System.err;

import id.jeval.highlighter.PositionsHighlighter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import jdk.jshell.Diag;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class SnippetAnalyzer {

    private SnippetFileNavigator navigator;

    public SnippetAnalyzer(SnippetDetails snippetDetails) {
        navigator =
                new SnippetFileNavigator(
                        snippetDetails.getFile(),
                        snippetDetails.getEndLineNumber(),
                        snippetDetails.getSource());
    }

    public void printDiagnostics(Map<Long, List<Diag>> diags) {
        for (Entry<Long, List<Diag>> e : diags.entrySet()) {
            long pos = e.getKey();
            for (Diag d : e.getValue()) {
                err.println(highlight((int) pos, d.getMessage(null)));
            }
        }
    }

    public void printUnresolvedDependencies(List<String> symbols) {
        if (symbols.isEmpty()) return;
        symbols.stream()
                .forEach(
                        s -> {
                            err.format(
                                    "%s: Unresolved symbol in the snippet starting at line %d:"
                                            + " %s\n\n",
                                    navigator.getFile(), navigator.getStartLineNumber(), s);
                        });
    }

    private String highlight(int pos, String message) {
        int snippetLineNum = navigator.toSnippetLineNum(pos);
        String line = navigator.snippetLine(snippetLineNum);
        String hline =
                new PositionsHighlighter(line)
                        .withPositions(
                                Arrays.asList(pos - navigator.toSnippetLinePos(snippetLineNum)))
                        .highlight();
        return String.format(
                "%s: %d: %s\n%s\n",
                navigator.getFile(), navigator.toFileLineNum(snippetLineNum), message, hline);
    }
}

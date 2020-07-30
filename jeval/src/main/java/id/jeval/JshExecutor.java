/*
 * Copyright 2019 lambdaprime
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

import static jdk.jshell.SourceCodeAnalysis.Completeness.COMPLETE;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SourceCodeAnalysis;

class JshExecutor implements Subscriber<String> {
    
    private JShell jshell;
    private SourceCodeAnalysis srcAnalysis;
    private StringBuilder buf = new StringBuilder();
    private boolean isDocumentHere;
    private DocumentHereProcessor documentHere = new DocumentHereProcessor();
    private boolean isExecuted;
    private boolean isComplete;
    
    public JshExecutor(JShell jshell) {
        this.jshell = jshell;
        srcAnalysis = jshell.sourceCodeAnalysis();
    }

    @Override
    public void onComplete() {
        isComplete = true;
        if (isExecuted) return;
        String snippet = buf.toString();
        jshell.eval(snippet);
    }

    public boolean isComplete() {
        return isComplete;
    }
    
    @Override
    public void onError(Throwable arg0) {
        
    }

    @Override
    public void onNext(String line) {
        isExecuted = false;
        if (documentHere.isStarted(line)) {
            isDocumentHere = true;
            return;
        }
        if (isDocumentHere) {
            if (!documentHere.isFinished(line))
                return;
            line = documentHere.buildLine();
            isDocumentHere = false;
        }
        buf.append(line);
        buf.append("\n");
        String src = buf.toString();
        if (srcAnalysis.analyzeCompletion(src).completeness() != COMPLETE)
            return;
        boolean isTmpExpression = srcAnalysis.sourceToSnippets(src).stream()
            .map(s -> s.subKind())
            .filter(Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND::equals)
            .findFirst().isPresent();
        if (!isTmpExpression || src.trim().endsWith(";")) {
            buf.setLength(0);
            jshell.eval(src);
            isExecuted = true;
        }
    }

    @Override
    public void onSubscribe(Subscription arg0) {
        
    }
    
}
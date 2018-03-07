/* 
 * jeval - java code evaluator
 *
 * This source file is a part of jeval command line program.
 * Description for this  project/command/program can be found in README.org
 *
 * jeval is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jeval is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jeval. If not, see <http://www.gnu.org/licenses/>.
 *
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
    private SourceCodeAnalysis srcAnal; // of course I mean analysis :)
    private StringBuilder buf = new StringBuilder();
    private boolean isDocumentHere;
    private DocumentHereProcessor documentHere = new DocumentHereProcessor();
    private boolean isExecuted;
    
    public JshExecutor(JShell jshell) {
        this.jshell = jshell;
        srcAnal = jshell.sourceCodeAnalysis();
    }

    @Override
    public void onComplete() {
        if (isExecuted) return;
        String snippet = buf.toString();
        jshell.eval(snippet);
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
        String snippet = buf.toString();
        if (srcAnal.analyzeCompletion(snippet).completeness() != COMPLETE)
            return;
        boolean isTmpExpression = jshell.eval(snippet).stream()
            .map(e -> e.snippet().subKind())
            .filter(Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND::equals)
            .findFirst().isPresent();
        if (!isTmpExpression || snippet.trim().endsWith(";"))
            buf.setLength(0);
        isExecuted = true;
    }

    @Override
    public void onSubscribe(Subscription arg0) {
        
    }
    
}
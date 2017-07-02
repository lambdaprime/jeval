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

import jdk.jshell.JShell;
import jdk.jshell.SourceCodeAnalysis;

class JshExecutor {
    
    private JShell jshell;
    private SourceCodeAnalysis srcAnal; // :)
    private StringBuilder buf = new StringBuilder();
    
    public JshExecutor(JShell jshell) {
        this.jshell = jshell;
        srcAnal = jshell.sourceCodeAnalysis();
    }
    
    public void add(String line) {
        buf.append('\n').append(line);
        String snippet = buf.toString();
        if (srcAnal.analyzeCompletion(snippet).completeness() != COMPLETE)
            return;
        jshell.eval(snippet);
        buf.setLength(0);
    }
    
}
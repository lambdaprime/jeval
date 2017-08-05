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

class DocumentHereProcessor {
    
    private static final String SOF = "<<EOF";
    private static final String EOF = "EOF";
    
    private StringBuilder buf = new StringBuilder();
    private StringBuilder documentHere = new StringBuilder();
    
    public boolean isStarted(String line) {
        line = line.trim();
        if (!line.endsWith("<<" + EOF))
            return false;
        buf.setLength(0);
        documentHere.setLength(0);
        buf.append(line.substring(0, line.length() - SOF.length()));
        return true;
    }
    
    public boolean isFinished(String line) {
        if (line.startsWith(EOF)) {
            buf.append(makeLiteral() + line.substring(EOF.length()));
            return true;
        } else {
            documentHere.append(line);
            return false;
        }
    }

    public String buildLine() {
        return buf.toString();
    }
    
    private String makeLiteral() {
        return "\"" + documentHere.toString().replace("\"", "\\\"") + "\"";
    }
}
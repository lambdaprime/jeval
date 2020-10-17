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

import static java.util.stream.Collectors.toList;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

class DocumentHereProcessor {
    
    private static final String SOF = "<<EOF";
    private static final String EOF = "EOF";
    
    private StringBuilder buf = new StringBuilder();
    private List<String> documentHere = new LinkedList<>();
    
    public boolean isStarted(String line) {
        line = line.trim();
        if (!line.endsWith("<<" + EOF))
            return false;
        buf.setLength(0);
        documentHere.clear();
        buf.append(line.substring(0, line.length() - SOF.length()));
        return true;
    }

    public boolean isFinished(String line) {
        if (line.startsWith(EOF)) {
            buf.append(makeLiteral() + line.substring(EOF.length()));
            return true;
        } else {
            documentHere.add(line);
            return false;
        }
    }

    public String buildLine() {
        return buf.toString();
    }
    
    private String makeLiteral() {
        if (documentHere.isEmpty()) return "";
        List<String> lines = documentHere.stream()
            .map(s -> s.toString().replace("\"", "\\\""))
            .collect(toList());
        String str = lines.subList(0, lines.size() - 1).stream()
                .map(s -> String.format("\"%s\\n\" +\n", s))
                .collect(Collectors.joining());
        str += String.format("\"%s\"", lines.get(lines.size() - 1));
        return str;
    }
}
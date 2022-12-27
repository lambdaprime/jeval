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
package id.jeval.highlighter;

/**
 * @author lambdaprime intid@protonmail.com
 */
class HLine {
    private StringBuilder line = new StringBuilder();
    private StringBuilder highlighter = new StringBuilder();

    public void incHighlighter(char currentChar) {
        if (currentChar == '\t') highlighter.append('\t');
        else highlighter.append(' ');
    }

    public int length() {
        return line.length() + highlighter.length();
    }

    public boolean hasHighlighter() {
        return highlighter.length() > 0;
    }

    public boolean isEmpty() {
        return length() == 0;
    }

    @Override
    public String toString() {
        return line.toString() + highlighter.toString();
    }

    public void append(char ch) {
        line.append(ch);
        if (ch == '\n') highlighter.append(ch);
    }

    public void highlight() {
        highlighter.append('^');
    }

    public int highlighterLength() {
        return highlighter.length();
    }

    public void closeHighlighter() {
        if (highlighter.toString().isBlank()) highlighter.setLength(0);
    }
}

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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * @author lambdaprime intid@protonmail.com
 */
class SnippetFileNavigator {

    private String snippet;
    // snippet starting line number in file
    private int startLineNumber;
    private Path file;
    private List<String> lines;

    /*
     * To find to which file line belongs char at certain position we
     * keep positions of first chars of each line
     */
    private List<Integer> positions;

    /**
     * @param file file with the snippet
     * @param endLineNumber line number in the file where snippet ends
     * @param snippet snippet itself
     */
    public SnippetFileNavigator(Path file, int endLineNumber, String snippet) {
        this.file = file;
        this.snippet = snippet;
        readLines();
        this.startLineNumber = endLineNumber - lines.size();
    }

    /**
     * @param snippetPos char position inside of snippet
     * @return line number on which char with given position located
     */
    public int toSnippetLineNum(int snippetPos) {
        int lineNum = Collections.binarySearch(positions, snippetPos);
        if (lineNum >= 0) lineNum++;
        else lineNum = 0 - lineNum - 1;
        return lineNum;
    }

    public int toSnippetLinePos(int snippetLineNum) {
        return positions.get(snippetLineNum - 1);
    }

    public String snippetLine(int snippetLineNum) {
        return lines.get(snippetLineNum - 1);
    }

    public int toFileLineNum(int snippetLineNum) {
        return startLineNumber + snippetLineNum;
    }

    public Path getFile() {
        return file;
    }

    /**
     * Trying to return accurate position in the file where snippet starts. For that we ignore all
     * blank lines if any until the body of the snippet.
     */
    public int getStartLineNumber() {
        int i = 0;
        while (i < lines.size() - 1) {
            if (!lines.get(i).isBlank()) break;
            i++;
        }
        return startLineNumber + i + 1;
    }

    private void readLines() {
        positions = new ArrayList<>();
        lines = new ArrayList<>();
        try (Scanner s = new Scanner(snippet)) {
            int c = 0;
            while (s.hasNextLine()) {
                String line = s.nextLine();
                positions.add(c);
                lines.add(line);
                c += line.length() + 1;
            }
        }
    }
}

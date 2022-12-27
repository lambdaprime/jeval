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
import jdk.jshell.Snippet;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class SnippetDetails {

    private Snippet snippet;
    private int endLineNumber;
    private Path file;

    public SnippetDetails(Snippet snippet, Path file, int endLineNumber) {
        this.snippet = snippet;
        this.endLineNumber = endLineNumber;
        this.file = file;
    }

    public Snippet getSnippet() {
        return snippet;
    }

    public String getSource() {
        return snippet.source();
    }

    public int getEndLineNumber() {
        return endLineNumber;
    }

    public Path getFile() {
        return file;
    }
}

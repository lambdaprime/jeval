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
import java.util.HashMap;
import java.util.Map;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class LineCounter {

    private Map<Path, Integer> map = new HashMap<>();
    private Path currentFile;

    public Path getCurrentFile() {
        return currentFile;
    }

    public int getCurrentLine() {
        return map.get(currentFile);
    }

    public void nextLine(Path file) {
        map.compute(file, (k, v) -> v == null ? 1 : v + 1);
        currentFile = file;
    }
}

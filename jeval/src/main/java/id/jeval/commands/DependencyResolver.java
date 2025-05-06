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
package id.jeval.commands;

import static id.jeval.commands.CommandConstants.ADD_DEPENDENCY_COMMAND;
import static id.jeval.commands.CommandConstants.OPEN_COMMAND;
import static java.util.stream.Collectors.toList;

import id.depresolve.ArtifactInfo;
import id.depresolve.Depresolve;
import id.depresolve.Scope;
import id.xfunction.function.Unchecked;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class DependencyResolver {
    private OpenScripts opener = new OpenScripts();

    public List<String> resolve(Path scriptPath) throws Exception {
        return resolveInternal(scriptPath).stream()
                .map(Unchecked.wrapApply(File::getCanonicalPath))
                .collect(toList());
    }

    private List<File> resolveInternal(Path scriptPath) throws Exception {
        List<File> output = new ArrayList<>();
        Depresolve depresolve =
                new Depresolve().withSilentMode().withClasspathConsumer(output::add);
        Iterator<String> iter = Files.lines(scriptPath).iterator();
        while (iter.hasNext()) {
            String line = iter.next();
            if (line.startsWith(OPEN_COMMAND)) {
                Path openFile = opener.open(scriptPath, line);
                output.addAll(resolveInternal(openFile));
                continue;
            }
            if (!line.startsWith(ADD_DEPENDENCY_COMMAND)) continue;
            Optional.of(line)
                    .map(l -> l.replaceAll(ADD_DEPENDENCY_COMMAND + "\\s([^\\s]*).*", "$1"))
                    .map(artifact -> new ArtifactInfo(artifact, Scope.COMPILE))
                    .ifPresent(depresolve::addArtifactToResolve);
        }
        depresolve.run();
        return output;
    }
}

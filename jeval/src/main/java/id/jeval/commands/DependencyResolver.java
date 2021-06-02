package id.jeval.commands;

import static id.jeval.commands.CommandConstants.*;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import id.depresolve.ArtifactInfo;
import id.depresolve.Scope;
import id.depresolve.app.Depresolve;
import id.xfunction.function.Unchecked;

public class DependencyResolver {
    private OpenScripts opener = new OpenScripts();
    
    public List<String> resolve(Path scriptPath) throws Exception {
        return resolveInternal(scriptPath).stream()
                .map(Unchecked.wrapApply(File::getCanonicalPath))
                .collect(toList());
    }
    
    private List<File> resolveInternal(Path scriptPath) throws Exception {
        List<File> output = new ArrayList<>();
        Depresolve depresolve = new Depresolve()
                    .withGenerateClasspath(output);
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

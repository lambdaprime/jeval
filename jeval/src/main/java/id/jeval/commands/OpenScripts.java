package id.jeval.commands;

import static id.jeval.commands.CommandConstants.OPEN_COMMAND;

import java.nio.file.Path;
import java.nio.file.Paths;

public class OpenScripts {

    public Path open(Path parentScript, String line) {
        return parentScript.resolveSibling(Paths.get(line.replaceAll(OPEN_COMMAND + "\\s+(.*)", "$1")));
    }
}

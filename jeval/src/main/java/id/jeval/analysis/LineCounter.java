package id.jeval.analysis;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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
        map.compute(file, (k, v) -> v == null? 1: v + 1);
        currentFile = file;
    }

}

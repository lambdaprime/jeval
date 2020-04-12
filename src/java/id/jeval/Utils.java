package id.jeval;

import java.util.Arrays;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public class Utils {

    /**
     * Prints all exceptions including suppressed ones 
     */
    public void printExceptions(Throwable ex) {
        concat(of(ex, ex.getCause()), Arrays.stream(ex.getSuppressed()))
            .filter(e -> e != null)
            .forEach(Throwable::printStackTrace);
    }
}

/* 
 * jeval - java code evaluator
 *
 * This source file is a part of jeval command line program.
 * Description for this  project/command/program can be found in README.org
 *
 * jeval is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jeval is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jeval. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package id.jeval;

import static id.xfunction.Curry.curry;
import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.in;
import static java.lang.System.out;
import static java.util.Arrays.stream;

import id.xfunction.ThrowingRunnable;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jdk.jshell.EvalException;
import jdk.jshell.JShell;
import jdk.jshell.JShellException;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;

public class Main {

    private static boolean isError = false;
    private static JShell jshell;
    private static boolean isScript;
    private static JshExecutor jshExec;
    private static List<Snippet> unresolvedSnippets = new ArrayList<>();
    
    @SuppressWarnings("resource")
    private static void usage() throws IOException {
        Scanner scanner = new Scanner(Main.class.getResource("/README.md").openStream())
                .useDelimiter("\n");
        while (scanner.hasNext())
            System.out.println(scanner.next());
    }

    @SuppressWarnings("resource")
    private static void preloader(JshExecutor jshExec) throws IOException {
        isScript = true;
        Scanner scanner = new Scanner(Main.class.getResource("/preloader.jsh").openStream())
                .useDelimiter("\n");
        while (scanner.hasNext())
            jshExec.onNext(scanner.next());
    }
    
    private static List<String> splitByArgs(String s) {
        String regex = "\"([^\"]*)\"|(\\S+)";
        Matcher m = Pattern.compile(regex).matcher(s);
        List<String> r = new ArrayList<>();
        while (m.find()) {
            if (m.group(1) != null)
                r.add(m.group(1));
            else
                r.add(m.group(2));
        }
        return r;
    }
    
    private static void printException(Throwable ex) {
        if (ex instanceof EvalException) {
            EvalException evx = (EvalException)ex;
            out.format("%s %s\n", evx.getExceptionClassName(), evx.getMessage());
        }
        concat(of(ex, ex.getCause()), Arrays.stream(ex.getSuppressed()))
            .filter(e -> e != null)
            .forEach(Throwable::printStackTrace);
    }
    
    private static void defineArgs(JshExecutor jshExec, String args) {
        jshExec.onNext(String.format("args = new String[]{%s};", 
            splitByArgs(args).stream()
                .map(s -> "\"" + s + "\"")
                .collect(joining(", "))));
    }
    
    private static void onEvent(SnippetEvent ev) {
        JShellException ex = ev.exception();
        if (ex != null) {
            isError = true;
            printException(ex);
        }
        switch (ev.status()) {
        case VALID:
            if (ev.value() != null)
                if (!isScript || !ev.isSignatureChange())
                    out.print(ev.value());
            break;
        case REJECTED:
            isError = true;
            err.println("Rejected snippet: " + ev.snippet().source());
            printLocation(ev.snippet());
            for (Snippet s: unresolvedSnippets) {
                err.println("\nUnresolved snippet: ");
                err.println(s.source());
                printLocation(s);
            }
            break;
        case RECOVERABLE_DEFINED:
        case RECOVERABLE_NOT_DEFINED:
            unresolvedSnippets.add(ev.snippet());
            break;
        default:
            break;
        }
    }

    private static void printLocation(Snippet snippet) {
        jshell.diagnostics(snippet)
            .map(d -> d.getMessage(null) + "\nat position: " + d.getStartPosition())
            .forEach(err::println);
    }

    private static void runScript(String file) throws IOException {
        isScript = true;
        Files.readAllLines(Paths.get(file))
            .stream()
            .filter(l -> !isError)
            .forEach(jshExec::onNext);
    }
    
    private static void runSnippet(String snippet) {
        isScript = false;
        jshExec.onNext(snippet);
    }
    
    private static void mainInternal(String[] args) throws Exception {
        if (args.length < 1) {
            usage();
            exit(1);
        }

        String classPath = buildClassPath();
        jshell = JShell.builder()
            .out(out)
            .in(in)
            .err(err)
            .executionEngine("local")
            .compilerOptions("-g:none",
                "-implicit:none",
                "-proc:none",
                "-cp", classPath,
                "--add-modules", "ALL-MODULE-PATH")
            .build();

        jshell.addToClasspath(classPath);
        jshell.onSnippetEvent(Main::onEvent);

        jshExec = new JshExecutor(jshell);
        preloader(jshExec);

        ThrowingRunnable<Exception>[] r = new ThrowingRunnable[1];
        Map<String, Consumer<String>> handlers = Map.of(
            "-e", snippet -> r[0] = curry(Main::runSnippet, snippet),
            "-classpath", cp -> stream(cp.split(":"))
                .forEach(jshell::addToClasspath)
        );
        
        Function<String, Boolean> defaultHandler = arg -> {
            if (r[0] != null) {
                defineArgs(jshExec, arg);
                return false;
            }
            r[0] = curry(Main::runScript, arg);
            return true;
        };
        
        try
        {
            new SmartArgs(handlers, defaultHandler).parse(args);
            if (r[0] == null) throw new Exception();
        } catch (Exception e) {
            usage();
            exit(1);
        }
        
        try {
            r[0].run();
            jshExec.onComplete();
        } catch (Throwable ex) {
            printException(ex);
        } 

        jshell.close();
        
        exit(isError? 1: 0);
    }

    private static String buildClassPath() {
        return System.getProperty("java.class.path");
    }

    public static void main(String[] args) throws Exception {
        try {
            mainInternal(args);
        } catch (Throwable e) {
            e.printStackTrace();
            exit(1);
        }
    }
}

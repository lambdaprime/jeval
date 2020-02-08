/*
 * Copyright 2019 lambdaprime
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
package id.jeval;

import static id.xfunction.function.Curry.curry;
import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.in;
import static java.lang.System.out;
import static java.util.Arrays.stream;

import id.xfunction.function.ThrowingRunnable;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    private static Map<String, List<Snippet>> unresolvedSnippets = new LinkedHashMap<>();
    
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
        Snippet snippet = ev.snippet();
        String src = snippet.source();
        switch (ev.status()) {
        case VALID:
            if (ev.value() != null)
                if (!isScript || !ev.isSignatureChange())
                    out.print(ev.value());
            break;
        case REJECTED:
            isError = true;
            err.println("Rejected snippet: " + src);
            printLocation(snippet);
            for (Entry<String, List<Snippet>> e: unresolvedSnippets.entrySet()) {
                err.println("\nUnresolved snippet: ");
                err.println(e.getKey());
                e.getValue().forEach(Main::printLocation);
            }
            break;
        case RECOVERABLE_DEFINED:
        case RECOVERABLE_NOT_DEFINED:
            unresolvedSnippets.putIfAbsent(src, new ArrayList<>());
            unresolvedSnippets.get(src).add(snippet);
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

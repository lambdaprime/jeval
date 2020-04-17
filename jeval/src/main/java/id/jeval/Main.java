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

import static id.xfunction.function.Curry.curryAccept;
import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.in;
import static java.lang.System.out;
import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Arrays.stream;

import id.xfunction.XUtils;
import id.xfunction.function.ThrowingRunnable;
import jdk.jshell.JShell;

public class Main {

    private static JShell jshell;
    private static JshExecutor jshExec;
    private static EventHandler eventHandler;
    
    @SuppressWarnings("resource")
    private static void usage() throws IOException {
        Scanner scanner = new Scanner(Main.class.getResource("/README.md").openStream())
                .useDelimiter("\n");
        while (scanner.hasNext())
            System.out.println(scanner.next());
    }

    @SuppressWarnings("resource")
    private static void preloader(JshExecutor jshExec) throws IOException {
        eventHandler.setIsScript(true);
        Scanner scanner = new Scanner(Main.class.getResource("/preloader.jsh").openStream())
                .useDelimiter("\n");
        while (scanner.hasNext())
            jshExec.onNext(scanner.next());
    }
    
    private static void defineArgs(JshExecutor jshExec, List<String> args) {
        jshExec.onNext(String.format("args = new String[]{%s};", 
            args.stream()
                .map(s -> "\"" + s + "\"")
                .collect(joining(", "))));
    }
    
    private static void runScript(String file) throws IOException {
        eventHandler.setIsScript(true);
        Files.readAllLines(Paths.get(file))
            .stream()
            .filter(l -> !eventHandler.isError())
            .forEach(jshExec::onNext);
    }
    
    private static void runSnippet(String snippet) {
        eventHandler.setIsScript(false);
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
        
        eventHandler = new EventHandler(jshell);
        jshell.onSnippetEvent(eventHandler::onEvent);
        jshell.onShutdown(eventHandler::onShutdown);

        jshExec = new JshExecutor(jshell);
        preloader(jshExec);

        ThrowingRunnable<Exception>[] runnable = new ThrowingRunnable[1];
        Map<String, Consumer<String>> handlers = Map.of(
            "-e", snippet -> runnable[0] = curryAccept(Main::runSnippet, snippet),
            "-classpath", cp -> stream(cp.split(":"))
                .forEach(jshell::addToClasspath)
        );

        List<String> runnableArgs = new ArrayList<>();
        Function<String, Boolean> defaultHandler = arg -> {
            if (runnable[0] != null) {
                // if we set the runnable already we can start
                // populate the args
                runnableArgs.add(arg);
                return true;
            }
            runnable[0] = curryAccept(Main::runScript, arg);
            return true;
        };
        
        try
        {
            new SmartArgs(handlers, defaultHandler).parse(args);
            if (runnable[0] == null) throw new Exception();
        } catch (Exception e) {
            usage();
            exit(1);
        }
        
        defineArgs(jshExec, runnableArgs);
        
        try {
            runnable[0].run();
            jshExec.onComplete();
        } catch (Throwable ex) {
            XUtils.printExceptions(ex);
        } 

        jshell.close();
        
        exit(eventHandler.isError()? 1: 0);
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

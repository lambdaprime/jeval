/*
 * Copyright 2019 jeval project
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
package id.jeval;

import static id.jeval.commands.CommandConstants.OPEN_COMMAND;
import static id.xfunction.function.Curry.curryAccept;
import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.in;
import static java.lang.System.out;
import static java.util.stream.Collectors.joining;

import id.jeval.commands.DependencyResolver;
import id.jeval.commands.OpenScripts;
import id.xfunction.XUtils;
import id.xfunction.cli.ArgumentParsingException;
import id.xfunction.cli.SmartArgs;
import id.xfunction.function.ThrowingRunnable;
import id.xfunction.function.Unchecked;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jdk.jshell.JShell;
import jdk.jshell.JShell.Builder;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class Main {

    private static final String CLASSPATH_SEP = System.getProperty("path.separator", ":");
    private static JShell jshell;
    private static JshExecutor jshExec;
    private static EventHandler eventHandler;
    private static Optional<Path> scriptPath = Optional.empty();

    @SuppressWarnings("resource")
    private static void usage() throws IOException {
        Scanner scanner =
                new Scanner(Main.class.getResource("/jeval/README.md").openStream())
                        .useDelimiter("\n");
        while (scanner.hasNext()) System.out.println(scanner.next());
    }

    @SuppressWarnings("resource")
    private static void preloader(JshExecutor jshExec) throws IOException {
        eventHandler.setIsScript(true);
        Scanner scanner =
                new Scanner(Main.class.getResource("/jeval/jeval-startup.jsh").openStream())
                        .useDelimiter("\n");
        while (scanner.hasNext()) {
            eventHandler.onNextLine(Paths.get("jeval-startup.jsh"));
            jshExec.onNext(scanner.next());
        }
    }

    private static void defineArgs(JshExecutor jshExec, List<String> args) {
        jshExec.onNext(
                String.format(
                        "args = new String[]{%s};",
                        args.stream().map(Main::asStringLiteral).collect(joining(", "))));
    }

    private static void defineScriptPath(JshExecutor jshExec) {
        scriptPath.ifPresent(
                path -> {
                    jshExec.onNext(
                            String.format(
                                    "scriptPath = Optional.of(Paths.get(%s));",
                                    asStringLiteral(path.toString())));
                });
    }

    private static String asStringLiteral(String str) {
        // mask backslash so that it can be used as Java string literal
        str = str.replace("\\", "\\\\");
        return XUtils.quote(str);
    }

    private static void runScript(Path file, Stream<String> stream) {
        eventHandler.setIsScript(true);
        OpenScripts opener = new OpenScripts();
        boolean[] firstLine = new boolean[] {true};
        stream.forEach(
                line -> {
                    if (firstLine[0]) {
                        firstLine[0] = false;
                        if (line.startsWith("#!")) {
                            return;
                        }
                    }
                    if (eventHandler.isError()) return;
                    eventHandler.onNextLine(file);
                    if (line.startsWith(OPEN_COMMAND)) {
                        Path openFile = opener.open(file, line);
                        runScript(
                                openFile,
                                Unchecked.get(() -> Files.readAllLines(openFile).stream()));
                        return;
                    }
                    jshExec.onNext(line);
                });
    }

    private static void runSnippet(String snippet) {
        eventHandler.setIsScript(false);
        eventHandler.onNextLine(Paths.get(""));
        jshExec.onNext(snippet);
    }

    private static void mainInternal(String[] args) throws Exception {
        if (args.length < 1) {
            usage();
            exit(1);
        }

        List<String> classPathList = toClasspathList(System.getProperty("java.class.path", ""));

        ThrowingRunnable<Exception>[] runnable = new ThrowingRunnable[1];
        List<String> runnableArgs = new ArrayList<>();
        Map<String, Consumer<String>> handlers =
                Map.of(
                        "-e", snippet -> runnable[0] = curryAccept(Main::runSnippet, snippet),
                        "-classpath", cp -> classPathList.addAll(toClasspathList(cp)));
        Function<String, Boolean> defaultHandler =
                arg -> {
                    if (arg.equals("-i")) {
                        runnable[0] =
                                () -> {
                                    scriptPath = Optional.of(Paths.get("").toAbsolutePath());
                                    Scanner scanner =
                                            new Scanner(
                                                    new BufferedReader(new InputStreamReader(in)));
                                    Iterator<String> iter =
                                            new Iterator<String>() {
                                                @Override
                                                public boolean hasNext() {
                                                    return scanner.hasNextLine();
                                                }

                                                @Override
                                                public String next() {
                                                    return scanner.nextLine();
                                                }
                                            };
                                    Stream<String> stream =
                                            StreamSupport.stream(
                                                    Spliterators.spliteratorUnknownSize(
                                                            iter, Spliterator.ORDERED),
                                                    false);
                                    runScript(scriptPath.get(), stream);
                                };
                        return true;
                    }
                    if (runnable[0] != null) {
                        // if we set the runnable already we can start
                        // populate the args
                        runnableArgs.add(arg);
                        return true;
                    }
                    Path path = Paths.get(arg);
                    scriptPath = Optional.of(path.toAbsolutePath());
                    Unchecked.run(
                            () -> classPathList.addAll(new DependencyResolver().resolve(path)));
                    runnable[0] = () -> runScript(path, Files.readAllLines(path).stream());
                    return true;
                };

        try {
            new SmartArgs(handlers, defaultHandler).parse(args);
            if (runnable[0] == null) throw new Exception();
        } catch (ArgumentParsingException e) {
            usage();
            exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            exit(1);
        }

        String classPath = toClasspath(classPathList);
        Builder jshellBuilder =
                JShell.builder()
                        .out(out)
                        .in(in)
                        .err(err)
                        .executionEngine("local") // LocalExecutionControlProvider
                        .compilerOptions(
                                "-g:none",
                                "-implicit:none",
                                "-proc:none",
                                "-cp",
                                classPath,
                                "--add-modules",
                                "ALL-MODULE-PATH");

        jshell = jshellBuilder.build();

        jshell.addToClasspath(classPath);

        eventHandler = new EventHandler(jshell);
        jshell.onSnippetEvent(eventHandler::onEvent);

        jshExec = new JshExecutor(jshell);
        preloader(jshExec);

        defineArgs(jshExec, runnableArgs);
        defineScriptPath(jshExec);

        try {
            runnable[0].run();
            jshExec.onComplete();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        eventHandler.close();
        jshell.close();

        exit(eventHandler.isError() ? 1 : 0);
    }

    private static String toClasspath(List<String> classPathList) {
        return classPathList.stream().collect(joining(CLASSPATH_SEP));
    }

    private static List<String> toClasspathList(String classpath) {
        return new ArrayList<>(Arrays.asList(classpath.split(CLASSPATH_SEP)));
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

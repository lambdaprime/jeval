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
import static java.lang.System.in;
import static java.lang.System.out;
import static java.util.stream.Collectors.joining;

import id.jeval.commands.DependencyResolver;
import id.jeval.commands.OpenScripts;
import id.xfunction.Preconditions;
import id.xfunction.ResourceUtils;
import id.xfunction.XUtils;
import id.xfunction.function.ThrowingRunnable;
import id.xfunction.function.Unchecked;
import id.xfunction.lang.XExec;
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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jdk.jshell.JShell;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class JEval {

    private static final String STARTUP_PATH = "jeval/jeval-startup.jsh";
    private static final String CLASSPATH_SEP = System.getProperty("path.separator", ":");
    private static final ResourceUtils resourceUtils = new ResourceUtils();
    private JShell jshell;
    private JshExecutor jshExec;
    private EventHandler eventHandler;
    private Optional<Path> scriptPath = Optional.empty();
    private ThrowingRunnable<Exception> runnable;
    private List<String> runnableArgs = new ArrayList<>();
    private String classPath;

    public static class Builder {
        private List<String> classPathList =
                toClasspathList(System.getProperty("java.class.path", ""));
        private JEval jeval = new JEval();
        private boolean useJShellExternal;

        public Builder runSnippet(String snippet) {
            Preconditions.isTrue(
                    !useJShellExternal, "Cannot run snippet when external JShell requested");
            // in Windows quotes are not striped from the command arguments and are passed as-is to
            // the application
            // for this reason we strip them manually
            snippet = unwrap(snippet, true, "\'\"".toCharArray());
            jeval.runnable = curryAccept(jeval::runSnippet, snippet);
            return this;
        }

        public Builder addClasspath(String cp) {
            classPathList.addAll(toClasspathList(cp));
            return this;
        }

        public Builder addRunArgument(String arg) {
            jeval.runnableArgs.add(arg);
            return this;
        }

        public Builder runInteractive() {
            Preconditions.isTrue(
                    !useJShellExternal,
                    "Cannot run interactive mode when external JShell requested");
            jeval.runnable =
                    () -> {
                        jeval.scriptPath = Optional.of(Paths.get("").toAbsolutePath());
                        Scanner scanner =
                                new Scanner(new BufferedReader(new InputStreamReader(in)));
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
                        jeval.runScript(jeval.scriptPath.get(), stream);
                    };
            return this;
        }

        public Builder runScript(String arg) {
            Preconditions.isTrue(
                    !useJShellExternal, "Cannot run the script when external JShell requested");
            Path path = Paths.get(arg);
            jeval.scriptPath = Optional.of(path.toAbsolutePath());
            Unchecked.run(() -> classPathList.addAll(new DependencyResolver().resolve(path)));
            jeval.runnable = () -> jeval.runScript(path, Files.readAllLines(path).stream());
            return this;
        }

        public boolean hasRunnable() {
            return jeval.runnable != null;
        }

        public Builder runJShellExternal() {
            useJShellExternal = true;
            return this;
        }

        public JEval build() {
            jeval.classPath = toClasspath(classPathList);
            if (useJShellExternal)
                return new JEval() {
                    @Override
                    public boolean run() throws Exception {
                        var startupFile = Files.createTempFile("jeval", "startup");
                        startupFile.toFile().deleteOnExit();
                        resourceUtils.extractResource(STARTUP_PATH, startupFile);
                        var exec =
                                new XExec("jshell", "-startup", startupFile.toString())
                                        .withEnvironmentVariables(
                                                Map.of("CLASSPATH", jeval.classPath));
                        exec.getProcessBuilder().inheritIO();
                        return exec.start().await() == 0;
                    }
                };
            else return jeval;
        }
    }

    @SuppressWarnings("resource")
    private void preloader(JshExecutor jshExec) throws IOException {
        eventHandler.setIsScript(true);
        var script = Paths.get("jeval-startup.jsh");
        resourceUtils
                .readResourceAsList(STARTUP_PATH)
                .forEach(
                        line -> {
                            eventHandler.onNextLine(script);
                            jshExec.onNext(line);
                        });
    }

    private void defineArgs(JshExecutor jshExec, List<String> args) {
        jshExec.onNext(
                String.format(
                        "args = new String[]{%s};",
                        args.stream().map(JEval::asStringLiteral).collect(joining(", "))));
    }

    private void defineScriptPath(JshExecutor jshExec) {
        scriptPath.ifPresent(
                path -> {
                    jshExec.onNext(
                            String.format(
                                    "scriptPath = Optional.of(Paths.get(%s));",
                                    asStringLiteral(path.toString())));
                });
    }

    private void runScript(Path file, Stream<String> stream) {
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

    private void runSnippet(String snippet) {
        eventHandler.setIsScript(false);
        eventHandler.onNextLine(Paths.get(""));
        jshExec.onNext(snippet);
    }

    public boolean run() throws Exception {
        if (runnable == null) throw new Exception();
        JShell.Builder jshellBuilder =
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
            runnable.run();
            jshExec.onComplete();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        eventHandler.close();
        jshell.close();

        return eventHandler.isError();
    }

    private static String toClasspath(List<String> classPathList) {
        return classPathList.stream().collect(joining(CLASSPATH_SEP));
    }

    private static List<String> toClasspathList(String classpath) {
        return new ArrayList<>(Arrays.asList(classpath.split(CLASSPATH_SEP)));
    }

    private static String asStringLiteral(String str) {
        // mask backslash so that it can be used as Java string literal
        str = str.replace("\\", "\\\\");
        return XUtils.quote(str);
    }

    private static String unwrap(String s, boolean trim, char[] symbolsToR) {
        String st = trim ? s.trim() : s;
        if (st.isEmpty()) return s;
        if (st.length() < 2) return s;
        for (var ch : symbolsToR) {
            if (st.charAt(0) == ch && st.charAt(st.length() - 1) == ch)
                return st.substring(1, st.length() - 1);
        }
        return s;
    }
}

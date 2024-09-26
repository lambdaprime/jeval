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

import static java.lang.System.exit;

import id.xfunction.cli.ArgumentParsingException;
import id.xfunction.cli.SmartArgs;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class Main {

    @SuppressWarnings("resource")
    private static void usage() throws IOException {
        Scanner scanner =
                new Scanner(Main.class.getResource("/jeval/README.md").openStream())
                        .useDelimiter("\n");
        while (scanner.hasNext()) System.out.println(scanner.next());
    }

    private static void mainInternal(String[] args) throws Exception {
        if (args.length < 1) {
            usage();
            exit(1);
        }

        var builder = new JEval.Builder();
        Map<String, Consumer<String>> handlers =
                Map.of(
                        "-e", builder::runSnippet,
                        "-classpath", builder::addClasspath);
        Function<String, Boolean> defaultHandler =
                arg -> {
                    if (arg.equals("-i")) {
                        builder.runInteractive();
                        return true;
                    }
                    if (builder.hasRunnable()) {
                        // if we set the runnable already we can start
                        // populate the args
                        builder.addRunArgument(arg);
                        return true;
                    }
                    builder.runScript(arg);
                    return true;
                };

        try {
            new SmartArgs(handlers, defaultHandler).parse(args);
        } catch (ArgumentParsingException e) {
            usage();
            exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            exit(1);
        }

        exit(builder.build().run() ? 1 : 0);
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

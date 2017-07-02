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

import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.in;
import static java.lang.System.out;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;

public class Main {

    private static boolean isError = false;
    private static JShell jshell;
    private static boolean isScript;
    
    @SuppressWarnings("resource")
    static void usage() throws IOException {
        Scanner scanner = new Scanner(Main.class.getResource("README.org").openStream())
                .useDelimiter("\n");
        while (scanner.hasNext())
            System.out.println(scanner.next());
    }

    static void onEvent(SnippetEvent ev) {
        Throwable ex = ev.exception();
        if (ex != null) {
            concat(of(ex, ex.getCause()), Arrays.stream(ex.getSuppressed()))
                .filter(e -> e != null)
                .forEach(Throwable::printStackTrace);
        }
        switch (ev.status()) {
        case VALID:
            if (ev.value() != null)
                if (!isScript || !ev.isSignatureChange())
                    out.print(ev.value());
            break;
        case REJECTED:
            isError = true;
            jshell.diagnostics(ev.snippet())
                 .map(d -> d.getMessage(null) + "\nat position: " + d.getStartPosition())
                 .forEach(err::println);
            break;
        default:
            break;
        }
    }
    
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        if (args.length < 1) {
            usage();
            exit(1);
        }
        jshell = JShell.builder()
            .out(out)
            .in(in)
            .err(err)
            .compilerOptions("-g:none","-implicit:none", "-proc:none")
            .build();
        
        jshell.eval("import static java.util.stream.IntStream.*;");
        jshell.eval("import static java.util.stream.Collectors.*;");
        jshell.eval("import static java.lang.System.*;");
        jshell.eval("import static java.nio.file.Files.*;");
        jshell.eval("import static java.lang.Math.*;");
        
        jshell.eval("import java.util.*;");
        jshell.eval("import java.util.stream.*;");
        jshell.eval("import java.io.*;");
        jshell.eval("import java.nio.*;");
        jshell.eval("import java.nio.file.*;");
        jshell.eval("import javax.xml.parsers.*;");
        jshell.eval("import javax.xml.xpath.*;");
        jshell.eval("import java.net.*;");
        jshell.eval("import org.w3c.dom.*;");
        
        jshell.eval("BufferedReader stdin = new BufferedReader(new InputStreamReader(in));");

        jshell.onSnippetEvent(Main::onEvent);
        
        if ("-e".equals(args[0]))
            jshell.eval(args[1]);
        else {
            isScript = true;
            JshExecutor jshExec = new JshExecutor(jshell);
            Files.readAllLines(Paths.get(args[0]))
                .stream()
                .forEach(jshExec::add);
        }
        
        exit(isError? 1: 0);
    }

}

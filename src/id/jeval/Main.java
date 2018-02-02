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
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static void usage() throws IOException {
        Scanner scanner = new Scanner(Main.class.getResource("README.org").openStream())
                .useDelimiter("\n");
        while (scanner.hasNext())
            System.out.println(scanner.next());
    }

    @SuppressWarnings("resource")
    private static void preloader(JshExecutor jshExec) throws IOException {
        isScript = true;
        Scanner scanner = new Scanner(Main.class.getResource("preloader.jsh").openStream())
                .useDelimiter("\n");
        while (scanner.hasNext())
            jshExec.onNext(scanner.next());
    }
    
    private static List<String> split(String s) {
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
        concat(of(ex, ex.getCause()), Arrays.stream(ex.getSuppressed()))
            .filter(e -> e != null)
            .forEach(Throwable::printStackTrace);
    }
    
    private static void defineArgs(JshExecutor jshExec, String args) {
        jshExec.onNext(String.format("String[] args = {%s};", 
            split(args).stream()
                .map(s -> "\"" + s + "\"")
                .collect(joining(", "))));
    }
    
    private static void onEvent(SnippetEvent ev) {
        Throwable ex = ev.exception();
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
            .executionEngine("local")
            .compilerOptions("-g:none","-implicit:none", "-proc:none")
            .build();
        
        jshell.onSnippetEvent(Main::onEvent);

        JshExecutor jshExec = new JshExecutor(jshell);
        preloader(jshExec);
        
        try
        {
            if ("-e".equals(args[0])) {
                if (args.length < 2) {
                    usage();
                    exit(1);
                }
                if (args.length == 3)
                    defineArgs(jshExec, args[2]);
                isScript = false;
                jshExec.onNext(args[1]);
            } else {
                isScript = true;
                if (args.length == 2)
                    defineArgs(jshExec, args[1]);
                Files.readAllLines(Paths.get(args[0]))
                    .stream()
                    .filter(Predicate.isEqual("").negate())
                    .filter(l -> !isError)
                    .forEach(jshExec::onNext);
            }
            jshExec.onComplete();
        } catch (Throwable ex) {
            printException(ex);
        } 

        jshell.close();
        
        exit(isError? 1: 0);
    }

}

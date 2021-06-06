import static java.util.stream.IntStream.*;
import static java.util.stream.Collectors.*;
import static java.lang.System.*;
import static java.nio.file.Files.*;
import static java.lang.Math.*;
import static java.util.Arrays.*;
import static java.lang.String.*;

import javax.script.*;
import jdk.nashorn.api.scripting.*;
        
import java.util.*;
import java.util.stream.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.regex.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpResponse.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import id.xfunction.*;
import id.xfunction.function.*;
import id.xfunction.net.*;
import static id.xfunction.CommandLineInterface.*;
import static id.xfunction.XAsserts.*;

BufferedReader stdin = new BufferedReader(new InputStreamReader(in));

/**
 * Implements netcat operations:
 *
 * - Netcat.listen(int port)
 * - Netcat.connect(String host, int port)
 *
 * All input/output goes through stdin/stdout.
 *
 */
public class Netcat {

    private static void create(Socket s) throws IOException {
        Stream<String> sin = new BufferedReader(new InputStreamReader(
            s.getInputStream())).lines();
        ForkJoinPool.commonPool()
            .execute(() -> sin.forEach(System.out::println));
        PrintStream sout = new PrintStream(s.getOutputStream());
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        stdin.lines().forEach(sout::println);
    }

    /**
     * Starts listening for incoming connection on a given port.
     * 
     * All incoming data from the client will be forwarded to System.out.
     * All input from System.in will be sent back to the client.
     * 
     * @throws IOException
     */
    public static void listen(int port) throws IOException {
        create(new ServerSocket(port).accept());
    }

    /**
     * Establishes connection with a remote host.
     * 
     * All outgoing data from the remote host will be forwarded to System.out.
     * All input from System.in will be sent to the remote host.
     * 
     * @throws IOException
     */
    public static void connect(String host, int port) throws IOException {
        create(new Socket(host, port));
    }
}

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;


/**
 * Sleeps with no exception
 */
void sleep(long msec) {
    try {
        Thread.sleep(msec);
    } catch (Exception e) { }
}

String[] args = new String[0];

/**
 * When user specifies execution script its path will
 * be available here.
 */
Optional<Path> scriptPath = Optional.empty();

void printf(String s, String...fmt) {
    out.print(String.format(s, fmt));
}

Stream<String> findMatches(String regexp, String str) {
    return Pattern.compile(regexp).matcher(str).results().map(MatchResult::group);
}

void error(String msg) {
    throw new RuntimeException(msg);
}

int rand(int m) {
    return (int)(Math.random() * m);
}

String read() {
    return cli.read();
}

int readInt() {
    return cli.readInt();
}

void waitPressEnter() {
    cli.waitPressEnter();
}
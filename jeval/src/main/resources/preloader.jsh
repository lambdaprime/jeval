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

public class Microprofiler {

    private Optional<ThreadMXBean> mxbean = Optional.empty();
    
    public Microprofiler() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        if (threadMXBean.isCurrentThreadCpuTimeSupported())
            mxbean = Optional.of(threadMXBean);
    }

    /**
     * Measures real CPU execution time in milliseconds.
     */
    public long measureUserCpuTime(Runnable r) {
        if (!mxbean.isPresent())
            return -1;
        ThreadMXBean threadMXBean = mxbean.get();
        long s = threadMXBean.getCurrentThreadUserTime();
        r.run();
        return Duration.ofNanos(threadMXBean.getCurrentThreadUserTime() - s).toMillis();
    }
    
    /**
     * Measures execution time in milliseconds using wall clock.
     * It is not precise time since CPU may perform context switch to another
     * thread but the clock will still be ticking.
     */
    public long measureRealTime(Runnable r) {
        long s = Instant.now().toEpochMilli();
        r.run();
        return Instant.now().toEpochMilli() - s;
    }

    /**
     * Chooses the best available on current JVM way to measure the
     * execution time and returns it in milliseconds.
     */
    public long measureExecutionTime(Runnable r) {
        if (!mxbean.isPresent())
            return measureRealTime(r);
        else
            return measureUserCpuTime(r);
    }
    
    public static long gcCount() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream()
            .mapToLong(GarbageCollectorMXBean::getCollectionCount)
            .filter(n -> n >= 0)
            .sum();
    }

}

/**
 * Sleeps with no exception
 */
void sleep(long msec) {
    try {
        Thread.sleep(msec);
    } catch (Exception e) { }
}

String[] args = new String[0];

void printf(String s, String...fmt) {
    out.print(String.format(s, fmt));
}

Stream<String> findMatches(String regexp, String str) {
    return Pattern.compile(regexp).matcher(str).results().map(MatchResult::group);
}

void error(String msg) {
    throw new RuntimeException(msg);
}

void assertTrue(boolean expr) {
    if (!expr) error("Assertion error");
}

void assertTrue(boolean expr, String msg) {
    if (!expr) error(msg);
}
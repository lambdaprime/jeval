import static java.util.stream.IntStream.*;
import static java.util.stream.Collectors.*;
import static java.lang.System.*;
import static java.nio.file.Files.*;
import static java.lang.Math.*;
import javax.script.*;
import jdk.nashorn.api.scripting.*;
        
import java.util.*;
import java.util.stream.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.net.*;
import org.w3c.dom.*;
        
BufferedReader stdin = new BufferedReader(new InputStreamReader(in));

class Netcat {

    static void create(Socket s) throws IOException {
        Stream<String> sin = new BufferedReader(new InputStreamReader(
            s.getInputStream())).lines();
        ForkJoinPool.commonPool()
            .execute(() -> sin.forEach(out::println));
        PrintStream sout = new PrintStream(s.getOutputStream());
        stdin.lines().forEach(sout::println);
    }

    static void listen(int port) throws IOException {
        create(new ServerSocket(port).accept());
    }

    static void connect(String host, int port) throws IOException {
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

void sleep(int msec) {
    try {
        Thread.sleep(msec);
    } catch (Exception e) { }
}

String[] args = new String[0];
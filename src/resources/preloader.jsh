import static java.util.stream.IntStream.*;
import static java.util.stream.Collectors.*;
import static java.lang.System.*;
import static java.nio.file.Files.*;
import static java.lang.Math.*;
import static java.util.Arrays.*;
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
import org.w3c.dom.*;
import org.xml.sax.*;
        
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

/**
 * Run shell command and obtain its output as stream of lines.
 */
public class Exec {

    private String[] cmd;
    private boolean singleLine;
    private Stream<String> input;

    public static class Result {
        public Stream<String> stdout;
        public Stream<String> stderr;
        Future<Integer> code;
        Result(Stream<String> stdout, Stream<String> stderr) { this.stdout = stdout; this.stderr = stderr; }
    }

    /**
     * Constructor which accepts the command to run and list of arguments
     */
    public Exec(String... cmd) {
        this.cmd = cmd;
    }

    public Exec(String cmd) {
        this(new String[] {cmd});
        singleLine = true;
    }

    public Exec withInput(Stream<String> input) {
        this.input = input;
        return this;
    }

    public Result run() {
        try {
            Process p = runProcess();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            if (input != null) {
                PrintStream ps = new PrintStream(p.getOutputStream(), true);
                input.forEach(ps::println);
                ps.close();
            }
            BufferedReader ein = new BufferedReader(
                new InputStreamReader(p.getErrorStream()));
            Result result = new Result(in.lines(), ein.lines());
            result.code = p.onExit().thenApply(proc -> proc.exitValue());
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("Encountered error executing command: " + Arrays.toString(cmd), e);
        }
    }

    private Process runProcess() throws IOException {
        if (singleLine) {
            return Runtime.getRuntime().exec(cmd[0]);
        }
        return new ProcessBuilder(cmd).start();
    }

}

/**
 * Sleeps with no exception
 */
void sleep(int msec) {
    try {
        Thread.sleep(msec);
    } catch (Exception e) { }
}

String[] args = new String[0];

void printf(String s) {
    out.print(String.format(s));
}

String read() throws Exception {
    return stdin.readLine();
}

int readInt() {
    return new Scanner(System.in).nextInt();
}

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathEvaluationResult;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathNodes;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Xml {

    public static List<String> query(Path xml, String xpath) {
        List<String> out = new ArrayList<>();
        Consumer<Node> visitor = saveVisitor(out);
        try {
            xpath_(new InputSource(new FileInputStream(xml.toFile())), xpath, visitor);
            return out;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> query(String xml, String xpath) {
        List<String> out = new ArrayList<>();
        Consumer<Node> visitor = saveVisitor(out);
        xpath_(new InputSource(new StringReader(xml)), xpath, visitor);
        return out;
    }

    public static void replace(Path xml, String xpath, String value) {
        try {
            Consumer<Node> visitor = replaceVisitor(value);
            String str = asString(xpath_(new InputSource(new FileInputStream(xml.toFile())), xpath, visitor));
            Files.writeString(xml, str, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String replace(String xml, String xpath, String value) {
        Consumer<Node> visitor = replaceVisitor(value);
        return asString(xpath_(new InputSource(new StringReader(xml)), xpath, visitor));
    }

    private static void parseNodeList(NodeList l, Consumer<Node> visitor) {
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            switch (n.getNodeType()) {
            case Node.ELEMENT_NODE: {
                parseNodeList(n.getChildNodes(), visitor);
                break;
            }
            case Node.TEXT_NODE: {
                String value = n.getNodeValue();
                if (value.trim().isEmpty()) continue;
                visitor.accept(n);
            }}
        }
    }

    private static Consumer<Node> saveVisitor(List<String> out) {
        return n -> {
            out.add(n.getNodeValue());
        };
    }

    private static Consumer<Node> replaceVisitor(String value) {
        return n -> {
            n.setNodeValue(value);
        };
    }

    private static String asString(Document doc) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Document xpath_(InputSource src, String xpath, Consumer<Node> visitor) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src);
            XPathEvaluationResult<?> result = XPathFactory.newInstance().newXPath()
                    .evaluateExpression(xpath, doc);
            switch (result.type()) {
            case NODE: {
                Node n = (Node)result.value();
                visitor.accept(n);
                break;
            }
            case NODESET: {
                XPathNodes l = (XPathNodes) result.value();
                l.forEach(n -> {
                    parseNodeList(n.getChildNodes(), visitor);
                });
                break;
            }}
            return doc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
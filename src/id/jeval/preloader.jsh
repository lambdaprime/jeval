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

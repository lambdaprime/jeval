**jeval** - command line Java code interpreter similar to JShell except with **jeval** you can:

- execute Java code straight from the command line (same as you would do with perl -e, bash -c):
``` bash
% jeval -e 'out.println("Hello world")'
Hello world
```
With JShell it will look like:
``` bash
echo 'System.out.println("Hello world")' | jshell -
```
- execute complete Java shell scripts and see compilation errors if any
``` bash
% cat test.java
System.out.println("Hello world");
% jeval test.java 
Hello world
```
With JShell you would have to use "/exit" in the end (otherwise it will start interactive mode):
``` bash
% cat test.java
System.out.println("Hello world");
/exit
% jshell test.java
Hello world
```
And it will not print you any errors:
``` bash
% cat test.java
System.out.println("Hello world");
error here
/exit
% jshell test.java
Hello world
```
- use pipes to pass data to your Java code (**jeval** binds all standard streams to support lazy reading from stdin)
``` bash
echo -e "ab\ncd\nef" | jeval -e "stdin.lines().collect(joining(\",\"))"
```
For JShell there is no default support of that
- pass arguments to your Java scripts and handle them in same way as you do it in Java:
```bash
$ jeval -e 'format("args %s, %s", args[0], args[1])' "arg1" "arg2"
"args arg1, arg2"
```

**jeval** does not require you to write class body with main method and all boilerplate code (but you still can do it if you want). You just write Java as you would do it in JShell.

**jeval** comes with [**xfunction**](https://github.com/lambdaprime/xfunction) library and exports most of it methods to global space.

lambdaprime <id.blackmesa@gmail.com>

# Download

You can download **jeval** from <https://github.com/lambdaprime/jeval/blob/master/jeval/release>

# Requirements

Java 11

# Install

## Linux

``` bash
echo "export PATH=$PATH:<JEVAL_INSTALL_DIR>" >> ~/.bashrc
```

Or in case you use Zsh:

```bash
echo "export PATH=$PATH:<JEVAL_INSTALL_DIR>" >> ~/.zshrc
```

## Windows

Open cmd and execute following command:

```
setx PATH "%PATH%;<JEVAL_INSTALL_DIR>"
```

# Usage

```bash
jeval [ <JAVA_SCRIPT> | -e <JAVA_SNIPPET> ] [ARGS]
```

Where: 

JAVA_SCRIPT - Java script file to be executed. I prefer to save Java scripts with *.java extension so Eclipse will automatically highlight the syntax in them.

JAVA_SNIPPET - Java expression. **jeval** will evaluate the expression and print its result. If you are entering more than one expression please surround JAVA_SNIPPET with "{}". If your snippet contains quotes "" you need to escape them with backslash. In Linux it is enough to enclose the snippet in single quotes ''.

ARGS - optional user arguments which will be available to you through the global variable "args: String[]". 

## Class path

To add new JAR files into class path use CLASSPATH env variable:

``` java
$ CLASSPATH=/opt/javafx-sdk-11.0.2/lib/* jeval script.java 
```

## JVM arguments

To pass arguments to the JVM use JAVA_ARGS env variable:

``` java
$ JAVA_ARGS="-Dtest=hello -Xmx50m" jeval -e 'System.getProperty("test")'
"hello"
```

## Default imports

**jeval** by default imports following packages and static methods to the global space so you don't need to worry to import them each time manually:

```java
java.util.stream.IntStream.*;
java.util.stream.Collectors.*;
java.lang.System.*;
java.nio.file.Files.*;
java.lang.Math.*;
java.util.Arrays.*;
javax.script.*;
jdk.nashorn.api.scripting.*;
        
java.util.*;
java.util.stream.*;
java.util.concurrent.*;
java.util.function.*;
java.util.regex.*;
java.io.*;
java.nio.*;
java.nio.file.*;
javax.xml.parsers.*;
javax.xml.xpath.*;
java.net.*;
java.net.http.*;
java.net.http.HttpResponse.*;
org.w3c.dom.*;
org.xml.sax.*;

id.xfunction.*;
id.xfunction.function.*;
id.xfunction.net.*;
id.xfunction.CommandLineInterface.*;
```

## Predefined variables

```java
BufferedReader stdin = new BufferedReader(new InputStreamReader(in))
```

## Predefined functions

- sleep(long msec)

  Sleeps with no exception

- error(msg: String)

  Throws RuntimeException
  
- assertTrue(expr: boolean)

  If expr is false throw RuntimeException
  
- assertTrue(expr: boolean, message: String)

  If expr is false throw RuntimeException with message

- findMatches(regexp: String, str: String): Stream<String>

  Search string for substrings which satisfy the regexp and return them in a stream

## Predefined classes

### Netcat

Implements netcat operations:

- Netcat.listen(int port)
- Netcat.connect(String host, int port)
    
All input/output goes through stdin/stdout.

### Microprofiler

- Microprofiler.measureUserCpuTime(Runnable r): long
  
  Measures real CPU execution time in milliseconds and returns it.

- Microprofiler.measureRealTime(Runnable r): long

  Measures execution time in milliseconds using wall clock and returns it. It is not precise time since CPU may perform context switch to another thread but the clock will still be ticking.

- Microprofiler.measureExecutionTime(Runnable r): long

  Chooses the best available on current JVM way to measure the execution time and returns it in milliseconds.

## Spitting script in multiple files

**jeval** supports "/open" command similar to what JShell provides. For example using that command you can extract some common logic into separate script file so **jeval** will read that:

```bash
% cat << EOF > /tmp/script.java
/open parsers.java
printf(parseToHex(123) + "\n");
EOF
% cat << EOF > /tmp/parsers.java
String parseToHex(int num) {
    return Integer.toHexString(num);
}
EOF
% jeval /tmp/script.java
7b
```

Here in script.java we include parsers.java and then call parseToHex method defined there.

# Examples

## Say hello to the world:

```bash
$ jeval -e 'out.println("Hello world")'
Hello world
```

## Print sequence of numbers:

```bash
$ jeval -e "range(1,10).forEach(out::println)"
1
2
3
4
5
6
7
8
9
```

## Read XML and print value of the element using its XPath:

```bash
$ cat << EOF > /tmp/r.xml
<notes>
    <note>
        <to test="ggg1">Tove</to>
    </note>
    <note>
        <to test="ggg2">Bove</to>
    </note>
</notes>
EOF
$ jeval -e '{Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("/tmp/r.xml")); out.println(XPathFactory.newInstance().newXPath().evaluate("//note/to", d));}'
Tove
```

## Return integer in binary format:

```bash
$ echo 14 | jeval -e 'Integer.toBinaryString(new Scanner(in).nextInt())'
"1110"
```

## Create temporary file and return its name

```bash
$ jeval -e 'Files.createTempFile(null, "tmp")'
/tmp/11873450107364399793tmp
```

## Join lines using "," as a delimeter

```bash
$ echo -e "ab\ncd\nef" | jeval -e "stdin.lines().collect(joining(\",\"))"
"ab,cd,ef"
```

## Execute JavaScript snippet which will read JSON and return value of specified parameter

```bash
$ echo '{"menu":123}' | jeval -e 'new ScriptEngineManager().getEngineByName("nashorn").eval("var v = " + stdin.lines().collect(joining("\n")) + "; v[\"menu\"]");'
123
```

## Use "document-here" construction

```java
String json = <<EOF
{
  "menu": {
    "id": "1",
    "value": "File",
    "menuitem": [
      {"value": "2", "onclick": "CreateNewDoc()"},
      {"value": "3", "onclick": "OpenDoc()"},
      {"value": "4", "onclick": "CloseDoc()"}
    ]
  }
}
EOF;
```

## Use Netcat and listen for incoming connection on port 31337

```bash
$ jeval -e "Netcat.listen(31337)"
```

## Use commandline arguments

```bash
$ jeval -e 'format("args %s, %s", args[0], args[1])' "arg1" "arg2"
"args arg1, arg2"
```

## Measure execution real time

```bash
$ jeval -e "new Microprofiler().measureRealTime(() -> sleep(1000));"
```

## Run command

```bash
$ jeval -e 'new XExec("curl -L -G -vvv http://google.com").run().stderr().forEach(out::println)'
```

## Query XML using XPath

```bash
$ jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "//note/to/@test"))'
[ggg1, ggg2]
```

## Search substrings using regexp

```bash
$ jeval -e 'findMatches("\\d.jpg", "1.jpg 2.png 3.jpg 4.txt 5.txt").forEach(out::println)'
1.jpg
3.jpg
```

## Run commands in parallel

Here is an example which creates 20 files with random names doing this in parallel on different threads:

``` bash
$ jeval -e 'try (var c = new ParallelConsumer(s -> new XExec("touch /tmp/test-" + s).run().await())) {XUtils.infiniteRandomStream(12).limit(20).forEach(c);}'
```

# FAQ

## Why jeval -e 'out.format("args")' prints java.io.PrintStream@ at the end

With -e option **jeval** evaluates the expression and prints its result. In this case method PrintStream::format returns reference to PrintStream so that is why it is printed.
To overcome this you can use printf method defined by **jeval**.

## Why I get SPIResolutionException

Those are generated by JShell. Most likely you run multiple threads and some of them try to execute unresolved snippets. Wait until **jeval** finishes then search for its diagnostics in stderr.

**jeval** - command line Java code evaluator. It provides convenient way to use jshell without entering its interactive mode so you can execute Java code straight from the command line. *jeval* allows you to use Java same as you would use perl -e, bash -c, etc. It binds all standard streams to support piping and reading from stdin. With *jeval* you can execute complete Java shell scripts.

lambdaprime <id.blackmesa@gmail.com>

# Download

You can download *jeval* from https://github.com/lambdaprime/jeval/blob/master/release

# Requirements

Java 11

# Usage

```bash
jeval [ <JAVA_SCRIPT> | -e <JAVA_SNIPPET> ] "[ARGS]"
```

Where: 

JAVA_SCRIPT - Java shell script file to be executed. I prefer to save jshell scripts with *.java extension so Eclipse will automatically highlight the syntax in them.

JAVA_SNIPPET - Java expression. If you are entering more than one expression please surround JAVA_SNIPPET with "{}". If your snippet contains quotes "" you need to escape them with backslash. In Linux it is enough to enclose the snippet in single quotes ''.

ARGS - arguments which will be passed to the jshell through the global variable "args: String[]". All arguments should be enclosed in the quotes and passed to *jeval* as a single argument. For example if you want to pass to your jshell code two arguments "Hello world" and "jeval" you need to pass them to jeval like "\\"Hello world\\" jeval".

## Class path

To add new JAR files into class path use CLASSPATH env variable:

``` java
CLASSPATH=/opt/javafx-sdk-11.0.2/lib/* jeval script.java 
```

## Default imports

```java
java.util.stream.IntStream.*
java.util.stream.Collectors.*

java.lang.System.*
java.nio.file.Files.*
java.util.Arrays.*;
java.lang.Math.*
javax.script.*
jdk.nashorn.api.scripting.*

java.util.*
java.util.stream.*
java.util.concurrent.*;
java.util.function.*;
java.util.regex.*;
java.io.*
java.nio.*
java.nio.file.*
javax.xml.parsers.*
javax.xml.xpath.*
java.net.*
org.w3c.dom.*
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

### Exec

External commands executor.

- Exec(String cmd)

  Constructor which accepts the command to run with the arguments separated with whitespaces. The quotes are ignored.

- Exec(String... cmd)

  Constructor which accepts the command to run and list of arguments. Useful in case you don't want to mask upper level quotes.
  
- withInput(Stream<String> input)

  Specifies whether Exec needs to pass data to the command's standard input 

- run(): Exec.Result

  Runs the command and returns the result object which has following fields:
  
  - stdout: Stream<String>
  - stderr: Stream<String>
  - code: Future<Integer>

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

### Xml

Query and replace in XML files using XPath.

- Xml.query(xml: String, xpath: String): List<String>

  Query the xml string and return values based on xpath.
  
- Xml.query(xml: Path, xpath: String): List<String>

  Query the xml file and return values based on xpath.

- Xml.replace(xml: String, xpath: String, value: String): String

  Query the xml string and replace all values based on xpath. Returns new xml string.

- Xml.replace(xml: Path, xpath: String, value: String)

  Query the xml file and replace all values based on xpath. Replace is done inplace.

# Examples

## Say hello to the world:

```bash
$ jeval -e "out.println(\"Hello world\")"
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
$ jeval -e "{Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(\"/home/id/workspace/n.xml\")); out.println(XPathFactory.newInstance().newXPath().evaluate(\"//note/to\", d));}"
Tove
```

## Return integer in binary format:

```bash
$ jeval -e "Integer.toBinaryString(new Scanner(in).nextInt())"
14
"1110"
```

Or using pipe

```bash
$ echo 14 | jeval -e "Integer.toBinaryString(new Scanner(in).nextInt())"
"1110"
```

## Create temporary file and return its name

```bash
$ jeval -e "Files.createTempFile(null, \"tmp\")"
/tmp/11873450107364399793tmp
```

## Join lines using "," as a delimeter

```bash
$ jeval -e "stdin.lines().collect(joining(\",\"))"
ab
cd
ef
"ab,cd,ef"
```

## Execute JavaScript snippet which will read JSON and return value of specified parameter

```bash
$ echo '{"menu":123}' | jeval -e "new ScriptEngineManager().getEngineByName(\"nashorn\").eval(\"var v = \" + stdin.lines().collect(joining(\"\n\")) + \"; v[\\\"menu\\\"]\");"
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
$ jeval -e "out.println(\"args \" + args[1])" "Hello world"
```

## Measure execution real time

```bash
$ jeval -e "new Microprofiler().measureRealTime(() -> sleep(1000));"
```

## Run command

```java
new Exec("curl", "-L", "-G", "http://google.com")
    .run()
    .stdout
    .forEach(out::println);
```

Or

```bash
$ jeval -e 'new Exec("curl -L -G http://google.com").run().stdout.forEach(out::println)'
```

## Query XML using XPath

```bash
$ jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "//note/to/@test"))'
[ggg1, ggg2]
```

### Search substrings using regexp

```bash
jeval -e 'findMatches("\\d.jpg", "1.jpg 2.png 3.jpg 4.txt 5.txt").forEach(out::println)'
```

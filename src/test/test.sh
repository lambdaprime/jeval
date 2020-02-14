#!/bin/bash

PATH=$JEVAL_PATH:$PATH

OUT=$(jeval -e 'out.println("Hello world")')
if [ "$OUT" != "Hello world" ]; then
    echo "FAILED 0"
    exit 1
fi

jeval -e "range(1,10).forEach(out::println)"
jeval -e '{Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("/home/id/workspace/n.xml")); out.println(XPathFactory.newInstance().newXPath().evaluate("//note/to", d));}'
echo "enter integer"
echo "123" | jeval -e "Integer.toBinaryString(new Scanner(in).nextInt())"
echo 14 | jeval -e "Integer.toBinaryString(new Scanner(in).nextInt())"
jeval -e 'Files.createTempFile(null, "tmp")'
echo "enter lines"
echo -e "asdfasdf\ngggggggg" | jeval -e 'stdin.lines().collect(joining(","))'
echo '{"menu":123}' | jeval -e 'new ScriptEngineManager().getEngineByName("nashorn").eval("var v = " + stdin.lines().collect(joining("\n")) + "; v[\"menu\"]");'
jeval -e 'out.println("args " + args[1])' "Hello world"
jeval -e 'new Exec("curl -L -G http://google.com").run().stdout.forEach(out::println)'
jeval -e 'assertTrue(2 == new Exec("ls /sdfgsdfg").run().code.get(), "Return code is wrong")'
jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "//note/to/@test"))'
jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "/notes/note/to[@test=\"ggg2\"]"))'
jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "/notes/note/to"))'
jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "/notes/note[2]"))'
jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "/notes/note/to/@test"))'

OUT=$(jeval -e 'findMatches("\\d.jpg", "1.jpg 2.png 3.jpg 4.txt 5.txt").forEach(out::println)')
if [ "$OUT" != "1.jpg"$'\n'"3.jpg" ]; then
    echo "FAILED 1"
    exit 1
fi

cat << EOF > /tmp/r
class X {
    static X create() {
        m(null, null);
        return null;
    }
}
class Y {
    X x;
    static Y c(Z h, List<String> d) {
        if (y != null) return y;
        return null;
    }
}
String m() {
    return null;
}
Y y = null;
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED="Rejected snippet: Y y = null;

cannot find symbol
  symbol:   class Y
  location: class 
at position: 0

Unresolved snippet: 
class X {
    static X create() {
        m(null, null);
        return null;
    }
}

method m in class  cannot be applied to given types;
  required: no arguments
  found: <nulltype>,<nulltype>
  reason: actual and formal argument lists differ in length
at position: 42

Unresolved snippet: 
class Y {
    X x;
    static Y c(Z h, List<String> d) {
        if (y != null) return y;
        return null;
    }
}"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED 2"
    exit 1
fi

cat << EOF > /tmp/r
class X {
    static X create() {
        m(null, null);
        return null;
    }
}
class X {
    static X create() {
        m(null, null);
        return null;
    }
}
Y y = null;
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED="Rejected snippet: Y y = null;

cannot find symbol
  symbol:   class Y
  location: class 
at position: 0

Unresolved snippet: 
class X {
    static X create() {
        m(null, null);
        return null;
    }
}"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED 3"
    exit 1
fi

cat << EOF > /tmp/r
List.of(1,2,3).stream()
    .filter(i -> i > 0)
    .map(i -> "-" + i)
    .peek(out::println)
    // asdd
    .collect(toSet()).stream()
    .forEach(out::println);
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED="-1
-2
-3
-1
-2
-3"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED 4"
    exit 1
fi

OUT=$(JAVA_ARGS="-Dtest=hello" jeval -e 'System.getProperty("test")')
EXPECTED="\"hello\""
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED 5"
    exit 1
fi

OUT=$(yes yes | jeval -e 'cli.askConfirm("Execute this?")')
EXPECTED="Execute this?
Please confirm [yes/no]: true"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED 6 $OUT"
    exit 1
fi
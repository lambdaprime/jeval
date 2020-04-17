#!/bin/bash

PATH=$JEVAL_PATH:$PATH

echo "Test 0"
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
EXPECTED="
Unresolved snippet:
Y y = null;
^

cannot find symbol
  symbol:   class Y
  location: class 
at position: 0

Unresolved snippet:
class Y {
    X x;
    static Y c(Z h, List<String> d) {
        if (y != null) return y;
        return null;
    }
}

Unresolved references: class Z, variable y

Unresolved snippet:
class X {
    static X create() {
        m(null, null);
        ^
        return null;
    }
}

method m in class  cannot be applied to given types;
  required: no arguments
  found: <nulltype>,<nulltype>
  reason: actual and formal argument lists differ in length
at position: 42"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 1"
cat << EOF > /tmp/r
class X1 {
    static X create() {
        m(null, null);
        return null;
    }
}
class X2 {
    static X create() {
        m(null, null);
        return null;
    }
}
Y y = null;
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED="
Unresolved snippet:
Y y = null;
^

cannot find symbol
  symbol:   class Y
  location: class 
at position: 0

Unresolved snippet:
class X2 {
    static X create() {
        m(null, null);
        return null;
    }
}

Unresolved references: class X, method m(<nulltype>,<nulltype>)

Unresolved snippet:
class X1 {
    static X create() {
        m(null, null);
        return null;
    }
}

Unresolved references: class X, method m(<nulltype>,<nulltype>)"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 2"
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
    echo "FAILED $OUT"
    exit 1
fi

# Test that jeval supports JAVA_ARGS
echo "Test 3"
OUT=$(JAVA_ARGS="-Dtest=hello" jeval -e 'System.getProperty("test")')
EXPECTED="\"hello\""
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

# Test askConfir
echo "Test 4"
OUT=$(yes yes | jeval -e 'cli.askConfirm("Execute this?")')
EXPECTED="Execute this?
Please confirm [yes/no]: true"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

# Test multiple arguments are available from args
echo "Test 5"
OUT=$(jeval -e 'printf("args %s, %s\n", args[0], args[1])' "arg1" "arg2")
EXPECTED="args arg1, arg2"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

# Test that jeval shows unresolved references
echo "Test 6"
cat << EOF > /tmp/r
void f() {
    printf("ggg");
    printf(g);
}

f();
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED='jdk.jshell.UnresolvedReferenceException: Attempt to use definition snippet with unresolved references in MethodSnippet:f/()void-void f() {
    printf("ggg");
    printf(g);
}

	at .f(#47:1)
	at .(#49:1)

Unresolved snippet:
void f() {
    printf("ggg");
    printf(g);
}

Unresolved references: variable g'
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 7"
OUT=$(jeval -e 'assertTrue(2 == new XExec("ls /sdfgsdfg").run().code().get(), "Return code is wrong")')
EXPECTED=""
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 8"
OUT=$(jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "/notes/note/to[@test=\"ggg2\"]"))')
EXPECTED="[Bove]"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 9"
OUT=$(jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "/notes/note/to"))')
EXPECTED="[Tove, Bove]"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 10"
OUT=$(jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "/notes/note[2]"))')
EXPECTED="[Bove]"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 11"
OUT=$(jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "/notes/note/to/@test"))')
EXPECTED="[ggg1, ggg2]"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

# Test that jeval 
echo "Test 12"
cat << EOF > /tmp/r
void g() {
    var y = new Exec("ffff")
        .run();
}
Runnable r = () -> {
    try {
        g();
    } catch (Exception e) {
    }
};
var exec = Executors.newSingleThreadExecutor();
exec.submit(r);
exec.shutdown();
exec.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED='
Unresolved snippet:
void g() {
    var y = new Exec("ffff")
        .run();
}

Unresolved references: class Exec'
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi
